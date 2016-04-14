package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import org.simmetrics.StringMetric;
import uk.ac.shef.dcs.sti.STIEnum;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeader;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 11/03/14
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */
public class BaselineSimilarityClazzScorer implements ClazzScorer {
    private static final String SCORE_CTX_IN_HEADER = "score_ctx_header";
    private static final String SUM_CELL_VOTE = "sum_vote";
    private static final String SCORE_CELL_VOTE = "score_vote";
    private Lemmatizer lemmatizer;
    private List<String> stopWords;
    private StringMetric stringSimilarityMetric;

    public BaselineSimilarityClazzScorer(String nlpResources, List<String> stopWords,
                                         StringMetric stringMetric) throws IOException {
        this.lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();
        this.stopWords = stopWords;
        this.stringSimilarityMetric = stringMetric;
    }

    @Override
    public Map<String, Double> computeFinal(TColumnHeaderAnnotation ha, int tableRowsTotal) {
        Map<String, Double> scoreElements = ha.getScoreElements();
        Double sum_entity_vote = scoreElements.get(SUM_CELL_VOTE);
        if(sum_entity_vote==null) sum_entity_vote=0.0;
        double score_entity_vote = sum_entity_vote / (double) tableRowsTotal;
        scoreElements.put(SCORE_CELL_VOTE, score_entity_vote);

        double finalScore = score_entity_vote;
        Double namematch = scoreElements.get(SCORE_CTX_IN_HEADER);
        if (namematch != null) {
            finalScore = finalScore + namematch;
        }

        scoreElements.put(TColumnHeaderAnnotation.SCORE_FINAL, finalScore);
        ha.setFinalScore(finalScore);
        return scoreElements;
    }


    @Override
    public List<TColumnHeaderAnnotation> computeElementScores(
            List<Pair<Entity, Map<String, Double>>> input,
            Collection<TColumnHeaderAnnotation> headerAnnotationCandidates,
            Table table, List<Integer> rows, int column) throws STIException {
        List<TColumnHeaderAnnotation> candidates = new ArrayList<>();
        for (int row : rows)
            candidates = computeCEScore(input, headerAnnotationCandidates, table, row, column);
        candidates = computeCCScore(candidates, table, column);

        return candidates;
    }

    @Override
    public List<TColumnHeaderAnnotation> computeCEScore(
            List<Pair<Entity, Map<String, Double>>> entities,
            Collection<TColumnHeaderAnnotation> existingHeaderAnnotations,
            Table table, int row, int column) throws STIException {
        final Collection<TColumnHeaderAnnotation> candidateHeaderAnnotations =
                existingHeaderAnnotations;
        //for this row
        Entity winningEntity = null;
        double winningScore = 0.0;
        for (Pair<Entity, Map<String, Double>> es : entities) { //each candidate entity in this cell
            Entity entity = es.getKey();
            //each assigned type receives a computeElementScores of 1, and the bonus computeElementScores due to disambiguation result
            double entityScore = es.getValue().get(TCellAnnotation.SCORE_FINAL);
            if (entityScore > winningScore) {
                winningScore = entityScore;
                winningEntity = entity;
            }
        }
        if (entities.size() == 0 || winningEntity == null) {
            return new ArrayList<>(candidateHeaderAnnotations);
        }

        Set<String> votedTypes = new HashSet<>();    //each type will receive a max of 1 vote from each cell. If multiple candidates have the same highest computeElementScores and casts same votes, they are counted oly once
        for (Pair<Entity, Map<String, Double>> es : entities) {
            Entity currentEntity = es.getKey();
            double entityScore = es.getValue().get(TCellAnnotation.SCORE_FINAL);
            if (entityScore != winningScore)
                continue;

            List<Clazz> types = currentEntity.getTypes();

            //consolidate scores from this cell
            for (Clazz type : types) {
                if (votedTypes.contains(type.getId()))
                    continue;

                votedTypes.add(type.getId());
                String headerText = table.getColumnHeader(column).getHeaderText();

                TColumnHeaderAnnotation hAnnotation = null;
                for (TColumnHeaderAnnotation key : candidateHeaderAnnotations) {
                    if (key.getHeaderText().equals(headerText) && key.getAnnotation().getId().equals(type.getId()
                    )) {
                        hAnnotation = key;
                        break;
                    }
                }
                if (hAnnotation == null) {
                    hAnnotation = new TColumnHeaderAnnotation(headerText, type, 0.0);
                    candidateHeaderAnnotations.add(hAnnotation);
                }

                Map<String, Double> scoreElements = hAnnotation.getScoreElements();
                if (scoreElements == null || scoreElements.size() == 0) {
                    scoreElements = new HashMap<>();
                    scoreElements.put(SUM_CELL_VOTE, 0.0);
                }
                Double sumCellVote = scoreElements.get(SUM_CELL_VOTE);
                if(sumCellVote==null) sumCellVote=0.0;
                scoreElements.put(SUM_CELL_VOTE,
                        sumCellVote + 1.0);
                hAnnotation.setScoreElements(scoreElements);

            }
        }

        return new ArrayList<>(candidateHeaderAnnotations);
    }

    @Override
    public List<TColumnHeaderAnnotation> computeCCScore(Collection<TColumnHeaderAnnotation> candidates, Table table, int column) throws STIException {
        for (TColumnHeaderAnnotation ha : candidates) {
            Double score_ctx_header_text = ha.getScoreElements().get(SCORE_CTX_IN_HEADER);

            if (score_ctx_header_text == null) {
                TColumnHeader header = table.getColumnHeader(column);
                if (header != null &&
                        header.getHeaderText() != null &&
                        !header.getHeaderText().equals(STIEnum.TABLE_HEADER_UNKNOWN.getValue())) {
                    //double computeElementScores = CollectionUtils.diceCoefficientOptimized(header.getHeaderText(), ha.getAnnotation_label());
                    double score = stringSimilarityMetric.compare(
                            StringUtils.toAlphaNumericWhitechar(header.getHeaderText()),
                            StringUtils.toAlphaNumericWhitechar(ha.getAnnotation().getLabel()));
                    ha.getScoreElements().put(SCORE_CTX_IN_HEADER, score);
                }
            }
        }


        return new ArrayList<>(candidates);
    }

    @Override
    public double computeDC(TColumnHeaderAnnotation ha, List<String> domain_representation) throws STIException {
        throw new STIException("Not supported");
    }
}
