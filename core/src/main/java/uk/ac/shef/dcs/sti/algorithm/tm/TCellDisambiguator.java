package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.rep.Attribute;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.TContentCell;
import uk.ac.shef.dcs.sti.rep.Table;

import java.io.IOException;
import java.util.*;

/**
 */
public class TCellDisambiguator {

    private KBSearch kbSearch;
    private EntityScorer disambScorer;
    //private static Logger LOG = Logger.getLogger(TCellDisambiguator.class.getName());

    public TCellDisambiguator(KBSearch kbSearch, EntityScorer disambScorer) {
        this.kbSearch = kbSearch;
        this.disambScorer = disambScorer;
    }

    public List<Pair<Entity, Map<String, Double>>> disambiguate_learn_seeding(List<Entity> candidates, Table table,
                                                                                         List<Integer> entity_rows, int entity_column
    ) throws IOException {
        //do disambiguation scoring
        //LOG.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        TContentCell sample_tcc = table.getContentCell(entity_rows.get(0), entity_column);
        System.out.println("\t>> Disambiguation-LEARN(seeding), position at (" + entity_rows + "," + entity_column + ") "+
                sample_tcc+" candidates=" + candidates.size());
        List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();
        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getAttributes() == null || c.getAttributes().size() == 0) {
                List<Attribute> facts = kbSearch.findAttributesOfEntities(c);
                c.setAttributes(facts);
            }
            Map<String, Double> scoreMap = disambScorer.
                    score(c, candidates,
                            entity_column,
                            entity_rows.get(0),
                            entity_rows, table, new HashSet<String>());
            disambScorer.computeFinal(scoreMap, sample_tcc.getText());
            Pair<Entity, Map<String, Double>> entry = new Pair<>(c,scoreMap);
            disambiguationScores.add(entry);
        }
        return disambiguationScores;
    }


    public List<Pair<Entity, Map<String, Double>>> disambiguate_learn_consolidate(
            List<Entity> candidates,
            Table table,
            List<Integer> entity_rows,
            int entity_column,
            Set<String> assigned_column_types,
            boolean first_phase,
            Entity... reference_disambiguated_entities
    ) throws IOException {
        //do disambiguation scoring
        //LOG.info("\t>> Disambiguation-UPDATE , position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        TContentCell sample_tcc = table.getContentCell(entity_rows.get(0), entity_column);
        if(first_phase)
            System.out.println("\t>> Disambiguation-LEARN(consolidate) , position at (" + entity_rows + "," + entity_column + ") "+sample_tcc+" candidates=" + candidates.size());
        else
            System.out.println("\t>> Disambiguation-UPDATE, position at (" + entity_rows + "," + entity_column + ") "+sample_tcc+" (candidates)-" + candidates.size());
        List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();

        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getAttributes() == null || c.getAttributes().size() == 0) {
                List<Attribute> facts = kbSearch.findAttributesOfEntities(c);
                c.setAttributes(facts);
            }
            Map<String, Double> scoreMap = disambScorer.
                    score(c,candidates,
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


    //used with disambiguation results obtained from the first iteration of classification and disambiguation
    public List<Pair<Entity, Map<String, Double>>> revise(
            List<Pair<Entity, Map<String, Double>>> entities_for_this_cell_and_scores,
                       List<String> types) {
        List<Integer> removeIndex = new ArrayList<Integer>();
        Iterator<Pair<Entity, Map<String, Double>>> it = entities_for_this_cell_and_scores.iterator();
        int index=0;
        while (it.hasNext()) {
            Pair<Entity, Map<String, Double>> oo = it.next();
            TMPEntityScorer.
                    score_typeMatch(oo.getValue(), types, oo.getKey());
            double type_match_score = oo.getValue().get("type_match");
            if(type_match_score==0)
                removeIndex.add(index);
                //it.remove();
            index++;
            /*double pre_final = oo.getOtherObject().get("final");
            oo.getOtherObject().put("final", type_match_score + pre_final);*/
        }
        List<Pair<Entity, Map<String, Double>>> result = new ArrayList<>();
        if(removeIndex.size()<entities_for_this_cell_and_scores.size()){
            for(int i=0; i<entities_for_this_cell_and_scores.size();i++){
                if(removeIndex.contains(i))
                    continue;
                result.add(entities_for_this_cell_and_scores.get(i));
            }
        }
        return result;
        //To change body of created methods use File | Settings | File Templates.
    }


}
