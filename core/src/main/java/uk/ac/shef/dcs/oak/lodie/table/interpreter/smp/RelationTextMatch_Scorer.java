package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import org.openjena.atlas.iterator.Iter;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.UtilRelationMatcher;
import uk.ac.shef.dcs.oak.lodie.table.rep.*;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.util.*;

/**
 * Created by zqz on 21/04/2015.
 */
public class RelationTextMatch_Scorer {

    private Collection<String> stopWords;
    private double minimum_match_score;
    private AbstractStringMetric stringSimilarityMetric;

    public RelationTextMatch_Scorer(Collection<String> stopWords, AbstractStringMetric stringSimilarityMetric, double minimum_match_score) {
        this.stopWords = stopWords;
        this.stringSimilarityMetric = stringSimilarityMetric;
        this.minimum_match_score = minimum_match_score;
    }

    /**
     * This is the adapted implementation of SMP. Only the highest scoring cell entity annotation is used to infer candidate
     * relations.
     *
     * @param subjectCells NEs that have the highest score (there can be multiple)
     * @param objectCells  NEs that have the highest score (there can be multiple)
     * @return
     */
    public void match(int row,
                      List<CellAnnotation> subjectCells, List<CellAnnotation> objectCells,
                      LTableContentCell subjectCellText, LTableContentCell objectCellText,
                      DataTypeClassifier.DataType object_column_type,
                      LTableAnnotation tableAnnotation
    ) {
        List<ObjObj<String, Double>> result = new ArrayList<ObjObj<String, Double>>();
        if (subjectCells.size() > 0 && UtilRelationMatcher.isValidType(object_column_type)) {
            for (int s = 0; s < subjectCells.size(); s++) {
                CellAnnotation subjectEntity = subjectCells.get(s);
                String subText = subjectCellText.getText();
                for (int o = 0; o < objectCells.size(); o++) {
                    String objEntityLabel = null;
                    if (objectCells.size() > 0) {
                        CellAnnotation objectEntity = objectCells.get(o);
                        objEntityLabel = objectEntity.getTerm();
                    }
                    String objText = objectCellText.getText();

                    List<String[]> subject_entity_facts = subjectEntity.getAnnotation().getFacts();
                    Map<Integer, List<ObjObj<String[], Double>>> matching_scores =
                            new HashMap<Integer, List<ObjObj<String[], Double>>>();

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
                    for (int index = 0; index < subject_entity_facts.size(); index++) {
                        DataTypeClassifier.DataType type_of_fact_value = fact_data_types.get(index);
                        String[] fact = subject_entity_facts.get(index);
                        if (!UtilRelationMatcher.isValidType(type_of_fact_value)) {
                            continue;
                        }

                        double score = UtilRelationMatcher.score(objText, object_column_type, fact[1], type_of_fact_value, stopWords, stringSimilarityMetric);
                        if (objEntityLabel != null) {
                            double scoreAgainstObjEntityLabel = UtilRelationMatcher.score(objEntityLabel, object_column_type, fact[1], type_of_fact_value, stopWords, stringSimilarityMetric);
                            if (score < scoreAgainstObjEntityLabel)
                                score = scoreAgainstObjEntityLabel;
                        }

                        if (score > 0 && score > minimum_match_score)
                            fact_matched_scores.put(index, score);
                    }

                    //go thru all scores and make selection   within each subjectEntity-objectEntity pair
                    List<Integer> qualified = new ArrayList<Integer>(fact_data_types.keySet());
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
                        if (highestScore == score) {
                            String[] fact = subject_entity_facts.get(index);
                            result.add(new ObjObj<String, Double>(fact[0], score));
                        }
                    }


                    //sorting and selection for all and create annotations
                    Collections.sort(result, new Comparator<ObjObj<String, Double>>() {
                        @Override
                        public int compare(ObjObj<String, Double> o1, ObjObj<String, Double> o2) {
                            return o2.getOtherObject().compareTo(o1.getOtherObject());
                        }
                    });
                    maxScore = result.get(0).getOtherObject();
                    for (ObjObj<String, Double> r : result) {
                        tableAnnotation.addRelationAnnotation_per_row(new CellBinaryRelationAnnotation(
                                new Key_SubjectCol_ObjectCol(s, o), row, r.getMainObject(), r.getMainObject(),
                                null, maxScore
                        ));
                    }
                }// each subjectNE-objectNE pair
            }//each subjectNE
        }//if block checking whether the potential subject-object cell pairs are valid
    }
}
