package uk.ac.shef.dcs.sti.core.algorithm.smp;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 *
 */
public class TColumnClassifier {

    private static final Logger LOG =Logger.getLogger(TColumnClassifier.class.getName());

    private KBSearch kbSearch;
    private ClazzSpecificityCalculator csCalculator;

    public static final String SMP_SCORE_ENTITY_VOTE = "smp_score_entity_vote";
    public static final String SMP_SCORE_GRANULARITY = "smp_score_granularity";

    public TColumnClassifier(KBSearch kbSearch,
                             ClazzSpecificityCalculator csCalculator) {
        this.kbSearch = kbSearch;
        this.csCalculator=csCalculator;
    }

    public void classifyColumns(TAnnotation tableAnnotation, Table table, int col) throws KBSearchException {
        int totalNonEmpty = 0;
        //Firstly collect votes
        Map<String, Double> votes = new HashMap<>();
        for (int r = 0; r < table.getNumRows(); r++) {
            //in case multiple NEs have the same computeElementScores, we take them all
            if (!table.getContentCell(r, col).getType().equals(DataTypeClassifier.DataType.EMPTY))
                totalNonEmpty++;
            List<TCellAnnotation> winningCellAnnotations = tableAnnotation.getWinningContentCellAnnotation(r, col);
            if (winningCellAnnotations.size() > 0) {
                Set<String> distinctTypes = new HashSet<>();
                for (TCellAnnotation ca : winningCellAnnotations) {
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

        //Second, calculate vote score, and concept specificity score
        if (votes.size() != 0) { //couuld be 0 if the column has not NE annotations at all
            List<Pair<String, Double>> voteResult = new ArrayList<>();
            for (Map.Entry<String, Double> e : votes.entrySet()) {
                double voteScore = e.getValue() / totalNonEmpty;
                voteScore+=csCalculator.compute(e.getKey());
                LOG.info("\t\t>> computing class specificity score (can involve querying the KB...) for "+e.getKey());
                voteResult.add(new Pair<>(e.getKey(),
                        voteScore));
            }
            Collections.sort(voteResult, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

            //tie breaker based on granularity computeElementScores of concepts
            double maxScore = voteResult.get(0).getValue();
            //is there a tie?
            int count_same_max_score = 0;
            for (Pair<String, Double> oo : voteResult) {
                if (oo.getValue() == maxScore) {
                    count_same_max_score++;
                    if (count_same_max_score > 1) {
                        break;
                    }
                }
            }
            final Map<String, Double> granularityScore = new HashMap<>();
            if (count_same_max_score > 1) {
                for (Pair<String, Double> e : voteResult) {
                    if (e.getValue() == maxScore) {
                        granularityScore.put(e.getKey(), kbSearch.findGranularityOfClazz(e.getKey()));
                    }
                }
            }

            //a header annotation will only have granularity score if there are more than one candidate with the same vote computeElementScores
            TColumnHeaderAnnotation[] headerAnnotations = new TColumnHeaderAnnotation[voteResult.size()];
            int i = 0;
            for (Pair<String, Double> oo : voteResult) {
                TColumnHeaderAnnotation ha =
                        new TColumnHeaderAnnotation(table.getColumnHeader(col).getHeaderText(),
                       new Clazz(oo.getKey(), oo.getKey()), oo.getValue());
                ha.getScoreElements().put(SMP_SCORE_ENTITY_VOTE, oo.getValue());
                Double granularity = granularityScore.get(oo.getKey());
                granularity = granularity == null ? 0 : granularity;
                ha.getScoreElements().put(SMP_SCORE_GRANULARITY, granularity);
                ha.getScoreElements().put(TColumnHeaderAnnotation.SCORE_FINAL, oo.getValue());
                headerAnnotations[i] = ha;
                i++;
            }
            tableAnnotation.setHeaderAnnotation(col, headerAnnotations);
        }
    }


}
