package uk.ac.shef.dcs.sti.algorithm.tm;

import uk.ac.shef.dcs.sti.PlaceHolder;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.sti.rep.*;
import uk.ac.shef.dcs.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 *
 */
public class TMPRelationScorer implements RelationScorer {
    private Lemmatizer lemmatizer;
    private List<String> stopWords;
    private OntologyBasedBoWCreator bowCreator;
    private double[] wt;  //header text, column, title&caption, other

    public TMPRelationScorer(String nlpResources, OntologyBasedBoWCreator bowCreator, List<String> stopWords,
                             double[] wt) throws IOException {
        this.lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();
        this.bowCreator = bowCreator;
        this.stopWords = stopWords;
        this.wt = wt;
    }

    @Override
    public List<TColumnColumnRelationAnnotation> computeElementScores(List<TCellCellRelationAnotation> cellcellRelationsOnRow,
                                                                     Collection<TColumnColumnRelationAnnotation> output,
                                                                     int subjectCol, int objectCol,
                                                                     Table table) {
        List<TColumnColumnRelationAnnotation> candidates;

        candidates = computeREScore(cellcellRelationsOnRow, output, subjectCol, objectCol);
        candidates = computeRCScore(candidates, table, objectCol);

        return candidates;
    }

    /**
     * Compute relation instance score
     * @param cellcellRelationAnotations
     * @param output
     * @param subjectCol
     * @param objectCol
     * @return
     */
    public List<TColumnColumnRelationAnnotation> computeREScore(List<TCellCellRelationAnotation> cellcellRelationAnotations,
                                                               Collection<TColumnColumnRelationAnnotation> output,
                                                               int subjectCol, int objectCol) {

        //for this row
        TCellCellRelationAnotation winningAnnotation = null;
        double winningScore = 0.0;
        for (TCellCellRelationAnotation cellcellRelationAnnotation : cellcellRelationAnotations) { //each candidate entity in this cell
            double attrMatchScore = cellcellRelationAnnotation.getWinningAttributeMatchScore();
            if (attrMatchScore > winningScore) {
                winningScore = attrMatchScore;
                winningAnnotation = cellcellRelationAnnotation;
            }
        }
        if (cellcellRelationAnotations.size() == 0 || winningAnnotation == null)
            return new ArrayList<>(output);

        Collections.sort(cellcellRelationAnotations);

        //consolidate scores from this cell
        for (TCellCellRelationAnotation cellcellRelationAnnotation : cellcellRelationAnotations) {
            if (cellcellRelationAnnotation.getWinningAttributeMatchScore() < winningScore)
                break;

            TColumnColumnRelationAnnotation columncolumnRelationAnnotation = null;
            for (TColumnColumnRelationAnnotation key : output) {
                if (key.getRelationURI().equals(cellcellRelationAnnotation.getRelationURI())) {
                    columncolumnRelationAnnotation = key;
                    break;
                }
            }
            if (columncolumnRelationAnnotation == null) {
                columncolumnRelationAnnotation = new TColumnColumnRelationAnnotation(
                        new RelationColumns(subjectCol, objectCol),
                        cellcellRelationAnnotation.getRelationURI(),
                        cellcellRelationAnnotation.getRelationLabel(), 0.0);
            }

            Map<String, Double> scoreElements = columncolumnRelationAnnotation.getScoreElements();
            if (scoreElements == null || scoreElements.size() == 0) {
                scoreElements = new HashMap<>();
                scoreElements.put(TColumnColumnRelationAnnotation.SUM_RE, 0.0);
                scoreElements.put(TColumnColumnRelationAnnotation.SUM_CELL_VOTE, 0.0);
            }
            scoreElements.put(TColumnColumnRelationAnnotation.SUM_RE,
                    scoreElements.get(TColumnColumnRelationAnnotation.SUM_RE) + winningScore);
            scoreElements.put(TColumnColumnRelationAnnotation.SUM_CELL_VOTE,
                    scoreElements.get(TColumnColumnRelationAnnotation.SUM_CELL_VOTE) + 1.0);
            columncolumnRelationAnnotation.setScoreElements(scoreElements);

            output.add(columncolumnRelationAnnotation);
        }

        return new ArrayList<>(output);
    }

    /**
     * compute relation context score
     *
     * context scores are only computed once. The code will check if they already edist for each TColumnColumnRelationAnnotation
     * and if so, it will not recompute it.
     * @param candidates
     * @param table
     * @param column
     * @return
     */
    public List<TColumnColumnRelationAnnotation> computeRCScore(
            Collection<TColumnColumnRelationAnnotation> candidates,
            Table table, int column) {
        Set<String> bowHeader = null;
        List<String> bowColumn = null,
                bowOutTableImportantCtx = null, bowOutTableTrivialCtx = null;
        for (TColumnColumnRelationAnnotation ccRelationAnnotation : candidates) {
            Double scoreCtxHeaderText = ccRelationAnnotation.getScoreElements().get(TColumnHeaderAnnotation.SCORE_CTX_IN_HEADER);
            Double scoreCtxColumnText = ccRelationAnnotation.getScoreElements().get(TColumnHeaderAnnotation.SCORE_CTX_IN_COLUMN);
            Double scoreCtxTableContext = ccRelationAnnotation.getScoreElements().get(TColumnHeaderAnnotation.SCORE_CTX_OUT);

            if (scoreCtxColumnText != null &&
                    scoreCtxHeaderText != null
                    && scoreCtxTableContext != null)
                continue;

            Set<String> relationBOW =
                    createRelationBOW(ccRelationAnnotation, true, TableMinerConstants.BOW_DISCARD_SINGLE_CHAR);

            if (scoreCtxHeaderText == null) {
                bowHeader = createHeaderTextBOW(bowHeader, table, column);
                double ctxScoreHeaderText =
                        CollectionUtils.computeFrequencyWeightedDice(relationBOW, bowHeader) * wt[0];
                ccRelationAnnotation.getScoreElements().put(TColumnHeaderAnnotation.SCORE_CTX_IN_HEADER,
                        ctxScoreHeaderText);
            }

            if (scoreCtxColumnText == null) {
                bowColumn = createColumnBOW(bowColumn, table, column);
                double ctx_column = CollectionUtils.computeFrequencyWeightedDice(relationBOW, bowColumn) * wt[1];
                ccRelationAnnotation.getScoreElements().put(TColumnHeaderAnnotation.SCORE_CTX_IN_COLUMN, ctx_column);
            }

            if (scoreCtxTableContext == null) {
                bowOutTableImportantCtx = createImportantOutTableCtxBOW(bowOutTableImportantCtx, table);
                double ctx_out_important = CollectionUtils.computeFrequencyWeightedDice(relationBOW, bowOutTableImportantCtx) * wt[2];
                bowOutTableTrivialCtx = create_table_context_other_bow(bowOutTableTrivialCtx, table);
                double ctx_out_trivial = CollectionUtils.computeFrequencyWeightedDice(relationBOW, bowOutTableTrivialCtx) * wt[3];
                ccRelationAnnotation.getScoreElements().put(TColumnHeaderAnnotation.SCORE_CTX_OUT,
                        ctx_out_important + ctx_out_trivial);
            }

        }

        return new ArrayList<>(candidates);
    }

    private Set<String> createRelationBOW(TColumnColumnRelationAnnotation relation,
                                          boolean lowercase,
                                          boolean discard_single_char) {
        Set<String> bow = new HashSet<>();
        bow.addAll(bowCreator.create(relation.getRelationURI()));
        bow.addAll(StringUtils.toBagOfWords(relation.getRelationLabel(), lowercase, true, discard_single_char));
        bow.removeAll(TableMinerConstants.FUNCTIONAL_STOPWORDS);
        return bow;
    }

    private Set<String> createHeaderTextBOW(Set<String> bag_of_words_for_header, Table table, int column) {
        if (bag_of_words_for_header != null)
            return bag_of_words_for_header;
        Set<String> bow = new HashSet<>();
        TColumnHeader header = table.getColumnHeader(column);
        if (header != null &&
                header.getHeaderText() != null &&
                !header.getHeaderText().equals(PlaceHolder.TABLE_HEADER_UNKNOWN.getValue())) {
            bow.addAll(lemmatizer.lemmatize(
                            StringUtils.toBagOfWords(header.getHeaderText(), true, true, TableMinerConstants.BOW_DISCARD_SINGLE_CHAR))
            );
        }
        bow.removeAll(TableMinerConstants.FUNCTIONAL_STOPWORDS);
        //also remove special, generic words, like "title", "name"
        bow.remove("title");
        bow.remove("name");
        return bow;
    }

    private List<String> createImportantOutTableCtxBOW(
            List<String> bag_of_words_for_table_context, Table table) {
        if (bag_of_words_for_table_context != null)
            return bag_of_words_for_table_context;
        if (table.getContexts() == null)
            return new ArrayList<>();

        List<String> bow = new ArrayList<>();
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

    private List<String> createColumnBOW(List<String> bag_of_words_for_column, Table table, int column) {
        if (bag_of_words_for_column != null)
            return bag_of_words_for_column;
        List<String> bow = new ArrayList<>();
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





    public Map<String, Double> computeFinal(TColumnColumnRelationAnnotation relation, int tableRowsTotal) {
        Map<String, Double> scoreElements = relation.getScoreElements();
        double sum_score_match =
                scoreElements.get(TColumnColumnRelationAnnotation.SUM_RE);
        double score_match = sum_score_match / scoreElements.get(TColumnColumnRelationAnnotation.SUM_CELL_VOTE);
        scoreElements.put(TColumnColumnRelationAnnotation.SCORE_RE, score_match);

        scoreElements.put(TColumnColumnRelationAnnotation.SUM_RE, sum_score_match);

        double score_vote = scoreElements.get(TColumnColumnRelationAnnotation.SUM_CELL_VOTE) / (double) tableRowsTotal;
        scoreElements.put(TColumnColumnRelationAnnotation.SCORE_CELL_VOTE, score_vote);

        double base_score = compute_relation_base_score(sum_score_match, scoreElements.get(TColumnColumnRelationAnnotation.SCORE_CELL_VOTE),
                (double) tableRowsTotal);

        for (Map.Entry<String, Double> e : scoreElements.entrySet()) {
            if (e.getKey().equals(TColumnColumnRelationAnnotation.SUM_RE) ||
                    e.getKey().equals(TColumnColumnRelationAnnotation.SUM_CELL_VOTE) ||
                    e.getKey().equals(TColumnColumnRelationAnnotation.SCORE_RE) ||
                    e.getKey().equals(TColumnColumnRelationAnnotation.SCORE_CELL_VOTE) ||
                    e.getKey().equals(TColumnColumnRelationAnnotation.FINAL))
                continue;

            base_score += e.getValue();
        }
        scoreElements.put(TColumnHeaderAnnotation.FINAL, base_score);
        relation.setFinalScore(base_score);
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

    public double scoreDC(TColumnColumnRelationAnnotation hbr, List<String> domain_representation) {
        Set<String> annotation_bow = createRelationBOW(hbr,
                true,
                TableMinerConstants.BOW_DISCARD_SINGLE_CHAR);
        //annotation_bow.removeAll(TableMinerConstants.FUNCTIONAL_STOPWORDS);
        double score = CollectionUtils.computeFrequencyWeightedDice(annotation_bow, domain_representation);
        score = Math.sqrt(score);
        hbr.getScoreElements().put(TColumnColumnRelationAnnotation.SCORE_DOMAIN_CONSENSUS, score);

        return score;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
