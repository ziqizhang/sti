package uk.ac.shef.dcs.sti.core.algorithm.ji;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.util.StringUtils;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class JIAdaptedEntityScorer implements EntityScorer {
    public static final String SCORE_FINAL = "score_factor_graph-entity";
    public static final String SCORE_LEV = "stringsim_lev";
    public static final String SCORE_JACCARD = "stringsim_jaccard";
    public static final String SCORE_COSINE = "stringsim_cosine";

    private StringMetric cosine;
    private StringMetric jaccard;
    private StringMetric lev;

    public JIAdaptedEntityScorer() {
        cosine = StringMetrics.cosineSimilarity();
        jaccard = StringMetrics.jaccard();
        lev = StringMetrics.levenshtein();
    }

    @Override
    public Map<String, Double> computeElementScores(Entity candidate,
                                                    List<Entity> all_candidates,
                                                    int sourceColumnIndex,
                                                    int sourceRowIndex,
                                                    List<Integer> otherRows,
                                                    Table table,
                                                    Entity... referenceEntities) {

        TCell cell = table.getContentCell(sourceRowIndex, sourceColumnIndex);

        String normText = StringUtils.toAlphaNumericWhitechar(cell.getText());
        String normCandidateName = StringUtils.toAlphaNumericWhitechar(candidate.getLabel());
        double levScore = calculateStringSimilarity(normText, normCandidateName, lev);
        double jaccardScore = calculateStringSimilarity(normText, normCandidateName, jaccard);
        double cosineScore = calculateStringSimilarity(normText, normCandidateName, cosine);

        Map<String, Double> score_elements = new HashMap<>();
        score_elements.put(SCORE_FINAL, levScore + jaccardScore + cosineScore);
        score_elements.put(SCORE_COSINE, cosineScore);
        score_elements.put(SCORE_JACCARD, jaccardScore);
        score_elements.put(SCORE_LEV, levScore);
        return score_elements;
    }

    private double calculateStringSimilarity(String text, String candidate, StringMetric lev) {
        double baseScore = lev.compare(text, candidate);
        return baseScore;
    }

    @Override
    public double computeFinal(Map<String, Double> scoreMap, String cellTextOriginal) {
        scoreMap.put(TCellAnnotation.SCORE_FINAL, scoreMap.get(SCORE_FINAL));
        return scoreMap.get(SCORE_FINAL);
    }
}
