package uk.ac.shef.dcs.sti.core.model;

import uk.ac.shef.dcs.kbsearch.model.Entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 Annotation for an entity or a concept
 */
public class TCellAnnotation implements Serializable, Comparable<TCellAnnotation>{

    private static final long serialVersionUID = -8136725814000843856L;

    public static final String SCORE_FINAL="final";

    private String term;
    private Entity annotation;
    private Map<String, Double> score_element_map;
    private double finalScore;

    public TCellAnnotation(String term, Entity annotation, double score, Map<String, Double> score_elements){
        this.term=term;
        this.annotation=annotation;
        this.finalScore =score;
        this.score_element_map=score_elements;
    }

    public static TCellAnnotation copy(TCellAnnotation ca){
        TCellAnnotation newCa = new TCellAnnotation(ca.getTerm(),
                ca.getAnnotation(),
                ca.getFinalScore(),
                new HashMap<>(ca.getScoreElements()));
        return newCa;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Entity getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Entity annotation) {
        this.annotation = annotation;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double score) {
        this.finalScore = score;
    }

    public String toString(){
        return getTerm()+","+getAnnotation();
    }

    @Override
    public int compareTo(TCellAnnotation o) {

        return new Double(o.getFinalScore()).compareTo(getFinalScore());

    }

    public Map<String, Double> getScoreElements() {
        return score_element_map;
    }


    public boolean equals(Object o){
        if(o instanceof TCellAnnotation){
            TCellAnnotation ca = (TCellAnnotation) o;
            return ca.getAnnotation().equals(getAnnotation()) && ca.getTerm().equals(getTerm());
        }
        return false;
    }
}
