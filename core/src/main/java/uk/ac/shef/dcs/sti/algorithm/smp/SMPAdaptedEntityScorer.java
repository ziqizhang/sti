package uk.ac.shef.dcs.sti.algorithm.smp;

import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearchResultFilter;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.algorithm.tm.EntityScorer;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.rep.TCellAnnotation;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.sti.rep.TContentCell;
import uk.ac.shef.dcs.sti.util.DiceSimilarity;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.IOException;
import java.util.*;

/**
 * An adapted version of the NE ranker (scorer) used in Semantic Message Passing
 */
public class SMPAdaptedEntityScorer implements EntityScorer {
    public static String SCORE_SMP_INDEX = "smp_index";
    public static String SCORE_SMP_LEV = "smp_stringsim_lev";
    public static String SCORE_SMP_DICE = "smp_stringsim_dice";
    public static String SCORE_SMP_CONTEXT = "smp_context";

    private AbstractStringMetric lev;
    private AbstractStringMetric dice;
    private List<String> stopWords;
    private Lemmatizer lemmatizer;

    public SMPAdaptedEntityScorer(List<String> stopWords,
                                  String nlpResources) throws IOException {
        lev = new Levenshtein();
        dice = new DiceSimilarity();
        if (nlpResources != null)
            lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();

        this.stopWords = stopWords;
    }

    @Override
    public Map<String, Double> score(Entity candidate,
                                     List<Entity> all_candidates,
                                     int entity_source_column,
                                     int entity_source_row,
                                     List<Integer> entity_source_rows,
                                     Table table,
                                     Set<String> assigned_column_semantic_types, Entity... reference_disambiguated_entities) {
        //entity index score
        double indexScore = 1.0 / all_candidates.size();

        //lev between NE and cell text
        TContentCell cell = table.getContentCell(entity_source_row, entity_source_column);
        double levScore = calculateStringSimilarity(cell.getText(), candidate, lev);
        //dice between NE and cell text
        double diceScore = calculateStringSimilarity(cell.getText(), candidate, dice);

        //column header and row values
         /* BOW OF THE ENTITY*/
        List<String[]> facts = candidate.getTriples();
        List<String> bag_of_words_for_entity = new ArrayList<String>();
        for (String[] f : facts) {
            if (!TableMinerConstants.USE_NESTED_RELATION_AND_FACTS_FOR_ENTITY_FEATURE && f[3].equals("y"))
                continue;
            if (FreebaseSearchResultFilter.ignoreFactFromBOW(f[0]))
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
        //double totalScore = 0.0;
        String headerText = table.getColumnHeader(entity_source_column).getHeaderText();
        List<String> bag_of_words_for_context = new ArrayList<String>();
        //context from the row

        for (int col = 0; col < table.getNumCols(); col++) {
            if (col == entity_source_column || table.getColumnHeader(col).getTypes().get(0).equals(
                    DataTypeClassifier.DataType.ORDERED_NUMBER
            ))
                continue;
            TContentCell tcc = table.getContentCell(entity_source_row, col);
            bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true, TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
        }

        bag_of_words_for_context.addAll(StringUtils.toBagOfWords(   //also add the column header as the row context of this entity
                headerText, true, true, TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));

        if (lemmatizer != null)
            bag_of_words_for_context = lemmatizer.lemmatize(bag_of_words_for_context);
        bag_of_words_for_context.removeAll(stopWords);
        double contextOverlapScore = CollectionUtils.scoreCoverage_against_b(bag_of_words_for_entity, bag_of_words_for_context);

        Map<String, Double> score_elements = new HashMap<String, Double>();
        score_elements.put(SCORE_SMP_INDEX, indexScore);
        score_elements.put(SCORE_SMP_LEV, levScore);
        score_elements.put(SCORE_SMP_DICE, diceScore);
        score_elements.put(SCORE_SMP_CONTEXT, contextOverlapScore);
        return score_elements;
    }

    private double calculateStringSimilarity(String text, Entity candidate, AbstractStringMetric lev) {
        List<String[]> facts = candidate.getTriples();
        double baseScore = lev.getSimilarity(text, candidate.getLabel());
        double totalAliases = 1.0, totalScore = baseScore;

        for (String[] f : facts) {
            if (f[0].equalsIgnoreCase("/common/topic/alias") && f[f.length - 1].equalsIgnoreCase("n")) {
                String v = f[1].trim();
                if (v.length() > 0) {
                    double score = lev.getSimilarity(text, v);
                    totalScore += score;
                    totalAliases += 1.0;
                }
            }
        }
        return totalScore / totalAliases;
    }

    @Override
    public double computeFinal(Map<String, Double> scoreMap, String cellTextOriginal) {
        double total = 0.0;
        for (Map.Entry<String, Double> e : scoreMap.entrySet()) {
            total += e.getValue();
        }
        scoreMap.put(TCellAnnotation.SCORE_FINAL, total);
        return total;
    }
}
