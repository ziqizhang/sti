package uk.ac.shef.dcs.oak.lodie.table.interpreter.interpret;

import org.apache.lucene.util.CollectionUtil;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.DataTypeClassifier;
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
            if (type_of_table_row_value == null || !isValidType(type_of_table_row_value))
                continue;

            double maxScore = 0.0;
            Map<Integer, Double> fact_matched_scores = new HashMap<Integer, Double>();
            for (int index = 0; index < facts.size(); index++) {
                DataTypeClassifier.DataType type_of_fact_value = fact_data_types.get(index);
                String[] fact = facts.get(index);
                /* if (stopProperties.contains(fact[0]))
                                    continue;
                */
                if (!isValidType(type_of_fact_value)) {
                    continue;
                }

                /*if(fact[0].contains("contains"))
                    System.out.println();*/
                double score = score(value_on_this_row, type_of_table_row_value, fact[1], type_of_fact_value,true);
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


    private boolean isValidType(DataTypeClassifier.DataType type_of_table_row_value) {
        if (type_of_table_row_value.equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
            return false;
        if (type_of_table_row_value.equals(DataTypeClassifier.DataType.EMPTY))
            return false;
        if (type_of_table_row_value.equals(DataTypeClassifier.DataType.LONG_TEXT))
            return false;
        return true;
    }


    public double score(String string1,
                        DataTypeClassifier.DataType type_of_string1,
                        String string2,
                        DataTypeClassifier.DataType type_of_string2,
                        boolean removeStop) {
        //in some cases certain types do not match
        if (type_of_string1.equals(DataTypeClassifier.DataType.NAMED_ENTITY) &&
                (type_of_string2.equals(DataTypeClassifier.DataType.NUMBER) || type_of_string2.equals(DataTypeClassifier.DataType.DATE)))
            return 0.0;
        if (type_of_string2.equals(DataTypeClassifier.DataType.NAMED_ENTITY) &&
                (type_of_string1.equals(DataTypeClassifier.DataType.NUMBER) || type_of_string1.equals(DataTypeClassifier.DataType.DATE)))
            return 0.0;
        if (type_of_string1.equals(DataTypeClassifier.DataType.LONG_STRING) &&
                type_of_string2.equals(DataTypeClassifier.DataType.LONG_STRING))
            return string_sim_levenstein.getSimilarity(string1, string2);
        if (type_of_string1.equals(DataTypeClassifier.DataType.LONG_STRING) ||
                type_of_string2.equals(DataTypeClassifier.DataType.LONG_STRING))
            return 0.0;

        //match number
        double score = -1.0;
        if (type_of_string1.equals(DataTypeClassifier.DataType.NUMBER) &&
                (type_of_string2.equals(DataTypeClassifier.DataType.NUMBER))) {
            score = matchNumber(string1, string2);
        }

        if (score == -1) {
            score = matchText(string1, string2,removeStop);
        }
        return score == -1.0 ? 0.0 : score;
    }

    private double matchText(String target, String base, boolean removeStop) {
        //method 1, check how much overlap the two texts have
        target = StringUtils.toAlphaNumericWhitechar(target);
        base = StringUtils.toAlphaNumericWhitechar(base);
        Set<String> target_toks = new HashSet<String>();
        Set<String> base_toks = new HashSet<String>();
        for (String s1 : target.split("\\s+")) {
            s1=s1.trim();
            if(removeStop&& stopWords.contains(s1))
                continue;
            if(TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW&&s1.length()<2)
                continue;
            target_toks.add(s1.toLowerCase());
        }
        for (String s2 : base.split("\\s+")) {
            s2=s2.trim();
            if(removeStop&&stopWords.contains(s2))
                continue;
            if(TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW&&s2.length()<2)
                continue;
            base_toks.add(s2.toLowerCase());
        }


        //method 1
        /*double score = 0.0;
        int base_original_size = base_toks.size();
        base_toks.retainAll(target_toks);
        if (base_toks.size() > 0) {
            score = ((double) base_toks.size() / base_original_size + (double) base_toks.size() / target_toks.size()) / 2.0;
        }
        if (score > 0)
            return score;*/

        //method 2
        double score = CollectionUtils.scoreOverlap_dice(target_toks, base_toks);
        return /*score * */score;

        //method 3, string similarity
        // score = string_sim_levenstein.getSimilarity(target, base);
        //return score;
    }

    private double matchNumber(String string1, String string2) {
        try {
            double number1 = Double.valueOf(string1);
            double number2 = Double.valueOf(string2);

            double max = Math.max(number1, number2);
            double maxDiff = max * 0.05; //the maximum difference allowed between the two numbers in order to mean they are equal is 10% of the max number
            double diff = Math.abs(number1 - number2);

            if (diff < maxDiff)
                return 1.0;
            else
                return maxDiff / diff;
        } catch (Exception e) {
            return -1.0;
        }
    }

}
