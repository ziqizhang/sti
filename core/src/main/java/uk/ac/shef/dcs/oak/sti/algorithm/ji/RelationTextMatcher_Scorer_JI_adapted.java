package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.algorithm.smp.RelationTextMatch_Scorer;
import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.misc.UtilRelationMatcher;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

import java.util.*;

/**
 * Created by zqz on 05/05/2015.
 */
public class RelationTextMatcher_Scorer_JI_adapted extends RelationTextMatch_Scorer {

    public RelationTextMatcher_Scorer_JI_adapted(Collection<String> stopWords, AbstractStringMetric stringSimilarityMetric, double minimum_match_score) {
        super(stopWords, stringSimilarityMetric, minimum_match_score);
    }

    public void match(int row,
                      List<CellAnnotation> subjectCellAnnotations, int subjectColumn,
                      List<CellAnnotation> objectCellAnnotations, int objectColumn,
                      LTableContentCell subjectCellText, LTableContentCell objectCellText,
                      DataTypeClassifier.DataType object_column_type,
                      LTableAnnotation_JI_Freebase tableAnnotation
    ) {
        if (subjectCellAnnotations.size() != 0) {
            List<ObjObj<String, Double>> result = new ArrayList<ObjObj<String, Double>>();
            if (subjectCellAnnotations.size() > 0 && UtilRelationMatcher.isValidType(object_column_type)) {
                for (int s = 0; s < subjectCellAnnotations.size(); s++) {
                    CellAnnotation subjectEntity = subjectCellAnnotations.get(s);
                    String subText = subjectCellText.getText();
                    for (int o = 0; o < objectCellAnnotations.size(); o++) {
                        CellAnnotation objectEntity=null;
                        if (objectCellAnnotations.size() > 0) {
                             objectEntity= objectCellAnnotations.get(o);
                        }
                        String objText = objectCellText.getText();

                        List<String[]> subject_entity_facts = subjectEntity.getAnnotation().getFacts();

                        //typing the objects of facts
                        Map<Integer, DataTypeClassifier.DataType> fact_data_types = new HashMap<Integer, DataTypeClassifier.DataType>();
                        for (int index = 0; index < subject_entity_facts.size(); index++) {
                            String[] fact = subject_entity_facts.get(index);
                            String prop = fact[0];
                            String val = fact[1];
                            String id_of_val = fact[2];
                            String nested = fact[3];

                            if (id_of_val != null)
                                fact_data_types.put(index, DataTypeClassifier.DataType.NAMED_ENTITY);
                            else {
                                DataTypeClassifier.DataType type = DataTypeClassifier.classify(val);
                                fact_data_types.put(index, type);
                            }
                        }

                        //scoring matches for the cell value on the row
                        double maxScore = 0.0;
                        final Map<Integer, Double> fact_matched_scores = new HashMap<Integer, Double>();
                        //to keep record of whether the fact matches an entity or just cell text
                        final Map<String, Boolean> fact_matched_entity = new HashMap<String, Boolean>();
                        for (int index = 0; index < subject_entity_facts.size(); index++) {
                            DataTypeClassifier.DataType type_of_fact_value = fact_data_types.get(index);
                            String[] fact = subject_entity_facts.get(index);
                            if (!UtilRelationMatcher.isValidType(type_of_fact_value)) {
                                continue;
                            }

                            //match the cell text
                            double score = UtilRelationMatcher.score(objText, object_column_type, fact[1], type_of_fact_value, stopWords, stringSimilarityMetric);
                            fact_matched_entity.put(fact[1], false);
                            if (objectEntity != null) {
                                double scoreAgainstObjEntityLabel = UtilRelationMatcher.score(
                                        objectEntity.getAnnotation().getName(), object_column_type, fact[1], type_of_fact_value, stopWords, stringSimilarityMetric);
                                if (score < scoreAgainstObjEntityLabel) {
                                    score = scoreAgainstObjEntityLabel;
                                    fact_matched_entity.put(fact[1], true);
                                }
                            }

                            if (score > 0 && score > minimum_match_score)
                                fact_matched_scores.put(index, score);
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
                        List<Integer> selected_index_of_facts = new ArrayList<Integer>();
                        Double highestScore = fact_matched_scores.get(qualified.get(0));
                        for (Map.Entry<Integer, Double> e : fact_matched_scores.entrySet()) {
                            int index = e.getKey();
                            Double score = e.getValue();
                            if (score.equals(highestScore)) {
                                String[] fact = subject_entity_facts.get(index);
                                result.add(new ObjObj<String, Double>(fact[0], score));
                                selected_index_of_facts.add(index);
                            }
                        }

                        //sorting and selection for all and create annotations
                        Collections.sort(result, new Comparator<ObjObj<String, Double>>() {
                            @Override
                            public int compare(ObjObj<String, Double> o1, ObjObj<String, Double> o2) {
                                return o2.getOtherObject().compareTo(o1.getOtherObject());
                            }
                        });
                        maxScore = result.get(0).getOtherObject(); //between the current candidate Subject NE and Object NE,
                        //the relation that has the maximum score
                        for (ObjObj<String, Double> r : result) {
                            tableAnnotation.addRelationAnnotation_per_row(new CellBinaryRelationAnnotation(
                                    new Key_SubjectCol_ObjectCol(subjectColumn, objectColumn), row, r.getMainObject(), r.getMainObject(),
                                    new ArrayList<String[]>(), maxScore
                            ));
                            //subject entity, its concepts and relation
                            populateEntityAndRelationScore(tableAnnotation, subjectEntity.getAnnotation().getId(), r.getMainObject(), maxScore);
                            populateConceptAndRelationScore(tableAnnotation, subjectEntity,
                                    row,subjectColumn,
                                    r.getMainObject(), maxScore);
                            //object entity (if any), its concepts and relation
                            if(fact_matched_entity.get(r.getMainObject())){
                                populateEntityAndRelationScore(tableAnnotation,objectEntity.getAnnotation().getId(),
                                        r.getMainObject(), maxScore);
                                populateConceptAndRelationScore(tableAnnotation,objectEntity, row, objectColumn,r.getMainObject(), maxScore);
                            }
                        }
                    }// each subjectNE-objectNE pair
                }//each subjectNE
            }//if block checking whether the potential subject-object cell pairs are valid
        }
    }

    private void populateConceptAndRelationScore(LTableAnnotation_JI_Freebase tableAnnotation,
                                                 CellAnnotation entity,
                                                 int entityRow, int entityColumn,
                                                 String relationURL,
                                                 double maxScore) {
        for(String typeURL: entity.getAnnotation().getTypeIds()){
            tableAnnotation.setScore_conceptAndRelation(entityRow,
                    entityColumn, typeURL, relationURL, maxScore);
        }
    }

    private void populateEntityAndRelationScore(LTableAnnotation_JI_Freebase tableAnnotation,
                                                String entityId, String relationURL, double maxScore) {
        tableAnnotation.setScore_entityAndRelation(entityId, relationURL, maxScore);
    }
}
