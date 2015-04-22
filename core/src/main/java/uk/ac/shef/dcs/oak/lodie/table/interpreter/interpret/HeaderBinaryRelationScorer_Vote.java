package uk.ac.shef.dcs.oak.lodie.table.interpreter.interpret;

import uk.ac.shef.dcs.oak.lodie.PlaceHolder;
import uk.ac.shef.dcs.oak.lodie.nlptools.Lemmatizer;
import uk.ac.shef.dcs.oak.lodie.nlptools.NLPTools;
import uk.ac.shef.dcs.oak.lodie.table.rep.*;
import uk.ac.shef.dcs.oak.lodie.test.TableMinerConstants;
import uk.ac.shef.dcs.oak.util.CollectionUtils;
import uk.ac.shef.dcs.oak.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 27/02/14
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class HeaderBinaryRelationScorer_Vote implements HeaderBinaryRelationScorer {
    public static double MAX_SCORE=0;
    private Lemmatizer lemmatizer;
    private List<String> stopWords;
    private Creator_OntologyEntityHierarchicalBOW bow_creator;
    private double[] weights;  //entity, header text, column, title&caption, other

    public HeaderBinaryRelationScorer_Vote(String nlpResources, Creator_OntologyEntityHierarchicalBOW bow_creator, List<String> stopWords,
                                           double[] weights) throws IOException {
        this.lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();
        this.bow_creator = bow_creator;
        this.stopWords = stopWords;
        this.weights = weights;
    }

    @Override
    public Set<HeaderBinaryRelationAnnotation> score(List<CellBinaryRelationAnnotation> input_from_row,
                                                     Set<HeaderBinaryRelationAnnotation> header_binary_relations_prev,
                                                     int subjectCol, int objectCol,
                                                     LTable table) {
        Set<HeaderBinaryRelationAnnotation> candidates;
        if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 0)
            candidates = score_entity_best_candidate_contribute(input_from_row, header_binary_relations_prev, subjectCol, objectCol);
        else
            candidates = score_entity_all_candidate_contribute(input_from_row, header_binary_relations_prev, subjectCol, objectCol);
        candidates = score_context(candidates, table, objectCol, false);

        return candidates;
    }

    public Set<HeaderBinaryRelationAnnotation> score_entity_all_candidate_contribute(List<CellBinaryRelationAnnotation> input_from_row,
                                                                                     Set<HeaderBinaryRelationAnnotation> header_binary_relations_prev,
                                                                                     int subjectCol, int objectCol) {
        final Set<HeaderBinaryRelationAnnotation> candidate_header_binary_relations =
                header_binary_relations_prev;

        //for this row
        Map<String, Double> tmp_relation_annotation_and_max_score = new HashMap<String, Double>();
        Map<String, String> header_relation_and_text = new HashMap<String, String>();

        for (CellBinaryRelationAnnotation cbr : input_from_row) { //each candidate relation in this cell
            //each assigned type receives a score of 1, and the bonus score due to disambiguation result
            double cbr_score = cbr.getScore();
            String url = cbr.getAnnotation_url();
            String text=cbr.getAnnotation_label();
            header_relation_and_text.put(url, text);

            Double score = tmp_relation_annotation_and_max_score.get(url);
            score = score == null ? 0 : score;
            if (cbr_score > score) {
                tmp_relation_annotation_and_max_score.put(url, cbr_score);
                /*if(score!=0)
                System.out.println();*/
            }

        }
        if (input_from_row.size() == 0 || tmp_relation_annotation_and_max_score.size() == 0) {
            //this entity has a score of 0.0, it should not contribute to the header typing, but we may still keep it as candidate for this cell
            //System.out.print("x(" + row + "," + column + ")");
            return candidate_header_binary_relations;
        }

        //consolidate scores from this cell
        for (Map.Entry<String, Double> e : tmp_relation_annotation_and_max_score.entrySet()) {
            HeaderBinaryRelationAnnotation hbr = null;

            for (HeaderBinaryRelationAnnotation key : header_binary_relations_prev) {
                if (key.getAnnotation_url().equals(e.getKey())) {
                    hbr = key;
                    break;
                }
            }
            if (hbr == null) {
                hbr = new HeaderBinaryRelationAnnotation(
                        new Key_SubjectCol_ObjectCol(subjectCol,objectCol),
                        e.getKey(),
                        header_relation_and_text.get(e.getKey()),0.0);
            }

            Map<String, Double> tmp_score_elements = hbr.getScoreElements();
            if (tmp_score_elements == null || tmp_score_elements.size() == 0) {
                tmp_score_elements = new HashMap<String, Double>();
                tmp_score_elements.put(HeaderBinaryRelationAnnotation.SUM_CBR_MATCH_SCORE, 0.0);
                tmp_score_elements.put(HeaderBinaryRelationAnnotation.SUM_CBR_VOTE, 0.0);
            }
            tmp_score_elements.put(HeaderBinaryRelationAnnotation.SUM_CBR_MATCH_SCORE,
                    tmp_score_elements.get(HeaderBinaryRelationAnnotation.SUM_CBR_MATCH_SCORE) + e.getValue());
            tmp_score_elements.put(HeaderBinaryRelationAnnotation.SUM_CBR_VOTE,
                    tmp_score_elements.get(HeaderBinaryRelationAnnotation.SUM_CBR_VOTE) + 1.0);
            hbr.setScoreElements(tmp_score_elements);

            candidate_header_binary_relations.add(hbr);
        }

        return candidate_header_binary_relations;
    }

    public Set<HeaderBinaryRelationAnnotation> score_entity_best_candidate_contribute(List<CellBinaryRelationAnnotation> input_from_row,
                                                                                      Set<HeaderBinaryRelationAnnotation> header_binary_relations_prev,
                                                                                      int subjectCol, int objectCol) {
        final Set<HeaderBinaryRelationAnnotation> candidate_header_binary_relations =
                header_binary_relations_prev;

        //for this row
        CellBinaryRelationAnnotation cbr_with_highest_match_score = null;
        double best_score = 0.0;
        for (CellBinaryRelationAnnotation cbr : input_from_row) { //each candidate entity in this cell
            double match_score = cbr.getScore();
            if (match_score > best_score) {
                best_score = match_score;
                cbr_with_highest_match_score = cbr;
            }
        }
        if (input_from_row.size() == 0 || cbr_with_highest_match_score == null) {
            //this entity has a score of 0.0, it should not contribute to the header typing, but we may still keep it as candidate for this cell
            return candidate_header_binary_relations;
        }

        Collections.sort(input_from_row);
        //consolidate scores from this cell
        for (CellBinaryRelationAnnotation cbr : input_from_row) {
            if(cbr.getScore()<best_score)
                break;

            HeaderBinaryRelationAnnotation hbr = null;
            for (HeaderBinaryRelationAnnotation key : candidate_header_binary_relations) {
                if (key.getAnnotation_url().equals(cbr.getAnnotation_url())) {
                    hbr = key;
                    break;
                }
            }
            if (hbr == null) {
                hbr = new HeaderBinaryRelationAnnotation(
                        new Key_SubjectCol_ObjectCol(subjectCol,objectCol),
                        cbr.getAnnotation_url(),cbr.getAnnotation_label(),0.0);
            }

            Map<String, Double> tmp_score_elements = hbr.getScoreElements();
            if (tmp_score_elements == null || tmp_score_elements.size() == 0) {
                tmp_score_elements = new HashMap<String, Double>();
                tmp_score_elements.put(HeaderBinaryRelationAnnotation.SUM_CBR_MATCH_SCORE, 0.0);
                tmp_score_elements.put(HeaderBinaryRelationAnnotation.SUM_CBR_VOTE, 0.0);
            }
            tmp_score_elements.put(HeaderBinaryRelationAnnotation.SUM_CBR_MATCH_SCORE,
                    tmp_score_elements.get(HeaderBinaryRelationAnnotation.SUM_CBR_MATCH_SCORE) + best_score);
            tmp_score_elements.put(HeaderBinaryRelationAnnotation.SUM_CBR_VOTE,
                    tmp_score_elements.get(HeaderBinaryRelationAnnotation.SUM_CBR_VOTE) + 1.0);
            hbr.setScoreElements(tmp_score_elements);

            candidate_header_binary_relations.add(hbr);
        }

        return candidate_header_binary_relations;
    }

    public Set<HeaderBinaryRelationAnnotation> score_context(Set<HeaderBinaryRelationAnnotation> candidates, LTable table, int column, boolean overwrite) {
        Set<String> bag_of_words_for_header = null;
        List<String> bag_of_words_for_column = null, bag_of_words_for_table_major_context = null, bag_of_words_for_table_other_context = null;
        for (HeaderBinaryRelationAnnotation hbr : candidates) {
            Double score_ctx_header_text = hbr.getScoreElements().get(HeaderAnnotation.SCORE_CTX_NAME_MATCH);
            Double score_ctx_column_text = hbr.getScoreElements().get(HeaderAnnotation.SCORE_CTX_COLUMN_TEXT);
            Double score_ctx_table_context = hbr.getScoreElements().get(HeaderAnnotation.SCORE_CTX_TABLE_CONTEXT);

            if (score_ctx_column_text != null &&
                    score_ctx_header_text != null
                    && score_ctx_table_context != null && !overwrite)
                continue;

            Set<String> annotation_bow = create_annotation_bow(hbr, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW);

            if (overwrite || (!overwrite && score_ctx_header_text == null)) {
                bag_of_words_for_header = create_header_bow(bag_of_words_for_header, table, column);
                double ctx_header_text =
                        CollectionUtils.scoreOverlap_dice_keepFrequency(annotation_bow, bag_of_words_for_header) * weights[1];
                hbr.getScoreElements().put(HeaderAnnotation.SCORE_CTX_NAME_MATCH, ctx_header_text);
            }

            if (overwrite || (!overwrite && score_ctx_column_text == null)) {
                bag_of_words_for_column = create_column_bow(bag_of_words_for_column, table, column);
                double ctx_column = CollectionUtils.scoreOverlap_dice_keepFrequency(annotation_bow, bag_of_words_for_column) * weights[2];
                hbr.getScoreElements().put(HeaderAnnotation.SCORE_CTX_COLUMN_TEXT, ctx_column);
            }

            if (overwrite || (!overwrite && score_ctx_table_context == null)) {
                bag_of_words_for_table_major_context = create_table_context_major_bow(bag_of_words_for_table_major_context, table);
                double ctx_table_major = CollectionUtils.scoreOverlap_dice_keepFrequency(annotation_bow, bag_of_words_for_table_major_context) * weights[3];
                bag_of_words_for_table_other_context = create_table_context_other_bow(bag_of_words_for_table_other_context, table);
                double ctx_table_other = CollectionUtils.scoreOverlap_dice_keepFrequency(annotation_bow, bag_of_words_for_table_other_context) * weights[4];
                hbr.getScoreElements().put(HeaderAnnotation.SCORE_CTX_TABLE_CONTEXT,
                        ctx_table_major + ctx_table_other);
            }

        }

        return candidates;
    }

    private List<String> create_table_context_major_bow(List<String> bag_of_words_for_table_context, LTable table) {
        if (bag_of_words_for_table_context != null)
            return bag_of_words_for_table_context;
        if (table.getContexts() == null)
            return new ArrayList<String>();

        List<String> bow = new ArrayList<String>();
        for (int i = 0; i < table.getContexts().size(); i++) {
            LTableContext tx = table.getContexts().get(i);
            if (tx.getType().equals(LTableContext.TableContextType.PAGETITLE) ||
                    tx.getType().equals(LTableContext.TableContextType.CAPTION)) {
                bow.addAll(lemmatizer.lemmatize(
                        StringUtils.toBagOfWords(tx.getText(), true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW))
                );
            }
        }
        bow.removeAll(stopWords);
        return bow;
    }

    private List<String> create_table_context_other_bow(List<String> bag_of_words_for_table_context, LTable table) {
        if (bag_of_words_for_table_context != null)
            return bag_of_words_for_table_context;
        if (table.getContexts() == null)
            return new ArrayList<String>();

        List<String> bow = new ArrayList<String>();
        for (int i = 0; i < table.getContexts().size(); i++) {
            LTableContext tx = table.getContexts().get(i);
            if (!tx.getType().equals(LTableContext.TableContextType.PAGETITLE) &&
                    !tx.getType().equals(LTableContext.TableContextType.CAPTION)) {
                bow.addAll(lemmatizer.lemmatize(
                        StringUtils.toBagOfWords(tx.getText(), true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW))
                );
            }
        }
        bow.removeAll(stopWords);
        return bow;
    }

    private List<String> create_column_bow(List<String> bag_of_words_for_column, LTable table, int column) {
        if (bag_of_words_for_column != null)
            return bag_of_words_for_column;
        List<String> bow = new ArrayList<String>();
        for (int row = 0; row < table.getNumRows(); row++) {
            LTableContentCell tcc = table.getContentCell(row, column);
            if (tcc.getText() != null) {
                bow.addAll(lemmatizer.lemmatize(
                        StringUtils.toBagOfWords(tcc.getText(), true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW))
                );
            }
        }
        bow.removeAll(stopWords);
        return bow;
    }

    private Set<String> create_header_bow(Set<String> bag_of_words_for_header, LTable table, int column) {
        if (bag_of_words_for_header != null)
            return bag_of_words_for_header;
        Set<String> bow = new HashSet<String>();
        LTableColumnHeader header = table.getColumnHeader(column);
        if (header != null &&
                header.getHeaderText() != null &&
                !header.getHeaderText().equals(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue())) {
            bow.addAll(lemmatizer.lemmatize(
                    StringUtils.toBagOfWords(header.getHeaderText(), true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW))
            );
        }
        bow.removeAll(stopWords);
        //also remove special, generic words, like "title", "name"
        bow.remove("title");
        bow.remove("name");
        return bow;
    }

    public Set<String> create_annotation_bow(HeaderBinaryRelationAnnotation hbr, boolean lowercase, boolean discard_single_char) {
        Set<String> bow = new HashSet<String>();
        bow.addAll(bow_creator.create(hbr.getAnnotation_url()));
        String label = StringUtils.toAlphaNumericWhitechar(hbr.getAnnotation_label()).trim();
        for (String s : label.split("\\s+")) {
            s = s.trim();
            if(discard_single_char&& s.length()<2)
                continue;
            if (s.length() > 0) {
                if (lowercase) s = s.toLowerCase();
                bow.add(s);
            }
        }
        bow.removeAll(TableMinerConstants.stopwords_small);
        return bow;
    }

    public Map<String, Double> compute_final_score(HeaderBinaryRelationAnnotation hbr, int tableRowsTotal) {
        Map<String, Double> scoreElements = hbr.getScoreElements();
        double sum_score_match =
                scoreElements.get(HeaderBinaryRelationAnnotation.SUM_CBR_MATCH_SCORE);
        double score_match = sum_score_match/ scoreElements.get(HeaderBinaryRelationAnnotation.SUM_CBR_VOTE);
        scoreElements.put(HeaderBinaryRelationAnnotation.SCORE_CBR_MATCH, score_match);

        scoreElements.put(HeaderBinaryRelationAnnotation.SUM_CBR_MATCH_SCORE, sum_score_match);

        double score_vote = scoreElements.get(HeaderBinaryRelationAnnotation.SUM_CBR_VOTE) / (double) tableRowsTotal;
        scoreElements.put(HeaderBinaryRelationAnnotation.SCORE_CBR_VOTE, score_vote);

        double base_score = compute_relation_base_score(sum_score_match, scoreElements.get(HeaderBinaryRelationAnnotation.SCORE_CBR_VOTE),
                (double) tableRowsTotal);

        for (Map.Entry<String, Double> e : scoreElements.entrySet()) {
            if (e.getKey().equals(HeaderBinaryRelationAnnotation.SUM_CBR_MATCH_SCORE) ||
                    e.getKey().equals(HeaderBinaryRelationAnnotation.SUM_CBR_VOTE) ||
                    e.getKey().equals(HeaderBinaryRelationAnnotation.SCORE_CBR_MATCH) ||
                    e.getKey().equals(HeaderBinaryRelationAnnotation.SCORE_CBR_VOTE) ||
                    e.getKey().equals(HeaderBinaryRelationAnnotation.FINAL))
                continue;

            base_score += e.getValue();
        }
        scoreElements.put(HeaderAnnotation.FINAL, base_score);
        hbr.setFinalScore(base_score);
        return scoreElements;
    }

    public static double compute_relation_base_score(double sum_cbr_match,
                                                     double sum_cbr_vote,
                                                     double total_table_rows) {
        if (sum_cbr_vote == 0)
            return 0.0;

        double score_cbr_vote = sum_cbr_vote / total_table_rows;
        double base_score = score_cbr_vote * (sum_cbr_match / sum_cbr_vote);
        return base_score;
    }

    public double score_domain_consensus(HeaderBinaryRelationAnnotation hbr, List<String> domain_representation) {
        Set<String> annotation_bow = create_annotation_bow(hbr,
                true,
                TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW);
        //annotation_bow.removeAll(TableMinerConstants.stopwords_small);
        double score = CollectionUtils.scoreOverlap_dice_keepFrequency(annotation_bow, domain_representation);
        score = Math.sqrt(score);
        hbr.getScoreElements().put(HeaderBinaryRelationAnnotation.SCORE_DOMAIN_CONSENSUS, score);

        return score;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
