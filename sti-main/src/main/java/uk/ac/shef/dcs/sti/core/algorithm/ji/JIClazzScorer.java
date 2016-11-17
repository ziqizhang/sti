package uk.ac.shef.dcs.sti.core.algorithm.ji;

import javafx.util.Pair;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;
import uk.ac.shef.dcs.util.StringUtils;

import java.util.*;

/**
 * Created by zqz on 01/05/2015.
 */
public class JIClazzScorer implements ClazzScorer {
    public static final String SCORE_FINAL = "score_factor_graph-clazz";
    private static final String SCORE_LEV = "stringsim_lev";
    private static final String SCORE_JACCARD = "stringsim_jaccard";
    private static final String SCORE_COSINE = "stringsim_cosine";

    private StringMetric cosine;
    private StringMetric jaccard;
    private StringMetric lev;

    public JIClazzScorer() {
        cosine = StringMetrics.cosineSimilarity();
        jaccard = StringMetrics.jaccard();
        lev = StringMetrics.levenshtein();
    }


    private double calculateStringSimilarity(String text, String candidate, StringMetric lev) {
        double baseScore = lev.compare(text, candidate);
        return baseScore;
    }


    @Override
    public Map<String, Double> computeFinal(TColumnHeaderAnnotation ha, int tableRowsTotal) {
        Map<String, Double> scoreMap = ha.getScoreElements();
        double finalScore = scoreMap.get(SCORE_FINAL);
        scoreMap.put(TCellAnnotation.SCORE_FINAL, finalScore);
        ha.setFinalScore(finalScore);
        return scoreMap;
    }

    @Override
    public List<TColumnHeaderAnnotation> computeElementScores(List<Pair<Entity, Map<String, Double>>> input,
                                                              Collection<TColumnHeaderAnnotation> headerAnnotationCandidates,
                                                              Table table,
                                                              List<Integer> rows,
                                                              int column) throws STIException {

        for (Pair<Entity, Map<String, Double>> entity : input) {
            Entity e = entity.getKey();
            Map<String, Double> scoreElements = entity.getValue();
            if (scoreElements.get(
                    JIAdaptedEntityScorer.SCORE_FINAL) == 0.0) {
                continue;
            }

            for (Clazz c : e.getTypes()) {
                TColumnHeaderAnnotation hAnnotation = null;
                for (TColumnHeaderAnnotation headerAnnotation : headerAnnotationCandidates) {
                    if (headerAnnotation.getAnnotation().equals(c)) {
                        hAnnotation = headerAnnotation;
                        break;
                    }
                }

                if (hAnnotation == null) {
                    String headerText = table.getColumnHeader(column).getHeaderText();
                    hAnnotation = new TColumnHeaderAnnotation(
                            headerText, c, 0.0
                    );

                    String normText = StringUtils.toAlphaNumericWhitechar(headerText);
                    String normAnnotationText = StringUtils.toAlphaNumericWhitechar(hAnnotation.getAnnotation().getLabel());
                    double levScore = calculateStringSimilarity(normText, normAnnotationText, lev);
                    double jaccardScore = calculateStringSimilarity(normText, normAnnotationText, jaccard);
                    double cosineScore = calculateStringSimilarity(normText, normAnnotationText, cosine);

                    Map<String, Double> clazz_score_elements = new HashMap<>();
                    clazz_score_elements.put(SCORE_FINAL, levScore + jaccardScore + cosineScore);
                    clazz_score_elements.put(SCORE_COSINE, cosineScore);
                    clazz_score_elements.put(SCORE_JACCARD, jaccardScore);
                    clazz_score_elements.put(SCORE_LEV, levScore);
                    hAnnotation.setScoreElements(clazz_score_elements);

                    headerAnnotationCandidates.add(hAnnotation);
                }

            }
        }

        return new ArrayList<>(headerAnnotationCandidates);
    }

    @Override
    public List<TColumnHeaderAnnotation> computeCEScore(List<Pair<Entity, Map<String, Double>>> entities, Collection<TColumnHeaderAnnotation> existingHeaderAnnotations, Table table, int row, int column) throws STIException {
        throw new STIException("Not supported");
    }

    @Override
    public List<TColumnHeaderAnnotation> computeCCScore(Collection<TColumnHeaderAnnotation> candidates, Table table, int column) throws STIException {
        throw new STIException("Not supported");
    }

    @Override
    public double computeDC(TColumnHeaderAnnotation ha, List<String> domain_representation) throws STIException {
        throw new STIException("Not supported");
    }
}

