package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import javafx.util.Pair;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.kbsearch.rep.Entity;

import java.io.IOException;
import java.util.*;

/**
 * Created by zqz on 01/05/2015.
 */
public class CandidateEntityGenerator {
    private KnowledgeBaseSearcher kbSearcher;
    private DisambiguationScorer_JI_adapted disambScorer;
    //private static Logger log = Logger.getLogger(Disambiguator.class.getName());

    public CandidateEntityGenerator(KnowledgeBaseSearcher kbSearcher, DisambiguationScorer_JI_adapted disambScorer) {
        this.kbSearcher = kbSearcher;
        this.disambScorer = disambScorer;
    }

    public void generateCandidateEntity(
            LTableAnnotation tableAnnotations, LTable table,
            int row, int column
    ) throws IOException {
        List<Pair<Entity, Map<String, Double>>> scores = scoreCandidateNamedEntities(table, row, column);
        List<Pair<Entity, Double>> sorted = new ArrayList<>();
        for (Pair<Entity, Map<String, Double>> e : scores) {
            double score = e.getValue().get(DisambiguationScorer_JI_adapted.SCORE_CELL_FACTOR);
            sorted.add(new Pair<Entity, Double>(e.getKey(), score));
        }
        Collections.sort(sorted, new Comparator<Pair<Entity, Double>>() {
            @Override
            public int compare(Pair<Entity, Double> o1, Pair<Entity, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        LTableContentCell tcc = table.getContentCell(row, column);
        String text = tcc.getText().trim().replaceAll("[^a-zA-Z0-9]", "");
        if (text.length() > 2) {
            CellAnnotation[] annotations = new CellAnnotation[scores.size()];
            int i = 0;
            for (Pair<Entity, Map<String, Double>> oo : scores) {
                CellAnnotation ca = new CellAnnotation(tcc.getText(),
                        oo.getKey(), oo.getValue().get(DisambiguationScorer_JI_adapted.SCORE_CELL_FACTOR),
                        oo.getValue());
                annotations[i] = ca;
                i++;
            }
            tableAnnotations.setContentCellAnnotations(row, column, annotations);
        }
        //return sorted;
    }

    public List<Pair<Entity, Map<String, Double>>> scoreCandidateNamedEntities(LTable table,
                                                                                          int row, int column
    ) throws IOException {
        //do disambiguation scoring
        //log.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        LTableContentCell cell = table.getContentCell(row, column);
        System.out.print("\t\t>> Candidate Entity Generator, position at (" + row + "," + column + ") " +
                cell);
       /* if(row==11)
            System.out.println();*/
        List<Entity> candidates = kbSearcher.findEntityCandidates(cell);
        List<Entity> removeDuplicates = new ArrayList<>();
        for(Entity ec: candidates){
            if(!removeDuplicates.contains(ec))
                removeDuplicates.add(ec);
        }
        candidates=removeDuplicates;

        System.out.println(" candidates=" + candidates.size());
        //each candidate will have a map containing multiple elements of scores. See DisambiguationScorer_SMP_adapted
        List<Pair<Entity, Map<String, Double>>> disambiguationScores =
                new ArrayList<>();
        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getTriples() == null || c.getTriples().size() == 0) {
                List<String[]> facts = kbSearcher.findTriplesOfEntityCandidates(c);
                c.setTriples(facts);
            }
            Map<String, Double> scoreMap = disambScorer.
                    score(c, candidates,
                            column, row, Arrays.asList(row),
                            table, new HashSet<String>());
            disambScorer.compute_final_score(scoreMap, cell.getText());
            Pair<Entity, Map<String, Double>> entry = new Pair<>(c,scoreMap);
            disambiguationScores.add(entry);
        }
        return disambiguationScores;
    }

}
