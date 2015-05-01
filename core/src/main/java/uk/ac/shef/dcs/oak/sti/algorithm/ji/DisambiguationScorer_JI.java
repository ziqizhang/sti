package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * todo
 */
public class DisambiguationScorer_JI implements uk.ac.shef.dcs.oak.sti.algorithm.tm.DisambiguationScorer{
    public static String SCORE_CELL_FACTOR ="score_factor_graph-cell";

    @Override
    public Map<String, Double> score(EntityCandidate candidate, List<EntityCandidate> all_candidates, int entity_source_column, int entity_source_row, List<Integer> other_entity_source_rows, LTable table, Set<String> assigned_column_semantic_types, EntityCandidate... reference_disambiguated_entities) {
        return null;
    }

    @Override
    public double compute_final_score(Map<String, Double> scoreMap, String cellTextOriginal) {
        return 0;
    }
}
