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
    private Map<String, Double> score_conceptAndRelation= new HashMap<String, Double>();
    private Map<String, List<String>> scoreContributingCells_conceptsAndRelation = new HashMap<String, List<String>>();
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

    /**
     *
     * @param row the row of cell where an entity has voted for this concept and relation
     * @param column the column of cell where an entity has voted for this concept and relation
     * @param conceptId
     * @param relationId
     * @param score
     */
    public void setScore_conceptAndRelation(int row, int column, String conceptId,
                                            String relationId, double score){
        String cellPosition = row+","+column;
        List<String> contributingCells = scoreContributingCells_conceptsAndRelation.get(
                cellPosition
        );
        if(contributingCells==null)
            contributingCells=new ArrayList<String>();
        if(contributingCells.contains(cellPosition)){
            double existingScore = getScore_conceptAndRelation(conceptId, relationId);
            if(existingScore<score)
                score_conceptAndRelation.put(createKey(conceptId, relationId), score);
        }
        else{
            contributingCells.add(cellPosition);
            score_conceptAndRelation.put(createKey(conceptId, relationId), score);
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
