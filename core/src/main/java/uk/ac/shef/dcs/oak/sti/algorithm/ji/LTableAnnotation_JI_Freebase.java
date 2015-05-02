package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zqz on 01/05/2015.
 */
public class LTableAnnotation_JI_Freebase extends LTableAnnotation {

    private Map<String, Double> score_entityAndConcept= new HashMap<String, Double>();
    private Map<String, Double> score_entityAndRelation= new HashMap<String, Double>();
    private Map<String, Double> score_conceptAndRelation= new HashMap<String, Double>();
    public LTableAnnotation_JI_Freebase(int rows, int cols) {
        super(rows, cols);
    }

    public double getScore_entityAndConcept(String entityId, String conceptId){
        Double v = score_entityAndConcept.get(createKey(entityId, conceptId));
        if(v==null)
            v=0.0;
        return v;
    }
    public void setScore_entityAndConcept(String entityId, String conceptId, double score){
        score_entityAndConcept.put(createKey(entityId, conceptId), score);
    }

    public double getScore_conceptAndRelation(String conceptId, String relationId){
        Double v = score_conceptAndRelation.get(createKey(conceptId, relationId));
        if(v==null)
            v=0.0;
        return v;
    }



    public double getScore_entityAndRelation(String entityId, String relationId){
        Double v = score_entityAndRelation.get(createKey(entityId, relationId));
        if(v==null)
            v=0.0;
        return v;
    }

    private String createKey(String s1, String s2){
        return s1+"|"+s2;
    }
}
