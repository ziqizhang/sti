package uk.ac.shef.dcs.sti.core.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.sti.STIEnum;
import uk.ac.shef.dcs.sti.core.feature.OntologyBasedBoWCreator;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.core.model.*;
import uk.ac.shef.dcs.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 30/05/14
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
public class TMPISWCTColumnClassifier implements ClazzScorer {

    private Lemmatizer lemmatizer;
    private List<String> stopWords;
    //private Levenshtein stringSimilarityMetric;
    //private Jaro stringSimilarityMetric;
    //private Levenshtein stringSimilarityMetric;
    private AbstractStringMetric stringSimilarityMetric;
    private OntologyBasedBoWCreator bow_creator;

    public TMPISWCTColumnClassifier(String nlpResources, OntologyBasedBoWCreator bow_creator, List<String> stopWords,
                                    double[] weights) throws IOException {
        this.lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();
        this.bow_creator = bow_creator;
        this.stopWords = stopWords;
        this.stringSimilarityMetric = new Levenshtein();
    }


    @Override
    public List<TColumnHeaderAnnotation> computeElementScores(List<Pair<Entity, Map<String, Double>>> input,
                                                             Collection<TColumnHeaderAnnotation> headerAnnotationCandidates,
                                                             Table table,
                                                             List<Integer> rows, int column) {
        List<TColumnHeaderAnnotation> candidates = new ArrayList<>();

            for (int row : rows)
                candidates = score_entity_best_candidate_contribute(input, headerAnnotationCandidates, table, row, column);

        candidates = computeCCScore(candidates, table, column);

        return candidates;
    }


    public List<TColumnHeaderAnnotation> score_entity_best_candidate_contribute(List<Pair<Entity, Map<String, Double>>> input,
                                                                        Collection<TColumnHeaderAnnotation> headerAnnotations_prev, Table table,
                                                                        int row, int column) {
        final List<TColumnHeaderAnnotation> candidate_header_annotations =
                new ArrayList<>(headerAnnotations_prev);

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


        for (Pair<Entity, Map<String, Double>> es : input) {
            Entity current_candidate = es.getKey();
            double entity_disamb_score = es.getValue().get(TCellAnnotation.SCORE_FINAL);
            if (entity_disamb_score != best_score)
                continue;

            Set<String> types_already_received_votes_by_cell = new HashSet<String>();    //each type will receive a max of 1 vote from each cell. If multiple candidates have the same highest computeElementScores and casts same votes, they are counted oly once
            List<Clazz> type_voted_by_this_cell = current_candidate.getTypes();

            //consolidate scores from this cell
            for (Clazz type : type_voted_by_this_cell) {
                if (types_already_received_votes_by_cell.contains(type.getId()))
                    continue;

                types_already_received_votes_by_cell.add(type.getId());

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
                    tmp_score_elements = new HashMap<String, Double>();
                    tmp_score_elements.put(TColumnHeaderAnnotation.SUM_CE, 0.0);
                    tmp_score_elements.put(TColumnHeaderAnnotation.SUM_CELL_VOTE, 0.0);
                }
                tmp_score_elements.put(TColumnHeaderAnnotation.SUM_CE,
                        tmp_score_elements.get(TColumnHeaderAnnotation.SUM_CE) + best_score);
                tmp_score_elements.put(TColumnHeaderAnnotation.SUM_CELL_VOTE,
                        tmp_score_elements.get(TColumnHeaderAnnotation.SUM_CELL_VOTE) + 1.0);
                hAnnotation.setScoreElements(tmp_score_elements);

                candidate_header_annotations.add(hAnnotation);
            }
        }

        return candidate_header_annotations;
    }

    public List<TColumnHeaderAnnotation> computeCCScore(Collection<TColumnHeaderAnnotation> candidates, Table table, int column) {
        List<String> bag_of_words_for_header = null, bag_of_words_for_other_headers = null;
        List<String> bag_of_words_for_column = null, bag_of_words_for_table_major_context = null, bag_of_words_for_table_other_context = null;
        for (TColumnHeaderAnnotation ha : candidates) {
            Double score_ctx_header_text = ha.getScoreElements().get(TColumnHeaderAnnotation.SCORE_CTX_IN_HEADER);
            Double score_ctx_column_text = ha.getScoreElements().get(TColumnHeaderAnnotation.SCORE_CTX_IN_COLUMN);
            Double score_ctx_table_context = ha.getScoreElements().get(TColumnHeaderAnnotation.SCORE_CTX_OUT);

            if (score_ctx_column_text != null &&
                    score_ctx_header_text != null
                    && score_ctx_table_context != null)
                continue;

            Set<String> annotation_bow = new HashSet<String>(create_annotation_bow(ha,
                    true,
                    TableMinerConstants.BOW_DISCARD_SINGLE_CHAR,
                    TableMinerConstants.CLAZZBOW_INCLUDE_URI));

            if (score_ctx_header_text == null) {
                String headerText = "";
                TColumnHeader header = table.getColumnHeader(column);
                if (header != null &&
                        header.getHeaderText() != null &&
                        !header.getHeaderText().equals(STIEnum.TABLE_HEADER_UNKNOWN.getValue())) {
                    headerText = header.getHeaderText();
                }

                bag_of_words_for_header = create_header_bow(bag_of_words_for_header, table, column);
                double ctx_header_text = CollectionUtils.computeFrequencyWeightedDice(annotation_bow, bag_of_words_for_header);
                //double ctx_header_text = stringSimilarityMetric.getSimilarity(ha.getAnnotation_url(), headerText);


                ha.getScoreElements().put(TColumnHeaderAnnotation.SCORE_CTX_IN_HEADER, ctx_header_text);
            }

            if (score_ctx_column_text == null) {
                bag_of_words_for_column = create_column_bow(bag_of_words_for_column, table, column);
                double ctx_column =
                        CollectionUtils.computeFrequencyWeightedDice(annotation_bow, bag_of_words_for_column);
                //CollectionUtils.computeCoverage(bag_of_words_for_column, new ArrayList<String>(annotation_bow)) * weights[1];
                ha.getScoreElements().put(TColumnHeaderAnnotation.SCORE_CTX_IN_COLUMN, ctx_column);
            }

            if (score_ctx_table_context == null) {
                bag_of_words_for_table_major_context = create_table_context_major_bow(bag_of_words_for_table_major_context, table);
                double ctx_table_major =
                        CollectionUtils.computeFrequencyWeightedDice(annotation_bow, bag_of_words_for_table_major_context);
                //CollectionUtils.computeCoverage(bag_of_words_for_table_major_context, new ArrayList<String>(annotation_bow)) * weights[3];
                bag_of_words_for_table_other_context = create_table_context_other_bow(bag_of_words_for_table_other_context, table);
                double ctx_table_other =
                        CollectionUtils.computeFrequencyWeightedDice(annotation_bow, bag_of_words_for_table_other_context);
                //CollectionUtils.computeCoverage(bag_of_words_for_table_other_context, new ArrayList<String>(annotation_bow)) * weights[2];
                ha.getScoreElements().put(TColumnHeaderAnnotation.SCORE_CTX_OUT,
                        ctx_table_major + ctx_table_other);
            }

        }

        return new ArrayList<>(candidates);
    }

    @Override
    public double computeDC(TColumnHeaderAnnotation ha, List<String> domain_representation) {
        return 0.0;
    }

    private List<String> create_table_context_major_bow(List<String> bag_of_words_for_table_context, Table table) {
        if (bag_of_words_for_table_context != null)
            return bag_of_words_for_table_context;
        if (table.getContexts() == null)
            return new ArrayList<String>();

        List<String> bow = new ArrayList<String>();
        for (int i = 0; i < table.getContexts().size(); i++) {
            TContext tx = table.getContexts().get(i);
            if (tx.getType().equals(TContext.TableContextType.PAGETITLE) ||
                    tx.getType().equals(TContext.TableContextType.CAPTION)) {
                bow.addAll(lemmatizer.lemmatize(
                        StringUtils.toBagOfWords(tx.getText(), true, true, TableMinerConstants.BOW_DISCARD_SINGLE_CHAR))
                );
            }
        }
        bow.removeAll(stopWords);
        return bow;
    }

    private List<String> create_table_context_other_bow(List<String> bag_of_words_for_table_context, Table table) {
        if (bag_of_words_for_table_context != null)
            return bag_of_words_for_table_context;
        if (table.getContexts() == null)
            return new ArrayList<String>();

        List<String> bow = new ArrayList<String>();
        for (int i = 0; i < table.getContexts().size(); i++) {
            TContext tx = table.getContexts().get(i);
            if (!tx.getType().equals(TContext.TableContextType.PAGETITLE) &&
                    !tx.getType().equals(TContext.TableContextType.CAPTION)) {
                bow.addAll(lemmatizer.lemmatize(
                        StringUtils.toBagOfWords(tx.getText(), true, true, TableMinerConstants.BOW_DISCARD_SINGLE_CHAR))
                );
            }
        }
        bow.removeAll(stopWords);
        return bow;
    }

    private List<String> create_column_bow(List<String> bag_of_words_for_column, Table table, int column) {
        if (bag_of_words_for_column != null)
            return bag_of_words_for_column;
        List<String> bow = new ArrayList<String>();
        for (int row = 0; row < table.getNumRows(); row++) {
            TCell tcc = table.getContentCell(row, column);
            if (tcc.getText() != null) {
                bow.addAll(lemmatizer.lemmatize(
                        StringUtils.toBagOfWords(tcc.getText(), true, true, TableMinerConstants.BOW_DISCARD_SINGLE_CHAR))
                );
            }
        }
        bow.removeAll(stopWords);
        return bow;
    }

    private List<String> create_header_bow(List<String> bag_of_words_for_header, Table table, int column) {
        if (bag_of_words_for_header != null)
            return bag_of_words_for_header;
        List<String> bow = new ArrayList<String>();

        // for (int c = 0; c < table.getNumCols(); c++) {
        TColumnHeader header = table.getColumnHeader(column);
        if (header != null &&
                header.getHeaderText() != null &&
                !header.getHeaderText().equals(STIEnum.TABLE_HEADER_UNKNOWN.getValue())) {
            bow.addAll(lemmatizer.lemmatize(
                    StringUtils.toBagOfWords(header.getHeaderText(), true, true, TableMinerConstants.BOW_DISCARD_SINGLE_CHAR))
            );
        }
        //   }
        bow.removeAll(stopWords);
        //also remove special, generic words, like "title", "name"
        bow.remove("title");
        bow.remove("name");
        return bow;
    }

    private List<String> create_annotation_bow(TColumnHeaderAnnotation ha, boolean lowercase, boolean discard_single_char, boolean include_url) {
        List<String> bow = new ArrayList<String>();
        if (include_url) {
            bow.addAll(bow_creator.create(ha.getAnnotation().getId()));
        }

        String label = StringUtils.toAlphaNumericWhitechar(ha.getAnnotation().getId()).trim();
        for (String s : label.split("\\s+")) {
            s = s.trim();
            if (s.length() > 0) {
                if (lowercase) s = s.toLowerCase();
                bow.add(s);
            }
        }

        if (discard_single_char) {
            Iterator<String> it = bow.iterator();
            while (it.hasNext()) {
                String t = it.next();
                if (t.length() < 2)
                    it.remove();
            }
        }
        bow.removeAll(TableMinerConstants.FUNCTIONAL_STOPWORDS);
        return bow;
    }



    public Map<String, Double> computeFinal(TColumnHeaderAnnotation ha, int tableRowsTotal) {
        Map<String, Double> scoreElements = ha.getScoreElements();
        double sum_entity_disamb =
                scoreElements.get(TColumnHeaderAnnotation.SUM_CE);
        double sum_entity_vote = scoreElements.get(TColumnHeaderAnnotation.SUM_CELL_VOTE);

        scoreElements.put(TColumnHeaderAnnotation.SCORE_CE, sum_entity_disamb / sum_entity_vote);

        double score_entity_vote = scoreElements.get(TColumnHeaderAnnotation.SUM_CELL_VOTE) / (double) tableRowsTotal;
        scoreElements.put(TColumnHeaderAnnotation.SCORE_CELL_VOTE, score_entity_vote);

        double base_score = compute_typing_base_score(sum_entity_disamb, scoreElements.get(TColumnHeaderAnnotation.SUM_CELL_VOTE),
                (double) tableRowsTotal);

        for (Map.Entry<String, Double> e : scoreElements.entrySet()) {
            if (e.getKey().equals(TColumnHeaderAnnotation.SUM_CE) ||
                    e.getKey().equals(TColumnHeaderAnnotation.SUM_CELL_VOTE) ||
                    e.getKey().equals(TColumnHeaderAnnotation.SCORE_CE) ||
                    e.getKey().equals(TColumnHeaderAnnotation.SCORE_CELL_VOTE) ||
                    e.getKey().equals(TColumnHeaderAnnotation.FINAL))
                continue;

            base_score += e.getValue();
        }
        scoreElements.put(TColumnHeaderAnnotation.FINAL, base_score);
        ha.setFinalScore(base_score);
        return scoreElements;
    }

    public static double compute_typing_base_score(double sum_entity_disamb,
                                                   double sum_entity_vote,
                                                   double total_table_rows) {
        if (sum_entity_vote == 0)
            return 0.0;

        double score_entity_vote = sum_entity_vote / total_table_rows;
        double base_score = score_entity_vote * (sum_entity_disamb / sum_entity_vote);
        return base_score;
    }
}
