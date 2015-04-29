package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.content.KBSearcher;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.KB_InstanceFilter;
import uk.ac.shef.dcs.oak.lodie.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.HeaderAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.io.IOException;
import java.util.*;

/**
 * Created by zqz on 20/04/2015.
 */
public class ColumnClassifier {

    private KBSearcher kbSearcher;

    public static final String SMP_SCORE_ENTITY_VOTE = "smp_score_entity_vote";
    public static final String SMP_SCORE_GRANULARITY = "smp_score_granularity";

    public ColumnClassifier(KBSearcher kbSearcher) {
        this.kbSearcher = kbSearcher;
    }

    public void rankColumnConcepts(LTableAnnotation tableAnnotation, LTable table, int col) throws IOException {
        int totalNonEmpty = 0;
        Map<String, Double> votes = new HashMap<String, Double>();
        for (int r = 0; r < table.getNumRows(); r++) {
            //in case multiple NEs have the same score, we take them all
            if (!table.getContentCell(r, col).getType().equals(DataTypeClassifier.DataType.EMPTY))
                totalNonEmpty++;
            List<CellAnnotation> bestCellAnnotations = tableAnnotation.getBestContentCellAnnotations(r, col);
            if (bestCellAnnotations.size() > 0) {
                Set<String> distinctTypes = new HashSet<String>();
                for (CellAnnotation ca : bestCellAnnotations) {
                    EntityCandidate e = ca.getAnnotation();
                    distinctTypes.addAll(e.getTypeIds());
                }
                for (String t : distinctTypes) {
                    if (KB_InstanceFilter.ignoreType(t, t))
                        continue;
                    Double v = votes.get(t);
                    v = v == null ? 1.0 : v;
                    v += 1.0;
                    votes.put(t, v);
                }
            }
        }

        if (votes.size() != 0) { //couuld be 0 if the column has not NE annotations at all
            List<ObjObj<String, Double>> result_votes = new ArrayList<ObjObj<String, Double>>();
            for (Map.Entry<String, Double> e : votes.entrySet()) {
                result_votes.add(new ObjObj<String, Double>(e.getKey(), e.getValue() / totalNonEmpty));
            }
            Collections.sort(result_votes, new Comparator<ObjObj<String, Double>>() {
                @Override
                public int compare(ObjObj<String, Double> o1, ObjObj<String, Double> o2) {
                    return o2.getOtherObject().compareTo(o1.getOtherObject());
                }
            });

            //tie breaker based on granularity score of concepts
            double maxScore = result_votes.get(0).getOtherObject();
            //is there a tie?
            int count_same_max_score = 0;
            for (ObjObj<String, Double> oo : result_votes) {
                if (oo.getOtherObject() == maxScore) {
                    count_same_max_score++;
                    if (count_same_max_score > 1) {
                        break;
                    }
                }
            }
            final Map<String, Double> result_granularity = new HashMap<String, Double>();
            if (count_same_max_score > 1) {
                for (ObjObj<String, Double> e : result_votes) {
                    if (e.getOtherObject() == maxScore) {
                        result_granularity.put(e.getMainObject(), kbSearcher.find_granularityForType(e.getMainObject()));
                    }
                }
            }

            //a header annotation will only have granularity score if there are more than one candidate with the same vote score
            HeaderAnnotation[] headerAnnotations = new HeaderAnnotation[result_votes.size()];
            int i = 0;
            for (ObjObj<String, Double> oo : result_votes) {
                HeaderAnnotation ha = new HeaderAnnotation(table.getColumnHeader(col).getHeaderText(),
                        oo.getMainObject(), oo.getMainObject(), oo.getOtherObject());
                ha.getScoreElements().put(SMP_SCORE_ENTITY_VOTE, oo.getOtherObject());
                Double granularity_score = result_granularity.get(oo.getMainObject());
                granularity_score = granularity_score == null ? 0 : granularity_score;
                ha.getScoreElements().put(SMP_SCORE_GRANULARITY, granularity_score);
                ha.getScoreElements().put(HeaderAnnotation.FINAL, oo.getOtherObject());
                headerAnnotations[i] = ha;
                i++;
            }
            tableAnnotation.setHeaderAnnotation(col, headerAnnotations);
        }
    }
}
