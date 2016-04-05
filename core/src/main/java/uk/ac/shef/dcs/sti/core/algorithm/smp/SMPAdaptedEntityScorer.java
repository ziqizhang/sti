package uk.ac.shef.dcs.sti.core.algorithm.smp;

import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.DiceSimilarity;
import uk.ac.shef.dcs.sti.util.CollectionUtils;
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
    public Map<String, Double> computeElementScores(Entity candidate,
                                                    List<Entity> all_candidates,
                                                    int sourceColumnIndex,
                                                    int sourceRowIndex,
                                                    List<Integer> otherRows,
                                                    Table table,
                                                    Entity... referenceEntities) {
        //entity index computeElementScores
        double indexScore = 1.0 / all_candidates.size();

        //lev between NE and cell text
        TCell cell = table.getContentCell(sourceRowIndex, sourceColumnIndex);
        double levScore = calculateStringSimilarity(cell.getText(), candidate, lev);
        //dice between NE and cell text
        double diceScore = calculateStringSimilarity(cell.getText(), candidate, dice);

        //column header and row values
         /* BOW OF THE ENTITY*/
        List<Attribute> facts = candidate.getAttributes();
        List<String> bag_of_words_for_entity = new ArrayList<String>();
        for (Attribute f : facts) {
            if (!STIConstantProperty.ENTITYBOW_INCLUDE_INDIRECT_ATTRIBUTE &&
                    !f.isDirect())
                continue;
            String value = f.getValue();
            if (!StringUtils.isPath(value))
                bag_of_words_for_entity.addAll(StringUtils.toBagOfWords(value, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
            else
                bag_of_words_for_entity.add(value);
        }
        if (lemmatizer != null)
            bag_of_words_for_entity = lemmatizer.lemmatize(bag_of_words_for_entity);
        bag_of_words_for_entity.removeAll(stopWords);
       /* BOW OF THE Row context*/
        //double totalScore = 0.0;
        String headerText = table.getColumnHeader(sourceColumnIndex).getHeaderText();
        List<String> bag_of_words_for_context = new ArrayList<String>();
        //context from the row

        for (int col = 0; col < table.getNumCols(); col++) {
            if (col == sourceColumnIndex || table.getColumnHeader(col).getTypes().get(0).equals(
                    DataTypeClassifier.DataType.ORDERED_NUMBER
            ))
                continue;
            TCell tcc = table.getContentCell(sourceRowIndex, col);
            bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
        }

        bag_of_words_for_context.addAll(StringUtils.toBagOfWords(   //also add the column header as the row context of this entity
                headerText, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));

        if (lemmatizer != null)
            bag_of_words_for_context = lemmatizer.lemmatize(bag_of_words_for_context);
        bag_of_words_for_context.removeAll(stopWords);
        double contextOverlapScore = CollectionUtils.computeCoverage(bag_of_words_for_entity, bag_of_words_for_context);

        Map<String, Double> score_elements = new HashMap<String, Double>();
        score_elements.put(SCORE_SMP_INDEX, indexScore);
        score_elements.put(SCORE_SMP_LEV, levScore);
        score_elements.put(SCORE_SMP_DICE, diceScore);
        score_elements.put(SCORE_SMP_CONTEXT, contextOverlapScore);
        return score_elements;
    }

    private double calculateStringSimilarity(String text, Entity candidate, AbstractStringMetric lev) {
        List<Attribute> facts = candidate.getAttributes();
        double baseScore = lev.getSimilarity(text, candidate.getLabel());
        double totalAliases = 1.0, totalScore = baseScore;

        for (Attribute f : facts) {
            if (f.getRelationURI().equalsIgnoreCase("/common/topic/alias")
                    && f.isDirect()) {
                String v = f.getValue().trim();
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
