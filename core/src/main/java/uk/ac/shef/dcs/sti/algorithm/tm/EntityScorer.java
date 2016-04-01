package uk.ac.shef.dcs.sti.algorithm.tm;

import uk.ac.shef.dcs.kbsearch.rep.Attribute;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.rep.TCell;
import uk.ac.shef.dcs.sti.rep.TContext;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.util.StringUtils;

import java.util.*;

/**
 * Disambiguate entity names in table
 */
public abstract class EntityScorer {

    /**
     * @param candidate              candidate NE to be scored
     * @param allCandidates          all candidate NEs matching the search
     * @param sourceColumnIndex      column id of the candidate NE
     * @param sourceRowIndex         row id of the candidate NE
     * @param block                  rows in this column where the text is the same as the cell identified by
     *                               the row id and column id (including the row-in-question)
     * @param table                  the table object
     * @param referenceEntities      if the relatedness between this NE and NEs from other cells in the same
     *                               column should be computed, here is the list of NEs from other cells in the same column
     * @return a map where key= the name of a disambiguation computeElementScores element; value=computeElementScores
     */
    public abstract Map<String, Double> computeElementScores(Entity candidate,
                                                             List<Entity> allCandidates,
                                                             int sourceColumnIndex,
                                                             int sourceRowIndex,
                                                             List<Integer> block,
                                                             Table table,
                                                             Entity... referenceEntities);

    public abstract double computeFinal(Map<String, Double> scoreMap, String cellTextOriginal);


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
            if (!TableMinerConstants.ENTITYBOW_INCLUDE_INDIRECT_ATTRIBUTE
                    && !f.isDirect())
                continue;
            String value = f.getValue();
            if (!StringUtils.isPath(value))
                bow_of_entity.addAll(StringUtils.toBagOfWords(value, true, true, TableMinerConstants.BOW_DISCARD_SINGLE_CHAR));
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
                bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true, TableMinerConstants.BOW_DISCARD_SINGLE_CHAR));
            }
        }
        bag_of_words_for_context.addAll(StringUtils.toBagOfWords(   //also add the column header as the row context of this entity
                columnHeaderText, true, true, TableMinerConstants.BOW_DISCARD_SINGLE_CHAR));

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
            bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tcc.getText(), true, true,TableMinerConstants.BOW_DISCARD_SINGLE_CHAR));
        }
        return normalize(bag_of_words_for_context, lemmatizer, stopWords);
    }


    protected Collection<String> createOutCtxBow(
                                                 Table table,
                                                 Lemmatizer lemmatizer, Collection<String> stopWords){
        /*BOW OF table table context (from paragraphs etc)*/
        List<String> bag_of_words_for_context = new ArrayList<>();
        for (TContext tc : table.getContexts()) {
            bag_of_words_for_context.addAll(StringUtils.toBagOfWords(tc.getText(), true, true,TableMinerConstants.BOW_DISCARD_SINGLE_CHAR));
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
