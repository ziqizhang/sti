package uk.ac.shef.dcs.sti.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.rep.Attribute;
import uk.ac.shef.dcs.sti.algorithm.tm.TMPEntityScorer;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.Table;

import java.io.IOException;
import java.util.*;

/**

 */
public class Base_TM_no_Update_Disambiguator {

    private KBSearch kbSearch;
    private Base_TM_no_Update_EntityDisambiguationScorer disambScorer;
    public Base_TM_no_Update_Disambiguator(KBSearch kbSearch, Base_TM_no_Update_EntityDisambiguationScorer disambScorer) {
        this.kbSearch = kbSearch;
        this.disambScorer = disambScorer;
    }

    public List<Pair<Entity, Map<String, Double>>> disambiguate_learn(
            List<Entity> candidates, Table table,
                                                                                 int entity_row, int entity_column) throws IOException {
        System.out.println("\t>> Disambiguation-LEARN, position at [" + entity_row + "," + entity_column + "]: "+ table.getContentCell(entity_row,entity_column)+
                " candidates=" + candidates.size());
        List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();
        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getAttributes() == null || c.getAttributes().size() == 0) {
                List<Attribute> facts = kbSearch.findAttributesOfEntities(c);
                c.setAttributes(facts);
            }
            Map<String, Double> scoreMap = disambScorer.
                    score(c,
                            new ArrayList<>(),
                            entity_column,
                            entity_row, table, new HashSet<>());
            Base_TM_no_Update_EntityDisambiguationScorer.compute_final_score(scoreMap);
            Pair<Entity, Map<String, Double>> entry = new Pair<>(c,scoreMap);
            disambiguationScores.add(entry);
        }
        return disambiguationScores;
    }

    public List<Pair<Entity, Map<String, Double>>> revise(List<Pair<Entity, Map<String, Double>>> entities_for_this_cell_and_scores, List<String> types) {
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
    }
}
