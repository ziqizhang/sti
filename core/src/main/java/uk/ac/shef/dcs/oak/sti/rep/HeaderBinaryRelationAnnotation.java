package uk.ac.shef.dcs.oak.sti.rep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class HeaderBinaryRelationAnnotation implements Serializable, Comparable<HeaderBinaryRelationAnnotation> {

    private Key_SubjectCol_ObjectCol subject_object_key;
    private List<Integer> supportingRows;

    private String annotation_url;
    private String annotation_label;
    public static final String SUM_CBR_MATCH_SCORE ="sum_cbr_match_score";
    public static final String SUM_CBR_VOTE ="sum_cbr_vote";
    public static final String SCORE_CBR_MATCH ="cbr_match_score";
    public static final String SCORE_CBR_VOTE ="cbr_vote_score";
    public static final String SCORE_CTX_HEADER_TEXT ="ctx_header_text";
    public static final String SCORE_CTX_COLUMN_TEXT ="ctx_column_text";
    public static final String SCORE_CTX_TABLE_CONTEXT="ctx_table_context";
    public static final String FINAL="final";
    public static final String SCORE_DOMAIN_CONSENSUS="domain_consensus";

    private double finalScore;
    private Map<String, Double> scoreElements;


    //matched_value[]: (0)=property name (1)=the attribute value matched with the objecCol field on this row; (2) id/uri, if any (used for later knowledge base retrieval)
    public HeaderBinaryRelationAnnotation(Key_SubjectCol_ObjectCol key, String relation_annotation, String relation_label, double score) {
        this.subject_object_key = key;
        this.annotation_label=relation_label;
        this.annotation_url = relation_annotation;
        this.finalScore = score;
        this.scoreElements=new HashMap<String, Double>();
        scoreElements=new HashMap<String, Double>();
        scoreElements.put(SUM_CBR_MATCH_SCORE, 0.0);
        scoreElements.put(SUM_CBR_VOTE, 0.0);
        scoreElements.put(SCORE_CBR_MATCH, 0.0);
        scoreElements.put(SCORE_CBR_VOTE, 0.0);
        scoreElements.put(SCORE_CTX_HEADER_TEXT, null);
        scoreElements.put(SCORE_CTX_COLUMN_TEXT, null);
        scoreElements.put(SCORE_CTX_TABLE_CONTEXT, null);
        scoreElements.put(FINAL, 0.0);
        this.supportingRows = new ArrayList<Integer>();
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public String getAnnotation_url() {
        return annotation_url;
    }


    @Override
    public int compareTo(HeaderBinaryRelationAnnotation o) {
        int compared = new Double(o.getFinalScore()).compareTo(getFinalScore());

        if (compared == 0)
            return new Integer(o.getSupportingRows().size()).compareTo(getSupportingRows().size());

        return compared;
    }

    public Key_SubjectCol_ObjectCol getSubject_object_key() {
        return subject_object_key;
    }

    public void setSubject_object_key(Key_SubjectCol_ObjectCol subject_object_key) {
        this.subject_object_key = subject_object_key;
    }


    public String toString() {
        return annotation_url;
    }

    public String toStringExpanded(){
        return "("+getSubject_object_key()+")"+annotation_url;
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

    public String getAnnotation_label() {
        return annotation_label;
    }

    public void setAnnotation_label(String annotation_label) {
        this.annotation_label = annotation_label;
    }

    public boolean equals(Object o){
        if(o instanceof HeaderBinaryRelationAnnotation){
            HeaderBinaryRelationAnnotation hbr = (HeaderBinaryRelationAnnotation) o;
            return hbr.getSubject_object_key().equals(getSubject_object_key()) &&
                    hbr.getAnnotation_url().equals(getAnnotation_url());
        }
        return false;
    }
}
