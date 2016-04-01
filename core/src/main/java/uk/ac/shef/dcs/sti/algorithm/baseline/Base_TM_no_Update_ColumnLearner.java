package uk.ac.shef.dcs.sti.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.*;

import java.util.*;

/**
 */
public class Base_TM_no_Update_ColumnLearner {

    private KBSearch kbSearch;
    private Base_TM_no_Update_Disambiguator disambiguation_learn;
    private Base_TM_no_Update_ClassificationScorer classifier_learn;


    public Base_TM_no_Update_ColumnLearner(
            KBSearch candidateFinder,
            Base_TM_no_Update_Disambiguator disambiguation_learn,
            Base_TM_no_Update_ClassificationScorer algorithm) {
        this.kbSearch = candidateFinder;
        this.disambiguation_learn = disambiguation_learn;
        this.classifier_learn = algorithm;

    }

    public void learn(Table table, TAnnotation table_annotation, int column, Integer... skipRows) throws KBSearchException {

        //1. gather list of strings from this column to be interpreted

        //3. computeElementScores column and also disambiguate initial rows in the selected sample
        Map<Integer, List<Pair<Entity, Map<String, Double>>>> candidates_and_scores_for_each_row =
                new HashMap<>();
        Set<TColumnHeaderAnnotation> headerAnnotationScores = new HashSet<TColumnHeaderAnnotation>();

        int countRows = 0;
        Map<Object, Double> state = new HashMap<Object, Double>();

        for (int row_index = 0; row_index < table.getNumRows(); row_index++) {

            /* if(row_index<39)
            continue;*/
            /* if(row_index==13)
            System.out.println();*/
            countRows++;
            //find candidate entities
            TCell tcc = table.getContentCell(row_index, column);
            System.out.println("\t>> Classification-LEARN, row " + row_index + "," + tcc);
            if (tcc.getText().length() < 2) {
                System.out.println("\t\t>>> Very short text cell skipped: " + row_index + "," + column + " " + tcc.getText());
                continue;
            }

            boolean skip = false;
            for (int row : skipRows) {
                if (row == row_index) {
                    skip = true;
                    break;
                }
            }

            List<Pair<Entity, Map<String, Double>>> candidates_and_scores_on_this_row;
            if (skip) {
                candidates_and_scores_on_this_row = collect_existing(table_annotation, row_index, column);
            } else {
                List<Entity> candidates = kbSearch.findEntityCandidates(tcc.getText());
                //do disambiguation scoring
                candidates_and_scores_on_this_row =
                        disambiguation_learn.disambiguate_learn(
                                candidates, table, row_index, column
                        );
                candidates_and_scores_for_each_row.put(row_index, candidates_and_scores_on_this_row);
            }
            //todo: wrong, state should be created based on the map object
            //run algorithm to runPreliminaryColumnClassifier column typing; header annotation scores are updated constantly, but supporting rows are not.
            state = update_column_class(
                    classifier_learn.score(candidates_and_scores_on_this_row, headerAnnotationScores, table, row_index, column),
                    table.getNumRows()
            );


        }


        System.out.println("\t>> All rows processed");
        create_typing_annotations(state, table_annotation, column); //supporting rows not added
        revise_disambiguation_and_create_annotation(table_annotation, table, candidates_and_scores_for_each_row, column);

    }

    private List<Pair<Entity, Map<String, Double>>> collect_existing(TAnnotation table_annotation, int row_index, int column) {
        List<Pair<Entity, Map<String, Double>>> candidates = new ArrayList<>();
        TCellAnnotation[] annotations = table_annotation.getContentCellAnnotations(row_index, column);
        for (TCellAnnotation can : annotations) {
            Entity ec = can.getAnnotation();
            Map<String, Double> scoreElements = can.getScoreElements();
            scoreElements.put(TCellAnnotation.SCORE_FINAL, can.getFinalScore());
            candidates.add(new Pair<>(ec, scoreElements));
        }

        return candidates;
    }

    private Map<Object, Double> update_column_class(
            Set<TColumnHeaderAnnotation> scores, int tableRowsTotal) {
        Map<Object, Double> state = new HashMap<Object, Double>();
        for (TColumnHeaderAnnotation ha : scores) {
            //Map<String, Double> scoreElements =ha.getScoreElements();
            ha.getScoreElements().put(
                    TColumnHeaderAnnotation.FINAL,
                    classifier_learn.compute_final_score(ha, tableRowsTotal).get(TColumnHeaderAnnotation.FINAL)
            );
            state.put(ha, ha.getFinalScore());
        }
        return state;
    }


    private void revise_disambiguation_and_create_annotation(TAnnotation table_annotation,
                                                             Table table,
                                                             Map<Integer, List<Pair<Entity, Map<String, Double>>>> candidates_and_scores_for_each_row,
                                                             int column) {
        List<TColumnHeaderAnnotation> bestHeaderAnnotations = table_annotation.getWinningHeaderAnnotations(column);
        List<String> types = new ArrayList<String>();
        for (TColumnHeaderAnnotation ha : bestHeaderAnnotations)
            types.add(ha.getAnnotation().getId());
        for (Map.Entry<Integer, List<Pair<Entity, Map<String, Double>>>> e :
                candidates_and_scores_for_each_row.entrySet()) {

            int row = e.getKey();
            List<Pair<Entity, Map<String, Double>>> entities_for_this_cell_and_scores = e.getValue();
            if (entities_for_this_cell_and_scores.size() == 0)
                continue;

            List<Pair<Entity, Map<String, Double>>> revised = disambiguation_learn.revise(entities_for_this_cell_and_scores, types);
            if (revised.size() != 0)
                entities_for_this_cell_and_scores = revised;
            List<Entity> best_entities = create_entity_annotations(table, table_annotation, row, column, entities_for_this_cell_and_scores
            ); //supporting rows are added here, impossible other places
            update_typing_supporting_rows(best_entities, row, column, table_annotation);
        }
    }

    //assigns highest scoring column_type_label to the column;
    //then disambiguate those rows that contributed to the prediction to column_type_scorings
    //WARNING: SUPPORTING ROWS NOT ADDED HERE
    private void create_typing_annotations(final Map<Object, Double> state,
                                           TAnnotation table_annotation,
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
            TColumnHeaderAnnotation[] final_header_annotations = new TColumnHeaderAnnotation[candidate_header_annotations.size()];
            for (int i = 0; i < candidate_header_annotations.size(); i++)
                final_header_annotations[i] = (TColumnHeaderAnnotation) candidate_header_annotations.get(i);
            table_annotation.setHeaderAnnotation(column, final_header_annotations);
        }
    }

    private List<Entity> create_entity_annotations(
            Table table,
            TAnnotation table_annotation,
            int table_cell_row,
            int table_cell_col,
            List<Pair<Entity, Map<String, Double>>> candidates_and_scores_for_cell) {

        Collections.sort(candidates_and_scores_for_cell, new Comparator<Pair<Entity, Map<String, Double>>>() {
            @Override
            public int compare(Pair<Entity, Map<String, Double>> o1, Pair<Entity, Map<String, Double>> o2) {
                Double o2_score = o2.getValue().get("final");
                Double o1_score = o1.getValue().get("final");
                return o2_score.compareTo(o1_score);
            }
        });

        double max = 0.0;
        TCellAnnotation[] annotationsForCell = new TCellAnnotation[candidates_and_scores_for_cell.size()];
        for (int i = 0; i < candidates_and_scores_for_cell.size(); i++) {
            Pair<Entity, Map<String, Double>> e = candidates_and_scores_for_cell.get(i);
            double score = e.getValue().get("final");
            if (score > max)
                max = score;
            annotationsForCell[i] = new TCellAnnotation(table.getContentCell(table_cell_row, table_cell_col).getText(),
                    e.getKey(), e.getValue().get("final"), e.getValue());

        }
        table_annotation.setContentCellAnnotations(table_cell_row, table_cell_col, annotationsForCell);

        List<Entity> best = new ArrayList<>();
        for (int i = 0; i < candidates_and_scores_for_cell.size(); i++) {
            Pair<Entity, Map<String, Double>> e = candidates_and_scores_for_cell.get(i);
            double score = e.getValue().get("final");
            if (score == max)
                best.add(e.getKey());
        }
        return best;
    }

    private void update_typing_supporting_rows(List<Entity> bestCandidates,
                                               int row,
                                               int column,
                                               TAnnotation table_annotation) {
        TColumnHeaderAnnotation[] headers = table_annotation.getHeaderAnnotation(column);
        if (headers != null) {
            for (TColumnHeaderAnnotation ha : headers) {
                for (Entity ec : bestCandidates) {
                    if (ec.getTypeIds().contains(ha.getAnnotation().getId()))
                        ha.addSupportingRow(row);
                }
            }
        }
    }
}
