package uk.ac.shef.dcs.oak.sti.algorithm.smp;

import javafx.util.Pair;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.DisambiguationScorer;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.oak.triplesearch.rep.Entity;

import java.io.IOException;
import java.util.*;

/**
 * NE ranker creates initial disambiguation of an NE column
 */
public class NamedEntityRanker {

    private KnowledgeBaseSearcher kbSearcher;
    private DisambiguationScorer disambScorer;
    //private static Logger log = Logger.getLogger(Disambiguator.class.getName());

    public NamedEntityRanker(KnowledgeBaseSearcher kbSearcher, DisambiguationScorer disambScorer) {
        this.kbSearcher = kbSearcher;
        this.disambScorer = disambScorer;
    }

    public void rankCandidateNamedEntities(
            LTableAnnotation tableAnnotations, LTable table,
            int row, int column
    ) throws IOException {
        List<Pair<Entity, Map<String, Double>>> scores = scoreCandidateNamedEntities(table, row, column);
        List<Pair<Entity, Double>> sorted = new ArrayList<>();
        for (Pair<Entity, Map<String, Double>> e : scores) {
            double score = e.getValue().get(CellAnnotation.SCORE_FINAL);
            sorted.add(new Pair<>(e.getKey(), score));
        }
        Collections.sort(sorted, new Comparator<Pair<Entity, Double>>() {
            @Override
            public int compare(Pair<Entity, Double> o1, Pair<Entity, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        LTableContentCell tcc = table.getContentCell(row, column);
        CellAnnotation[] annotations = new CellAnnotation[scores.size()];
        int i = 0;
        for (Pair<Entity, Map<String, Double>> oo : scores) {
            CellAnnotation ca = new CellAnnotation(tcc.getText(), oo.getKey(),
                    oo.getValue().get(CellAnnotation.SCORE_FINAL), oo.getValue());
            annotations[i] = ca;
            i++;
        }
        tableAnnotations.setContentCellAnnotations(row, column, annotations);
        //return sorted;
    }

    public List<Pair<Entity, Map<String, Double>>> scoreCandidateNamedEntities(LTable table,
                                                                                          int row, int column
    ) throws IOException {
        //do disambiguation scoring
        //log.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        LTableContentCell cell = table.getContentCell(row, column);
        System.out.print("\t\t>> NamedEntityRanker, position at (" + row + "," + column + ") " +
                cell);
       /* if(row==11)
            System.out.println();*/
        List<Entity> candidates = kbSearcher.findEntityCandidates(cell);
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
