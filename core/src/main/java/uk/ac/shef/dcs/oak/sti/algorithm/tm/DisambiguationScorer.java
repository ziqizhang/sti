package uk.ac.shef.dcs.oak.sti.algorithm.tm;

import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.oak.sti.rep.LTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Disambiguate entity names in table
 */
public interface DisambiguationScorer {

    /**
     *
     * @param candidate candidate NE to be scored
     * @param all_candidates all candidate NEs matching the search
     * @param entity_source_column column id of the candidate NE
     * @param entity_source_row row id of the candidate NE
     * @param other_entity_source_rows other rows in this column where the text is the same as the cell identified by
     *                                 the row id and column id (to  be used under OSPD assumption)
     * @param table the table object
     * @param assigned_column_semantic_types the semantic concept currently assigned to the column
     * @param reference_disambiguated_entities if the relatedness between this NE and NEs from other cells in the same
     *                                         column should be computed, here is the list of NEs from other cells in the same column
     * @return a map where key= the name of a disambiguation score element; value=score
     */
    Map<String, Double> score(Entity candidate,
                              List<Entity> all_candidates,
                              int entity_source_column,
                              int entity_source_row,
                              List<Integer> other_entity_source_rows,
                              LTable table,
                              Set<String> assigned_column_semantic_types,
                              Entity... reference_disambiguated_entities);

    double compute_final_score(Map<String, Double> scoreMap, String cellTextOriginal);
}
