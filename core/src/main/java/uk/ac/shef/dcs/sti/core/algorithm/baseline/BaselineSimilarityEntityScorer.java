package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import org.simmetrics.Metric;
import org.simmetrics.StringMetric;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPEntityScorer;
import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 11/03/14
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class BaselineSimilarityEntityScorer implements EntityScorer {
    private static final String SCORE_NAME_MATCH = "name_string_similarity";
    private List<String> stopWords;
    private Lemmatizer lemmatizer;
    private StringMetric stringSimilarityMetric;

    /*
    context weights: 0-row context; 1-column context; 2-table context
     */
    public BaselineSimilarityEntityScorer(
            List<String> stopWords,
            String nlpResources,
            StringMetric stringSimilarityMetric) throws IOException {
        if (nlpResources != null)
            lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();

        this.stopWords = stopWords;
        this.stringSimilarityMetric=stringSimilarityMetric;
    }


    public Map<String, Double> computeElementScores(Entity candidate,
                                     List<Entity> allCandidates,
                                     int entity_source_column,
                                     int entity_source_row,
                                     List<Integer> block,
                                                    Table table,
                                     Entity... reference_disambiguated_entities) {
        Map<String, Double> scoreMap = new HashMap<>();
        String headerText =table.getColumnHeader(entity_source_column).getHeaderText();
        if(block==null||block.size()==0)
            block=new ArrayList<>();
        if(!block.contains(entity_source_row))
            block.add(entity_source_row);

        /* BOW OF THE ENTITY*/
        List<Attribute> attributes = candidate.getAttributes();
        List<String> entityBoW = new ArrayList<>();
        for (Attribute f : attributes) {
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
        List<String> rowContextBoW = new ArrayList<>();
        for (int row : block) {
            for (int col = 0; col < table.getNumCols(); col++) {
                if (col == entity_source_column || table.getColumnHeader(col).getTypes().get(0).getType().equals(
                        DataTypeClassifier.DataType.ORDERED_NUMBER
                ))
                    continue;
                TCell tcc = table.getContentCell(row, col);
                rowContextBoW.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
            }
            rowContextBoW.addAll(StringUtils.toBagOfWords(
                    headerText, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
        }
        if (lemmatizer != null)
            rowContextBoW = lemmatizer.lemmatize(rowContextBoW);
        rowContextBoW.removeAll(stopWords);
        /*rowContextBoW.addAll(StringUtils.toBagOfWords(   //also add the column header as the row context of this entity
                headerText, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
        */
        double contextOverlapScore = CollectionUtils.computeCoverage(entityBoW, rowContextBoW);
        scoreMap.put(TMPEntityScorer.SCORE_IN_CTX_ROW, contextOverlapScore);

        String cell_text = table.getContentCell(entity_source_row, entity_source_column).getText();
        cell_text=StringUtils.toAlphaNumericWhitechar(cell_text);
        String entity_name = candidate.getLabel();
        entity_name=StringUtils.toAlphaNumericWhitechar(entity_name);
        //double stringSim = CollectionUtils.diceCoefficientOptimized(cell_text,entity_name);
        double stringSim = stringSimilarityMetric.compare(cell_text, entity_name);
        scoreMap.put(SCORE_NAME_MATCH, stringSim);

        return scoreMap;
    }


    @Override
    public double computeFinal(Map<String, Double> scoreMap, String cellTextOriginal) {
        double sum = 0.0;
        for (Map.Entry<String, Double> e : scoreMap.entrySet()) {
            if (e.getKey().startsWith("ctx_"))
                sum += e.getValue();
            if(e.getKey().equals(SCORE_NAME_MATCH))
                sum+=e.getValue();
        }

        scoreMap.put(TCellAnnotation.SCORE_FINAL, sum);
        return sum;
    }
}
