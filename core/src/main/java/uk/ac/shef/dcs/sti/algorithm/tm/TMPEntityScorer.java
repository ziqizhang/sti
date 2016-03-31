package uk.ac.shef.dcs.sti.algorithm.tm;

import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.TCellAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * scoring based on how much overlap a candidate entity has with its context
 */
public class TMPEntityScorer extends EntityScorer {
    private List<String> stopWords;
    private double[] wt;
    private Lemmatizer lemmatizer;

    /*
    context weights: 0-row context; 1-column context; 2-table context
     */
    public TMPEntityScorer(
            List<String> stopWords,
            double[] wt,
            String nlpResources) throws IOException {
        if (nlpResources != null)
            lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();

        this.stopWords = stopWords;
        this.wt = wt;
    }


    public Map<String, Double> score(Entity candidate,
                                     List<Entity> all_candidates,
                                     int sourceColumnIndex,
                                     int sourceRowIndex,
                                     List<Integer> block,
                                     Table table,
                                     Set<String> preliminaryColumnLabel,
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
        scoreMap.put(TCellAnnotation.SCORE_IN_CTX_ROW, coverageRowCtx);

        /*BOW OF Column context*/
        Collection<String> bow_of_column = createColumnBow(sourceColumnIndex, block, table, lemmatizer, stopWords);
        double coverageColumnCtx = CollectionUtils.computeCoverage(bow_of_entity, bow_of_column) * wt[1];
        scoreMap.put(TCellAnnotation.SCORE_IN_CTX_COLUMN, coverageColumnCtx);

        /*BOW of column header */
        String entityLabel = candidate.getLabel();
        Set<String> bow_of_entityLabel = new HashSet<>(StringUtils.toBagOfWords(entityLabel, true, true, TableMinerConstants.ENTITYBOW_DISCARD_SINGLE_CHAR));

        Collection<String> bow_of_columnHeader = new HashSet<>(
                StringUtils.toBagOfWords(columnHeaderText, true, true, TableMinerConstants.ENTITYBOW_DISCARD_SINGLE_CHAR)
        );
        bow_of_columnHeader = normalize(bow_of_columnHeader, lemmatizer, stopWords);
        double nameHeaderCtxScore = CollectionUtils.computeDice(bow_of_entityLabel, bow_of_columnHeader) * wt[2];
        scoreMap.put(TCellAnnotation.SCORE_IN_CTX_COLUMN_HEADER, nameHeaderCtxScore/* +
                name_and_col_match_score + name_and_context_match_score*/);

        /*BOW OF out table context (from paragraphs etc)*/
        Collection<String> bow_of_outctx = createOutCtxBow(table, lemmatizer, stopWords);
        double fwDice = CollectionUtils.computeFrequencyWeightedDice(bow_of_entity, bow_of_outctx) * wt[3];
        scoreMap.put(TCellAnnotation.SCORE_OUT_CTX, fwDice);


        //todo: what does this do??
        Set<String> columnLabelBOW = new HashSet<>();
        if (preliminaryColumnLabel.size() > 0 && candidate.getTypes().size() > 0) {
            columnLabelBOW.clear();
            columnLabelBOW.addAll(preliminaryColumnLabel);
            Set<String> types_strings = new HashSet<>(candidate.getTypeIds());
/*            for (String[] type : candidate.getTypeIds())
                types_strings.add(type[0]);*/
            double score_type_match = CollectionUtils.computeDice(columnLabelBOW, types_strings);
            scoreMap.put(TCellAnnotation.SCORE_TYPE_MATCH, score_type_match);
        }
        //todo end

        /*NAME MATCH SCORE */
        String cellText = table.getContentCell(block.get(0), sourceColumnIndex).getText();
        Set<String> bow_of_cellText = new HashSet<>(StringUtils.toBagOfWords(cellText, true, true, TableMinerConstants.ENTITYBOW_DISCARD_SINGLE_CHAR));
        double en_score = CollectionUtils.computeDice(bow_of_cellText, bow_of_entityLabel);
        scoreMap.put(TCellAnnotation.SCORE_NAME_MATCH, Math.sqrt(en_score));
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
        Double nameMatch = scoreMap.get(TCellAnnotation.SCORE_NAME_MATCH);
        if (nameMatch != null)
            nm_score = nameMatch;

        sum = ctx_scores * weight_ctx + nm_score * weight_nm;

        scoreMap.put(TCellAnnotation.SCORE_FINAL, sum);
        return sum;
    }


    public static void score_typeMatch(Map<String, Double> scoreMap,
                                       List<String> assigned_column_types,
                                       Entity candidate) {
        List<String> bag_of_words_for_context = new ArrayList<String>();
        if (assigned_column_types.size() > 0 && candidate.getTypes().size() > 0) {
            bag_of_words_for_context.addAll(assigned_column_types);
            int original_size_header_types = bag_of_words_for_context.size();
            bag_of_words_for_context.retainAll(candidate.getTypeIds());
            if (bag_of_words_for_context.size() == 0)
                scoreMap.put(TCellAnnotation.SCORE_TYPE_MATCH, 0.0);
            else {
                double score_type_match = ((double) bag_of_words_for_context.size() / original_size_header_types
                        + (double) bag_of_words_for_context.size() / candidate.getTypes().size()) / 2.0;
                scoreMap.put(TCellAnnotation.SCORE_TYPE_MATCH, score_type_match);
            }
        } else {
            scoreMap.put(TCellAnnotation.SCORE_TYPE_MATCH, 0.0);
        }

    }

}
