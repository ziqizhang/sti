package uk.ac.shef.dcs.oak.lodie.table.interpreter.interpret;

import org.apache.lucene.util.CollectionUtil;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.UtilRelationMatcher;
import uk.ac.shef.dcs.oak.lodie.test.TableMinerConstants;
import uk.ac.shef.dcs.oak.util.CollectionUtils;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.dcs.oak.util.StringUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.util.*;

/**
 */
public class RelationTextMatch_Scorer {

    private List<String> stopWords;
    private double minimum_match_score;
    private Levenshtein string_sim_levenstein;
    public static final double MAX_SCORE=1.0;

    public RelationTextMatch_Scorer(double minimum_match_score, List<String> stopWords) {
        this.minimum_match_score = minimum_match_score;
        this.stopWords = stopWords;
        string_sim_levenstein = new Levenshtein();
    }


    public Map<Integer, List<ObjObj<String[], Double>>> match(List<String[]> facts,
                                                        Map<Integer, String> values_on_the_row,
                                                        Map<Integer, DataTypeClassifier.DataType> column_types) {
        Map<Integer, List<ObjObj<String[], Double>>> matching_scores =
                new HashMap<Integer, List<ObjObj<String[],Double>>>();

        //typing facts

        Map<Integer, DataTypeClassifier.DataType> fact_data_types = new HashMap<Integer, DataTypeClassifier.DataType>();
        for (int index = 0; index < facts.size(); index++) {
            String[] fact = facts.get(index);
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

        //scoring matches for each value on the row
        for (Map.Entry<Integer, String> e : values_on_the_row.entrySet()) {
            int column = e.getKey();
            String value_on_this_row = e.getValue();
            DataTypeClassifier.DataType type_of_table_row_value = column_types.get(column);
            if (type_of_table_row_value == null || !UtilRelationMatcher.isValidType(type_of_table_row_value))
                continue;

            double maxScore = 0.0;
            Map<Integer, Double> fact_matched_scores = new HashMap<Integer, Double>();
            for (int index = 0; index < facts.size(); index++) {
                DataTypeClassifier.DataType type_of_fact_value = fact_data_types.get(index);
                String[] fact = facts.get(index);
                /* if (stopProperties.contains(fact[0]))
                                    continue;
                */
                if (!UtilRelationMatcher.isValidType(type_of_fact_value)) {
                    continue;
                }

                /*if(fact[0].contains("contains"))
                    System.out.println();*/
                double score = UtilRelationMatcher.score(value_on_this_row, type_of_table_row_value, fact[1], type_of_fact_value,stopWords, string_sim_levenstein);
                if (score > maxScore) {
                    maxScore = score;
                }
                fact_matched_scores.put(index, score);
            }


            if (maxScore >= minimum_match_score&&maxScore!=0.0) {
                List<ObjObj<String[], Double>> list = new ArrayList<ObjObj<String[], Double>>();
                for(Map.Entry<Integer, Double> nexte: fact_matched_scores.entrySet()){
                    if(nexte.getValue()==maxScore){
                        ObjObj<String[], Double> score_obj = new ObjObj<String[], Double>();
                        String[] string_array_of_matched_fact = facts.get(nexte.getKey());
                        score_obj.setMainObject(string_array_of_matched_fact);
                        score_obj.setOtherObject(maxScore);
                        list.add(score_obj);
                    }
                }
                if(list.size()>0)
                    matching_scores.put(column, list);

            }

        }
        return matching_scores;
    }





}
