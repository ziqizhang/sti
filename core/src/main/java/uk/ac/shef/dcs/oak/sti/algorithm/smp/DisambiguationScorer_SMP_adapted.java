package uk.ac.shef.dcs.oak.sti.algorithm.smp;

import uk.ac.shef.dcs.oak.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.oak.sti.nlp.NLPTools;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.DisambiguationScorer;
import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.misc.KB_InstanceFilter;
import uk.ac.shef.dcs.oak.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.sti.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.sti.util.DiceSimilarity;
import uk.ac.shef.dcs.oak.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.CollectionUtils;
import uk.ac.shef.dcs.oak.util.StringUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.IOException;
import java.util.*;

/**
 * An adapted version of the NE ranker (scorer) used in Semantic Message Passing
 */
public class DisambiguationScorer_SMP_adapted implements DisambiguationScorer {
    public static String SCORE_SMP_INDEX = "smp_index";
    public static String SCORE_SMP_LEV = "smp_stringsim_lev";
    public static String SCORE_SMP_DICE = "smp_stringsim_dice";
    public static String SCORE_SMP_CONTEXT = "smp_context";

    private AbstractStringMetric lev;
    private AbstractStringMetric dice;
    private List<String> stopWords;
    private Lemmatizer lemmatizer;

    public DisambiguationScorer_SMP_adapted(List<String> stopWords,
                                            String nlpResources) throws IOException {
        lev = new Levenshtein();
        dice = new DiceSimilarity();
        if (nlpResources != null)
            lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();

        this.stopWords = stopWords;
    }

    @Override
    public Map<String, Double> score(EntityCandidate candidate,
                                     List<EntityCandidate> all_candidates,
                                     int entity_source_column,
                                     int entity_source_row,
                                     List<Integer> entity_source_rows,
                                     LTable table,
                                     Set<String> assigned_column_semantic_types, EntityCandidate... reference_disambiguated_entities) {
        //entity index score
        double indexScore = 1.0 / all_candidates.size();

        //lev between NE and cell text
        LTableContentCell cell = table.getContentCell(entity_source_row, entity_source_column);
        double levScore = calculateStringSimilarity(cell.getText(), candidate, lev);
        //dice between NE and cell text
        double diceScore = calculateStringSimilarity(cell.getText(), candidate, dice);

        //column header and row values
         /* BOW OF THE ENTITY*/
        List<String[]> facts = candidate.getFacts();
        List<String> bag_of_words_for_entity = new ArrayList<String>();
        for (String[] f : facts) {
            if (!TableMinerConstants.USE_NESTED_RELATION_AND_FACTS_FOR_ENTITY_FEATURE && f[3].equals("y"))
                continue;
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
        //double totalScore = 0.0;
        String headerText = table.getColumnHeader(entity_source_column).getHeaderText();
        List<String> bag_of_words_for_context = new ArrayList<String>();
        //context from the row

        for (int col = 0; col < table.getNumCols(); col++) {
            if (col == entity_source_column || table.getColumnHeader(col).getTypes().get(0).equals(
                    DataTypeClassifier.DataType.ORDERED_NUMBER
            ))
                continue;
            LTableContentCell tcc = table.getContentCell(entity_source_row, col);
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

    private double calculateStringSimilarity(String text, EntityCandidate candidate, AbstractStringMetric lev) {
        List<String[]> facts = candidate.getFacts();
        double baseScore = lev.getSimilarity(text, candidate.getName());
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
    public double compute_final_score(Map<String, Double> scoreMap, String cellTextOriginal) {
        double total = 0.0;
        for (Map.Entry<String, Double> e : scoreMap.entrySet()) {
            total += e.getValue();
        }
        scoreMap.put(CellAnnotation.SCORE_FINAL, total);
        return total;
    }
}
