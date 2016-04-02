package uk.ac.shef.dcs.sti.core.algorithm.smp;

import javafx.util.Pair;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;

/**
 * Created by zqz on 20/04/2015.
 */
public class RelationLearner {
    private RelationTextMatch_Scorer matcher;

    public RelationLearner(RelationTextMatch_Scorer matcher) {
        this.matcher = matcher;
    }

    public void inferRelation(TAnnotation tableAnnotations, Table table, boolean useMainSubjectColumn, int[] ignoreColumns) {
        //RelationDataStructure result = new RelationDataStructure();

        //mainColumnIndexes contains indexes of columns that are possible NEs
        Map<Integer, DataTypeClassifier.DataType> colTypes
                = new HashMap<Integer, DataTypeClassifier.DataType>();
        for (int c = 0; c < table.getNumCols(); c++) {
            DataTypeClassifier.DataType type =
                    table.getColumnHeader(c).getTypes().get(0).getType();
            colTypes.put(c, type);
        }

        //aggregate candidate relations between any pairs of columns
        List<Integer> subjectColumnsToConsider = new ArrayList<Integer>();
        if(useMainSubjectColumn)
            subjectColumnsToConsider.add(tableAnnotations.getSubjectColumn());
        else {
            for(int c=0; c<table.getNumCols(); c++) {
                if(!TI_SemanticMessagePassing.ignoreColumn(c, ignoreColumns))
                    subjectColumnsToConsider.add(c);
            }
        }
        for (int subjectColumn :subjectColumnsToConsider) {  //choose a column to be subject column (must be NE column)
            if (!table.getColumnHeader(subjectColumn).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                continue;

            for (int objectColumn = 0; objectColumn < table.getNumCols(); objectColumn++) { //choose a column to be object column (any data type)
                if (subjectColumn == objectColumn)
                    continue;
                DataTypeClassifier.DataType columnDataType = table.getColumnHeader(subjectColumn).getFeature().getMostFrequentDataType().getType();
                if (columnDataType.equals(DataTypeClassifier.DataType.EMPTY) || columnDataType.equals(DataTypeClassifier.DataType.LONG_TEXT) ||
                        columnDataType.equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
                    continue;

                for (int r = 0; r < table.getNumRows(); r++) {
                    //in SMP, only the highest ranked NE (the disambiguated NE) is needed from each cell to aggregate candidate relation
                    List<TCellAnnotation> subjectCells = tableAnnotations.getWinningContentCellAnnotation(r, subjectColumn);
                    TCell subjectCellText = table.getContentCell(r, subjectColumn);
                    List<TCellAnnotation> objectCells = tableAnnotations.getWinningContentCellAnnotation(r, objectColumn);
                    TCell objectCellText = table.getContentCell(r, objectColumn);

                    //aggregate relation on each row, between each pair of subject-object cells
                    matcher.match(r, subjectCells, subjectColumn, objectCells, objectColumn,
                            objectCellText, colTypes.get(objectColumn),
                            tableAnnotations);
                }
            }
        }

        //aggregate overall scores for relations on each column pairs and populate relation annotation object
        aggregate(tableAnnotations, table);
    }

    private void aggregate(
            TAnnotation tableAnnotation, Table table) {

        List<RelationColumns> processed = new ArrayList<RelationColumns>();
        for (Map.Entry<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>> e :
                tableAnnotation.getCellcellRelations().entrySet()) {

            //firstly, check the directional relation where sub comes from sub col of the final_relationKey and ob comes from obj col of the ...
            RelationColumns current_relationKey = e.getKey(); //key indicating the directional relationship (subject col, object col)
            if (processed.contains(current_relationKey))
                continue;
            Map<String, Pair<Integer, Double>> votes = new HashMap<String, Pair<Integer, Double>>();

            processed.add(current_relationKey);
            Map<Integer, List<TCellCellRelationAnotation>> relations_on_rows = e.getValue(); //map object where key=row id, value=collection of binary relations between the sub col and obj col on this row
            collectVotes(relations_on_rows, votes);//among all relations that apply to the direction subcol-objcol, collect votes
            List<RelationDataTuple> best_subobj = selectBest(votes, current_relationKey);   //the highest scoring relation in the direction of subject-object

            //next, reverse the relation direction
            votes.clear();
            RelationColumns reverse_relationKey = new RelationColumns(current_relationKey.getObjectCol(), current_relationKey.getSubjectCol());
            relations_on_rows = tableAnnotation.getCellcellRelations().get(reverse_relationKey);
            if (relations_on_rows != null) {
                processed.add(reverse_relationKey);
                collectVotes(relations_on_rows, votes);
                List<RelationDataTuple> best_objsub = selectBest(votes, reverse_relationKey);   //the highest scoring relation in the direction of object-subject relation

                integrateCreateHeaderBinaryRelationAnnotations(best_subobj, best_objsub, tableAnnotation, table);
            } else {//no relation from reverse direction,
                integrateCreateHeaderBinaryRelationAnnotations(best_subobj, null, tableAnnotation, table);
            }
        }
    }

    //need to resolve conflicts. between two directions (sub-ob, or ob-sub, we must choose only 1)
    private void integrateCreateHeaderBinaryRelationAnnotations(
            List<RelationDataTuple> best_subobj,
            List<RelationDataTuple> best_objsub,
            TAnnotation tableAnnotation,
            Table table) {
        if (best_objsub != null)
            best_subobj.addAll(best_objsub);
        Collections.sort(best_subobj);

        RelationDataTuple template = best_subobj.get(0);
        int maxVote = template.votes;
        double maxScore = template.score;
        RelationColumns relationColumns = template.relationColumns;
        int countNonEmptyRows = 0;
        for (int r = 0; r < tableAnnotation.getRows(); r++) {
            TCell c1 = table.getContentCell(r, relationColumns.getSubjectCol());
            TCell c2 = table.getContentCell(r, relationColumns.getObjectCol());
            if (!c1.getType().equals(DataTypeClassifier.DataType.EMPTY) &&
                    !c1.getType().equals(DataTypeClassifier.DataType.EMPTY))
                countNonEmptyRows++;
        }

        for (RelationDataTuple rdt : best_subobj) {
            if (rdt.votes == maxVote && rdt.score == maxScore && rdt.relationColumns.getSubjectCol() == relationColumns.getSubjectCol() &&
                    rdt.relationColumns.getObjectCol() == relationColumns.getObjectCol()) {
                TColumnColumnRelationAnnotation hbr = new TColumnColumnRelationAnnotation(relationColumns,
                        rdt.relationString,
                        rdt.relationString,
                        (double) maxVote / countNonEmptyRows);
                tableAnnotation.addColumnColumnRelation(hbr);
            } else {
                break;
            }
        }

    }

    private List<RelationDataTuple> selectBest(Map<String, Pair<Integer, Double>> votes, RelationColumns relationColumnsKey) {
        List<RelationDataTuple> out = new ArrayList<RelationDataTuple>();
        for (Map.Entry<String, Pair<Integer, Double>> e : votes.entrySet()) {
            RelationDataTuple rdt = new RelationDataTuple();
            rdt.relationString = e.getKey();
            rdt.relationColumns = relationColumnsKey;
            rdt.votes = e.getValue().getKey();
            rdt.score = e.getValue().getValue();
            out.add(rdt);
        }
        Collections.sort(out);
        int maxVote = out.get(0).votes;
        double maxScore = out.get(0).score;
        Iterator<RelationDataTuple> it = out.iterator();
        while (it.hasNext()) {
            RelationDataTuple rdt = it.next();
            if (rdt.votes < maxVote)
                it.remove();
            else if (rdt.votes == maxVote && rdt.score < maxScore)
                it.remove();
        }
        return out;
    }

    private void collectVotes(Map<Integer, List<TCellCellRelationAnotation>> relations,
                              Map<String, Pair<Integer, Double>> votes
    ) {
        for (List<TCellCellRelationAnotation> candidatesOnRow : relations.values()) {        //go thru each row
            Set<String> distinctRelations = new HashSet<String>();
            for (TCellCellRelationAnotation candidate : candidatesOnRow) { //go thru each candidate of each row
                String relationCandidate = candidate.getRelationURI();
                distinctRelations.add(relationCandidate);
            }

            for (String relation: distinctRelations) { //go thru each candidate of each row
                double maxScore=0.0;
                for(TCellCellRelationAnotation candidate : candidatesOnRow){
                    if(candidate.getRelationURI().equals(relation) && candidate.getWinningAttributeMatchScore()>maxScore)
                        maxScore=candidate.getWinningAttributeMatchScore();
                }

                Pair<Integer, Double> votesAndScore = votes.get(relation); //let's record both votes and computeElementScores. so when there is a tie at votes, we resort to computeElementScores
                if (votesAndScore == null) {
                    votesAndScore = new Pair<>(0, 0.0);
                }
                else{
                    votesAndScore = new Pair<>(votesAndScore.getKey()+1,
                            votesAndScore.getValue()+maxScore);
                }

                votes.put(relation, votesAndScore);
            }
        }
    }

/*    private void removeIgnoreRelations(List<String[]> facts) {
        Iterator<String[]> it = facts.iterator();
        while (it.hasNext()) {
            String[] fact = it.next();
            if (!TableMinerConstants.USE_NESTED_RELATION_FOR_RELATION_INTERPRETATION && fact[3].equals("y"))
                it.remove();
            else if (KB_InstanceFilter.ignoreRelation_from_relInterpreter(fact[0]))
                it.remove();
        }
    }*/

    private class RelationDataTuple implements Comparable<RelationDataTuple> {
        protected String relationString;
        protected RelationColumns relationColumns;
        protected int votes;
        protected double score;

        @Override
        public int compareTo(RelationDataTuple o) {
            int compare = Integer.valueOf(o.votes).compareTo(votes);
            if (compare == 0) {
                return Double.valueOf(o.score).compareTo(score);
            }
            return compare;
        }

        public String toString() {
            return relationString + "," + votes + "," + score;
        }
    }
}
