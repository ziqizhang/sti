package uk.ac.shef.dcs.oak.sti.algorithm.smp;

import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.util.*;

/**
 * Created by zqz on 20/04/2015.
 */
public class RelationLearner {
    private RelationTextMatch_Scorer matcher;

    public RelationLearner(RelationTextMatch_Scorer matcher) {
        this.matcher = matcher;
    }

    public void inferRelation(LTableAnnotation tableAnnotations, LTable table, boolean useMainSubjectColumn, int[] ignoreColumns) {
        //RelationDataStructure result = new RelationDataStructure();

        //mainColumnIndexes contains indexes of columns that are possible NEs
        Map<Integer, DataTypeClassifier.DataType> colTypes
                = new HashMap<Integer, DataTypeClassifier.DataType>();
        for (int c = 0; c < table.getNumCols(); c++) {
            DataTypeClassifier.DataType type =
                    table.getColumnHeader(c).getTypes().get(0).getCandidateType();
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
            if (!table.getColumnHeader(subjectColumn).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                continue;

            for (int objectColumn = 0; objectColumn < table.getNumCols(); objectColumn++) { //choose a column to be object column (any data type)
                if (subjectColumn == objectColumn)
                    continue;
                DataTypeClassifier.DataType columnDataType = table.getColumnHeader(subjectColumn).getFeature().getMostDataType().getCandidateType();
                if (columnDataType.equals(DataTypeClassifier.DataType.EMPTY) || columnDataType.equals(DataTypeClassifier.DataType.LONG_TEXT) ||
                        columnDataType.equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
                    continue;

                for (int r = 0; r < table.getNumRows(); r++) {
                    //in SMP, only the highest ranked NE (the disambiguated NE) is needed from each cell to aggregate candidate relation
                    List<CellAnnotation> subjectCells = tableAnnotations.getBestContentCellAnnotations(r, subjectColumn);
                    LTableContentCell subjectCellText = table.getContentCell(r, subjectColumn);
                    List<CellAnnotation> objectCells = tableAnnotations.getBestContentCellAnnotations(r, objectColumn);
                    LTableContentCell objectCellText = table.getContentCell(r, objectColumn);

                    //aggregate relation on each row, between each pair of subject-object cells
                    matcher.match(r, subjectCells, subjectColumn, objectCells, objectColumn,
                            subjectCellText, objectCellText, colTypes.get(objectColumn),
                            tableAnnotations);
                }
            }
        }

        //aggregate overall scores for relations on each column pairs and populate relation annotation object
        aggregate(tableAnnotations, table);
    }

    private void aggregate(
            LTableAnnotation tableAnnotation, LTable table) {

        List<Key_SubjectCol_ObjectCol> processed = new ArrayList<Key_SubjectCol_ObjectCol>();
        for (Map.Entry<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>> e :
                tableAnnotation.getRelationAnnotations_per_row().entrySet()) {

            //firstly, check the directional relation where sub comes from sub col of the final_relationKey and ob comes from obj col of the ...
            Key_SubjectCol_ObjectCol current_relationKey = e.getKey(); //key indicating the directional relationship (subject col, object col)
            if (processed.contains(current_relationKey))
                continue;
            Map<String, ObjObj<Integer, Double>> votes = new HashMap<String, ObjObj<Integer, Double>>();

            processed.add(current_relationKey);
            Map<Integer, List<CellBinaryRelationAnnotation>> relations_on_rows = e.getValue(); //map object where key=row id, value=collection of binary relations between the sub col and obj col on this row
            collectVotes(relations_on_rows, votes);//among all relations that apply to the direction subcol-objcol, collect votes
            List<RelationDataTuple> best_subobj = selectBest(votes, current_relationKey);   //the highest scoring relation in the direction of subject-object

            //next, reverse the relation direction
            votes.clear();
            Key_SubjectCol_ObjectCol reverse_relationKey = new Key_SubjectCol_ObjectCol(current_relationKey.getObjectCol(), current_relationKey.getSubjectCol());
            relations_on_rows = tableAnnotation.getRelationAnnotations_per_row().get(reverse_relationKey);
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
            LTableAnnotation tableAnnotation,
            LTable table) {
        if (best_objsub != null)
            best_subobj.addAll(best_objsub);
        Collections.sort(best_subobj);

        RelationDataTuple template = best_subobj.get(0);
        int maxVote = template.votes;
        double maxScore = template.score;
        Key_SubjectCol_ObjectCol relationDirection = template.relationDirection;
        int countNonEmptyRows = 0;
        for (int r = 0; r < tableAnnotation.getRows(); r++) {
            LTableContentCell c1 = table.getContentCell(r, relationDirection.getSubjectCol());
            LTableContentCell c2 = table.getContentCell(r, relationDirection.getObjectCol());
            if (!c1.getType().equals(DataTypeClassifier.DataType.EMPTY) &&
                    !c1.getType().equals(DataTypeClassifier.DataType.EMPTY))
                countNonEmptyRows++;
        }

        for (RelationDataTuple rdt : best_subobj) {
            if (rdt.votes == maxVote && rdt.score == maxScore && rdt.relationDirection.getSubjectCol() == relationDirection.getSubjectCol() &&
                    rdt.relationDirection.getObjectCol() == relationDirection.getObjectCol()) {
                HeaderBinaryRelationAnnotation hbr = new HeaderBinaryRelationAnnotation(relationDirection,
                        rdt.relationString,
                        rdt.relationString,
                        (double) maxVote / countNonEmptyRows);
                tableAnnotation.addRelationAnnotation_across_column(hbr);
            } else {
                break;
            }
        }

    }

    private List<RelationDataTuple> selectBest(Map<String, ObjObj<Integer, Double>> votes, Key_SubjectCol_ObjectCol relationDirectionKey) {
        List<RelationDataTuple> out = new ArrayList<RelationDataTuple>();
        for (Map.Entry<String, ObjObj<Integer, Double>> e : votes.entrySet()) {
            RelationDataTuple rdt = new RelationDataTuple();
            rdt.relationString = e.getKey();
            rdt.relationDirection = relationDirectionKey;
            rdt.votes = e.getValue().getMainObject();
            rdt.score = e.getValue().getOtherObject();
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

    private void collectVotes(Map<Integer, List<CellBinaryRelationAnnotation>> relations, Map<String, ObjObj<Integer, Double>> votes
    ) {
        for (List<CellBinaryRelationAnnotation> candidatesOnRow : relations.values()) {        //go thru each row
            Set<String> distinctRelations = new HashSet<String>();
            for (CellBinaryRelationAnnotation candidate : candidatesOnRow) { //go thru each candidate of each row
                String relationCandidate = candidate.getAnnotation_url();
                distinctRelations.add(relationCandidate);
            }

            for (String relation: distinctRelations) { //go thru each candidate of each row
                double maxScore=0.0;
                for(CellBinaryRelationAnnotation candidate : candidatesOnRow){
                    if(candidate.getAnnotation_url().equals(relation) && candidate.getScore()>maxScore)
                        maxScore=candidate.getScore();
                }

                ObjObj<Integer, Double> votesAndScore = votes.get(relation); //let's record both votes and score. so when there is a tie at votes, we resort to score
                if (votesAndScore == null)
                    votesAndScore = new ObjObj<Integer, Double>(0, 0.0);
                votesAndScore.setMainObject(votesAndScore.getMainObject() + 1);
                votesAndScore.setOtherObject(votesAndScore.getOtherObject() + maxScore);

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
        protected Key_SubjectCol_ObjectCol relationDirection;
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
