package uk.ac.shef.dcs.sti.core.algorithm.smp;

import javafx.util.Pair;
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
public class SMPAttributeValueMatcher extends AttributeValueMatcher{

    public SMPAttributeValueMatcher(double minScoreThreshold, List<String> stopWords,
                                    AbstractStringMetric stringMetric) {
        super(minScoreThreshold,stopWords,stringMetric);
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
        Map<Integer, DataTypeClassifier.DataType> attributeValueDataTypes =
                classifyAttributeValueDataType(attributes);

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


            if (maxScore!=0&&maxScore >= minScoreThreshold) {
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
                      DataTypeClassifier.DataType objectColumnDataType,
                      TAnnotation tableAnnotation
    ) {
        if (subjectCellAnnotations.size() != 0) {
            if (subjectCellAnnotations.size() > 0 && isValidType(objectColumnDataType)) {
                for (int s = 0; s < subjectCellAnnotations.size(); s++) {
                    TCellAnnotation subjectEntity = subjectCellAnnotations.get(s);
                    List<Attribute> subEntityAttributes = subjectEntity.getAnnotation().getAttributes();
                    Map<Integer, DataTypeClassifier.DataType> attrValueDataTypes = classifyAttributeValueDataType(
                            subEntityAttributes
                    );

                    String objText = objectCellText.getText();
                    //scoring matches for the cell on the row
                    final Map<Integer, Double> fact_matched_scores = new HashMap<Integer, Double>();
                    for (int index = 0; index < subEntityAttributes.size(); index++) {
                        DataTypeClassifier.DataType type_of_fact_value = attrValueDataTypes.get(index);
                        Attribute fact = subEntityAttributes.get(index);
                        if (isValidType(type_of_fact_value)) {
                            continue;
                        }

                        double scoreWithCell = score(objText, objectColumnDataType, fact.getValue(), type_of_fact_value, stopWords);
                        for (int o = 0; o < objectCellAnnotations.size(); o++) {
                            String objEntityLabel = objectCellAnnotations.get(o).getAnnotation().getLabel();
                            if (objEntityLabel != null) {
                                double scoreAgainstObjEntityLabel = score(objEntityLabel, objectColumnDataType, fact.getValue(), type_of_fact_value, stopWords);
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
                    Collections.sort(qualified, (o1, o2)
                            -> fact_matched_scores.get(o2).compareTo(fact_matched_scores.get(o1)));
                    Double highestScore = fact_matched_scores.get(qualified.get(0));
                    for (Map.Entry<Integer, Double> e : fact_matched_scores.entrySet()) {
                        int index = e.getKey();
                        Double score = e.getValue();
                        if (score.equals(highestScore)) {
                            Attribute fact = subEntityAttributes.get(index);
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


}