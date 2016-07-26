package uk.ac.shef.dcs.sti.core.subjectcol;

import javafx.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by - on 18/03/2016.
 */
 class SubjectColumnScorerHeuristic extends SubjectColumnScorer {
    private static final boolean USE_TOKEN_DIVERSITY = false;
    private static final boolean USE_MAX_SCORE_BOOST = false;
    private static final double CM_WEIGHT = 2.0; //CM computeElementScores weight
    private static final boolean NORMAMLIZE_SCORE_BY_DISTANCE_TO_FIRST_COL = true;

    private static final String SCORE_COMPONENT_UC="uc";
    private static final String SCORE_COMPONENT_EMC="emc";
    private static final String SCORE_COMPONENT_AC="ac";
    private static final String SCORE_COMPONENT_DF="df";
    private static final String SCORE_COMPONENT_CM="cm";
    private static final String SCORE_COMPONENT_WS="ws";


    @Override
    protected Map<Integer, Pair<Double, Boolean>> score(List<TColumnFeature> featuresOfNEColumns) {
        Map<Integer, Pair<Double, Boolean>> scores = new HashMap<>();
        //sort by column ids
        Collections.sort(featuresOfNEColumns, (o1, o2) -> {
            int compared = new Integer(o1.getColId()).compareTo(o2.getColId());
            return compared;
        });

        //find max scores to calculate computeElementScores boosters
        Map<String, Double> max_scores_for_each_feature = new HashMap<>();
        for (int index = 0; index < featuresOfNEColumns.size(); index++) {
            TColumnFeature cf = featuresOfNEColumns.get(index);
            double uc = USE_TOKEN_DIVERSITY?cf.getUniqueTokenCount() + cf.getUniqueCellCount():
                    cf.getUniqueCellCount();
            double cm = cf.getCMScore();
            double ws = cf.getWSScore();

            Double max_uc = max_scores_for_each_feature.get(SCORE_COMPONENT_UC);
            max_uc = max_uc == null ? 0.0 : max_uc;
            if (uc > max_uc)
                max_uc = uc;
            max_scores_for_each_feature.put(SCORE_COMPONENT_UC, max_uc);

            Double max_cm = max_scores_for_each_feature.get(SCORE_COMPONENT_CM);
            max_cm = max_cm == null ? 0.0 : max_cm;
            if (cm > max_cm)
                max_cm = cm;
            max_scores_for_each_feature.put(SCORE_COMPONENT_CM, max_cm);

            Double max_wb = max_scores_for_each_feature.get(SCORE_COMPONENT_WS);
            max_wb = max_wb == null ? 0.0 : max_wb;
            if (ws > max_wb)
                max_wb = ws;
            max_scores_for_each_feature.put(SCORE_COMPONENT_WS, max_wb);
        }

        //calculating scores
        for (int index = 0; index < featuresOfNEColumns.size(); index++) {
            TColumnFeature cf = featuresOfNEColumns.get(index);
            double sum,uc;

            if (USE_TOKEN_DIVERSITY)
                uc = cf.getUniqueTokenCount() + cf.getUniqueCellCount();
            else
                uc = cf.getUniqueCellCount();
            double cm = cf.getCMScore() * CM_WEIGHT;
            double ws = cf.getWSScore();// * CM_WEIGHT; todo: check this, original code uses booster
            double emc = cf.getEmptyCellCount() / (double) cf.getNumRows();

            int max_component_score_booster = 0;
            for (Map.Entry<String, Double> e : max_scores_for_each_feature.entrySet()) {
                if (e.getKey().equals(SCORE_COMPONENT_UC) && e.getValue() == uc && uc != 0)
                    max_component_score_booster++;
                if (e.getKey().equals(SCORE_COMPONENT_CM) && e.getValue() == cm && cm != 0)
                    max_component_score_booster++;
                if (e.getKey().equals(SCORE_COMPONENT_WS) && e.getValue() == ws && ws != 0)
                    max_component_score_booster++;
            }
            max_component_score_booster =
                    max_component_score_booster == 0 ? 1 : max_component_score_booster;


            boolean f = false;
            sum = uc + cm + ws - emc;
            if (cf.isAcronymColumn()) {
                sum = sum - 1.0;
                f = true;
            }
            if (USE_MAX_SCORE_BOOST)
                sum = Math.pow(sum, max_component_score_booster);


            //sum=sum/(index+1);
            if (NORMAMLIZE_SCORE_BY_DISTANCE_TO_FIRST_COL) {
                sum = sum / Math.sqrt(index + 1);
            }
            Pair<Double, Boolean> score_object = new Pair<>(sum, f);
            scores.put(cf.getColId(), score_object);
        }

        return scores;
    }
}
