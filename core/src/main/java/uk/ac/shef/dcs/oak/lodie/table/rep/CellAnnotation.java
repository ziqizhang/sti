package uk.ac.shef.dcs.oak.lodie.table.rep;

import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 Annotation for an entity or a concept
 */
public class CellAnnotation implements Serializable, Comparable<CellAnnotation>{

    public static final String SCORE_FINAL="final";
    public static final String SCORE_NAME_MATCH="name_match";
    public static final String SCORE_NAME_MATCH_CTX ="name_match_header";
    public static final String SCORE_CTX_ROW ="ctx_row";
    public static final String SCORE_CTX_COLUMN ="ctx_column";
    public static final String SCORE_TYPE_MATCH="type_match";
    public static final String SCORE_COOCCUR_ENTITIES="ctx_coocur_entities";
    public static final String SCORE_CTX_OTHER ="ctx_other";

    private String term;
    private EntityCandidate annotation;
    private Map<String, Double> score_element_map;
    private double finalScore;

    public CellAnnotation(String term, EntityCandidate annotation, double score, Map<String, Double> score_elements){
        this.term=term;
        this.annotation=annotation;
        this.finalScore =score;
        this.score_element_map=score_elements;
    }

    public static CellAnnotation copy(CellAnnotation ca){
        CellAnnotation newCa = new CellAnnotation(ca.getTerm(),
                ca.getAnnotation(),
                ca.getFinalScore(),
                new HashMap<String, Double>(ca.getScore_element_map()));
        return newCa;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public EntityCandidate getAnnotation() {
        return annotation;
    }

    public void setAnnotation(EntityCandidate annotation) {
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
    public int compareTo(CellAnnotation o) {

        return new Double(o.getFinalScore()).compareTo(getFinalScore());

    }

    public Map<String, Double> getScore_element_map() {
        return score_element_map;
    }

    public void setScore_element_map(Map<String, Double> score_element_map) {
        this.score_element_map = score_element_map;
    }

    public boolean equals(Object o){
        if(o instanceof CellAnnotation){
            CellAnnotation ca = (CellAnnotation) o;
            return ca.getAnnotation().equals(getAnnotation()) && ca.getTerm().equals(getTerm());
        }
        return false;
    }
}
