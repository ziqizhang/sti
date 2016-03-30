package uk.ac.shef.dcs.sti.algorithm.ji;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.TCellAnnotation;
import uk.ac.shef.dcs.sti.rep.TAnnotation;
import uk.ac.shef.dcs.sti.rep.TContentCell;
import uk.ac.shef.dcs.sti.rep.Table;

import java.io.IOException;
import java.util.*;

/**
 * Created by zqz on 01/05/2015.
 */
public class CandidateEntityGenerator {
    private KBSearch kbSearch;
    private JIAdaptedEntityScorer disambScorer;
    //private static Logger LOG = Logger.getLogger(TCellDisambiguator.class.getName());

    public CandidateEntityGenerator(KBSearch kbSearch, JIAdaptedEntityScorer disambScorer) {
        this.kbSearch = kbSearch;
        this.disambScorer = disambScorer;
    }

    public void generateCandidateEntity(
            TAnnotation tableAnnotations, Table table,
            int row, int column
    ) throws IOException {
        List<Pair<Entity, Map<String, Double>>> scores = scoreCandidateNamedEntities(table, row, column);
        List<Pair<Entity, Double>> sorted = new ArrayList<>();
        for (Pair<Entity, Map<String, Double>> e : scores) {
            double score = e.getValue().get(JIAdaptedEntityScorer.SCORE_CELL_FACTOR);
            sorted.add(new Pair<Entity, Double>(e.getKey(), score));
        }
        Collections.sort(sorted, new Comparator<Pair<Entity, Double>>() {
            @Override
            public int compare(Pair<Entity, Double> o1, Pair<Entity, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        TContentCell tcc = table.getContentCell(row, column);
        String text = tcc.getText().trim().replaceAll("[^a-zA-Z0-9]", "");
        if (text.length() > 2) {
            TCellAnnotation[] annotations = new TCellAnnotation[scores.size()];
            int i = 0;
            for (Pair<Entity, Map<String, Double>> oo : scores) {
                TCellAnnotation ca = new TCellAnnotation(tcc.getText(),
                        oo.getKey(), oo.getValue().get(JIAdaptedEntityScorer.SCORE_CELL_FACTOR),
                        oo.getValue());
                annotations[i] = ca;
                i++;
            }
            tableAnnotations.setContentCellAnnotations(row, column, annotations);
        }
        //return sorted;
    }

    public List<Pair<Entity, Map<String, Double>>> scoreCandidateNamedEntities(Table table,
                                                                                          int row, int column
    ) throws IOException {
        //do disambiguation scoring
        //LOG.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        TContentCell cell = table.getContentCell(row, column);
        System.out.print("\t\t>> Candidate Entity Generator, position at (" + row + "," + column + ") " +
                cell);
       /* if(row==11)
            System.out.println();*/
        List<Entity> candidates = kbSearch.findEntityCandidates(cell.getText());
        List<Entity> removeDuplicates = new ArrayList<>();
        for(Entity ec: candidates){
            if(!removeDuplicates.contains(ec))
                removeDuplicates.add(ec);
        }
        candidates=removeDuplicates;

        System.out.println(" candidates=" + candidates.size());
        //each candidate will have a map containing multiple elements of scores. See SMPAdaptedEntityScorer
        List<Pair<Entity, Map<String, Double>>> disambiguationScores =
                new ArrayList<>();
        for (Entity c : candidates) {
            //find facts of each entity
            if (c.getTriples() == null || c.getTriples().size() == 0) {
                List<String[]> facts = kbSearch.findTriplesOfEntityCandidates(c);
                c.setTriples(facts);
            }
            Map<String, Double> scoreMap = disambScorer.
                    score(c, candidates,
                            column, row, Arrays.asList(row),
                            table, new HashSet<String>());
            disambScorer.computeFinal(scoreMap, cell.getText());
            Pair<Entity, Map<String, Double>> entry = new Pair<>(c,scoreMap);
            disambiguationScores.add(entry);
        }
        return disambiguationScores;
    }

}
