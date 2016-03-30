package uk.ac.shef.dcs.sti.algorithm.smp;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.TCellAnnotation;
import uk.ac.shef.dcs.sti.rep.HeaderAnnotation;
import uk.ac.shef.dcs.sti.rep.TAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;

import java.io.IOException;
import java.util.*;

/**
 * Created by zqz on 20/04/2015.
 */
public class ColumnClassifier {

    protected static double FREEBASE_TOTAL_TOPICS=47560900;
    private KBSearch kbSearch;

    public static final String SMP_SCORE_ENTITY_VOTE = "smp_score_entity_vote";
    public static final String SMP_SCORE_GRANULARITY = "smp_score_granularity";

    public ColumnClassifier(KBSearch kbSearch) {
        this.kbSearch = kbSearch;
    }

    public void rankColumnConcepts(TAnnotation tableAnnotation, Table table, int col) throws IOException {
        int totalNonEmpty = 0;
        Map<String, Double> votes = new HashMap<String, Double>();
        for (int r = 0; r < table.getNumRows(); r++) {
            //in case multiple NEs have the same score, we take them all
            if (!table.getContentCell(r, col).getType().equals(DataTypeClassifier.DataType.EMPTY))
                totalNonEmpty++;
            List<TCellAnnotation> bestCellAnnotations = tableAnnotation.getBestContentCellAnnotations(r, col);
            if (bestCellAnnotations.size() > 0) {
                Set<String> distinctTypes = new HashSet<String>();
                for (TCellAnnotation ca : bestCellAnnotations) {
                    Entity e = ca.getAnnotation();
                    distinctTypes.addAll(e.getTypeIds());
                }
                for (String t : distinctTypes) {
                    Double v = votes.get(t);
                    v = v == null ? 1.0 : v;
                    v += 1.0;
                    votes.put(t, v);
                }
            }
        }

        if (votes.size() != 0) { //couuld be 0 if the column has not NE annotations at all
            List<Pair<String, Double>> result_votes = new ArrayList<>();
            for (Map.Entry<String, Double> e : votes.entrySet()) {
                double voteScore = e.getValue() / totalNonEmpty;
                voteScore+=computeConceptSpecificity(e.getKey(), kbSearch);
                result_votes.add(new Pair<>(e.getKey(),
                        voteScore));
            }
            Collections.sort(result_votes, new Comparator<Pair<String, Double>>() {
                @Override
                public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            //tie breaker based on granularity score of concepts
            double maxScore = result_votes.get(0).getValue();
            //is there a tie?
            int count_same_max_score = 0;
            for (Pair<String, Double> oo : result_votes) {
                if (oo.getValue() == maxScore) {
                    count_same_max_score++;
                    if (count_same_max_score > 1) {
                        break;
                    }
                }
            }
            final Map<String, Double> result_granularity = new HashMap<String, Double>();
            if (count_same_max_score > 1) {
                for (Pair<String, Double> e : result_votes) {
                    if (e.getValue() == maxScore) {
                        result_granularity.put(e.getKey(), kbSearch.find_granularityForConcept(e.getKey()));
                    }
                }
            }

            //a header annotation will only have granularity score if there are more than one candidate with the same vote score
            HeaderAnnotation[] headerAnnotations = new HeaderAnnotation[result_votes.size()];
            int i = 0;
            for (Pair<String, Double> oo : result_votes) {
                HeaderAnnotation ha = new HeaderAnnotation(table.getColumnHeader(col).getHeaderText(),
                        oo.getKey(), oo.getKey(), oo.getValue());
                ha.getScoreElements().put(SMP_SCORE_ENTITY_VOTE, oo.getValue());
                Double granularity_score = result_granularity.get(oo.getKey());
                granularity_score = granularity_score == null ? 0 : granularity_score;
                ha.getScoreElements().put(SMP_SCORE_GRANULARITY, granularity_score);
                ha.getScoreElements().put(HeaderAnnotation.FINAL, oo.getValue());
                headerAnnotations[i] = ha;
                i++;
            }
            tableAnnotation.setHeaderAnnotation(col, headerAnnotations);
        }
    }

    private double computeConceptSpecificity(String concept_url, KBSearch kbSearch) throws IOException {
        double conceptGranularity = kbSearch.find_granularityForConcept(concept_url);
        if(conceptGranularity<0)
            return 0.0;
        return 1-Math.sqrt(conceptGranularity/FREEBASE_TOTAL_TOPICS);
    }
}
