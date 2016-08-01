package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class TColumnColumnRelationAnnotation implements Serializable, Comparable<TColumnColumnRelationAnnotation> {

    private static final long serialVersionUID = -1208912663212074692L;
    private RelationColumns relationColumns;
    private List<Integer> supportingRows;

    private String relationURI;
    private String relationLabel;

    public static final String SCORE_FINAL ="final";

    private double finalScore;
    private Map<String, Double> scoreElements;


    //matched_value[]: (0)=property name (1)=the attribute value matched with the objecCol field on this row; (2) id/uri, if any (used for later knowledge base retrieval)
    public TColumnColumnRelationAnnotation(RelationColumns key, String relationURI, String relation_label, double score) {
        this.relationColumns = key;
        this.relationLabel =relation_label;
        this.relationURI = relationURI;
        this.finalScore = score;
        this.scoreElements=new HashMap<>();
        scoreElements=new HashMap<>();
        if(score!=0)
            scoreElements.put(SCORE_FINAL, score);
        else
            scoreElements.put(SCORE_FINAL, 0.0);
        this.supportingRows = new ArrayList<>();
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public String getRelationURI() {
        return relationURI;
    }


    @Override
    public int compareTo(TColumnColumnRelationAnnotation o) {
        int compared = new Double(o.getFinalScore()).compareTo(getFinalScore());

        if (compared == 0)
            return new Integer(o.getSupportingRows().size()).compareTo(getSupportingRows().size());

        return compared;
    }

    public RelationColumns getRelationColumns() {
        return relationColumns;
    }

    public void setRelationColumns(RelationColumns relationColumns) {
        this.relationColumns = relationColumns;
    }


    public String toString() {
        return relationURI;
    }

    public String toStringExpanded(){
        return "("+ getRelationColumns()+")"+ relationURI;
    }
    public static String toStringExpanded(int fromCol, int toCol, String relationURL){
        return "("+fromCol+"-"+toCol+")"+relationURL;
    }

    public List<Integer> getSupportingRows() {
        return supportingRows;
    }

    public void addSupportingRow(int row) {
        if (!supportingRows.contains(row))
            supportingRows.add(row);
    }

    public Map<String, Double> getScoreElements() {
        return scoreElements;
    }

    public void setScoreElements(Map<String, Double> scoreElements) {
        this.scoreElements = scoreElements;
    }

    public String getRelationLabel() {
        return relationLabel;
    }

    public void setRelationLabel(String relationLabel) {
        this.relationLabel = relationLabel;
    }

    public boolean equals(Object o){
        if(o instanceof TColumnColumnRelationAnnotation){
            TColumnColumnRelationAnnotation hbr = (TColumnColumnRelationAnnotation) o;
            return hbr.getRelationColumns().equals(getRelationColumns()) &&
                    hbr.getRelationURI().equals(getRelationURI());
        }
        return false;
    }
}
