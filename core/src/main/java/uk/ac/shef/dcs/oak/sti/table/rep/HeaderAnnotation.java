package uk.ac.shef.dcs.oak.sti.table.rep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 24/01/14
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
public class HeaderAnnotation implements Comparable<HeaderAnnotation> {
    public static final String SUM_ENTITY_DISAMB ="sum_entity_disamb";
    public static final String SUM_ENTITY_VOTE ="sum_entity_vote";
    public static final String SCORE_ENTITY_DISAMB ="entity_disamb_score";
    public static final String SCORE_ENTITY_VOTE ="entity_vote_score";
    public static final String SCORE_CTX_NAME_MATCH ="ctx_header_text";
    public static final String SCORE_CTX_COLUMN_TEXT ="ctx_column_text";
    //public static final String SCORE_CTX_ROW_TEXT ="ctx_row_text";
    public static final String SCORE_CTX_TABLE_CONTEXT="ctx_table_context";
    public static final String SCORE_CTX_RELATION_IF_ANY="ctx_relation_with_sub_col";
    public static final String FINAL="final";
    public static String SCORE_DOMAIN_CONSENSUS = "domain_consensus";

    private String term;
    private String annotation_url;
    private String annotation_label;
    private double finalScore;
    private Map<String, Double> scoreElements;
    private List<Integer> supportingRows;


    public HeaderAnnotation(String term, String annotation_url, String annotation_label, double finalScore) {
        this.term = term;
        this.annotation_url = annotation_url;
        this.annotation_label=annotation_label;
        this.finalScore = finalScore;
        supportingRows = new ArrayList<Integer>();
        scoreElements=new HashMap<String, Double>();
        scoreElements.put(SUM_ENTITY_DISAMB, 0.0);
        scoreElements.put(SUM_ENTITY_VOTE, 0.0);
        scoreElements.put(SCORE_ENTITY_DISAMB, 0.0);
        scoreElements.put(SCORE_ENTITY_VOTE, 0.0);
        scoreElements.put(SCORE_CTX_NAME_MATCH, null);
        scoreElements.put(SCORE_CTX_COLUMN_TEXT, null);
        scoreElements.put(SCORE_CTX_TABLE_CONTEXT, null);
        scoreElements.put(FINAL, 0.0);
    }

    public static HeaderAnnotation copy(HeaderAnnotation ha){
        HeaderAnnotation newHa = new HeaderAnnotation(ha.getTerm(),ha.getAnnotation_url(),ha.getAnnotation_label(), ha.getFinalScore());
        for(int i: ha.getSupportingRows())
            newHa.addSupportingRow(i);
        newHa.setScoreElements(new HashMap<String, Double>(ha.getScoreElements()));
        return newHa;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getAnnotation_url() {
        return annotation_url;
    }

    public void setAnnotation_url(String annotation_url) {
        this.annotation_url = annotation_url;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public List<Integer> getSupportingRows() {
        return supportingRows;
    }

    public void addSupportingRow(int rowId) {
        if(!supportingRows.contains(rowId))
            supportingRows.add(rowId);
    }

    public boolean equals(Object o) {
        if (o instanceof HeaderAnnotation) {
            HeaderAnnotation ha = (HeaderAnnotation) o;
            return ha.getTerm().equals(getTerm()) && ha.getAnnotation_url().equals(getAnnotation_url());
        }
        return false;
    }

    public int hashCode() {
        return getTerm().hashCode() + 19 * getAnnotation_url().hashCode();
    }

    public String toString() {
        return term + "," + annotation_url;
    }

    @Override
    public int compareTo(HeaderAnnotation o) {
        int compared = ((Double) o.getFinalScore()).compareTo(getFinalScore());
        if (compared == 0)
            return new Integer(o.getSupportingRows().size()).compareTo(getSupportingRows().size());

        return compared;
    }

    public String getAnnotation_label() {
        return annotation_label;
    }

    public void setAnnotation_label(String annotation_label) {
        this.annotation_label = annotation_label;
    }

    public Map<String, Double> getScoreElements() {
        return scoreElements;
    }

    public void setScoreElements(Map<String, Double> scoreElements) {
        this.scoreElements = scoreElements;
    }
}
