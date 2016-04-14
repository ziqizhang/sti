package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 *
 */
public abstract class TCellDisambiguator {

    protected KBSearch kbSearch;

    public TCellDisambiguator(
            KBSearch candidateFinder) {
        this.kbSearch = candidateFinder;
    }

    protected abstract Map<Integer, List<Pair<Entity, Map<String, Double>>>> disambiguate(
            Table table, TAnnotation table_annotation, int column, Integer... skipRows) throws KBSearchException;

    protected void revise(TAnnotation tableAnnotation,
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
}
