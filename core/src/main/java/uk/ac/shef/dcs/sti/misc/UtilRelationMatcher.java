package uk.ac.shef.dcs.sti.misc;

import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zqz on 22/04/2015.
 */
public class UtilRelationMatcher {

    public static boolean isValidType(DataTypeClassifier.DataType type_of_table_row_value) {
        if (type_of_table_row_value.equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
            return false;
        if (type_of_table_row_value.equals(DataTypeClassifier.DataType.EMPTY))
            return false;
        if (type_of_table_row_value.equals(DataTypeClassifier.DataType.LONG_TEXT))
            return false;
        return true;
    }

    public static double matchText(String target, String base, Collection<String> stopWords) {
        //method 1, check how much overlap the two texts have
        target = StringUtils.toAlphaNumericWhitechar(target);
        base = StringUtils.toAlphaNumericWhitechar(base);
        Set<String> target_toks = new HashSet<String>();
        Set<String> base_toks = new HashSet<String>();
        for (String s1 : target.split("\\s+")) {
            s1=s1.trim();
            if(stopWords!=null&& stopWords.contains(s1))
                continue;
            if(TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW&&s1.length()<2)
                continue;
            target_toks.add(s1.toLowerCase());
        }
        for (String s2 : base.split("\\s+")) {
            s2=s2.trim();
            if(stopWords!=null&&stopWords.contains(s2))
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

    public static double matchNumber(String string1, String string2) {
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

    public static double score(String string1,
                        DataTypeClassifier.DataType type_of_string1,
                        String string2,
                        DataTypeClassifier.DataType type_of_string2,
                        Collection<String> stopWords,
                        AbstractStringMetric stringSimilarity) {
        //in some cases certain types do not score
        if (type_of_string1.equals(DataTypeClassifier.DataType.NAMED_ENTITY) &&
                (type_of_string2.equals(DataTypeClassifier.DataType.NUMBER) || type_of_string2.equals(DataTypeClassifier.DataType.DATE)))
            return 0.0;
        if (type_of_string2.equals(DataTypeClassifier.DataType.NAMED_ENTITY) &&
                (type_of_string1.equals(DataTypeClassifier.DataType.NUMBER) || type_of_string1.equals(DataTypeClassifier.DataType.DATE)))
            return 0.0;
        if (type_of_string1.equals(DataTypeClassifier.DataType.LONG_STRING) &&
                type_of_string2.equals(DataTypeClassifier.DataType.LONG_STRING))
            return stringSimilarity.getSimilarity(string1, string2);
        if (type_of_string1.equals(DataTypeClassifier.DataType.LONG_STRING) ||
                type_of_string2.equals(DataTypeClassifier.DataType.LONG_STRING))
            return 0.0;

        //score number
        double score = -1.0;
        if (type_of_string1.equals(DataTypeClassifier.DataType.NUMBER) &&
                (type_of_string2.equals(DataTypeClassifier.DataType.NUMBER))) {
            score = UtilRelationMatcher.matchNumber(string1, string2);
        }

        if (score == -1) {
            score = UtilRelationMatcher.matchText(string1, string2,stopWords);
        }
        return score == -1.0 ? 0.0 : score;
    }
}
