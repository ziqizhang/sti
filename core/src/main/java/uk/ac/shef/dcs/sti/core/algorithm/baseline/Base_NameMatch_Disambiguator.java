package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 */
public class Base_NameMatch_Disambiguator {

    public List<Pair<Entity, Map<String, Double>>> disambiguate(List<Entity> candidates, Table table,
                                                                           int entity_row, int entity_column
    )  {
        //do disambiguation scoring
        //LOG.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        System.out.println("\t>> Disambiguation-, position at [" + entity_row + "," + entity_column + "]: " + table.getContentCell(entity_row, entity_column) +
                " candidates=" + candidates.size());
        List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();
        if (candidates.size() > 0) {
            List<Entity> candidatesCopy = new ArrayList<>();
            for (Entity ec : candidates) {
                TCell tcc = table.getContentCell(entity_row, entity_column);
                if (tcc.getText() != null) {
                    if (ec.getLabel().equalsIgnoreCase(tcc.getText().trim()))
                        candidatesCopy.add(ec);
                }
            }

            if (candidatesCopy.size() > 0) {

                disambiguationScores.add(new Pair<>(
                        candidatesCopy.get(0), new HashMap<>()
                ));
            } else {
                disambiguationScores.add(new Pair<>(
                        candidates.get(0), new HashMap<>()
                ));
            }

        }
        return disambiguationScores;
    }

    public List<Pair<Entity, Map<String, Double>>> revise(List<Pair<Entity, Map<String, Double>>> entities_for_this_cell_and_scores,
                                                                     List<String> types) {

        Iterator<Pair<Entity, Map<String, Double>>> it = entities_for_this_cell_and_scores.iterator();
        List<Pair<Entity, Map<String, Double>>> original = new ArrayList<>(
                entities_for_this_cell_and_scores
        );

        while (it.hasNext()) {
            Pair<Entity, Map<String, Double>> oo = it.next();
            Set<String> entity_types = new HashSet<>(oo.getKey().getTypeIds());
            entity_types.retainAll(types);
            if (entity_types.size() == 0)
                it.remove();
            /*double pre_final = oo.getOtherObject().get("final");
            oo.getOtherObject().put("final", type_match_score + pre_final);*/
        }

        if (entities_for_this_cell_and_scores.size() == 0)
            return original;

        return entities_for_this_cell_and_scores;
        //To change body of created methods use File | Settings | File Templates.
    }
}
