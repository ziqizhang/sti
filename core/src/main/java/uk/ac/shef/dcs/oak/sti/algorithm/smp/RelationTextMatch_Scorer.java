package uk.ac.shef.dcs.oak.sti.algorithm.smp;

import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseFreebaseFilter;
import uk.ac.shef.dcs.oak.sti.misc.UtilRelationMatcher;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

import java.util.*;

/**
 * Created by zqz on 21/04/2015.
 */
public class RelationTextMatch_Scorer {

    protected Collection<String> stopWords;
    protected double minimum_match_score;
    protected AbstractStringMetric stringSimilarityMetric;

    public RelationTextMatch_Scorer(Collection<String> stopWords, AbstractStringMetric stringSimilarityMetric, double minimum_match_score) {
        this.stopWords = stopWords;
        this.stringSimilarityMetric = stringSimilarityMetric;
        this.minimum_match_score = minimum_match_score;
    }

    /**
     * This is the adapted implementation of SMP. Only the highest scoring cell entity annotation is used to infer candidate
     * relations.
     *
     * @param subjectCellAnnotations NEs that have the highest score (there can be multiple)
     * @param objectCellAnnotations  NEs that have the highest score (there can be multiple)
     * @return
     */
    public void match(int row,
                      List<CellAnnotation> subjectCellAnnotations, int subjectColumn,
                      List<CellAnnotation> objectCellAnnotations, int objectColumn,
                      LTableContentCell objectCellText,
                      DataTypeClassifier.DataType object_column_type,
                      LTableAnnotation tableAnnotation
    ) {
        if (subjectCellAnnotations.size() != 0) {
            if (subjectCellAnnotations.size() > 0 && UtilRelationMatcher.isValidType(object_column_type)) {
                for (int s = 0; s < subjectCellAnnotations.size(); s++) {
                    CellAnnotation subjectEntity = subjectCellAnnotations.get(s);
                    List<String[]> subject_entity_facts = subjectEntity.getAnnotation().getTriples();
                    KnowledgeBaseFreebaseFilter.filterRelations(subject_entity_facts);
                    Map<Integer, DataTypeClassifier.DataType> fact_data_types = classifyFactObjDataType(
                            subject_entity_facts
                    );

                    String objText = objectCellText.getText();
                    //scoring matches for the cell on the row
                    final Map<Integer, Double> fact_matched_scores = new HashMap<Integer, Double>();
                    for (int index = 0; index < subject_entity_facts.size(); index++) {
                        DataTypeClassifier.DataType type_of_fact_value = fact_data_types.get(index);
                        String[] fact = subject_entity_facts.get(index);
                        if (!UtilRelationMatcher.isValidType(type_of_fact_value)) {
                            continue;
                        }

                        double scoreWithCell = UtilRelationMatcher.score(objText, object_column_type, fact[1], type_of_fact_value, stopWords, stringSimilarityMetric);
                        for (int o = 0; o < objectCellAnnotations.size(); o++) {
                            String objEntityLabel = objectCellAnnotations.get(o).getAnnotation().getLabel();
                            if (objEntityLabel != null) {
                                double scoreAgainstObjEntityLabel = UtilRelationMatcher.score(objEntityLabel, object_column_type, fact[1], type_of_fact_value, stopWords, stringSimilarityMetric);
                                if (scoreWithCell < scoreAgainstObjEntityLabel)
                                    scoreWithCell = scoreAgainstObjEntityLabel;
                            }
                        }
                        if (scoreWithCell > 0 && scoreWithCell > minimum_match_score)
                            fact_matched_scores.put(index, scoreWithCell);
                    }

                    if (fact_matched_scores.size() == 0) continue;
                    //go thru all scores and make selection   within each subjectEntity-objectEntity pair
                    List<Integer> qualified = new ArrayList<Integer>(fact_matched_scores.keySet());
                    Collections.sort(qualified, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer o1, Integer o2) {
                            return fact_matched_scores.get(o2).compareTo(fact_matched_scores.get(o1));
                        }
                    });
                    Double highestScore = fact_matched_scores.get(qualified.get(0));
                    for (Map.Entry<Integer, Double> e : fact_matched_scores.entrySet()) {
                        int index = e.getKey();
                        Double score = e.getValue();
                        if (score.equals(highestScore)) {
                            String[] fact = subject_entity_facts.get(index);
                            tableAnnotation.addRelationAnnotation_per_row(new CellBinaryRelationAnnotation(
                                    new Key_SubjectCol_ObjectCol(subjectColumn, objectColumn), row,
                                    fact[0], fact[0],
                                    new ArrayList<String[]>(), score
                            ));
                        }
                    }
                }//each subjectNE
            }//if block checking whether the potential subject-object cell pairs are valid
        }
    }

    protected Map<Integer, DataTypeClassifier.DataType> classifyFactObjDataType(List<String[]> sbjEntityFacts) {
        Map<Integer, DataTypeClassifier.DataType> dataTypes = new HashMap<Integer, DataTypeClassifier.DataType>();
        //typing the objects of facts
        for (int index = 0; index < sbjEntityFacts.size(); index++) {
            String[] fact = sbjEntityFacts.get(index);
            String val = fact[1];
            String id_of_val = fact[2];

            if (id_of_val != null)
                dataTypes.put(index, DataTypeClassifier.DataType.NAMED_ENTITY);
            else {
                DataTypeClassifier.DataType type = DataTypeClassifier.classify(val);
                dataTypes.put(index, type);
            }
        }
        return dataTypes;
    }
}