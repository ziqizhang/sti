package uk.ac.shef.dcs.oak.sti.table.interpreter.interpret;

import uk.ac.shef.dcs.oak.kbsearch.Entity;
import uk.ac.shef.dcs.oak.sti.table.interpreter.content.KBSearcher;
import uk.ac.shef.dcs.oak.sti.table.rep.*;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.io.IOException;
import java.util.*;

/**
 */
public class Disambiguator {

    private KBSearcher kbSearcher;
    private DisambiguationScorer disambScorer;
    //private static Logger log = Logger.getLogger(Disambiguator.class.getName());

    public Disambiguator(KBSearcher kbSearcher, DisambiguationScorer disambScorer) {
        this.kbSearcher = kbSearcher;
        this.disambScorer = disambScorer;
    }

    public List<ObjObj<Entity, Map<String, Double>>> disambiguate_learn_seeding(List<Entity> candidates, LTable table,
                                                                                         List<Integer> entity_rows, int entity_column
    ) throws IOException {
        //do disambiguation scoring
        //log.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        LTableContentCell sample_tcc = table.getContentCell(entity_rows.get(0), entity_column);
        System.out.println("\t>> Disambiguation-LEARN(seeding), position at (" + entity_rows + "," + entity_column + ") "+
                sample_tcc+" candidates=" + candidates.size());
        List<ObjObj<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<ObjObj<Entity, Map<String, Double>>>();
        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getFacts() == null || c.getFacts().size() == 0) {
                List<String[]> facts = kbSearcher.find_triplesForEntity(c);
                c.setFacts(facts);
            }
            Map<String, Double> scoreMap = disambScorer.
                    score(c, candidates,
                            entity_column,
                            entity_rows.get(0),
                            entity_rows, table, new HashSet<String>());
            disambScorer.compute_final_score(scoreMap, sample_tcc.getText());
            ObjObj<Entity, Map<String, Double>> entry = new ObjObj<Entity, Map<String, Double>>();
            entry.setMainObject(c);
            entry.setOtherObject(scoreMap);
            disambiguationScores.add(entry);
        }
        return disambiguationScores;
    }


    public List<ObjObj<Entity, Map<String, Double>>> disambiguate_learn_consolidate(
            List<Entity> candidates,
            LTable table,
            List<Integer> entity_rows,
            int entity_column,
            Set<String> assigned_column_types,
            boolean first_phase,
            Entity... reference_disambiguated_entities
    ) throws IOException {
        //do disambiguation scoring
        //log.info("\t>> Disambiguation-UPDATE , position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        LTableContentCell sample_tcc = table.getContentCell(entity_rows.get(0), entity_column);
        if(first_phase)
            System.out.println("\t>> Disambiguation-LEARN(consolidate) , position at (" + entity_rows + "," + entity_column + ") "+sample_tcc+" candidates=" + candidates.size());
        else
            System.out.println("\t>> Disambiguation-UPDATE, position at (" + entity_rows + "," + entity_column + ") "+sample_tcc+" (candidates)-" + candidates.size());
        List<ObjObj<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<ObjObj<Entity, Map<String, Double>>>();

        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getFacts() == null || c.getFacts().size() == 0) {
                List<String[]> facts = kbSearcher.find_triplesForEntity(c);
                c.setFacts(facts);
            }
            Map<String, Double> scoreMap = disambScorer.
                    score(c,candidates,
                            entity_column,
                            entity_rows.get(0),
                            entity_rows,
                            table,
                            assigned_column_types,
                            reference_disambiguated_entities);
            disambScorer.compute_final_score(scoreMap, sample_tcc.getText());
            ObjObj<Entity, Map<String, Double>> entry = new ObjObj<Entity, Map<String, Double>>();
            entry.setMainObject(c);
            entry.setOtherObject(scoreMap);
            disambiguationScores.add(entry);
        }
        return disambiguationScores;
    }


    //used with disambiguation results obtained from the first iteration of classification and disambiguation
    public List<ObjObj<Entity, Map<String, Double>>> revise(
            List<ObjObj<Entity, Map<String, Double>>> entities_for_this_cell_and_scores,
                       List<String> types) {
        List<Integer> removeIndex = new ArrayList<Integer>();
        Iterator<ObjObj<Entity, Map<String, Double>>> it = entities_for_this_cell_and_scores.iterator();
        int index=0;
        while (it.hasNext()) {
            ObjObj<Entity, Map<String, Double>> oo = it.next();
            DisambiguationScorer_Overlap.
                    score_typeMatch(oo.getOtherObject(), types, oo.getMainObject());
            double type_match_score = oo.getOtherObject().get("type_match");
            if(type_match_score==0)
                removeIndex.add(index);
                //it.remove();
            index++;
            /*double pre_final = oo.getOtherObject().get("final");
            oo.getOtherObject().put("final", type_match_score + pre_final);*/
        }
        List<ObjObj<Entity, Map<String, Double>>> result = new ArrayList<ObjObj<Entity, Map<String, Double>>>();
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
