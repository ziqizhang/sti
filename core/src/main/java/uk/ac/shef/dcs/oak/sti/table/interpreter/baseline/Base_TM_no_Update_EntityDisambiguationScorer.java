package uk.ac.shef.dcs.oak.sti.table.interpreter.baseline;

import uk.ac.shef.dcs.oak.kbsearch.Entity;
import uk.ac.shef.dcs.oak.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.oak.sti.nlp.NLPTools;
import uk.ac.shef.dcs.oak.sti.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.table.interpreter.misc.KB_InstanceFilter;
import uk.ac.shef.dcs.oak.sti.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.sti.table.rep.LTable;
import uk.ac.shef.dcs.oak.sti.table.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.sti.test.TableMinerConstants;
import uk.ac.shef.dcs.oak.util.CollectionUtils;
import uk.ac.shef.dcs.oak.util.StringUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 11/03/14
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class Base_TM_no_Update_EntityDisambiguationScorer {
    private List<String> stopWords;
    private Lemmatizer lemmatizer;
    //private Levenshtein  levenshtein;
    //private Levenshtein stringSimilarityMetric;
    private AbstractStringMetric stringSimilarityMetric;

    /*
    context weights: 0-row context; 1-column context; 2-table context
     */
    public Base_TM_no_Update_EntityDisambiguationScorer(
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

    public Map<String, Double> score(Entity candidate,
                                     List<Entity> other_candidates_returned,
                                     int entity_source_column,
                                     int entity_source_row,
                                     LTable table,
                                     Set<String> assigned_column_types,
                                     Entity... reference_disambiguated_entities) {
        /*if(candidate.getName().contains("Republican"))
            System.out.println();*/
        Map<String, Double> scoreMap = new HashMap<String, Double>();
        String headerText =table.getColumnHeader(entity_source_column).getHeaderText();

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
        for (int col = 0; col < table.getNumCols(); col++) {
            if (col == entity_source_column || table.getColumnHeader(col).getTypes().get(0).equals(
                    DataTypeClassifier.DataType.ORDERED_NUMBER
            ))
                continue;
            LTableContentCell tcc = table.getContentCell(entity_source_row, col);
            bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
        }
        bag_of_words_for_context.addAll(StringUtils.toBagOfWords(   //also add the column header as the row context of this entity
                headerText, true, true,TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
        if (lemmatizer != null)
            bag_of_words_for_context = lemmatizer.lemmatize(bag_of_words_for_context);
        bag_of_words_for_context.removeAll(stopWords);
        double contextOverlapScore = CollectionUtils.scoreCoverage_against_b(bag_of_words_for_entity, bag_of_words_for_context);
        //double contextOverlapScore = scoreOverlap(bag_of_words_for_entity, bag_of_words_for_context);
        scoreMap.put(CellAnnotation.SCORE_CTX_ROW, contextOverlapScore);



        /* TYPE MATCH SCORE */
        /*if (assigned_column_types.size() > 0 && candidate.getTypes().size() > 0) {
            bag_of_words_for_context.clear();
            bag_of_words_for_context.addAll(assigned_column_types);
            int original_size_header_types = bag_of_words_for_context.size();
            bag_of_words_for_context.retainAll(candidate.getTypes());
            if (bag_of_words_for_context.size() == 0)
                scoreMap.put("type_match", 0.0);
            else {
                double score_type_match = ((double) bag_of_words_for_context.size() / original_size_header_types
                        + (double) bag_of_words_for_context.size() / candidate.getTypes().size()) / 2.0;
                scoreMap.put("type_match", score_type_match);
            }
        }*/
        if (assigned_column_types.size() > 0 && candidate.getTypes().size() > 0) {
            bag_of_words_for_context.clear();
            bag_of_words_for_context.addAll(assigned_column_types);
            Set<String> types_strings = new HashSet<String>();
            for(String[] type: candidate.getTypes())
                types_strings.add(type[0]);
            double score_type_match = CollectionUtils.scoreOverlap_dice(bag_of_words_for_context, types_strings);
            scoreMap.put(CellAnnotation.SCORE_TYPE_MATCH, score_type_match);
        }

        /*NAME MATCH SCORE */
        /*String cell_text = table.getContentCell(entity_source_row, entity_source_column).getText();
        String entity_name = candidate.getName();
        Set<String> bag_of_words_for_cell_text = new HashSet<String>(StringUtils.toBagOfWords(cell_text, true, true));
        Set<String> bag_of_words_for_entity_name = new HashSet<String>(StringUtils.toBagOfWords(entity_name, true, true));
        Set<String> intersection = new HashSet<String>(bag_of_words_for_cell_text);
        intersection.retainAll(bag_of_words_for_entity_name);
        double name_score = ((double) intersection.size() / bag_of_words_for_cell_text.size() + (double) intersection.size() / bag_of_words_for_entity_name.size()) / 2.0;
        */
        String cell_text = table.getContentCell(entity_source_row, entity_source_column).getText();
        String entity_name = candidate.getName();
        //double stringSim = CollectionUtils.diceCoefficientOptimized(cell_text,entity_name);
        double stringSim = stringSimilarityMetric.getSimilarity(cell_text,entity_name);
        /*double stringSim = stringSimilarityMetric.getSimilarity(
                StringUtils.toAlphaNumericWhitechar(cell_text),
                StringUtils.toAlphaNumericWhitechar(entity_name));
        */scoreMap.put(CellAnnotation.SCORE_NAME_MATCH, stringSim);

        return scoreMap;
    }

    public static double compute_final_score(Map<String, Double> scoreMap) {
        double sum = 0.0;
        for (Map.Entry<String, Double> e : scoreMap.entrySet()) {
            if (e.getKey().startsWith("ctx_"))
                sum += e.getValue();
            if(e.getKey().equals(CellAnnotation.SCORE_NAME_MATCH))
                sum+=e.getValue();
        }

        scoreMap.put(CellAnnotation.SCORE_FINAL, sum);
        return sum;
    }

}
