package uk.ac.shef.dcs.oak.sti.algorithm.smp;

import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.DisambiguationScorer;
import uk.ac.shef.dcs.oak.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;

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
        List<ObjObj<EntityCandidate, Map<String, Double>>> scores = scoreCandidateNamedEntities(table, row, column);
        List<ObjObj<EntityCandidate, Double>> sorted = new ArrayList<ObjObj<EntityCandidate, Double>>();
        for (ObjObj<EntityCandidate, Map<String, Double>> e : scores) {
            double score = e.getOtherObject().get(CellAnnotation.SCORE_FINAL);
            sorted.add(new ObjObj<EntityCandidate, Double>(e.getMainObject(), score));
        }
        Collections.sort(sorted, new Comparator<ObjObj<EntityCandidate, Double>>() {
            @Override
            public int compare(ObjObj<EntityCandidate, Double> o1, ObjObj<EntityCandidate, Double> o2) {
                return o2.getOtherObject().compareTo(o1.getOtherObject());
            }
        });

        LTableContentCell tcc = table.getContentCell(row, column);
        CellAnnotation[] annotations = new CellAnnotation[scores.size()];
        int i = 0;
        for (ObjObj<EntityCandidate, Map<String, Double>> oo : scores) {
            CellAnnotation ca = new CellAnnotation(tcc.getText(), oo.getMainObject(), oo.getOtherObject().get(CellAnnotation.SCORE_FINAL), oo.getOtherObject());
            annotations[i] = ca;
            i++;
        }
        tableAnnotations.setContentCellAnnotations(row, column, annotations);
        //return sorted;
    }

    public List<ObjObj<EntityCandidate, Map<String, Double>>> scoreCandidateNamedEntities(LTable table,
                                                                                          int row, int column
    ) throws IOException {
        //do disambiguation scoring
        //log.info("\t>> Disambiguation-LEARN, position at (" + entity_row + "," + entity_column + ") candidates=" + candidates.size());
        LTableContentCell cell = table.getContentCell(row, column);
        System.out.print("\t\t>> NamedEntityRanker, position at (" + row + "," + column + ") " +
                cell);
       /* if(row==11)
            System.out.println();*/
        List<EntityCandidate> candidates = kbSearcher.findEntitiesForCell(cell);
        System.out.println(" candidates=" + candidates.size());
        //each candidate will have a map containing multiple elements of scores. See DisambiguationScorer_SMP_adapted
        List<ObjObj<EntityCandidate, Map<String, Double>>> disambiguationScores = new ArrayList<ObjObj<EntityCandidate, Map<String, Double>>>();
        for (EntityCandidate c : candidates) {
            //find facts of each entity
            if (c.getFacts() == null || c.getFacts().size() == 0) {
                List<String[]> facts = kbSearcher.find_triplesForEntity(c);
                c.setFacts(facts);
            }
            Map<String, Double> scoreMap = disambScorer.
                    score(c, candidates,
                            column, row, Arrays.asList(row),
                            table, new HashSet<String>());
            disambScorer.compute_final_score(scoreMap, cell.getText());
            ObjObj<EntityCandidate, Map<String, Double>> entry = new ObjObj<EntityCandidate, Map<String, Double>>();
            entry.setMainObject(c);
            entry.setOtherObject(scoreMap);
            disambiguationScores.add(entry);
        }
        return disambiguationScores;
    }


}
