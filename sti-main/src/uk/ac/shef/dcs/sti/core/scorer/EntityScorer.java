package uk.ac.shef.dcs.sti.core.scorer;

import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 * Disambiguate entity names in table
 */
public interface EntityScorer {

    /**
     * @param candidate         candidate NE to be scored
     * @param allCandidates     all candidate NEs matching the search
     * @param sourceColumnIndex column id of the candidate NE
     * @param sourceRowIndex    row id of the candidate NE
     * @param block             rows in this column where the text is the same as the cell identified by
     *                          the row id and column id (including the row-in-question)
     * @param table             the table object
     * @param referenceEntities if the relatedness between this NE and NEs from other cells in the same
     *                          column should be computed, here is the list of NEs from other cells in the same column
     * @return a map where key= the name of a disambiguation computeElementScores element; value=computeElementScores
     */
    Map<String, Double> computeElementScores(Entity candidate,
                                             List<Entity> allCandidates,
                                             int sourceColumnIndex,
                                             int sourceRowIndex,
                                             List<Integer> block,
                                             Table table,
                                             Entity... referenceEntities);

    double computeFinal(Map<String, Double> scoreMap, String cellTextOriginal);

}
