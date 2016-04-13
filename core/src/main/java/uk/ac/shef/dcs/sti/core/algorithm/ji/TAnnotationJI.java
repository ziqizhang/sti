package uk.ac.shef.dcs.sti.core.algorithm.ji;

import uk.ac.shef.dcs.sti.core.model.TAnnotation;

import java.util.*;

/**
 * Created by zqz on 01/05/2015.
 */
public class TAnnotationJI extends TAnnotation {

    //==debug purpose
    private Set<String> usedKey_score_entityAndConcept = new HashSet<>();
    private Set<String> usedKey_score_entityPairAndRelation = new HashSet<>();
    private Set<String> usedKey_score_entityAndRelation = new HashSet<>();
    private Set<String> usedKey_score_conceptPairAndRelation_instaceEvidence = new HashSet<>();
    private Set<String> usedKey_score_conceptPairAndRelation_conceptEvidence = new HashSet<>();
    private Set<String> usedKey_scoreContributingRows_conceptPairAndRelation = new HashSet<>();
    //==debug purpose

    private Map<String, Double> score_entityAndConcept = new HashMap<>();
    private Map<String, Double> score_entityPairAndRelation = new HashMap<>();
    private Map<String, Double> score_entityAndRelation = new HashMap<>();
    private Map<String, Double> score_conceptPairAndRelation_instaceEvidence = new HashMap<>();
    private Map<String, Double> score_conceptPairAndRelation_conceptEvidence = new HashMap<>();
    private Map<String, Map<String, Double>> scoreContributingRows_conceptPairAndRelation = new HashMap<>();

    public TAnnotationJI(int rows, int cols) {
        super(rows, cols);
    }

    public double getScore_entityAndRelation(String entityId, String relationId) {
        usedKey_score_entityAndRelation.add(createKeyPair(entityId, relationId));
        Double v = score_entityAndRelation.get(createKeyPair(entityId, relationId));
        if (v == null)
            v = 0.0;
        return v;
    }

    public void setScore_entityAndRelation(String entityId, String relationId, double score) {
        score_entityAndRelation.put(createKeyPair(entityId, relationId), score);
    }

    public double getScoreEntityAndConceptSimilarity(String entityId, String conceptId) {
        usedKey_score_entityAndConcept.add(createKeyPair(entityId, conceptId));
        Double v = score_entityAndConcept.get(createKeyPair(entityId, conceptId));
        if (v == null)
            v = 0.0;
        return v;
    }

    public void setScoreEntityAndConceptSimilarity(String entityId, String conceptId, double score) {
        score_entityAndConcept.put(createKeyPair(entityId, conceptId), score);
    }

    private double getScore_conceptPairAndRelation_conceptEvidence(String sbjConceptId, String relationId, String objConceptId) {
        usedKey_score_conceptPairAndRelation_conceptEvidence.add(createKeyTriple(sbjConceptId, objConceptId, relationId));
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
        usedKey_score_conceptPairAndRelation_instaceEvidence.add(createKeyTriple(sbjConceptId, objConceptId, relationId));
        Double v = score_conceptPairAndRelation_instaceEvidence.get(createKeyTriple(sbjConceptId, objConceptId, relationId));
        if (v == null)
            v = 0.0;
        return v;
    }

    public double getScore_conceptPairAndRelation(String sbjConceptId, String relationId, String objConceptId, int norm) {
        double v = getScore_conceptPairAndRelation_conceptEvidence(sbjConceptId, relationId, objConceptId);

        Map<String, Double> cells = scoreContributingRows_conceptPairAndRelation.get(
                createKeyTriple(sbjConceptId, objConceptId, relationId));
        usedKey_scoreContributingRows_conceptPairAndRelation.add(createKeyTriple(sbjConceptId, objConceptId, relationId));

        if (cells != null)
            v = v + Math.sqrt(cells.size() / (double) norm);
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
            contributingRows = new HashMap<>();
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
            } else if (existingScore < score) { //previously this cell has contributed to a computeElementScores, but that is smaller
                //so we need to recalculate the instanceEvidence computeElementScores using the new computeElementScores
                contributingRows.put(String.valueOf(row), score);
                double diff = score - existingScore;
                score_conceptPairAndRelation_instaceEvidence.put(key,
                        diff + getScore_conceptPairAndRelation_instanceEvidence(sbjConceptId, relationId, objConceptId));
            }
        }
    }

    public double getScore_entityPairAndRelation(String sbjEntityId, String objEntityId, String relationId) {
        usedKey_score_entityPairAndRelation.add(createKeyTriple(sbjEntityId, objEntityId, relationId));
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

    public void debugAffinity(String tableId) {
        List<String> tmp = new ArrayList<>(score_entityAndConcept.keySet());
        tmp.removeAll(usedKey_score_entityAndConcept);
        if (tmp.size() > 0)
            System.err.println(tableId+"-score_entityAndConcept unused:" + tmp);

        tmp = new ArrayList<>(score_entityPairAndRelation.keySet());
        tmp.removeAll(usedKey_score_entityPairAndRelation);
        if (tmp.size() > 0)
            System.err.println(tableId+"-score_entityPairAndRelation unused:" + tmp);

        tmp = new ArrayList<>(score_entityAndRelation.keySet());
        tmp.removeAll(usedKey_score_entityAndRelation);
        if (tmp.size() > 0)
            System.err.println(tableId+"-score_entityAndRelation unused:" + tmp);

        /*tmp = new ArrayList<String>(score_conceptPairAndRelation_instaceEvidence.keySet());
        tmp.removeAll(usedKey_score_conceptPairAndRelation_instaceEvidence);
        if (tmp.size() > 0)
            System.err.println("score_conceptPairAndRelation_instaceEvidence unused:" + tmp);*/

        tmp = new ArrayList<>(score_conceptPairAndRelation_conceptEvidence.keySet());
        tmp.removeAll(usedKey_score_conceptPairAndRelation_conceptEvidence);
        if (tmp.size() > 0)
            System.err.println(tableId+"-score_conceptPairAndRelation_conceptEvidence unused:" + tmp);

        tmp = new ArrayList<>(scoreContributingRows_conceptPairAndRelation.keySet());
        tmp.removeAll(usedKey_scoreContributingRows_conceptPairAndRelation);
        if (tmp.size() > 0)
            System.err.println(tableId+"-scoreContributingRows_conceptPairAndRelation unused:" + tmp);
    }
}
