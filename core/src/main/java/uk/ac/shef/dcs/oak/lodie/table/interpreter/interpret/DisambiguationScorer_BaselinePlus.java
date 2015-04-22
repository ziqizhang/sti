package uk.ac.shef.dcs.oak.lodie.table.interpreter.interpret;

import uk.ac.shef.dcs.oak.lodie.nlptools.Lemmatizer;
import uk.ac.shef.dcs.oak.lodie.nlptools.NLPTools;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.KB_InstanceFilter;
import uk.ac.shef.dcs.oak.lodie.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.lodie.test.TableMinerConstants;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.CollectionUtils;
import uk.ac.shef.dcs.oak.util.StringUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 30/05/14
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public class DisambiguationScorer_BaselinePlus implements DisambiguationScorer {

    private List<String> stopWords;
    private Lemmatizer lemmatizer;
    //private Levenshtein  levenshtein;
    //private Levenshtein stringSimilarityMetric;
    private AbstractStringMetric stringSimilarityMetric;

    /*
    context weights: 0-row context; 1-column context; 2-table context
     */
    public DisambiguationScorer_BaselinePlus(
            List<String> stopWords,
            double[] context_weights,
            String nlpResources) throws IOException {
        if (nlpResources != null)
            lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();

        this.stopWords = stopWords;
        //levenshtein=new Levenshtein();
        stringSimilarityMetric = new Levenshtein();
        //stringSimilarityMetric=new CosineSimilarity();
    }

    public Map<String, Double> score(EntityCandidate candidate,
                                     List<EntityCandidate> all_candidates,
                                     int entity_source_column,
                                     int entity_source_row,
                                     List<Integer> other_entity_source_rows,
                                     LTable table,
                                     Set<String> assigned_column_semantic_types,
                                     EntityCandidate... reference_disambiguated_entities) {
        /*if(candidate.getName().contains("Republican"))
            System.out.println();*/
        Map<String, Double> scoreMap = new HashMap<String, Double>();
        String headerText = table.getColumnHeader(entity_source_column).getHeaderText();

        /* BOW OF THE ENTITY*/
        List<String[]> facts = candidate.getFacts();
        List<String> bag_of_words_for_entity = new ArrayList<String>();
        for (String[] f : facts) {
            if (KB_InstanceFilter.ignoreFact_from_bow(f[0]))
                continue;
            String value = f[1];
            if (!StringUtils.isPath(value))
                bag_of_words_for_entity.addAll(StringUtils.toBagOfWords(value, true, true, TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
            else
                bag_of_words_for_entity.add(value);
        }
        if (lemmatizer != null)
            bag_of_words_for_entity = lemmatizer.lemmatize(bag_of_words_for_entity);
        bag_of_words_for_entity.removeAll(stopWords);

        /* BOW OF THE Row context*/
        double totalScore = 0.0;
        List<String> bag_of_words_for_context = new ArrayList<String>();
        //context from the row
        for (int row : other_entity_source_rows) {
            for (int col = 0; col < table.getNumCols(); col++) {
                if (col == entity_source_column || table.getColumnHeader(col).getTypes().get(0).equals(
                        DataTypeClassifier.DataType.ORDERED_NUMBER
                ))
                    continue;
                LTableContentCell tcc = table.getContentCell(row, col);
                bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true, TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
            }
            bag_of_words_for_context.addAll(StringUtils.toBagOfWords(   //also add the column header as the row context of this entity
                    headerText, true, true, TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
        }

        bag_of_words_for_context.addAll(StringUtils.toBagOfWords(   //also add the column header as the row context of this entity
                headerText, true, true, TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));

        if (lemmatizer != null)
            bag_of_words_for_context = lemmatizer.lemmatize(bag_of_words_for_context);
        bag_of_words_for_context.removeAll(stopWords);
        double contextOverlapScore = CollectionUtils.scoreCoverage_against_b(bag_of_words_for_entity, bag_of_words_for_context);
        //double contextOverlapScore = scoreOverlap(bag_of_words_for_entity, bag_of_words_for_context);
        scoreMap.put(CellAnnotation.SCORE_CTX_ROW, contextOverlapScore);


        /*BOW OF Column context*/
        /*bag_of_words_for_context.clear();
        for (int row = 0; row < table.getNumRows(); row++) {
            if (entity_source_rows.contains(row))
                continue;
            LTableContentCell tcc = table.getContentCell(row, entity_source_column);
            bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
        }
        if (lemmatizer != null)
            bag_of_words_for_context = lemmatizer.lemmatize(bag_of_words_for_context);
        bag_of_words_for_context.removeAll(stopWords);
        contextOverlapScore = CollectionUtils.scoreCoverage_against_b(bag_of_words_for_entity, bag_of_words_for_context);
        //contextOverlapScore = scoreOverlap(bag_of_words_for_entity, bag_of_words_for_context);
        scoreMap.put(CellAnnotation.SCORE_CTX_COLUMN, contextOverlapScore);*/


        /*NAME MATCH SCORE */
        String entity_name = candidate.getName();
        Set<String> bag_of_words_for_entity_name = new HashSet<String>(StringUtils.toBagOfWords(entity_name, true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));

        String cell_text = table.getContentCell(other_entity_source_rows.get(0), entity_source_column).getText();
        Set<String> bag_of_words_for_cell_text = new HashSet<String>(StringUtils.toBagOfWords(cell_text, true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
        double name_score = CollectionUtils.scoreOverlap_dice(bag_of_words_for_cell_text, bag_of_words_for_entity_name);
        Set<String> intersection = new HashSet<String>(bag_of_words_for_cell_text);
        intersection.retainAll(bag_of_words_for_entity_name);

        scoreMap.put(CellAnnotation.SCORE_NAME_MATCH, Math.sqrt(name_score));
        scoreMap.put("matched_name_tokens",(double)intersection.size());


        /*String cell_text = table.getContentCell(entity_source_rows.get(0), entity_source_column).getText();
        String entity_name = candidate.getName();
        //double stringSim = CollectionUtils.diceCoefficientOptimized(cell_text,entity_name);
        double stringSim = stringSimilarityMetric.getSimilarity(cell_text, entity_name);
        *//*double stringSim = stringSimilarityMetric.getSimilarity(
                StringUtils.toAlphaNumericWhitechar(cell_text),
                StringUtils.toAlphaNumericWhitechar(entity_name));
        *//*
        scoreMap.put(CellAnnotation.SCORE_NAME_MATCH, stringSim);*/

        return scoreMap;
    }

    public double compute_final_score(Map<String, Double> scoreMap, String cellTextOriginal) {
        double sum = 0.0;
        for (Map.Entry<String, Double> e : scoreMap.entrySet()) {
            if (e.getKey().startsWith("ctx_"))
                sum += e.getValue();
            if (e.getKey().equals(CellAnnotation.SCORE_NAME_MATCH))
                sum += e.getValue();
        }

        scoreMap.put(CellAnnotation.SCORE_FINAL, sum);
        return sum;
    }
}
