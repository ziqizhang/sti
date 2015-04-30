package uk.ac.shef.dcs.oak.sti.table.interpreter.maincol;

import cern.colt.matrix.DoubleMatrix2D;
import org.apache.solr.client.solrj.SolrServer;
import uk.ac.shef.dcs.oak.sti.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.table.interpreter.selector.RowSelector;
import uk.ac.shef.dcs.oak.sti.table.interpreter.stopping.StoppingCriteriaInstantiator;
import uk.ac.shef.dcs.oak.sti.table.rep.LTable;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.dcs.oak.websearch.bing.v2.APIKeysDepletedException;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * This class implements a decision tree logic to infer among all columns in a table, which ONE is likely the main entity
 * column
 */
public class MainColumnFinder {

    private static boolean use_token_diversity=false;
    private static boolean use_max_score_boost=false;
    private static double wb_cm_weight=2.0;



    public static boolean use_ordering=false;
    private static boolean score_normamlize_by_distance_to_first_col=true;

    private static Logger log = Logger.getLogger(MainColumnFinder_old.class.getName());
    private ColumnFeatureGenerator featureGenerator;
    private RowSelector row_sampler;
    private String stoppingCriteriaClassname;
    private String[] stoppingCriteriaParams;
    private boolean apply_websearch_scorer;

    public MainColumnFinder(String cache, String nlpResource, boolean apply_websearch_scorer, List<String> stopwords,String... searchAPIKeys) throws IOException {
        featureGenerator = new ColumnFeatureGenerator(cache, nlpResource, stopwords,searchAPIKeys);
        this.apply_websearch_scorer = apply_websearch_scorer;
    }

    public MainColumnFinder(RowSelector sampler, String stoppingCriteriaClassname, String[] stoppingCriteriaParams,
                            String cache, String nlpResource, boolean apply_websearch_scorer,List<String> stopwords, String... searchAPIKeys) throws IOException {
        featureGenerator = new ColumnFeatureGenerator(cache, nlpResource, stopwords,searchAPIKeys);
        this.row_sampler = sampler;
        this.stoppingCriteriaClassname = stoppingCriteriaClassname;
        this.stoppingCriteriaParams = stoppingCriteriaParams;
        this.apply_websearch_scorer = apply_websearch_scorer;
    }

    public MainColumnFinder(RowSelector sampler, String stoppingCriteriaClassname, String[] stoppingCriteriaParams,
                            SolrServer cache, String nlpResource, boolean apply_websearch_scorer, List<String> stopwords,String... searchAPIKeys) throws IOException {
        featureGenerator = new ColumnFeatureGenerator(cache, nlpResource, stopwords,searchAPIKeys);
        this.row_sampler = sampler;
        this.stoppingCriteriaClassname = stoppingCriteriaClassname;
        this.stoppingCriteriaParams = stoppingCriteriaParams;
        this.apply_websearch_scorer = apply_websearch_scorer;
    }

    /**
     * The decision tree logic is:
     * 1. If col is the only NE likely col in the table, choose the column
     * 2. If col is NE likely, and it is the only one having non-empty cells, choose the column
     *
     * @param table
     * @return a list of ObjectWithObject objects, where first object is the column index; second is the score
     *         probability that asserts that column being the main column of the table. (only NE likely columns can be
     *         considered main column)
     */
    public List<ObjObj<Integer, ObjObj<Double, Boolean>>> compute(LTable table, int... skipColumns) throws APIKeysDepletedException, IOException {
        List<ObjObj<Integer, ObjObj<Double, Boolean>>> rs = new ArrayList<ObjObj<Integer, ObjObj<Double, Boolean>>>();

        //1. initiate all columns' feature objects
        List<ColumnFeature> allColumnCandidates = new ArrayList<ColumnFeature>(table.getNumCols());
        for (int c = 0; c < table.getNumCols(); c++) {
            boolean skip = false;
            for (int i : skipColumns) {
                if (c == i) {
                    skip = true;
                    break;
                }
            }
            if (!skip)
                allColumnCandidates.add(new ColumnFeature(c, table.getNumRows()));
        }

        //2. infer column datatype
        featureGenerator.feature_columnDataTypes(table);

        //3. infer the most frequent datatype,
        featureGenerator.feature_mostDataType(allColumnCandidates, table);

        //4. select only NE columns to further process
        List<ColumnFeature> allNEColumnCandidates = new ArrayList<ColumnFeature>();
        for (ColumnFeature cf : allColumnCandidates) {
            if (cf.getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                allNEColumnCandidates.add(cf);
        }
        //EXCEPTION: what if no NE columns found?
        if (allNEColumnCandidates.size() == 0) {
            for (ColumnFeature cf : allColumnCandidates) {
                if (cf.getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.SHORT_TEXT))
                    allNEColumnCandidates.add(cf);
            }
        }

        featureGenerator.feature_countEmptyCells(allNEColumnCandidates, table); //warning:always count empty cells first!!!!!
        featureGenerator.feature_valueDiversity(allNEColumnCandidates, table);
        featureGenerator.feature_isColumnAcronymOrCode(allNEColumnCandidates, table);   //warning: this must be run after counting empty cells!!!

        //EXCEPTION: what if no SHORT TEXT columns found?
        if (allNEColumnCandidates.size() == 0) {
            log.warning("This table does not contain columns that are likely to contain named entities.");
            ObjObj<Integer, ObjObj<Double, Boolean>> oo = new ObjObj<Integer, ObjObj<Double, Boolean>>();
            oo.setMainObject(0);
            oo.setOtherObject(new ObjObj<Double, Boolean>(1.0, false));
            rs.add(oo);
            for (ColumnFeature cf : allColumnCandidates) {
                table.getColumnHeader(cf.getColId()).setFeature(cf);
            }
            return rs;
        }

        //5. is any NE column the only valid NE column in the table?
        int onlyNECol = featureGenerator.feature_isTheOnlyNEColumn(allNEColumnCandidates);
        //5 - yes:
        if (onlyNECol != -1) {
            ObjObj<Integer, ObjObj<Double, Boolean>> oo = new ObjObj<Integer, ObjObj<Double, Boolean>>();
            oo.setMainObject(onlyNECol);
            oo.setOtherObject(new ObjObj<Double, Boolean>(1.0, false));
            rs.add(oo);
            for (ColumnFeature cf : allColumnCandidates) {
                table.getColumnHeader(cf.getColId()).setFeature(cf);
            }
            return rs;
        }

        //6. is any NE column the only one that has no empty cells?
        int onlyNECol_with_no_emtpy = -1, num = 0;
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (cf.getEmptyCells() == 0 /*&& !cf.isCode_or_Acronym()*/) {
                num++;
                if (onlyNECol_with_no_emtpy == -1)
                    onlyNECol_with_no_emtpy = cf.getColId();
                else
                    break;
            }
        }
        //6 - yes:
        if (onlyNECol_with_no_emtpy != -1 && num == 1) {
            if (!allColumnCandidates.get(onlyNECol_with_no_emtpy).isCode_or_Acronym()) {
                ObjObj<Integer, ObjObj<Double, Boolean>> oo = new ObjObj<Integer, ObjObj<Double, Boolean>>();
                oo.setMainObject(onlyNECol_with_no_emtpy);
                oo.setOtherObject(new ObjObj<Double, Boolean>(1.0, false));
                rs.add(oo);
                for (ColumnFeature cf : allColumnCandidates) {
                    table.getColumnHeader(cf.getColId()).setFeature(cf);
                }
                return rs;
            }
        }

        //7. is any NE column the only one that has non-duplicate values on every row and that it is NOT an acronym column?
        int onlyNECol_non_duplicate = -1;
        num = 0;
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (cf.getCellValueDiversity() == 1.0
                    && !cf.isCode_or_Acronym()
                    && cf.getMostDataType().getCountRows() == table.getNumRows()) {
                num++;
                if (onlyNECol_non_duplicate == -1)
                    onlyNECol_non_duplicate = cf.getColId();
                else
                    break;
            }
        }

        //7 - yes:
        if (onlyNECol_non_duplicate != -1 && num == 1) {
            if (!allColumnCandidates.get(onlyNECol_non_duplicate).isCode_or_Acronym()) {
                ObjObj<Integer, ObjObj<Double, Boolean>> oo = new ObjObj<Integer, ObjObj<Double, Boolean>>();
                oo.setMainObject(onlyNECol_non_duplicate);
                oo.setOtherObject(new ObjObj<Double, Boolean>(1.0, false));
                //     rs.add(oo);
                for (ColumnFeature cf : allColumnCandidates) {
                    table.getColumnHeader(cf.getColId()).setFeature(cf);
                }
                //     return rs;
            }
        }

        //7.5 ====== this is a dangerous rule as it MAY overdo (have not checked thou) true positives ======
        List<Integer> ignoreColumns = new ArrayList<Integer>();
        featureGenerator.feature_headerInvalidSyntactic(allNEColumnCandidates, table);
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (cf.isInvalidPOS())
                ignoreColumns.add(cf.getColId());
        }
        //if columns to be ignored due to invalid header text is less than total columns to be considered,we can ignore them
        //otherwise, if we are told all columns should be ignored, dont ignore any candidate ne columns
        if (ignoreColumns.size() != allNEColumnCandidates.size()) {
            Iterator<ColumnFeature> it = allNEColumnCandidates.iterator();
            while (it.hasNext()) {
                ColumnFeature cf = it.next();
                if (cf.isInvalidPOS())
                    it.remove();
            }
        }
        if (allNEColumnCandidates.size() == 1) {
            ObjObj<Integer, ObjObj<Double, Boolean>> oo = new ObjObj<Integer, ObjObj<Double, Boolean>>();
            oo.setMainObject(allNEColumnCandidates.get(0).getColId());
            oo.setOtherObject(new ObjObj<Double, Boolean>(1.0, false));
            rs.add(oo);
            for (ColumnFeature cf : allColumnCandidates) {
                table.getColumnHeader(cf.getColId()).setFeature(cf);
            }
            return rs;
        }

        //8. generate feature - 1st NE column
        featureGenerator.feature_isFirstNEColumn(allNEColumnCandidates);

        //9. generate features - context match
        log.finest("Computing context matching");
        featureGenerator.feature_contextMatchScore(allNEColumnCandidates, table);

        //10. generate features - web search matcher
        if (apply_websearch_scorer) {
            log.finest("Computing web search matching (total rows " + table.getNumRows());

                DoubleMatrix2D scores;
                if (row_sampler != null) {
                    scores = featureGenerator.feature_webSearchScore_with_sampling(allNEColumnCandidates, table,
                            row_sampler, StoppingCriteriaInstantiator.instantiate(stoppingCriteriaClassname, stoppingCriteriaParams), 1);
                } else {
                    scores = featureGenerator.feature_webSearchScore(allNEColumnCandidates, table);
                }
                double total = 0.0;
                for (ColumnFeature cf : allNEColumnCandidates) {
                    for (int row = 0; row < scores.rows(); row++) {
                        total += scores.get(row, cf.getColId());
                    }
                    cf.setWebSearchScore(total);
                    total = 0.0;
                }

        }

        //added: reset all scores to use relative scoring
        reset_token_diversity(allNEColumnCandidates);
        reset_relative_scores(allNEColumnCandidates);

        //12. then let's perform reasoning based on the remaining features: diversity score; 1st ne column; context match; web search match
        //final Map<Integer, ObjObj<Double, Boolean>> inferenceScores = infer_multiFeatures_vote(allNEColumnCandidates);
        final Map<Integer, ObjObj<Double, Boolean>> inferenceScores = infer_multiFeatures_score(allNEColumnCandidates);
        List<Integer> candidates = new ArrayList<Integer>(inferenceScores.keySet());
        final Map<Integer, ColumnFeature> map_column_to_columnFeature = new HashMap<Integer, ColumnFeature>();
        for (ColumnFeature cf : allNEColumnCandidates) {
            map_column_to_columnFeature.put(cf.getColId(), cf);
        }
        //tiebreaker_reset(allNEColumnCandidates, inferenceScores);

        Collections.sort(candidates, new Comparator<Integer>() { //sort by score first; then column, left most first
            @Override
            public int compare(Integer o1, Integer o2) {
                int compared = inferenceScores.get(o2).getMainObject().compareTo(inferenceScores.get(o1).getMainObject());
                /*if (compared == 0) { //where there is a tie, choose the one having the highest diversity score
                    Double vd_o1 = map_column_to_columnFeature.get(o1).getCellValueDiversity();
                    Double vd_o2 = map_column_to_columnFeature.get(o2).getCellValueDiversity();
                    compared = vd_o2.compareTo(vd_o1);
                    if (compared == 0) {
                        vd_o1 = map_column_to_columnFeature.get(o1).getTokenValueDiversity();
                        vd_o2 = map_column_to_columnFeature.get(o2).getTokenValueDiversity();
                        return vd_o2.compareTo(vd_o1);
                    }
                }*/
                return compared;
            }
        });

        for (int ci : candidates) {
            ObjObj<Integer, ObjObj<Double, Boolean>> oo = new ObjObj<Integer, ObjObj<Double, Boolean>>();
            oo.setMainObject(ci);
            oo.setOtherObject(inferenceScores.get(ci));
            rs.add(oo);
        }

        for (ColumnFeature cf : allColumnCandidates) {
            table.getColumnHeader(cf.getColId()).setFeature(cf);
        }
        return rs;
    }

    private void reset_token_diversity(List<ColumnFeature> allNEColumnCandidates) {
        if(!use_token_diversity){
            for(ColumnFeature cf: allNEColumnCandidates)
                cf.setTokenValueDiversity(0);
        }

    }

    private Map<Integer, ObjObj<Double, Boolean>> infer_multiFeatures_score(List<ColumnFeature> allNEColumnCandidates) {
        Map<Integer, ObjObj<Double, Boolean>> scores = new HashMap<Integer, ObjObj<Double, Boolean>>();
        //a. vote by diversity score
        Collections.sort(allNEColumnCandidates, new Comparator<ColumnFeature>() {
            @Override
            public int compare(ColumnFeature o1, ColumnFeature o2) {
                int compared = new Integer(o1.getColId()).compareTo(o2.getColId());
                return compared;
            }
        });

        //find max scores to calculate score boosters
        Map<String, Double> max_scores_for_each_feature = new HashMap<String, Double>();
        for (int index=0; index<allNEColumnCandidates.size(); index++) {
            ColumnFeature cf = allNEColumnCandidates.get(index);
            double diversity = cf.getTokenValueDiversity() + cf.getCellValueDiversity();
            double cm = cf.getContextMatchScore();
            double wb = cf.getWebSearchScore();

            Double max_diversity = max_scores_for_each_feature.get("diversity");
            max_diversity=max_diversity==null?0.0:max_diversity;
            if(diversity>max_diversity)
                max_diversity=diversity;
            max_scores_for_each_feature.put("diversity",max_diversity);

            Double max_cm = max_scores_for_each_feature.get("cm");
            max_cm=max_cm==null?0.0:max_cm;
            if(cm>max_cm)
                max_cm=cm;
            max_scores_for_each_feature.put("cm",max_cm);

            Double max_wb = max_scores_for_each_feature.get("wb");
            max_wb=max_wb==null?0.0:max_wb;
            if(wb>max_wb)
                max_wb=wb;
            max_scores_for_each_feature.put("wb",max_wb);
        }

        for (int index=0; index<allNEColumnCandidates.size(); index++) {
            ColumnFeature cf = allNEColumnCandidates.get(index);
            double sum = 0.0;
            double diversity=0.0;
            if(use_token_diversity)
                diversity = cf.getTokenValueDiversity() + cf.getCellValueDiversity();
            else
                diversity=cf.getCellValueDiversity();
            double cm = cf.getContextMatchScore()*wb_cm_weight;
            double wb = cf.getWebSearchScore()*wb_cm_weight;
            double empty_cell_penalty = cf.getEmptyCells() / (double) cf.getNumRows();

            int max_component_score_booster=0;
            for(Map.Entry<String, Double> e: max_scores_for_each_feature.entrySet()){
                if(e.getKey().equals("diversity") && e.getValue()==diversity&&diversity!=0)
                    max_component_score_booster++;
                if(e.getKey().equals("cm") && e.getValue()==cm&&cm!=0)
                    max_component_score_booster++;
                if(e.getKey().equals("wb") && e.getValue()==wb&&wb!=0)
                    max_component_score_booster++;
            }
            max_component_score_booster=max_component_score_booster==0?1:max_component_score_booster;

            ObjObj<Double, Boolean> score_object = new ObjObj<Double, Boolean>();

            sum = diversity + cm + wb - empty_cell_penalty;
            if (cf.isCode_or_Acronym()){
                sum = sum - 1.0;
                score_object.setOtherObject(true);
            }
            else{
                score_object.setOtherObject(false);
            }
            if(use_max_score_boost)
                sum=Math.pow(sum, max_component_score_booster);


            //sum=sum/(index+1);
            if(score_normamlize_by_distance_to_first_col){
                sum=sum/Math.sqrt(index+1);
            }
            score_object.setMainObject(sum);
            scores.put(cf.getColId(), score_object);
        }

        return scores;
    }

    private void reset_relative_scores(List<ColumnFeature> allNEColumnCandidates) {
        //c. context matcher
        Collections.sort(allNEColumnCandidates, new Comparator<ColumnFeature>() {
            @Override
            public int compare(ColumnFeature o1, ColumnFeature o2) {
                return new Double(o2.getContextMatchScore()).compareTo(o1.getContextMatchScore());
            }
        });
        double maxContextMatchScore = allNEColumnCandidates.get(0).getContextMatchScore();
        if (maxContextMatchScore > 0) {
            for (ColumnFeature cf : allNEColumnCandidates) {
                double rel_score = cf.getContextMatchScore() / maxContextMatchScore;
                cf.setContextMatchScore(rel_score);
            }
        }

        //e. vote by search matcher
        Collections.sort(allNEColumnCandidates, new Comparator<ColumnFeature>() {
            @Override
            public int compare(ColumnFeature o1, ColumnFeature o2) {
                return new Double(o2.getWebSearchScore()).compareTo(o1.getWebSearchScore());
            }
        });
        double maxSearchMatchScore = allNEColumnCandidates.get(0).getWebSearchScore();
        if (maxSearchMatchScore > 0) {
            for (ColumnFeature cf : allNEColumnCandidates) {
                double rel_score = cf.getWebSearchScore() / maxSearchMatchScore;
                cf.setWebSearchScore(rel_score);
            }
        }
    }

    /*private void tiebreaker_reset(
            List<ColumnFeature> allNEColumnCandidates,
            Map<Integer, ObjObj<Double, Boolean>> inferenceScores) {
        List<Integer> ties = new ArrayList<Integer>();
        double maxScore = 0;
        for (Map.Entry<Integer, ObjObj<Double, Boolean>> e : inferenceScores.entrySet()) {
            if (e.getValue().getMainObject() > maxScore) {
                maxScore = e.getValue().getMainObject();
            }
        }
        for (Map.Entry<Integer, ObjObj<Double, Boolean>> e : inferenceScores.entrySet()) {
            if (e.getValue().getMainObject() == maxScore) {
                ties.add(e.getKey());
            }
        }

        if (ties.size() > 1) {

            double max = 0;
            int best = 0;
            for (int i : ties) {
                for (ColumnFeature cf : allNEColumnCandidates) {
                    if (cf.getColId() == i) {
                        double sum = cf.getCellValueDiversity() + cf.getContextMatchScore()
                                + cf.getTokenValueDiversity() + cf.getWebSearchScore()
                                - (cf.getEmptyCells() / (double) cf.getNumRows());

                        if (sum > max) {
                            max = sum;
                            best = i;
                        }
                        break;
                    }
                }
            }
            // try {
            inferenceScores.get(best).setMainObject(
                    inferenceScores.get(best).getMainObject() + 1.0
            );
            // } catch (NullPointerException n) {
            //     System.out.println();
            // }
        }
    }
*/
    //key: col id; value: score
    //currently performs following scoring: diversity; context match; 1st ne column; acronym column checker; search
    //results are collected as number of votes by each dimension
    private Map<Integer, ObjObj<Double, Boolean>> infer_multiFeatures_vote(List<ColumnFeature> allNEColumnCandidates) {
        Map<Integer, ObjObj<Double, Boolean>> votes = new HashMap<Integer, ObjObj<Double, Boolean>>();
        //a. vote by diversity score
        Collections.sort(allNEColumnCandidates, new Comparator<ColumnFeature>() {
            @Override
            public int compare(ColumnFeature o1, ColumnFeature o2) {
                int compared = new Double(o2.getCellValueDiversity()).compareTo(o1.getCellValueDiversity());
                if (compared == 0)
                    return new Double(o2.getTokenValueDiversity()).compareTo(o1.getTokenValueDiversity());
                return compared;
            }
        });
        double maxDiversityScore = -1.0;
        for (ColumnFeature cf : allNEColumnCandidates) {
            double diversity = cf.getTokenValueDiversity() + cf.getCellValueDiversity();
            if (diversity >= maxDiversityScore && diversity != 0) {
                maxDiversityScore = diversity;
                votes.put(cf.getColId(), new ObjObj<Double, Boolean>(1.0, false));
            } else
                break; //already sorted, so following this there shouldnt be higher diversity scores
        }


        //b. vote by 1st ne column
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (cf.isFirstNEColumn()) {
                ObjObj<Double, Boolean> entry = votes.get(cf.getColId());
                entry = entry == null ? new ObjObj<Double, Boolean>(0.0, false) : entry;
                Double vts = entry.getMainObject();
                vts = vts + 1.0;
                entry.setMainObject(vts);
                votes.put(cf.getColId(), entry);
                break;
            }
        }
        //c. vote by context matcher
        Collections.sort(allNEColumnCandidates, new Comparator<ColumnFeature>() {
            @Override
            public int compare(ColumnFeature o1, ColumnFeature o2) {
                return new Double(o2.getContextMatchScore()).compareTo(o1.getContextMatchScore());
            }
        });
        double maxContextMatchScore = -1.0;
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (cf.getContextMatchScore() >= maxContextMatchScore && cf.getContextMatchScore() != 0) {
                maxContextMatchScore = cf.getContextMatchScore();
                ObjObj<Double, Boolean> entry = votes.get(cf.getColId());
                entry = entry == null ? new ObjObj<Double, Boolean>(0.0, false) : entry;
                Double vts = entry.getMainObject();
                vts = vts + 1.0;
                entry.setMainObject(vts);
                votes.put(cf.getColId(), entry);
            } else
                break;
        }
        //d. vote by acronym columns
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (cf.isCode_or_Acronym()) {
                ObjObj<Double, Boolean> entry = votes.get(cf.getColId());
                entry = entry == null ? new ObjObj<Double, Boolean>(0.0, false) : entry;
                Double vts = entry.getMainObject();
                vts = vts - 1.0;
                entry.setMainObject(vts);
                entry.setOtherObject(true);
                votes.put(cf.getColId(), entry);
            }
        }

        //e. vote by search matcher
        Collections.sort(allNEColumnCandidates, new Comparator<ColumnFeature>() {
            @Override
            public int compare(ColumnFeature o1, ColumnFeature o2) {
                return new Double(o2.getWebSearchScore()).compareTo(o1.getWebSearchScore());
            }
        });
        double maxSearchMatchScore = -1.0;
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (cf.getWebSearchScore() >= maxSearchMatchScore && cf.getWebSearchScore() != 0) {
                maxSearchMatchScore = cf.getWebSearchScore();
                ObjObj<Double, Boolean> entry = votes.get(cf.getColId());
                entry = entry == null ? new ObjObj<Double, Boolean>(0.0, false) : entry;
                Double vts = entry.getMainObject();
                vts = vts + 1.0;
                entry.setMainObject(vts);
                votes.put(cf.getColId(), entry);
            } else
                break;
        }

        for (ColumnFeature cf : allNEColumnCandidates) {
            if (votes.containsKey(cf.getColId()))
                continue;
            votes.put(cf.getColId(), new ObjObj<Double, Boolean>(0.0, false));
        }
        return votes;
    }

    /*private Map<Integer, ObjObj<Double, Boolean>> infer_multiFeatures(List<ColumnFeature> allNEColumnCandidates) {
        Map<Integer, ObjObj<Double, Boolean>> votes = new HashMap<Integer, ObjObj<Double, Boolean>>();
        //a. vote by diversity score
        Collections.sort(allNEColumnCandidates, new Comparator<ColumnFeature>() {
            @Override
            public int compare(ColumnFeature o1, ColumnFeature o2) {
                int compared = new Double(o2.getCellValueDiversity()).compareTo(o1.getCellValueDiversity());
                if (compared == 0)
                    return new Double(o2.getTokenValueDiversity()).compareTo(o1.getTokenValueDiversity());
                return compared;
            }
        });
        ColumnFeature first = allNEColumnCandidates.get(0);
        double maxDiversityScore = first.getCellValueDiversity() + first.getTokenValueDiversity();
        for (ColumnFeature cf : allNEColumnCandidates) {
            double diversity = cf.getTokenValueDiversity() + cf.getCellValueDiversity();
            double rel_diversity = diversity / maxDiversityScore;
            ObjObj<Double, Boolean> entry = votes.get(cf.getColId());
            entry = entry == null ? new ObjObj<Double, Boolean>(0.0, false) : entry;
            Double score = entry.getMainObject();
            score = score + rel_diversity;
            entry.setMainObject(score);
            votes.put(cf.getColId(), entry);
        }

        //b. vote by 1st ne column
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (cf.isFirstNEColumn()) {
                ObjObj<Double, Boolean> entry = votes.get(cf.getColId());
                entry = entry == null ? new ObjObj<Double, Boolean>(0.0, false) : entry;
                Double vts = entry.getMainObject();
                vts = vts + 1.0;
                entry.setMainObject(vts);
                votes.put(cf.getColId(), entry);
                break;
            }
        }
        //c. vote by context matcher
        Collections.sort(allNEColumnCandidates, new Comparator<ColumnFeature>() {
            @Override
            public int compare(ColumnFeature o1, ColumnFeature o2) {
                return new Double(o2.getContextMatchScore()).compareTo(o1.getContextMatchScore());
            }
        });
        double maxContextMatchScore = allNEColumnCandidates.get(0).getContextMatchScore();
        for (ColumnFeature cf : allNEColumnCandidates) {
            double rel_score = cf.getContextMatchScore() / maxContextMatchScore;
            ObjObj<Double, Boolean> entry = votes.get(cf.getColId());
            entry = entry == null ? new ObjObj<Double, Boolean>(0.0, false) : entry;
            Double score = entry.getMainObject();
            score = score + rel_score;
            entry.setMainObject(score);
            votes.put(cf.getColId(), entry);
        }

        //d. vote by acronym columns
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (cf.isCode_or_Acronym()) {
                ObjObj<Double, Boolean> entry = votes.get(cf.getColId());
                entry = entry == null ? new ObjObj<Double, Boolean>(0.0, false) : entry;
                entry.setOtherObject(true);
                votes.put(cf.getColId(), entry);
            }
        }

        //e. vote by search matcher
        Collections.sort(allNEColumnCandidates, new Comparator<ColumnFeature>() {
            @Override
            public int compare(ColumnFeature o1, ColumnFeature o2) {
                return new Double(o2.getWebSearchScore()).compareTo(o1.getWebSearchScore());
            }
        });
        double maxSearchMatchScore = allNEColumnCandidates.get(0).getWebSearchScore();
        for (ColumnFeature cf : allNEColumnCandidates) {
            double rel_score = cf.getWebSearchScore()/maxSearchMatchScore;
            ObjObj<Double, Boolean> entry = votes.get(cf.getColId());
            entry = entry == null ? new ObjObj<Double, Boolean>(0.0, false) : entry;
            Double score = entry.getMainObject();
            score = score + rel_score;
            entry.setMainObject(score);
            votes.put(cf.getColId(), entry);
        }

        for (ColumnFeature cf : allNEColumnCandidates) {
            if (votes.containsKey(cf.getColId()))
                continue;
            votes.put(cf.getColId(), new ObjObj<Double, Boolean>(0.0, false));
        }
        return votes;
    }*/

    //key: col id; value: score
    //currently performs following scoring: diversity; context match; 1st ne column; NO search
    //results are collected as number of votes by each dimension
    private Map<Integer, Double> infer_multiFeatures_without_search(List<ColumnFeature> allNEColumnCandidates) {
        Map<Integer, Double> votes = new HashMap<Integer, Double>();
        //a. vote by diversity score
        Collections.sort(allNEColumnCandidates, new Comparator<ColumnFeature>() {
            @Override
            public int compare(ColumnFeature o1, ColumnFeature o2) {
                int compared = new Double(o2.getCellValueDiversity()).compareTo(o1.getCellValueDiversity());
                if (compared == 0)
                    return new Double(o2.getTokenValueDiversity()).compareTo(o1.getTokenValueDiversity());
                return compared;
            }
        });
        double maxDiversityScore = -1.0;
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (maxDiversityScore == -1.0) {
                maxDiversityScore = cf.getCellValueDiversity() + cf.getTokenValueDiversity();
                votes.put(cf.getColId(), 1.0);
            } else if ((cf.getCellValueDiversity() + cf.getTokenValueDiversity()) < maxDiversityScore)
                break;
            else
                votes.put(cf.getColId(), 1.0);
        }
        //b. vote by 1st ne column
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (cf.isFirstNEColumn()) {
                Double vts = votes.get(cf.getColId());
                vts = vts == null ? 0 : vts;
                vts = vts + 1.0;
                votes.put(cf.getColId(), vts);
            }
        }
        //c. vote by context matcher
        Collections.sort(allNEColumnCandidates, new Comparator<ColumnFeature>() {
            @Override
            public int compare(ColumnFeature o1, ColumnFeature o2) {
                return new Double(o2.getContextMatchScore()).compareTo(o1.getContextMatchScore());
            }
        });
        double maxContextMatchScore = -1.0;
        for (ColumnFeature cf : allNEColumnCandidates) {
            if (maxContextMatchScore == -1.0) {
                maxContextMatchScore = cf.getContextMatchScore();
                Double vts = votes.get(cf.getColId());
                vts = vts == null ? 0 : vts;
                vts = vts + 1;
                votes.put(cf.getColId(), vts);
            } else if (cf.getContextMatchScore() < maxContextMatchScore)
                break;
            else {
                Double vts = votes.get(cf.getColId());
                vts = vts == null ? 0 : vts;
                vts = vts + 1;
                votes.put(cf.getColId(), vts);
            }
        }
        return votes;
    }

}
