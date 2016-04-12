package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 25/02/14
 * Time: 14:54
 * To change this template use File | Settings | File Templates.
 */
public class TCellDisambiguatorNameMatch {
    private static final Logger LOG = Logger.getLogger(TCellDisambiguatorNameMatch.class.getName());
    private KBSearch kbSearch;

    public TCellDisambiguatorNameMatch(
            KBSearch candidateFinder) {
        this.kbSearch = candidateFinder;

    }

    public Map<Integer, List<Pair<Entity, Map<String, Double>>>> disambiguate(Table table, TAnnotation table_annotation, int column, Integer... skipRows) throws KBSearchException {
        Map<Integer, List<Pair<Entity, Map<String, Double>>>> rowIndex_and_entities =
                new HashMap<>();

        for (int row_index = 0; row_index < table.getNumRows(); row_index++) {
            TCell tcc = table.getContentCell(row_index, column);
            LOG.info("\t>> Disambiguation: row=" + row_index + "," + tcc);

            if (tcc.getText().length() < 2) {
                LOG.debug("\t\t>> Very short text cell skipped: " + row_index + "," + column + " " + tcc.getText());
                continue;
            }

            boolean skip = false;
            for (int row : skipRows) {
                if (row == row_index) {
                    skip = true;
                    break;
                }
            }

            List<Pair<Entity, Map<String, Double>>> disambResult;
            if (skip) {
                collectExistingAnnotations(table_annotation, row_index, column);
            } else {
                List<Entity> candidates = kbSearch.findEntityCandidates(tcc.getText());
                disambResult =
                        disambiguate(candidates, table, row_index, column);
                if (disambResult != null && disambResult.size() > 0) {
                    rowIndex_and_entities.put(row_index, disambResult);
                }
            }
        }
        return rowIndex_and_entities;
    }

    private List<Pair<Entity, Map<String, Double>>> collectExistingAnnotations(
            TAnnotation tableAnnotation, int row, int column) {
        List<Pair<Entity, Map<String, Double>>> candidates = new ArrayList<>();
        TCellAnnotation[] annotations = tableAnnotation.getContentCellAnnotations(row, column);
        for (TCellAnnotation can : annotations) {
            Entity ec = can.getAnnotation();
            Map<String, Double> scoreElements = can.getScoreElements();
            scoreElements.put(TCellAnnotation.SCORE_FINAL, can.getFinalScore());
            candidates.add(new Pair<>(ec, scoreElements));
        }

        return candidates;
    }

    private List<Pair<Entity, Map<String, Double>>> disambiguate(List<Entity> candidates, Table table,
                                                                 int entity_row, int entity_column
    ) {
        //name match: either find the one with identical name, or pick the highest rank (assuming input candidates is sorted)
        LOG.info("\t\t>> (disambiguation, position at [" + entity_row + "," + entity_column + "]: " + table.getContentCell(entity_row, entity_column) +
                " candidates=" + candidates.size() + ")");
        List<Pair<Entity, Map<String, Double>>> disambiguationScores = new ArrayList<>();
        if (candidates.size() > 0) {
            List<Entity> candidatesCopy = new ArrayList<>();
            for (Entity ec : candidates) {
                TCell tcc = table.getContentCell(entity_row, entity_column);
                if (tcc.getText() != null) {
                    if (ec.getLabel().equalsIgnoreCase(tcc.getText().trim()))
                        candidatesCopy.add(ec);
                }
            }

            if (candidatesCopy.size() > 0) {
                disambiguationScores.add(new Pair<>(
                        candidatesCopy.get(0), new HashMap<>()
                ));
            } else {
                disambiguationScores.add(new Pair<>(
                        candidates.get(0), new HashMap<>()
                ));
            }

        }
        return disambiguationScores;
    }

    public void revise(TAnnotation tableAnnotation,
                       Table table,
                       Map<Integer, List<Pair<Entity, Map<String, Double>>>> rowIndex_and_entityScores,
                       int column) {
        List<TColumnHeaderAnnotation> winningColumnClazz = tableAnnotation.getWinningHeaderAnnotations(column);
        List<String> types = new ArrayList<>();
        for (TColumnHeaderAnnotation ha : winningColumnClazz)
            types.add(ha.getAnnotation().getId());

        for (Map.Entry<Integer, List<Pair<Entity, Map<String, Double>>>> e :
                rowIndex_and_entityScores.entrySet()) {

            int row = e.getKey();
            List<Pair<Entity, Map<String, Double>>> entities_for_this_cell_and_scores = e.getValue();
            if (entities_for_this_cell_and_scores.size() == 0)
                continue;

            List<Pair<Entity, Map<String, Double>>> revised = reselect(entities_for_this_cell_and_scores, types);
            if (revised.size() != 0)
                entities_for_this_cell_and_scores = revised;

            List<Entity> winningEntities =
                    generateCellAnnotations(table,
                            tableAnnotation, row, column, entities_for_this_cell_and_scores
                    ); //supporting rows are added here, impossible other places
            updateColumnClazzSupportingRows(winningEntities, row, column, tableAnnotation);
        }
    }


    private List<Pair<Entity, Map<String, Double>>> reselect(
            List<Pair<Entity, Map<String, Double>>> entity_and_scoreMap,
            List<String> types) {

        Iterator<Pair<Entity, Map<String, Double>>> it = entity_and_scoreMap.iterator();
        List<Pair<Entity, Map<String, Double>>> original = new ArrayList<>(
                entity_and_scoreMap
        );

        while (it.hasNext()) {
            Pair<Entity, Map<String, Double>> oo = it.next();
            Set<String> entity_types = new HashSet<>(oo.getKey().getTypeIds());
            entity_types.retainAll(types);
            if (entity_types.size() == 0)
                it.remove();
        }

        if (entity_and_scoreMap.size() == 0)
            return original;

        return entity_and_scoreMap;
    }


    private void updateColumnClazzSupportingRows(List<Entity> winningEntities,
                                                 int row,
                                                 int column,
                                                 TAnnotation table_annotation) {
        TColumnHeaderAnnotation[] headers = table_annotation.getHeaderAnnotation(column);
        if (headers != null) {
            for (TColumnHeaderAnnotation ha : headers) {
                for (Entity ec : winningEntities) {
                    if (ec.getTypeIds().contains(ha.getAnnotation().getId())) {
                        ha.addSupportingRow(row);
                        break;
                    }
                }
            }
        }
    }


    private List<Entity> generateCellAnnotations(
            Table table,
            TAnnotation table_annotation,
            int table_cell_row,
            int table_cell_col,
            List<Pair<Entity, Map<String, Double>>> candidates_and_scores_for_cell) {

        Collections.sort(candidates_and_scores_for_cell, (o1, o2) -> {
            Double score1 = o1.getValue().get(TCellAnnotation.SCORE_FINAL);
            Double score2 = o2.getValue().get(TCellAnnotation.SCORE_FINAL);
            return score2.compareTo(score1);
        });

        double maxScore = candidates_and_scores_for_cell.get(0).getValue().get(TCellAnnotation.SCORE_FINAL);

        List<TCellAnnotation> annotations = new ArrayList<>();
        for (Pair<Entity, Map<String, Double>> e : candidates_and_scores_for_cell) {
            Double score = e.getValue().get(TCellAnnotation.SCORE_FINAL);
            if (score == maxScore) {
                TCellAnnotation ca = new TCellAnnotation(
                        table.getContentCell(table_cell_row, table_cell_col).getText(),
                        e.getKey(),
                        maxScore,
                        e.getValue());
                annotations.add(ca);
            } else
                break;
        }

        table_annotation.setContentCellAnnotations(table_cell_row, table_cell_col,
                annotations.toArray(new TCellAnnotation[0]));

        List<Entity> candidates = new ArrayList<>();
        for (int i = 0; i < candidates_and_scores_for_cell.size(); i++) {
            Pair<Entity, Map<String, Double>> e = candidates_and_scores_for_cell.get(i);

            candidates.add(e.getKey());
        }
        return candidates;
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