package uk.ac.shef.dcs.sti.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.*;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 25/02/14
 * Time: 14:54
 * To change this template use File | Settings | File Templates.
 */
public class Base_NameMatch_ColumnLearner {
    private KBSearch kbSearch;
    private Base_NameMatch_Disambiguator disambiguator;


    public Base_NameMatch_ColumnLearner(
            KBSearch candidateFinder,
            Base_NameMatch_Disambiguator disambiguation_learn) {
        this.kbSearch = candidateFinder;
        this.disambiguator = disambiguation_learn;

    }

    public void interpret(Table table, TAnnotation table_annotation, int column, Integer... skipRows) throws KBSearchException {
        Map<Integer, List<Pair<Entity, Map<String, Double>>>> candidate_for_each_row =
                new HashMap<>();
        Set<TColumnHeaderAnnotation> headerAnnotationScores = new HashSet<TColumnHeaderAnnotation>();


        for (int row_index = 0; row_index < table.getNumRows(); row_index++) {

            /* if(row_index==13)
            System.out.println();*/
            //find candidate entities
            TCell tcc = table.getContentCell(row_index, column);
            System.out.println("\t>> Classification-, row " + row_index + "," + tcc);

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

            List<Pair<Entity, Map<String, Double>>> disamb_result = null;
            if (skip) {
                disamb_result = collect_existing(table_annotation, row_index, column);
            } else {
                List<Entity> candidates = kbSearch.findEntityCandidates(tcc.getText());
                disamb_result =
                        disambiguator.disambiguate(candidates, table, row_index, column);
                if (disamb_result != null && disamb_result.size() > 0) {
                    candidate_for_each_row.put(row_index, disamb_result);
                }
            }
        }

        Map<Object, Double> state = new HashMap<Object, Double>();
        for (Map.Entry<Integer, List<Pair<Entity, Map<String, Double>>>> e : candidate_for_each_row.entrySet()) {
            List<Pair<Entity, Map<String, Double>>> container = e.getValue();
            if (container.size() > 0) {
                Entity ec = container.get(0).getKey();
                Set<String> types = ec.getTypeIds();
                for (String t : types) {
                    Double score = state.get(t);
                    if (score == null)
                        score = 0.0;
                    score += 1.0;
                    state.put(t, score);
                }
            }
        }
        create_typing_annotations(state, table_annotation, column); //supporting rows not added
        disambiguate(table_annotation, table, candidate_for_each_row, column);

    }

    private List<Pair<Entity, Map<String, Double>>> collect_existing(TAnnotation table_annotation, int row_index, int column) {
        List<Pair<Entity, Map<String, Double>>> candidates = new ArrayList<>();
        TCellAnnotation[] annotations = table_annotation.getContentCellAnnotations(row_index, column);
        for (TCellAnnotation can : annotations) {
            Entity ec = can.getAnnotation();
            Map<String, Double> scoreElements = can.getScoreElements();
            scoreElements.put(TCellAnnotation.SCORE_FINAL, can.getFinalScore());
            candidates.add(new Pair<Entity, Map<String, Double>>(ec, scoreElements));
        }

        return candidates;
    }



    private void disambiguate(TAnnotation table_annotation,
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

            List<Pair<Entity, Map<String, Double>>> revised = disambiguator.revise(entities_for_this_cell_and_scores, types);
            if (revised.size() != 0)
                entities_for_this_cell_and_scores = revised;

            List<Entity> best_entities =
                    create_entity_annotations(table, table_annotation, row, column, entities_for_this_cell_and_scores
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
        List<Object> candidate_header_annotations = new ArrayList<Object>(state.keySet());
        Collections.sort(candidate_header_annotations, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return state.get(o2).compareTo(state.get(o1));
            }
        });
        //insert column type annotations
        TColumnHeaderAnnotation[] final_header_annotations = new TColumnHeaderAnnotation[candidate_header_annotations.size()];
        for (int i = 0; i < candidate_header_annotations.size(); i++){
            String url = candidate_header_annotations.get(i).toString();
            TColumnHeaderAnnotation ha = new TColumnHeaderAnnotation(url,new Clazz(url,url),state.get(url));
            final_header_annotations[i] = ha;
        }
        table_annotation.setHeaderAnnotation(column, final_header_annotations);
    }

    private List<Entity> create_entity_annotations(
            Table table,
            TAnnotation table_annotation,
            int table_cell_row,
            int table_cell_col,
            List<Pair<Entity, Map<String, Double>>> candidates_and_scores_for_cell) {

        if (candidates_and_scores_for_cell.size() > 0) {
            TCellAnnotation[] annotationsForCell = new TCellAnnotation[1];
            Entity e = candidates_and_scores_for_cell.get(0).getKey();
            annotationsForCell[0] = new TCellAnnotation(table.getContentCell(table_cell_row, table_cell_col).getText(),
                    e, 1.0, new HashMap<String, Double>());
            table_annotation.setContentCellAnnotations(table_cell_row, table_cell_col, annotationsForCell);
        }

        List<Entity> best = new ArrayList<>();
        for (int i = 0; i < candidates_and_scores_for_cell.size(); i++) {
            Pair<Entity, Map<String, Double>> e = candidates_and_scores_for_cell.get(i);

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


    public void update_typing_annotations_best_candidate_contribute(List<Integer> rowsUpdated,
                                                                    int column,
                                                                    TAnnotation table_annotations,
                                                                    int tableRowsTotal) {
        TColumnHeaderAnnotation[] header_annotations = table_annotations.getHeaderAnnotation(column);
        //supporting rows are only added if a header for the type of the cell annotation exists
        if (header_annotations != null) {
            for (int row : rowsUpdated) {
                List<TCellAnnotation> bestCellAnnotations = table_annotations.getWinningContentCellAnnotation(row, column);

                for (TCellAnnotation ca : bestCellAnnotations) {
                    for (TColumnHeaderAnnotation ha : header_annotations) {
                        if (ca.getAnnotation().hasType(ha.getAnnotation().getId())) {
                            ha.addSupportingRow(row);
                        }
                        /* if(ha.getAnnotation().equals("/periodicals/newspaper_circulation_area"))
                        p.println(cAnn.getHeaderText()+","+ha.getFinalScore());*/
                    }
                }
                //p.close();

            }

            //final update to compute revised typing scores, then sort them
            List<TColumnHeaderAnnotation> resort = new ArrayList<TColumnHeaderAnnotation>(Arrays.asList(header_annotations));
            Collections.sort(resort);
            table_annotations.setHeaderAnnotation(column, resort.toArray(new TColumnHeaderAnnotation[0]));
        }
    }
}