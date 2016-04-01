package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.rep.Attribute;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.TCell;
import uk.ac.shef.dcs.sti.rep.TCellAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;

import java.util.*;

/**
 */
public class TCellDisambiguator {

    private KBSearch kbSearch;
    private EntityScorer disambScorer;
    private static Logger LOG = Logger.getLogger(TCellDisambiguator.class.getName());

    public TCellDisambiguator(KBSearch kbSearch, EntityScorer disambScorer) {
        this.kbSearch = kbSearch;
        this.disambScorer = disambScorer;
    }

    public List<Pair<Entity, Map<String, Double>>> coldstartDisambiguate(List<Entity> candidates, Table table,
                                                                         List<Integer> entity_rows, int entity_column
    ) throws KBSearchException {
        //do disambiguation scoring
        //LOG.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        TCell sample_tcc = table.getContentCell(entity_rows.get(0), entity_column);
        LOG.info("\t>> coldstart disamb, candidates=" + candidates.size());
        List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();
        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getAttributes() == null || c.getAttributes().size() == 0) {
                List<Attribute> attributes = kbSearch.findAttributesOfEntities(c);
                c.setAttributes(attributes);
            }
            Map<String, Double> scoreMap = disambScorer.
                    computeElementScores(c, candidates,
                            entity_column,
                            entity_rows.get(0),
                            entity_rows, table, new HashSet<>());
            disambScorer.computeFinal(scoreMap, sample_tcc.getText());
            Pair<Entity, Map<String, Double>> entry = new Pair<>(c, scoreMap);
            disambiguationScores.add(entry);
        }
        return disambiguationScores;
    }

    //reselect winning entity for this cell ensuring its types is contained in the winning clazz for the column
    public List<Pair<Entity, Map<String, Double>>> preliminaryDisambiguate(
            List<Pair<Entity, Map<String, Double>>> entitiesAndScores,
            List<String> winningClazzIdsForColumn) {
        List<Integer> removeIndex = new ArrayList<>();
        Iterator<Pair<Entity, Map<String, Double>>> it = entitiesAndScores.iterator();
        int index = 0;
        while (it.hasNext()) {
            Pair<Entity, Map<String, Double>> entity_to_scoreMap = it.next();
            int overlap=CollectionUtils.intersection(entity_to_scoreMap.getKey().getTypeIds(),
                    winningClazzIdsForColumn).size();
            if (overlap == 0)
                removeIndex.add(index);
            index++;
        }
        List<Pair<Entity, Map<String, Double>>> result = new ArrayList<>();
        if (removeIndex.size() < entitiesAndScores.size()) {
            for (int i = 0; i < entitiesAndScores.size(); i++) {
                if (removeIndex.contains(i))
                    continue;
                result.add(entitiesAndScores.get(i));
            }
        }
        return result;
    }

    public List<Pair<Entity, Map<String, Double>>> disambiguate_learn_consolidate(
            List<Entity> candidates,
            Table table,
            List<Integer> entity_rows,
            int entity_column,
            Set<String> assigned_column_types,
            boolean first_phase,
            Entity... reference_disambiguated_entities
    ) throws KBSearchException {
        //do disambiguation scoring
        //LOG.info("\t>> Disambiguation-UPDATE , position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        TCell sample_tcc = table.getContentCell(entity_rows.get(0), entity_column);
        if (first_phase)
            System.out.println("\t>> Disambiguation-LEARN(consolidate) , position at (" + entity_rows + "," + entity_column + ") " + sample_tcc + " candidates=" + candidates.size());
        else
            System.out.println("\t>> Disambiguation-UPDATE, position at (" + entity_rows + "," + entity_column + ") " + sample_tcc + " (candidates)-" + candidates.size());
        List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();

        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getAttributes() == null || c.getAttributes().size() == 0) {
                List<Attribute> facts = kbSearch.findAttributesOfEntities(c);
                c.setAttributes(facts);
            }
            Map<String, Double> scoreMap = disambScorer.
                    computeElementScores(c, candidates,
                            entity_column,
                            entity_rows.get(0),
                            entity_rows,
                            table,
                            assigned_column_types,
                            reference_disambiguated_entities);
            disambScorer.computeFinal(scoreMap, sample_tcc.getText());
            Pair<Entity, Map<String, Double>> entry = new Pair<>(c, scoreMap);
            disambiguationScores.add(entry);
        }
        return disambiguationScores;
    }

}
