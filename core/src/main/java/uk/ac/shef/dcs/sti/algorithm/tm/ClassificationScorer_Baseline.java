package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.sti.PlaceHolder;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.sti.rep.HeaderAnnotation;
import uk.ac.shef.dcs.sti.rep.LTable;
import uk.ac.shef.dcs.sti.rep.LTableColumnHeader;
import uk.ac.shef.dcs.util.StringUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 27/05/14
 * Time: 12:45
 * To change this template use File | Settings | File Templates.
 */
public class ClassificationScorer_Baseline implements ClassificationScorer {

    private Lemmatizer lemmatizer;
    private List<String> stopWords;
    //private Levenshtein stringSimilarityMetric;
    //private Jaro stringSimilarityMetric;
    //private Levenshtein stringSimilarityMetric;
    private AbstractStringMetric stringSimilarityMetric;

    public ClassificationScorer_Baseline(String nlpResources, List<String> stopWords) throws IOException {
        this.lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();
        this.stopWords = stopWords;
        this.stringSimilarityMetric = new Levenshtein();
        //this.stringSimilarityMetric=new CosineSimilarity();
    }

    public Set<HeaderAnnotation> score(List<Pair<Entity, Map<String, Double>>> input,
                                       Set<HeaderAnnotation> headerAnnotations_prev,
                                       LTable table,
                                       List<Integer> rows, int column) {
        Set<HeaderAnnotation> candidates = new HashSet<HeaderAnnotation>();
        if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 0) {
            for (int row : rows)
                candidates = score_entity_best_candidate_vote(input, headerAnnotations_prev, table, row, column);
        } else {
            for (int row : rows)
                candidates = score_entity_all_candidate_vote(input, headerAnnotations_prev, table, row, column);
        }
        candidates = score_context(candidates, table, column, false);

        return candidates;
    }

    public Set<HeaderAnnotation> score_entity_best_candidate_vote(List<Pair<Entity, Map<String, Double>>> input,
                                                                  Set<HeaderAnnotation> headerAnnotations_prev, LTable table,
                                                                  int row, int column) {
        final Set<HeaderAnnotation> candidate_header_annotations =
                headerAnnotations_prev;
        //for this row
        Entity entity_with_highest_disamb_score = null;
        double best_score = 0.0;
        for (Pair<Entity, Map<String, Double>> es : input) { //each candidate entity in this cell
            Entity entity = es.getKey();
            //each assigned type receives a score of 1, and the bonus score due to disambiguation result
            double entity_disamb_score = es.getValue().get(CellAnnotation.SCORE_FINAL);
            if (entity_disamb_score > best_score) {
                best_score = entity_disamb_score;
                entity_with_highest_disamb_score = entity;
            }
        }
        if (input.size() == 0 || entity_with_highest_disamb_score == null) {
            //this entity has a score of 0.0, it should not contribute to the header typing, but we may still keep it as candidate for this cell
            System.out.print("x(" + row + "," + column + ")");
            return candidate_header_annotations;
        }


        if (input.size() == 0) {
            //this entity has a score of 0.0, it should not contribute to the header typing, but we may still keep it as candidate for this cell
            System.out.print("x(" + row + "," + column + ")");
            return candidate_header_annotations;
        }

        Set<String> types_already_received_votes_by_cell = new HashSet<String>();    //each type will receive a max of 1 vote from each cell. If multiple candidates have the same highest score and casts same votes, they are counted oly once
        for (Pair<Entity, Map<String, Double>> es : input) {
            Entity current_candidate = es.getKey();
            double entity_disamb_score = es.getValue().get(CellAnnotation.SCORE_FINAL);
            if (entity_disamb_score != best_score)
                continue;

            List<Clazz> type_voted_by_this_cell = current_candidate.getTypes();

            //consolidate scores from this cell
            for (Clazz type : type_voted_by_this_cell) {
                if (TableMinerConstants.BEST_CANDIDATE_CONTRIBUTE_COUNT_ONLY_ONCE
                        && types_already_received_votes_by_cell.contains(type.getId()))
                    continue;

                types_already_received_votes_by_cell.add(type.getId());
                String headerText = table.getColumnHeader(column).getHeaderText();

                HeaderAnnotation hAnnotation = null;
                for (HeaderAnnotation key : candidate_header_annotations) {
                    if (key.getTerm().equals(headerText) && key.getAnnotation_url().equals(type.getId()
                    )) {
                        hAnnotation = key;
                        break;
                    }
                }
                if (hAnnotation == null) {
                    hAnnotation = new HeaderAnnotation(headerText, type.getId(), type.getLabel(), 0.0);
                }

                Map<String, Double> tmp_score_elements = hAnnotation.getScoreElements();
                if (tmp_score_elements == null || tmp_score_elements.size() == 0) {
                    tmp_score_elements = new HashMap<String, Double>();
                    tmp_score_elements.put(HeaderAnnotation.SUM_ENTITY_VOTE, 0.0);
                }
                tmp_score_elements.put(HeaderAnnotation.SUM_ENTITY_VOTE,
                        tmp_score_elements.get(HeaderAnnotation.SUM_ENTITY_VOTE) + 1.0);
                hAnnotation.setScoreElements(tmp_score_elements);

                candidate_header_annotations.add(hAnnotation);
            }
        }

        return candidate_header_annotations;
    }


    public Set<HeaderAnnotation> score_entity_all_candidate_vote(List<Pair<Entity, Map<String, Double>>> input,
                                                                 Set<HeaderAnnotation> headerAnnotations_prev, LTable table,
                                                                 int row, int column) {
        final Set<HeaderAnnotation> candidate_header_annotations =
                headerAnnotations_prev;

        if (input.size() == 0) {
            //this entity has a score of 0.0, it should not contribute to the header typing, but we may still keep it as candidate for this cell
            System.out.print("x(" + row + "," + column + ")");
            return candidate_header_annotations;
        }

        for (Pair<Entity, Map<String, Double>> es : input) {
            Entity current_candidate = es.getKey();

            List<Clazz> type_voted_by_this_cell = current_candidate.getTypes();

            //consolidate scores from this cell
            for (Clazz type : type_voted_by_this_cell) {
                String headerText = table.getColumnHeader(column).getHeaderText();

                HeaderAnnotation hAnnotation = null;
                for (HeaderAnnotation key : candidate_header_annotations) {
                    if (key.getTerm().equals(headerText) && key.getAnnotation_url().equals(type.getId()
                    )) {
                        hAnnotation = key;
                        break;
                    }
                }
                if (hAnnotation == null) {
                    hAnnotation = new HeaderAnnotation(headerText, type.getId(), type.getLabel(), 0.0);
                }

                Map<String, Double> tmp_score_elements = hAnnotation.getScoreElements();
                if (tmp_score_elements == null || tmp_score_elements.size() == 0) {
                    tmp_score_elements = new HashMap<String, Double>();
                    tmp_score_elements.put(HeaderAnnotation.SUM_ENTITY_VOTE, 0.0);
                }
                tmp_score_elements.put(HeaderAnnotation.SUM_ENTITY_VOTE,
                        tmp_score_elements.get(HeaderAnnotation.SUM_ENTITY_VOTE) + 1.0);
                hAnnotation.setScoreElements(tmp_score_elements);

                candidate_header_annotations.add(hAnnotation);
            }
        }

        return candidate_header_annotations;
    }

    public Set<HeaderAnnotation> score_context(Set<HeaderAnnotation> candidates, LTable table, int column, boolean overwrite) {
        for (HeaderAnnotation ha : candidates) {
            Double score_ctx_header_text = ha.getScoreElements().get(HeaderAnnotation.SCORE_CTX_NAME_MATCH);

            if (score_ctx_header_text == null) {
                LTableColumnHeader header = table.getColumnHeader(column);
                if (header != null &&
                        header.getHeaderText() != null &&
                        !header.getHeaderText().equals(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue())) {
                    //double score = CollectionUtils.diceCoefficientOptimized(header.getHeaderText(), ha.getAnnotation_label());
                    double score = stringSimilarityMetric.getSimilarity(
                            StringUtils.toAlphaNumericWhitechar(header.getHeaderText()),
                            StringUtils.toAlphaNumericWhitechar(ha.getAnnotation_label()));
                    ha.getScoreElements().put(HeaderAnnotation.SCORE_CTX_NAME_MATCH, score);
                }
            }
        }


        return candidates;
    }

    @Override
    public double score_domain_consensus(HeaderAnnotation ha, List<String> domain_representation) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public Map<String, Double> compute_final_score(HeaderAnnotation ha, int tableRowsTotal) {
        Map<String, Double> scoreElements = ha.getScoreElements();
        double sum_entity_vote = scoreElements.get(HeaderAnnotation.SUM_ENTITY_VOTE);
        double score_entity_vote = sum_entity_vote / (double) tableRowsTotal;
        scoreElements.put(HeaderAnnotation.SCORE_ENTITY_VOTE, score_entity_vote);

        double finalScore = score_entity_vote;
        Double namematch = scoreElements.get(HeaderAnnotation.SCORE_CTX_NAME_MATCH);
        if (namematch != null) {
            finalScore = finalScore + namematch;
        }

        scoreElements.put(HeaderAnnotation.FINAL, finalScore);
        ha.setFinalScore(finalScore);
        return scoreElements;
    }
}
