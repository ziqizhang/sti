package uk.ac.shef.dcs.sti.core.algorithm.smp;

import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

import java.util.*;
import java.util.List;

/**
 * Created by zqz on 21/04/2015.
 */
public class RelationTextMatch_Scorer extends AttributeValueMatcher{

    public RelationTextMatch_Scorer(double minScoreThreshold, List<String> stopWords,
                                    AbstractStringMetric stringMetric) {
        super(minScoreThreshold,stopWords,stringMetric);
    }

    /**
     * This is the adapted implementation of SMP. Only the highest scoring cell entity annotation is used to infer candidate
     * relations.
     *
     * @param subjectCellAnnotations NEs that have the highest computeElementScores (there can be multiple)
     * @param objectCellAnnotations  NEs that have the highest computeElementScores (there can be multiple)
     * @return
     */
    public void match(int row,
                      List<TCellAnnotation> subjectCellAnnotations, int subjectColumn,
                      List<TCellAnnotation> objectCellAnnotations, int objectColumn,
                      TCell objectCellText,
                      DataTypeClassifier.DataType object_column_type,
                      TAnnotation tableAnnotation
    ) {
        if (subjectCellAnnotations.size() != 0) {
            if (subjectCellAnnotations.size() > 0 && isValidType(object_column_type)) {
                for (int s = 0; s < subjectCellAnnotations.size(); s++) {
                    TCellAnnotation subjectEntity = subjectCellAnnotations.get(s);
                    List<Attribute> subject_entity_facts = subjectEntity.getAnnotation().getAttributes();
                    Map<Integer, DataTypeClassifier.DataType> fact_data_types = classifyFactObjDataType(
                            subject_entity_facts
                    );

                    String objText = objectCellText.getText();
                    //scoring matches for the cell on the row
                    final Map<Integer, Double> fact_matched_scores = new HashMap<Integer, Double>();
                    for (int index = 0; index < subject_entity_facts.size(); index++) {
                        DataTypeClassifier.DataType type_of_fact_value = fact_data_types.get(index);
                        Attribute fact = subject_entity_facts.get(index);
                        if (isValidType(type_of_fact_value)) {
                            continue;
                        }

                        double scoreWithCell = score(objText, object_column_type, fact.getValue(), type_of_fact_value, stopWords);
                        for (int o = 0; o < objectCellAnnotations.size(); o++) {
                            String objEntityLabel = objectCellAnnotations.get(o).getAnnotation().getLabel();
                            if (objEntityLabel != null) {
                                double scoreAgainstObjEntityLabel = score(objEntityLabel, object_column_type, fact.getValue(), type_of_fact_value, stopWords);
                                if (scoreWithCell < scoreAgainstObjEntityLabel)
                                    scoreWithCell = scoreAgainstObjEntityLabel;
                            }
                        }
                        if (scoreWithCell > 0 && scoreWithCell > minScoreThreshold)
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
                            Attribute fact = subject_entity_facts.get(index);
                            tableAnnotation.addCellCellRelation(new TCellCellRelationAnotation(
                                    new RelationColumns(subjectColumn, objectColumn), row,
                                    fact.getRelationURI(), fact.getRelationURI(),
                                    new ArrayList<>(), score
                            ));
                        }
                    }
                }//each subjectNE
            }//if block checking whether the potential subject-object cell pairs are valid
        }
    }

    protected Map<Integer, DataTypeClassifier.DataType> classifyFactObjDataType(List<Attribute> sbjEntityFacts) {
        Map<Integer, DataTypeClassifier.DataType> dataTypes = new HashMap<Integer, DataTypeClassifier.DataType>();
        //typing the objects of facts
        for (int index = 0; index < sbjEntityFacts.size(); index++) {
            Attribute fact = sbjEntityFacts.get(index);
            String val = fact.getValue();
            String id_of_val = fact.getValueURI();

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