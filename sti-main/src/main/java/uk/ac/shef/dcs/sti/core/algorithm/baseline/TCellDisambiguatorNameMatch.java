package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;

/**
 * the default cell disambiguator adopts the name match strategy B_nm
 */
public class TCellDisambiguatorNameMatch extends TCellDisambiguator {
    private static final Logger LOG = LoggerFactory.getLogger(TCellDisambiguatorNameMatch.class.getName());

    public TCellDisambiguatorNameMatch(
            KBProxy kbSearch) {
        super(kbSearch);
    }

    protected Map<Integer, List<Pair<Entity, Map<String, Double>>>> disambiguate(
            Table table, TAnnotation table_annotation, int column, Integer... skipRows) throws KBProxyException {
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

    protected List<Pair<Entity, Map<String, Double>>> disambiguate(List<Entity> candidates, Table table,
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

            Map<String, Double> scoreMap = new HashMap<>();
            scoreMap.put(TCellAnnotation.SCORE_FINAL,1.0);
            if (candidatesCopy.size() > 0) {
                disambiguationScores.add(new Pair<>(
                        candidatesCopy.get(0), scoreMap
                ));
            } else {
                disambiguationScores.add(new Pair<>(
                        candidates.get(0), scoreMap
                ));
            }

        }
        return disambiguationScores;
    }


}