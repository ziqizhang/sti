package uk.ac.shef.dcs.sti.core.model;

import uk.ac.shef.dcs.kbsearch.model.Clazz;

import java.io.Serializable;
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
public class TColumnHeaderAnnotation implements Serializable,Comparable<TColumnHeaderAnnotation> {
    private static final long serialVersionUID = -6208426814708405913L;

    public static final String SUM_CE ="sum_ce";
    public static final String SUM_CELL_VOTE ="sum_cell_vote";
    public static final String SCORE_CE ="ce_score";
    public static final String SCORE_CELL_VOTE ="cell_vote";
    public static final String SCORE_CTX_IN_HEADER ="ctx_header_text";
    public static final String SCORE_CTX_IN_COLUMN ="ctx_column_text";
    public static final String SCORE_CTX_OUT ="ctx_out_context";
    public static final String SCORE_CTX_RELATION_IF_ANY="ctx_relation_with_sub_col";
    public static final String FINAL="final";
    public static final String SCORE_DOMAIN_CONSENSUS = "domain_consensus";

    private String headerText;
    private Clazz annotation;
    private double finalScore;
    private Map<String, Double> scoreElements;
    private List<Integer> supportingRows;


    public TColumnHeaderAnnotation(String headerText, Clazz annotation, double finalScore) {
        this.headerText = headerText;
        this.annotation=annotation;
        this.finalScore = finalScore;
        supportingRows = new ArrayList<>();
        scoreElements=new HashMap<>();
        scoreElements.put(SUM_CE, 0.0);
        scoreElements.put(SUM_CELL_VOTE, 0.0);
        scoreElements.put(SCORE_CE, 0.0);
        scoreElements.put(SCORE_CELL_VOTE, 0.0);
        scoreElements.put(SCORE_CTX_IN_HEADER, null);
        scoreElements.put(SCORE_CTX_IN_COLUMN, null);
        scoreElements.put(SCORE_CTX_OUT, null);
        scoreElements.put(FINAL, 0.0);
    }

    public static TColumnHeaderAnnotation copy(TColumnHeaderAnnotation ha) {
        TColumnHeaderAnnotation newHa =
                new TColumnHeaderAnnotation(ha.getHeaderText(), ha.getAnnotation(), ha.getFinalScore());
        for (int i : ha.getSupportingRows())
            newHa.addSupportingRow(i);
        newHa.setScoreElements(new HashMap<>(ha.getScoreElements()));
        return newHa;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
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
        if (o instanceof TColumnHeaderAnnotation) {
            TColumnHeaderAnnotation ha = (TColumnHeaderAnnotation) o;
            return ha.getHeaderText().equals(getHeaderText()) && ha.getAnnotation().equals(getAnnotation());
        }
        return false;
    }

    public int hashCode() {
        return getHeaderText().hashCode() + 19 * getAnnotation().getId().hashCode();
    }

    public String toString() {
        return headerText + "," + getAnnotation();
    }

    @Override
    public int compareTo(TColumnHeaderAnnotation o) {
        int compared = ((Double) o.getFinalScore()).compareTo(getFinalScore());
        if (compared == 0)
            return new Integer(o.getSupportingRows().size()).compareTo(getSupportingRows().size());

        return compared;
    }

    public Map<String, Double> getScoreElements() {
        return scoreElements;
    }

    public void setScoreElements(Map<String, Double> scoreElements) {
        this.scoreElements = scoreElements;
    }

    public Clazz getAnnotation() {
        return annotation;
    }
}
