package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.sti.STIEnum;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPClazzScorer;
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
import uk.ac.shef.wit.simmetrics.similaritymetrics.*;

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
    private Lemmatizer lemmatizer;
    private List<String> stopWords;
    private AbstractStringMetric stringSimilarityMetric;

    public BaselineSimilarityClazzScorer(String nlpResources, List<String> stopWords,
                                         AbstractStringMetric stringMetric) throws IOException {
        this.lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();
        this.stopWords = stopWords;
        this.stringSimilarityMetric = stringMetric;
    }


    public Set<TColumnHeaderAnnotation> score_entity_best_candidate_vote(List<Pair<Entity, Map<String, Double>>> input,
                                                                  Set<TColumnHeaderAnnotation> headerAnnotations_prev, Table table,
                                                                  int row, int column) {
        final Set<TColumnHeaderAnnotation> candidate_header_annotations =
                headerAnnotations_prev;
        //for this row
        Entity entity_with_highest_disamb_score = null;
        double best_score = 0.0;
        for (Pair<Entity, Map<String, Double>> es : input) { //each candidate entity in this cell
            Entity entity = es.getKey();
            //each assigned type receives a computeElementScores of 1, and the bonus computeElementScores due to disambiguation result
            double entity_disamb_score = es.getValue().get(TCellAnnotation.SCORE_FINAL);
            if (entity_disamb_score > best_score) {
                best_score = entity_disamb_score;
                entity_with_highest_disamb_score = entity;
            }
        }
        if (input.size() == 0 || entity_with_highest_disamb_score == null) {
            //this entity has a computeElementScores of 0.0, it should not contribute to the header typing, but we may still keep it as candidate for this cell
            System.out.print("x(" + row + "," + column + ")");
            return candidate_header_annotations;
        }


        if (input.size() == 0) {
            //this entity has a computeElementScores of 0.0, it should not contribute to the header typing, but we may still keep it as candidate for this cell
            System.out.print("x(" + row + "," + column + ")");
            return candidate_header_annotations;
        }

        Set<String> types_already_received_votes_by_cell = new HashSet<String>();    //each type will receive a max of 1 vote from each cell. If multiple candidates have the same highest computeElementScores and casts same votes, they are counted oly once
        for (Pair<Entity, Map<String, Double>> es : input) {
            Entity current_candidate = es.getKey();
            double entity_disamb_score = es.getValue().get(TCellAnnotation.SCORE_FINAL);
            if (entity_disamb_score != best_score)
                continue;

            List<Clazz> type_voted_by_this_cell = current_candidate.getTypes();

            //consolidate scores from this cell
            for (Clazz type : type_voted_by_this_cell) {
                if (types_already_received_votes_by_cell.contains(type.getId()))
                    continue;

                types_already_received_votes_by_cell.add(type.getLabel());
                String headerText = table.getColumnHeader(column).getHeaderText();

                TColumnHeaderAnnotation hAnnotation = null;
                for (TColumnHeaderAnnotation key : candidate_header_annotations) {
                    if (key.getHeaderText().equals(headerText) && key.getAnnotation().getId().equals(type.getId()
                    )) {
                        hAnnotation = key;
                        break;
                    }
                }
                if (hAnnotation == null) {
                    hAnnotation = new TColumnHeaderAnnotation(headerText, type, 0.0);
                }

                Map<String, Double> tmp_score_elements = hAnnotation.getScoreElements();
                if (tmp_score_elements == null || tmp_score_elements.size() == 0) {
                    tmp_score_elements = new HashMap<>();
                    tmp_score_elements.put(TMPClazzScorer.SUM_CELL_VOTE, 0.0);
                }
                Double sumCellVote = tmp_score_elements.get(TMPClazzScorer.SUM_CELL_VOTE);
                if(sumCellVote==null) sumCellVote=0.0;
                tmp_score_elements.put(TMPClazzScorer.SUM_CELL_VOTE,
                        sumCellVote + 1.0);
                hAnnotation.setScoreElements(tmp_score_elements);

                candidate_header_annotations.add(hAnnotation);
            }
        }

        return candidate_header_annotations;
    }


    public Set<TColumnHeaderAnnotation> score_context(Set<TColumnHeaderAnnotation> candidates, Table table, int column, boolean overwrite) {
        for (TColumnHeaderAnnotation ha : candidates) {
            Double score_ctx_header_text = ha.getScoreElements().get(TMPClazzScorer.SCORE_CTX_IN_HEADER);

            if (score_ctx_header_text == null) {
                TColumnHeader header = table.getColumnHeader(column);
                if (header != null &&
                        header.getHeaderText() != null &&
                        !header.getHeaderText().equals(STIEnum.TABLE_HEADER_UNKNOWN.getValue())) {
                    //double computeElementScores = CollectionUtils.diceCoefficientOptimized(header.getHeaderText(), ha.getAnnotation_label());
                    double score = stringSimilarityMetric.getSimilarity(
                            StringUtils.toAlphaNumericWhitechar(header.getHeaderText()),
                            StringUtils.toAlphaNumericWhitechar(ha.getAnnotation().getLabel()));
                    ha.getScoreElements().put(TMPClazzScorer.SCORE_CTX_IN_HEADER, score);
                }
            }
        }


        return candidates;
    }


    @Override
    public Map<String, Double> computeFinal(TColumnHeaderAnnotation ha, int tableRowsTotal) {
        Map<String, Double> scoreElements = ha.getScoreElements();
        Double sum_entity_vote = scoreElements.get(TMPClazzScorer.SUM_CELL_VOTE);
        if(sum_entity_vote==null) sum_entity_vote=0.0;
        double score_entity_vote = sum_entity_vote / (double) tableRowsTotal;
        scoreElements.put(TMPClazzScorer.SCORE_CELL_VOTE, score_entity_vote);

        double finalScore = score_entity_vote;
        Double namematch = scoreElements.get(TMPClazzScorer.SCORE_CTX_IN_HEADER);
        if (namematch != null) {
            finalScore = finalScore + namematch;
        }

        scoreElements.put(TColumnHeaderAnnotation.FINAL, finalScore);
        ha.setFinalScore(finalScore);
        return scoreElements;
    }


    @Override
    public List<TColumnHeaderAnnotation> computeElementScores(
            List<Pair<Entity, Map<String, Double>>> input,
            Collection<TColumnHeaderAnnotation> headerAnnotationCandidates,
            Table table, List<Integer> rows, int column) throws STIException {
        return null;
    }

    @Override
    public List<TColumnHeaderAnnotation> computeCEScore(List<Pair<Entity, Map<String, Double>>> entities, Collection<TColumnHeaderAnnotation> existingHeaderAnnotations, Table table, int row, int column) throws STIException {
        return null;
    }

    @Override
    public List<TColumnHeaderAnnotation> computeCCScore(Collection<TColumnHeaderAnnotation> candidates, Table table, int column) throws STIException {
        return null;
    }

    @Override
    public double computeDC(TColumnHeaderAnnotation ha, List<String> domain_representation) throws STIException {
        throw new STIException("Not supported");
    }
}
