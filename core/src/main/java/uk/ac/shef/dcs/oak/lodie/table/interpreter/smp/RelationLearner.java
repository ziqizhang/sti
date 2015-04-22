package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import org.openjena.atlas.iterator.Iter;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.KB_InstanceFilter;
import uk.ac.shef.dcs.oak.lodie.table.rep.*;
import uk.ac.shef.dcs.oak.lodie.test.TableMinerConstants;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.util.*;

/**
 * Created by zqz on 20/04/2015.
 */
public class RelationLearner {
    private RelationMatcher matcher;

    public RelationLearner() {
        matcher = new RelationMatcher();
    }

    public void inferRelation(LTableAnnotation annotations, LTable table) {
        RelationDataStructure result = new RelationDataStructure();

        //mainColumnIndexes contains indexes of columns that are possile NEs
        Map<Integer, DataTypeClassifier.DataType> colTypes
                = new HashMap<Integer, DataTypeClassifier.DataType>();
        for (int c = 0; c < table.getNumCols(); c++) {
            DataTypeClassifier.DataType type =
                    table.getColumnHeader(c).getTypes().get(0).getCandidateType();
            if (type.equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
                continue; //ordered numbered columns are not interesting
            else
                colTypes.put(c, type);

        }

        //compute candidate relations between any pairs of columns
        for (int subjectColumn = 0; subjectColumn < table.getNumCols(); subjectColumn++) {
            if (!table.getColumnHeader(subjectColumn).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                continue;

            for (int objectColumn = 0; objectColumn < table.getNumCols() && objectColumn != subjectColumn; objectColumn++) {
                DataTypeClassifier.DataType columnDataType = table.getColumnHeader(subjectColumn).getFeature().getMostDataType().getCandidateType();
                if (columnDataType.equals(DataTypeClassifier.DataType.EMPTY) || columnDataType.equals(DataTypeClassifier.DataType.LONG_TEXT) ||
                        columnDataType.equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
                    continue;

                for (int r = 0; r < table.getNumRows(); r++) {
                    //in SMP, only ONE (the disambiguated NE) is needed from each cell to compute candidate relation
                    CellAnnotation[] subjectCells = annotations.getContentCellAnnotations(r, subjectColumn);
                    LTableContentCell subjectCellText = table.getContentCell(r, subjectColumn);
                    CellAnnotation[] objectCells = annotations.getContentCellAnnotations(r, objectColumn);
                    LTableContentCell objectCellText = table.getContentCell(r, objectColumn);

                    //compute relation
                    List<ObjObj<String, Double>> relations = matcher.match(subjectCells, objectCells,subjectCellText, objectCellText, table);
                    if(relations!=null && relations.size()>0)
                        result.addRCBetweenColumnsOnRow(subjectColumn, objectColumn, r, relations);
                }
            }
        }

        //compute overall scores for relations on each column pairs and populate relation annotation object
        Map<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>> relationAnnotations
                = new HashMap<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>>();
        compute(relationAnnotations, result);
    }

    private void compute(
            Map<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>> relationAnnotations,
            RelationDataStructure result) {
        Map<String, Map<Integer, List<ObjObj<String, Double>>>>
                data = result.getAllRelationCandidates();

        Set<String> processed = new HashSet<String>();
        for (Map.Entry<String, Map<Integer, List<ObjObj<String, Double>>>> e : data.entrySet()) {   //for each pair of column
            Key_SubjectCol_ObjectCol final_relationKey = null;
            RelationDataTriple final_relation = null;

            String key = e.getKey(); //the column id pair (e.g., subjectcol-objectcol)
            if (processed.contains(key))
                continue;

            Map<String, ObjObj<Integer, Double>> votes = new HashMap<String, ObjObj<Integer, Double>>();

            processed.add(key);
            Map<Integer, List<ObjObj<String, Double>>> relations = data.get(key);
            collectVotes(relations, votes);
            List<RelationDataTriple> best_subobj = selectBest(votes);   //subject-object relation

            //reverse the column-column id to find relation from reverse direction
            votes.clear();
            String[] parts = key.split(",");
            String reverseKey = parts[1] + "," + parts[0];
            Map<Integer, List<ObjObj<String, Double>>> reverseRelations = data.get(reverseKey);
            if (reverseRelations != null) {
                processed.add(reverseKey);
                collectVotes(reverseRelations, votes);
                List<RelationDataTriple> best_objsub = selectBest(votes);   //object-subject relation

                //for the current sub-obj or obj-sub key, find the best relation, and also direction (i.e., is it sub-obj, or obj-sub)
                RelationDataTriple subobj_relation_best = best_subobj.get(0);
                RelationDataTriple objsub_relation_best = best_objsub.get(0);

                if (subobj_relation_best.votes > objsub_relation_best.votes) {
                    final_relation = subobj_relation_best;
                    final_relationKey = new Key_SubjectCol_ObjectCol(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
                } else if (subobj_relation_best.votes == objsub_relation_best.votes) {
                    if (subobj_relation_best.score > objsub_relation_best.score) {
                        final_relation = subobj_relation_best;
                        final_relationKey = new Key_SubjectCol_ObjectCol(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
                    } else {
                        final_relation = objsub_relation_best;
                        final_relationKey = new Key_SubjectCol_ObjectCol(Integer.valueOf(parts[1]), Integer.valueOf(parts[0]));
                    }
                } else {
                    final_relation = objsub_relation_best;
                    final_relationKey = new Key_SubjectCol_ObjectCol(Integer.valueOf(parts[1]), Integer.valueOf(parts[0]));
                }
            }else{//no relation from reverse direction, todo
                final_relation = best_subobj.get(0);
                final_relationKey = new Key_SubjectCol_ObjectCol(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
            }
            //populate tableannotation object
            HeaderBinaryRelationAnnotation hbr = new HeaderBinaryRelationAnnotation(final_relationKey,
                    final_relation.relationString,
                    final_relation.relationString,
                    final_relation.votes);
            List<HeaderBinaryRelationAnnotation> res = new ArrayList<HeaderBinaryRelationAnnotation>();
            res.add(hbr);
            relationAnnotations.put(final_relationKey, res);
        }
    }

    private List<RelationDataTriple> selectBest(Map<String, ObjObj<Integer, Double>> votes) {
        List<RelationDataTriple> out = new ArrayList<RelationDataTriple>();
        for (Map.Entry<String, ObjObj<Integer, Double>> e : votes.entrySet()) {
            RelationDataTriple rdt = new RelationDataTriple();
            rdt.relationString = e.getKey();
            rdt.votes = e.getValue().getMainObject();
            rdt.score = e.getValue().getOtherObject();
            out.add(rdt);
        }
        Collections.sort(out);
        int maxVote = out.get(0).votes;
        Iterator<RelationDataTriple> it = out.iterator();
        while (it.hasNext()) {
            RelationDataTriple rdt = it.next();
            if (rdt.votes < maxVote)
                it.remove();
        }
        return out;
    }

    private void collectVotes(Map<Integer, List<ObjObj<String, Double>>> relations, Map<String, ObjObj<Integer, Double>> votes
    ) {
        for (List<ObjObj<String, Double>> candidatesOnRow : relations.values()) {        //go thru each row
            for (ObjObj<String, Double> candidate : candidatesOnRow) { //go thru each candidate of each row
                String relationCandidate = candidate.getMainObject();
                double score = candidate.getOtherObject();

                ObjObj<Integer, Double> votesAndScore = votes.get(relationCandidate);
                if (votesAndScore == null)
                    votesAndScore = new ObjObj<Integer, Double>(0, 0.0);
                votesAndScore.setMainObject(votesAndScore.getMainObject() + 1);
                votesAndScore.setOtherObject(votesAndScore.getOtherObject() + score);

                votes.put(relationCandidate, votesAndScore);
            }
        }
    }

    private void removeIgnoreRelations(List<String[]> facts) {
        Iterator<String[]> it = facts.iterator();
        while (it.hasNext()) {
            String[] fact = it.next();
            if (!TableMinerConstants.USE_NESTED_RELATION_FOR_RELATION_INTERPRETATION && fact[3].equals("y"))
                it.remove();
            else if (KB_InstanceFilter.ignoreRelation_from_relInterpreter(fact[0]))
                it.remove();
        }
    }

    private class RelationDataTriple implements Comparable<RelationDataTriple> {
        protected String relationString;
        protected int votes;
        protected double score;

        @Override
        public int compareTo(RelationDataTriple o) {
            int compare = Integer.valueOf(o.votes).compareTo(votes);
            if (compare == 0) {
                return Double.valueOf(o.score).compareTo(score);
            }
            return compare;
        }
    }
}
