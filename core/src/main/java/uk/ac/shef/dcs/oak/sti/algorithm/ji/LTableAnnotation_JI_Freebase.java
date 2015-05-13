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

    private Map<String, Double> score_entityAndConcept = new HashMap<String, Double>();
    private Map<String, Double> score_entityPairAndRelation = new HashMap<String, Double>();
    private Map<String, Double> score_entityAndRelation = new HashMap<String, Double>();
    private Map<String, Double> score_conceptPairAndRelation_instaceEvidence = new HashMap<String, Double>();
    private Map<String, Double> score_conceptPairAndRelation_conceptEvidence = new HashMap<String, Double>();
    private Map<String, Map<String, Double>> scoreContributingRows_conceptPairAndRelation = new HashMap<String, Map<String, Double>>();

    public LTableAnnotation_JI_Freebase(int rows, int cols) {
        super(rows, cols);
    }

    public double getScore_entityAndRelation(String entityId, String relationId){
        Double v = score_entityAndRelation.get(createKeyPair(entityId, relationId));
        if (v == null)
            v = 0.0;
        return v;
    }

    public void setScore_entityAndRelation(String entityId, String relationId, double score){
        score_entityAndRelation.put(createKeyPair(entityId, relationId), score);
    }

    public double getScore_entityAndConcept(String entityId, String conceptId) {
        Double v = score_entityAndConcept.get(createKeyPair(entityId, conceptId));
        if (v == null)
            v = 0.0;
        return v;
    }

    public void setScore_entityAndConcept(String entityId, String conceptId, double score) {
        score_entityAndConcept.put(createKeyPair(entityId, conceptId), score);
    }

    private double getScore_conceptPairAndRelation_conceptEvidence(String sbjConceptId, String relationId, String objConceptId) {
        Double v = score_conceptPairAndRelation_conceptEvidence.get(createKeyTriple(sbjConceptId, objConceptId, relationId));
        if (v == null)
            v = 0.0;
        return v;
    }

    public void setScore_conceptPairAndRelation_conceptEvidence(String sbjConceptId, String relationId, String objConceptId,
                                                                double score) {
        score_conceptPairAndRelation_conceptEvidence.put(createKeyTriple(sbjConceptId, objConceptId, relationId), score);
    }

    private double getScore_conceptPairAndRelation_instanceEvidence(String sbjConceptId, String relationId, String objConceptId) {
        Double v = score_conceptPairAndRelation_instaceEvidence.get(createKeyTriple(sbjConceptId, objConceptId, relationId));
        if (v == null)
            v = 0.0;
        return v;
    }

    public double getScore_conceptPairAndRelation(String sbjConceptId, String relationId, String objConceptId, int norm) {
        double v = getScore_conceptPairAndRelation_conceptEvidence(sbjConceptId, relationId, objConceptId);

        Map<String, Double> cells = scoreContributingRows_conceptPairAndRelation.get(
                createKeyTriple(sbjConceptId, objConceptId, relationId));
        if (cells != null)
            v = v + Math.sqrt(cells.size()/(double)norm);
        return v;
    }

    /**
     * @param row          the row of cell where an entity has voted for this concept and relation
     * @param sbjConceptId
     * @param relationId
     * @param score
     */
    public void setScore_conceptPairAndRelation_instanceEvidence(int row,
                                                                 String sbjConceptId,
                                                                 String relationId,
                                                                 String objConceptId, double score) {
        String key = createKeyTriple(sbjConceptId, objConceptId, relationId);
        Map<String, Double> contributingRows = scoreContributingRows_conceptPairAndRelation.get(
                key
        );
        if (contributingRows == null) {
            contributingRows = new HashMap<String, Double>();
            contributingRows.put(String.valueOf(row), score);
            score_conceptPairAndRelation_instaceEvidence.put(key, score);
            scoreContributingRows_conceptPairAndRelation.put(key, contributingRows);
        } else {
            Double existingScore = contributingRows.get(String.valueOf(row));
            if (existingScore == null) {
                contributingRows.put(String.valueOf(row), score);
                score_conceptPairAndRelation_instaceEvidence.put(key,
                        score + getScore_conceptPairAndRelation_instanceEvidence(sbjConceptId, relationId, objConceptId));
                scoreContributingRows_conceptPairAndRelation.put(key, contributingRows);
            } else if (existingScore < score) { //previously this cell has contributed to a score, but that is smaller
                //so we need to recalculate the instanceEvidence score using the new score
                contributingRows.put(String.valueOf(row), score);
                double diff = score - existingScore;
                score_conceptPairAndRelation_instaceEvidence.put(key,
                        diff + getScore_conceptPairAndRelation_instanceEvidence(sbjConceptId, relationId, objConceptId));
            }
        }
    }

    public double getScore_entityPairAndRelation(String sbjEntityId, String objEntityId, String relationId) {
        Double v = score_entityPairAndRelation.get(createKeyTriple(sbjEntityId, objEntityId, relationId));
        if (v == null)
            v = 0.0;
        return v;
    }

    public void setScore_entityPairAndRelation(String sbjEntityId, String objEntityId, String relationId, double score) {
        score_entityPairAndRelation.put(createKeyTriple(sbjEntityId, objEntityId, relationId), score);
    }

    private String createKeyTriple(String sbjId, String objId, String relation) {
        return sbjId + ">" + objId + "|" + relation;
    }

    private String createKeyPair(String s1, String s2) {
        return s1 + "|" + s2;
    }
}
