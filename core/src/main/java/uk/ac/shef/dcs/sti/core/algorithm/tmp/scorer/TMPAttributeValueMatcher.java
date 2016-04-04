package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

import java.util.*;

/**
 */
public class TMPAttributeValueMatcher extends AttributeValueMatcher {


    public TMPAttributeValueMatcher(double minScoreThreshold, List<String> stopWords, AbstractStringMetric stringMetric) {
        super(minScoreThreshold, stopWords, stringMetric);
    }

    /**
     *
     * @param attributes
     * @param cellTextValues key: column index; value: cell text in that column
     * @param columnDataTypes
     * @return key: column index; value: list of winning attribute and score matched agains the value in that column
     */
    public Map<Integer, List<Pair<Attribute, Double>>> match(List<Attribute> attributes,
                                                        Map<Integer, String> cellTextValues,
                                                        Map<Integer, DataTypeClassifier.DataType> columnDataTypes) {
        Map<Integer, List<Pair<Attribute, Double>>> matchedScores =
                new HashMap<>();

        //check the data type of attributes' values
        Map<Integer, DataTypeClassifier.DataType> attributeValueDataTypes = new HashMap<>();
        for (int index = 0; index < attributes.size(); index++) {
            Attribute attr = attributes.get(index);
            String prop = attr.getRelationURI();
            String val = attr.getValue();
            String id_of_val = attr.getValueURI();

            if (id_of_val != null)
                attributeValueDataTypes.put(index, DataTypeClassifier.DataType.NAMED_ENTITY);
            else {
                DataTypeClassifier.DataType type = DataTypeClassifier.classify(val);
                attributeValueDataTypes.put(index, type);
            }
        }

        //compute scores for each value on the row
        for (Map.Entry<Integer, String> e : cellTextValues.entrySet()) {
            int column = e.getKey();
            String textValue = e.getValue();
            DataTypeClassifier.DataType cellDataType = columnDataTypes.get(column);
            if (cellDataType == null || !isValidType(cellDataType))
                continue;

            double maxScore = 0.0;
            Map<Integer, Double> attrIndex_to_matchScores = new HashMap<>();
            for (int index = 0; index < attributes.size(); index++) {
                DataTypeClassifier.DataType dataTypeOfAttrValue = attributeValueDataTypes.get(index);
                Attribute attr = attributes.get(index);
                if (!isValidType(dataTypeOfAttrValue))
                    continue;

                double score = score(textValue, cellDataType, attr.getValue(), dataTypeOfAttrValue,stopWords);
                if (score > maxScore) {
                    maxScore = score;
                }
                attrIndex_to_matchScores.put(index, score);
            }


            if (maxScore >= minScoreThreshold) {
                List<Pair<Attribute, Double>> list = new ArrayList<>();
                for(Map.Entry<Integer, Double> entry: attrIndex_to_matchScores.entrySet()){
                    if(entry.getValue()==maxScore){
                        Attribute winningAttr = attributes.get(entry.getKey());
                        Pair<Attribute, Double> score_obj = new Pair<>(winningAttr,
                                maxScore);
                        list.add(score_obj);
                    }
                }
                if(list.size()>0)
                    matchedScores.put(column, list);

            }

        }
        return matchedScores;
    }





}
