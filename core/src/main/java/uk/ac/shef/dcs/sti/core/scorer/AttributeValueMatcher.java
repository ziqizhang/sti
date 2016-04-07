package uk.ac.shef.dcs.sti.core.scorer;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

import java.util.*;

/**
 * Created by - on 02/04/2016.
 */
public abstract class AttributeValueMatcher {

    protected List<String> stopWords;
    protected double minScoreThreshold;
    protected AbstractStringMetric stringMetric;

    public AttributeValueMatcher(double minScoreThreshold, List<String> stopWords,
                                    AbstractStringMetric stringMetric) {
        this.minScoreThreshold = minScoreThreshold;
        this.stopWords = stopWords;
        this.stringMetric = stringMetric;
    }

    public abstract Map<Integer, List<Pair<Attribute, Double>>> match(List<Attribute> attributes,
                                                             Map<Integer, String> cellTextValues,
                                                             Map<Integer, DataTypeClassifier.DataType> columnDataTypes);

    protected boolean isValidType(DataTypeClassifier.DataType dataType) {
        if (dataType.equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
            return false;
        if (dataType.equals(DataTypeClassifier.DataType.EMPTY))
            return false;
        if (dataType.equals(DataTypeClassifier.DataType.LONG_TEXT))
            return false;
        return true;
    }

    /**
     * number match scores are computed by matchNumber; text match scores are computed by dice;
     * long string (urls) are computed by a string similarity metric
     * @param string1
     * @param type_of_string1
     * @param string2
     * @param type_of_string2
     * @param stopWords
     * @return
     */
    protected double score(String string1,
                               DataTypeClassifier.DataType type_of_string1,
                               String string2,
                               DataTypeClassifier.DataType type_of_string2,
                               Collection<String> stopWords) {
        if (type_of_string1.equals(DataTypeClassifier.DataType.NAMED_ENTITY) &&
                (type_of_string2.equals(DataTypeClassifier.DataType.NUMBER)
                        || type_of_string2.equals(DataTypeClassifier.DataType.DATE)))
            return 0.0;
        if (type_of_string2.equals(DataTypeClassifier.DataType.NAMED_ENTITY) &&
                (type_of_string1.equals(DataTypeClassifier.DataType.NUMBER)
                        || type_of_string1.equals(DataTypeClassifier.DataType.DATE)))
            return 0.0;
        //long string like URL
        if (type_of_string1.equals(DataTypeClassifier.DataType.LONG_STRING) &&
                type_of_string2.equals(DataTypeClassifier.DataType.LONG_STRING))
            return stringMetric.getSimilarity(string1, string2);
        if (type_of_string1.equals(DataTypeClassifier.DataType.LONG_STRING) ||
                type_of_string2.equals(DataTypeClassifier.DataType.LONG_STRING))
            return 0.0;

        //number
        double score = -1.0;
        if (type_of_string1.equals(DataTypeClassifier.DataType.NUMBER) &&
                (type_of_string2.equals(DataTypeClassifier.DataType.NUMBER))) {
            score = matchNumber(string1, string2);
        }

        if (score == -1) {
            score = matchText(string1, string2,stopWords);
        }
        return score == -1.0 ? 0.0 : score;
    }


    protected double matchText(String target, String base, Collection<String> stopWords) {
        //method 1, check how much overlap the two texts have
        target = StringUtils.toAlphaNumericWhitechar(target);
        base = StringUtils.toAlphaNumericWhitechar(base);
        Set<String> target_toks = new HashSet<>(StringUtils.toBagOfWords(target,true,true,
                STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
        target_toks.removeAll(stopWords);
        Set<String> base_toks = new HashSet<>(StringUtils.toBagOfWords(base,true,true,
                STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
        base_toks.removeAll(stopWords);

        //method 2
        double score = CollectionUtils.computeDice(target_toks, base_toks);
        return score;
    }

    protected double matchNumber(String string1, String string2) {
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

    protected Map<Integer, DataTypeClassifier.DataType> classifyAttributeValueDataType(List<Attribute> attributes) {
        Map<Integer, DataTypeClassifier.DataType> dataTypes = new HashMap<>();
        //typing the objects of facts
        for (int index = 0; index < attributes.size(); index++) {
            Attribute fact = attributes.get(index);
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
