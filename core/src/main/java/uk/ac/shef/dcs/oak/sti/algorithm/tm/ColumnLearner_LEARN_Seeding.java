package uk.ac.shef.dcs.oak.sti.algorithm.tm;

import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.selector.CellSelector;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.stopping.StoppingCriteria;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.stopping.StoppingCriteriaInstantiator;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.io.IOException;
import java.util.*;

/**
 * this class interprets a column. it does classification of the column, then disambiguate entities in the column
 */


public class ColumnLearner_LEARN_Seeding {
    private CellSelector selector;
    private KnowledgeBaseSearcher kbSearcher;
    private Disambiguator disambiguation_learn;
    private ClassificationScorer classifier_learn;

    private String stopperClassname;
    private String[] stopperParams;

    public ColumnLearner_LEARN_Seeding(CellSelector selector,
                                       String stoppingCriteriaClassname,
                                       String[] stoppingCriteriaParams,
                                       KnowledgeBaseSearcher candidateFinder,
                                       Disambiguator disambiguation_learn,
                                       ClassificationScorer algorithm) {
        this.selector = selector;
        this.kbSearcher = candidateFinder;
        this.disambiguation_learn = disambiguation_learn;
        this.classifier_learn = algorithm;

        this.stopperClassname = stoppingCriteriaClassname;
        this.stopperParams = stoppingCriteriaParams;
    }

    public ObjObj<Integer, List<List<Integer>>> learn_seeding(LTable table, LTableAnnotation table_annotation, int column, Integer... skipRows) throws IOException {
        StoppingCriteria stopper = StoppingCriteriaInstantiator.instantiate(stopperClassname, stopperParams);

        //1. gather list of strings from this column to be interpreted
        List<List<Integer>> ranking = selector.select(table, column, table_annotation.getSubjectColumn());

        //3. score column and also disambiguate initial rows in the selected sample
        Map<Integer, List<ObjObj<EntityCandidate, Map<String, Double>>>> candidates_and_scores_for_each_row =
                new HashMap<Integer, List<ObjObj<EntityCandidate, Map<String, Double>>>>();
        Set<HeaderAnnotation> headerAnnotationScores = new HashSet<HeaderAnnotation>();

        int countProcessed = 0, totalRows=0;
        boolean stopped = false;
        Map<Object, Double> state = new HashMap<Object, Double>();

        for (List<Integer> rows_indexes : ranking) {

            /* if(row_index<39)
            continue;*/
            /* if(row_index==13)
            System.out.println();*/
            countProcessed++;    totalRows+=rows_indexes.size();
            //find candidate entities
            LTableContentCell sample = table.getContentCell(rows_indexes.get(0), column);
            /*if (sample.getType().equals(DataTypeClassifier.DataType.LONG_TEXT)) {
                System.out.println("\t\t>>> Long text cell skipped: " + rows_indexes + "," + column + " " + sample.getText());
                continue;
            }*/
            if (sample.getText().length() < 2) {
                System.out.println("\t\t>>> Very short text cell skipped: " + rows_indexes + "," + column + " " + sample.getText());
                continue;
            }

            System.out.println("\t>> Classification-LEARN(Seeding), row(s) " + rows_indexes + "," + sample);

            boolean skip = false;
            for (int row : skipRows) {
                if (rows_indexes.contains(row)) {
                    skip = true;
                    break;
                }
            }

            List<ObjObj<EntityCandidate, Map<String, Double>>> candidates_and_scores_on_this_block;
            if (skip) {
                candidates_and_scores_on_this_block = collect_existing(table_annotation, rows_indexes, column);
            } else {
                List<EntityCandidate> candidates = kbSearcher.findEntitiesForCell(sample);
                //do disambiguation scoring
                candidates_and_scores_on_this_block =
                        disambiguation_learn.disambiguate_learn_seeding(
                                candidates, table, rows_indexes, column
                        );
                for (int ri : rows_indexes) {
                    candidates_and_scores_for_each_row.put(ri, candidates_and_scores_on_this_block);
                }
            }

            //run algorithm to learn column typing; header annotation scores are updated constantly, but supporting rows are not.
            state = createState(
                    classifier_learn.score(candidates_and_scores_on_this_block, headerAnnotationScores, table, rows_indexes, column),
                    //table.getNumRows()
                    totalRows
            );
            boolean stop = stopper.stop(state, table.getNumRows());

            if (stop) {
                System.out.println("\t>> Classification-LEARN(seeding) converged, rows:"+totalRows);
                //state is stable. annotate using the type, and disambiguate entities
                create_typing_annotations(state, table_annotation, column);
                //then use classification results to revise disambiguation
                System.out.println("\t>> Disambiguation-LEARN(seeding revise " + candidates_and_scores_for_each_row.size() + " rows)");
                revise_disambiguation_and_create_annotations(table_annotation, table, candidates_and_scores_for_each_row, column);
                stopped = true;
                break;  //exit loop
            }
        }

        if (!stopped) {
            System.out.println("\t>> Classification-LEARN(seeding) no convergence");
            create_typing_annotations(state, table_annotation, column); //supporting rows not added
            revise_disambiguation_and_create_annotations(table_annotation, table, candidates_and_scores_for_each_row, column);
        }
        return new ObjObj<Integer, List<List<Integer>>>(countProcessed, ranking);
    }

    private List<ObjObj<EntityCandidate, Map<String, Double>>> collect_existing(LTableAnnotation table_annotation, List<Integer> rows_indexes, int column) {
        List<ObjObj<EntityCandidate, Map<String, Double>>> candidates = new ArrayList<ObjObj<EntityCandidate, Map<String, Double>>>();
        for (int row : rows_indexes) {
            CellAnnotation[] annotations = table_annotation.getContentCellAnnotations(row, column);
            for (CellAnnotation can : annotations) {
                EntityCandidate ec = can.getAnnotation();
                Map<String, Double> scoreElements = can.getScore_element_map();
                scoreElements.put(CellAnnotation.SCORE_FINAL, can.getFinalScore());
                candidates.add(new ObjObj<EntityCandidate, Map<String, Double>>(ec, scoreElements));
            }

        }
        return candidates;
    }

    private Map<Object, Double> createState(
            Set<HeaderAnnotation> scores, int tableRowsTotal) {
        Map<Object, Double> state = new HashMap<Object, Double>();
        for (HeaderAnnotation ha : scores) {
            //Map<String, Double> scoreElements =ha.getScoreElements();
            ha.getScoreElements().put(
                    HeaderAnnotation.FINAL,
                    classifier_learn.compute_final_score(ha, tableRowsTotal).get(HeaderAnnotation.FINAL)
            );
            state.put(ha, ha.getFinalScore());
        }
        return state;
    }


    private void revise_disambiguation_and_create_annotations(LTableAnnotation table_annotation,
                                                              LTable table,
                                                              Map<Integer, List<ObjObj<EntityCandidate, Map<String, Double>>>> candidates_and_scores_for_each_row,
                                                              int column) {
        List<HeaderAnnotation> bestHeaderAnnotations = table_annotation.getBestHeaderAnnotations(column);
        List<String> types = new ArrayList<String>();
        for (HeaderAnnotation ha : bestHeaderAnnotations)
            types.add(ha.getAnnotation_url());
        for (Map.Entry<Integer, List<ObjObj<EntityCandidate, Map<String, Double>>>> e :
                candidates_and_scores_for_each_row.entrySet()) {

            int row = e.getKey();
            List<ObjObj<EntityCandidate, Map<String, Double>>> entities_for_this_cell_and_scores = e.getValue();
            if (entities_for_this_cell_and_scores.size() == 0)
                continue;

            List<ObjObj<EntityCandidate, Map<String, Double>>> revised = disambiguation_learn.revise(entities_for_this_cell_and_scores, types);
            if (revised.size() != 0)
                entities_for_this_cell_and_scores = revised;
            List<EntityCandidate> best_entities = create_entity_annotations(table, table_annotation, row, column, entities_for_this_cell_and_scores
            ); //supporting rows are added here, impossible other places
            update_typing_supporting_rows(best_entities, row, column, table_annotation);
        }
    }

    //assigns highest scoring column_type_label to the column;
    //then disambiguate those rows that contributed to the prediction to column_type_scorings
    //WARNING: SUPPORTING ROWS NOT ADDED HERE
    private void create_typing_annotations(final Map<Object, Double> state,
                                           LTableAnnotation table_annotation,
                                           int column) {
        if (state.size() > 0) {
            List<Object> candidate_header_annotations = new ArrayList<Object>(state.keySet());
            Collections.sort(candidate_header_annotations, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    return state.get(o2).compareTo(state.get(o1));
                }
            });
            //insert column type annotations
            HeaderAnnotation[] final_header_annotations = new HeaderAnnotation[candidate_header_annotations.size()];
            for (int i = 0; i < candidate_header_annotations.size(); i++)
                final_header_annotations[i] = (HeaderAnnotation) candidate_header_annotations.get(i);
            table_annotation.setHeaderAnnotation(column, final_header_annotations);
        }
    }

    private List<EntityCandidate> create_entity_annotations(
            LTable table,
            LTableAnnotation table_annotation,
            int table_cell_row,
            int table_cell_col,
            List<ObjObj<EntityCandidate, Map<String, Double>>> candidates_and_scores_for_cell) {

        Collections.sort(candidates_and_scores_for_cell, new Comparator<ObjObj<EntityCandidate, Map<String, Double>>>() {
            @Override
            public int compare(ObjObj<EntityCandidate, Map<String, Double>> o1, ObjObj<EntityCandidate, Map<String, Double>> o2) {
                Double o2_score = o2.getOtherObject().get(CellAnnotation.SCORE_FINAL);
                Double o1_score = o1.getOtherObject().get(CellAnnotation.SCORE_FINAL);
                return o2_score.compareTo(o1_score);
            }
        });

        double max = 0.0;
        CellAnnotation[] annotationsForCell = new CellAnnotation[candidates_and_scores_for_cell.size()];
        for (int i = 0; i < candidates_and_scores_for_cell.size(); i++) {
            ObjObj<EntityCandidate, Map<String, Double>> e = candidates_and_scores_for_cell.get(i);
            double score = e.getOtherObject().get(CellAnnotation.SCORE_FINAL);
            if (score > max)
                max = score;
            annotationsForCell[i] = new CellAnnotation(table.getContentCell(table_cell_row, table_cell_col).getText(),
                    e.getMainObject(), e.getOtherObject().get(CellAnnotation.SCORE_FINAL), e.getOtherObject());

        }
        table_annotation.setContentCellAnnotations(table_cell_row, table_cell_col, annotationsForCell);

        List<EntityCandidate> best = new ArrayList<EntityCandidate>();
        for (int i = 0; i < candidates_and_scores_for_cell.size(); i++) {
            ObjObj<EntityCandidate, Map<String, Double>> e = candidates_and_scores_for_cell.get(i);
            double score = e.getOtherObject().get(CellAnnotation.SCORE_FINAL);
            if (score == max)
                best.add(e.getMainObject());
        }
        return best;
    }

    private void update_typing_supporting_rows(List<EntityCandidate> bestCandidates,
                                               int row,
                                               int column,
                                               LTableAnnotation table_annotation) {
        HeaderAnnotation[] headers = table_annotation.getHeaderAnnotation(column);
        if (headers != null) {
            for (HeaderAnnotation ha : headers) {
                for (EntityCandidate ec : bestCandidates) {
                    if (ec.getTypeIds().contains(ha.getAnnotation_url()))
                        ha.addSupportingRow(row);
                }
            }
        }
    }
}
