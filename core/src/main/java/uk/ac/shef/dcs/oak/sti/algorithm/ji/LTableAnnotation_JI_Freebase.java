package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zqz on 01/05/2015.
 */
public class LTableAnnotation_JI_Freebase extends LTableAnnotation {

    private Map<String, Double> score_entityAndConcept= new HashMap<String, Double>();
    private Map<String, Double> score_entityAndRelation= new HashMap<String, Double>();
    private Map<String, Double> score_conceptAndRelation_instaceEvidence = new HashMap<String, Double>();
    private Map<String, Double> score_conceptAndRelation_conceptEvidence=new HashMap<String, Double>();
    private Map<String, Map<String, Double>> scoreContributingCells_conceptsAndRelation = new HashMap<String, Map<String, Double>>();
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

    public double getScore_conceptAndRelation_conceptEvidence(String conceptId, String relationId){
        Double v = score_conceptAndRelation_conceptEvidence.get(createKey(conceptId, relationId));
        if(v==null)
            v=0.0;
        return v;
    }

    public void setScore_conceptAndRelation_conceptEvidence(String conceptId, String relationId, double score){
        score_conceptAndRelation_conceptEvidence.put(createKey(conceptId, relationId), score);
    }

    private double getScore_conceptAndRelation_instanceEvidence(String conceptId, String relationId){
        Double v = score_conceptAndRelation_instaceEvidence.get(createKey(conceptId, relationId));
        if(v==null)
            v=0.0;
        return v;
    }

    public double getScore_conceptAndRelation(String conceptId, String relationId){
        double v = getScore_conceptAndRelation_conceptEvidence(conceptId, relationId);

        Map<String, Double> cells=scoreContributingCells_conceptsAndRelation.get(createKey(conceptId, relationId));
        if(cells!=null)
            v = v+ cells.size();
        return v;
    }

    /**
     *
     * @param row the row of cell where an entity has voted for this concept and relation
     * @param column the column of cell where an entity has voted for this concept and relation
     * @param conceptId
     * @param relationId
     * @param score
     */
    public void setScore_conceptAndRelation_instanceEvidence(int row, int column, String conceptId,
                                                             String relationId, double score){
        String cellPosition = row+","+column;
        String key=createKey(conceptId, relationId);
        Map<String, Double> contributingCells = scoreContributingCells_conceptsAndRelation.get(
                key
        );
        if(contributingCells==null) {
            contributingCells = new HashMap<String, Double>();
            contributingCells.put(cellPosition, score);
            score_conceptAndRelation_instaceEvidence.put(key, score);
            scoreContributingCells_conceptsAndRelation.put(key, contributingCells);
        }
        else{
            Double existingScore = contributingCells.get(cellPosition);
            if(existingScore==null){
                contributingCells.put(cellPosition,score);
                score_conceptAndRelation_instaceEvidence.put(key,
                        score+getScore_conceptAndRelation_instanceEvidence(conceptId, relationId));
                scoreContributingCells_conceptsAndRelation.put(key, contributingCells);
            }
            else if(existingScore<score) { //previously this cell has contributed to a score, but that is smaller
                //so we need to recalculate the instanceEvidence score using the new score
                contributingCells.put(cellPosition,score);
                double diff = score-existingScore;
                score_conceptAndRelation_instaceEvidence.put(key,
                        diff+getScore_conceptAndRelation_instanceEvidence(conceptId, relationId));
            }
        }
    }

    public double getScore_entityAndRelation(String entityId, String relationId){
        Double v = score_entityAndRelation.get(createKey(entityId, relationId));
        if(v==null)
            v=0.0;
        return v;
    }

    public void setScore_entityAndRelation(String entityId, String relationURL, double score){
        score_entityAndRelation.put(createKey(entityId, relationURL),score);
    }

    private String createKey(String s1, String s2){
        return s1+"|"+s2;
    }
}
