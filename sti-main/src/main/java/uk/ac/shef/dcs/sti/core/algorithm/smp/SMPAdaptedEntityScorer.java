package uk.ac.shef.dcs.sti.core.algorithm.smp;

import org.simmetrics.Metric;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.KBDefinition;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * An adapted version of the NE ranker (scorer) used in Semantic Message Passing
 */
public class SMPAdaptedEntityScorer implements EntityScorer {
  private static final String SMP_SCORE_INDEX = "smp_index";
  private static final String SMP_SCORE_LEV = "smp_stringsim_lev";
  private static final String SMP_SCORE_DICE = "smp_stringsim_dice";
  private static final String SMP_SCORE_CONTEXT = "smp_context";

  private StringMetric lev;
  private StringMetric dice;
  private List<String> stopWords;
  private Lemmatizer lemmatizer;
  private KBDefinition kbDefinition;

  public SMPAdaptedEntityScorer(List<String> stopWords,
                                String nlpResources, KBDefinition kbDefinition) throws IOException {
    lev = StringMetrics.levenshtein();
    dice = StringMetrics.dice();
    if (nlpResources != null)
      lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();

    this.stopWords = stopWords;
    this.kbDefinition = kbDefinition;
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
    List<Attribute> attributes = candidate.getAttributes();
    List<String> entityBoW = new ArrayList<>();
    for (Attribute f : attributes) {
      if (!STIConstantProperty.BOW_ENTITY_INCLUDE_INDIRECT_ATTRIBUTE &&
              !f.isDirect())
        continue;
      String value = f.getValue();
      if (!StringUtils.isPath(value))
        entityBoW.addAll(StringUtils.toBagOfWords(value, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
      else
        entityBoW.add(value);
    }
    if (lemmatizer != null)
      entityBoW = lemmatizer.lemmatize(entityBoW);
    entityBoW.removeAll(stopWords);
       /* BOW OF THE Row context*/
    //double totalScore = 0.0;
    String headerText = table.getColumnHeader(sourceColumnIndex).getHeaderText();
    List<String> contextBoW = new ArrayList<>();
    //context from the row

    for (int col = 0; col < table.getNumCols(); col++) {
      if (col == sourceColumnIndex || table.getColumnHeader(col).getTypes().get(0).getType().equals(
              DataTypeClassifier.DataType.ORDERED_NUMBER
      ))
        continue;
      TCell tcc = table.getContentCell(sourceRowIndex, col);
      contextBoW.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
    }

    contextBoW.addAll(StringUtils.toBagOfWords(   //also add the column header as the row context of this entity
            headerText, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));

    if (lemmatizer != null)
      contextBoW = lemmatizer.lemmatize(contextBoW);
    contextBoW.removeAll(stopWords);
    double contextOverlapScore = CollectionUtils.computeCoverage(entityBoW, contextBoW);

    Map<String, Double> score_elements = new HashMap<>();
    score_elements.put(SMP_SCORE_INDEX, indexScore);
    score_elements.put(SMP_SCORE_LEV, levScore);
    score_elements.put(SMP_SCORE_DICE, diceScore);
    score_elements.put(SMP_SCORE_CONTEXT, contextOverlapScore);
    return score_elements;
  }

  private double calculateStringSimilarity(String text, Entity candidate, Metric<String> lev) {
    String normText = StringUtils.toAlphaNumericWhitechar(text);
    double totalAliases = 1.0,
            totalScore = (double) lev.compare(
                    normText,
                    StringUtils.toAlphaNumericWhitechar(candidate.getLabel()));

    for (String alias : candidate.getAliases(kbDefinition)) {
      alias = StringUtils.toAlphaNumericWhitechar(alias);
      if (alias.length() > 0) {
        double score = lev.compare(normText, alias);
        totalScore += score;
        totalAliases += 1.0;
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
