package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * scoring based on how much overlap a candidate entity has with its context
 */
public class TMPEntityScorer implements EntityScorer {

    public static final String SCORE_NAME_MATCH="name_match";
    public static final String SCORE_IN_CTX_COLUMN_HEADER ="ctx_column_header";
    public static final String SCORE_IN_CTX_ROW ="ctx_row";
    public static final String SCORE_IN_CTX_COLUMN ="ctx_column";
    public static final String SCORE_OUT_CTX ="ctx_out";

    private List<String> stopWords;
    private double[] wt; //context weights: 0-row context; 1-column context; 2-column header; 3-context (all)
    private Lemmatizer lemmatizer;

    public TMPEntityScorer(
            List<String> stopWords,
            double[] wt,
            String nlpResources) throws IOException {
        if (nlpResources != null)
            lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();

        this.stopWords = stopWords;
        this.wt = wt;
    }


    public Map<String, Double> computeElementScores(Entity candidate,
                                                    List<Entity> all_candidates,
                                                    int sourceColumnIndex,
                                                    int sourceRowIndex,
                                                    List<Integer> block,
                                                    Table table,
                                                    Entity... referenceEntities) {
        /*if(candidate.getName().contains("Republican"))
            System.out.println();*/
        Map<String, Double> scoreMap = new HashMap<>();
        String columnHeaderText = table.getColumnHeader(sourceColumnIndex).getHeaderText();

        /* BOW OF THE ENTITY*/
        Collection<String> bow_of_entity = createEntityBOW(candidate, lemmatizer, stopWords);

        /* BOW OF THE Row context*/
        Collection<String> bow_of_row = createRowBOW(sourceColumnIndex, columnHeaderText, block, table, lemmatizer, stopWords);
        double coverageRowCtx = CollectionUtils.computeCoverage(bow_of_entity, bow_of_row) * wt[0];

        //double contextOverlapScore = scoreOverlap(bag_of_words_for_entity, bag_of_words_for_context);
        scoreMap.put(SCORE_IN_CTX_ROW, coverageRowCtx);

        /*BOW OF Column context*/
        Collection<String> bow_of_column = createColumnBow(sourceColumnIndex, block, table, lemmatizer, stopWords);
        double coverageColumnCtx = CollectionUtils.computeCoverage(bow_of_entity, bow_of_column) * wt[1];
        scoreMap.put(SCORE_IN_CTX_COLUMN, coverageColumnCtx);

        /*BOW of column header */
        String entityLabel = candidate.getLabel();
        Set<String> bow_of_entityLabel = new HashSet<>(StringUtils.toBagOfWords(entityLabel, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));

        Collection<String> bow_of_columnHeader = new HashSet<>(
                StringUtils.toBagOfWords(columnHeaderText, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR)
        );
        bow_of_columnHeader = normalize(bow_of_columnHeader, lemmatizer, stopWords);
        double nameHeaderCtxScore = CollectionUtils.computeDice(bow_of_entityLabel, bow_of_columnHeader) * wt[2];
        scoreMap.put(SCORE_IN_CTX_COLUMN_HEADER, nameHeaderCtxScore/* +
                name_and_col_match_score + name_and_context_match_score*/);

        /*BOW OF out table context (from paragraphs etc)*/
        Collection<String> bow_of_outctx = createOutCtxBow(table, lemmatizer, stopWords);
        double fwDice = CollectionUtils.computeFrequencyWeightedDice(bow_of_entity, bow_of_outctx) * wt[3];
        scoreMap.put(SCORE_OUT_CTX, fwDice);

        /*NAME MATCH SCORE */
        String cellText = table.getContentCell(block.get(0), sourceColumnIndex).getText();
        Set<String> bow_of_cellText = new HashSet<>(StringUtils.toBagOfWords(cellText, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
        double en_score = CollectionUtils.computeDice(bow_of_cellText, bow_of_entityLabel);
        scoreMap.put(SCORE_NAME_MATCH, Math.sqrt(en_score));
        //scoreMap.put("matched_name_tokens", (double) intersection.size());

        return scoreMap;
    }

    public double computeFinal(Map<String, Double> scoreMap, String cellTextOriginal) {
        double sum = 0.0, ctx_scores = 0.0, nm_score = 0.0;
        cellTextOriginal = StringUtils.toAlphaNumericWhitechar(cellTextOriginal).trim();

        int length = cellTextOriginal.split("\\s+").length;

        double weight_ctx =/* 1.0/length*/ Math.sqrt(1.0 / length);
        double weight_nm = 1.0;

        for (Map.Entry<String, Double> e : scoreMap.entrySet()) {
            /*if (e.getKey().startsWith("ctx_"))
                ctx_scores += e.getValue();
            if (e.getKey().equals(TCellAnnotation.SCORE_IN_CTX_COLUMN_HEADER))
                sum += e.getValue();*/
            ctx_scores += e.getValue();
        }
        Double nameMatch = scoreMap.get(SCORE_NAME_MATCH);
        if (nameMatch != null)
            nm_score = nameMatch;

        sum = ctx_scores * weight_ctx + nm_score * weight_nm;

        scoreMap.put(TCellAnnotation.SCORE_FINAL, sum);
        return sum;
    }


    /**
     * create bow of entity
     * @param candidate
     * @param lemmatizer
     * @param stopWords
     * @return
     */
    protected Collection<String> createEntityBOW(Entity candidate, Lemmatizer lemmatizer, Collection<String> stopWords) {
        List<Attribute> attributes = candidate.getAttributes();
        List<String> bow_of_entity = new ArrayList<>();
        for (Attribute f : attributes) {
            if (!STIConstantProperty.BOW_ENTITY_INCLUDE_INDIRECT_ATTRIBUTE
                    && !f.isDirect())
                continue;
            String value = f.getValue();
            if (!StringUtils.isPath(value))
                bow_of_entity.addAll(StringUtils.toBagOfWords(value, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
            else
                bow_of_entity.add(value);
        }
        return normalize(bow_of_entity, lemmatizer, stopWords);
    }

    /**
     * create bow of the in-table context based on other columns on the same row
     *
     * @param sourceColumnIndex
     * @param columnHeaderText
     * @param block rows where given the current column index, the text in the cell of that row is identical to the source cell
     *                  for the entity candidate
     * @param table
     * @param lemmatizer
     * @param stopWords
     * @return
     */
    protected Collection<String> createRowBOW(int sourceColumnIndex,
                                              String columnHeaderText,
                                              List<Integer> block,
                                              Table table,
                                              Lemmatizer lemmatizer, Collection<String> stopWords) {
        List<String> bag_of_words_for_context = new ArrayList<>();
        //context from the row
        for (int row : block) {
            for (int col = 0; col < table.getNumCols(); col++) {
                if (col == sourceColumnIndex || table.getColumnHeader(col).getTypes().get(0).getType().equals(
                        DataTypeClassifier.DataType.ORDERED_NUMBER
                ))
                    continue;
                TCell tcc = table.getContentCell(row, col);
                bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
            }
        }
        bag_of_words_for_context.addAll(StringUtils.toBagOfWords(   //also add the column header as the row context of this entity
                columnHeaderText, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));

        return normalize(bag_of_words_for_context, lemmatizer, stopWords);
    }

    /**
     * create bow of the in-table context based on other rows on the same column
     * @param sourceColumnIndex
     * @param block
     * @param table
     * @param lemmatizer
     * @param stopWords
     * @return
     */
    protected Collection<String> createColumnBow(int sourceColumnIndex,
                                                 List<Integer> block,
                                                 Table table,
                                                 Lemmatizer lemmatizer, Collection<String> stopWords){
        List<String> bag_of_words_for_context = new ArrayList<>();
        for (int row = 0; row < table.getNumRows(); row++) {
            if (block.contains(row))
                continue;
            TCell tcc = table.getContentCell(row, sourceColumnIndex);
            bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
        }
        return normalize(bag_of_words_for_context, lemmatizer, stopWords);
    }


    protected Collection<String> createOutCtxBow(
            Table table,
            Lemmatizer lemmatizer, Collection<String> stopWords){
        /*BOW OF table table context (from paragraphs etc)*/
        List<String> bag_of_words_for_context = new ArrayList<>();
        for (TContext tc : table.getContexts()) {
            bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tc.getText(), true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
        }
        return normalize(bag_of_words_for_context, lemmatizer, stopWords);
    }

    protected Collection<String> normalize(Collection<String> input, Lemmatizer lemmatizer, Collection<String> stopWords) {
        if (lemmatizer != null)
            input = lemmatizer.lemmatize(input);
        input.removeAll(stopWords);
        return input;
    }
}
