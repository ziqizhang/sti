package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.rep.TCellAnnotation;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.TAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;

import java.util.*;

/**
 * Represents the LEARNING phase, creates preliminary column classification and cell disambiguation
 */
public class LEARNING {

    private LEARNINGPreliminaryColumnClassifier columnTagger;
    private LEARNINGPreliminaryDisamb cellTagger;


    public LEARNING(LEARNINGPreliminaryColumnClassifier columnTagger, LEARNINGPreliminaryDisamb cellTagger) {
        this.columnTagger = columnTagger;
        this.cellTagger = cellTagger;
    }

    public void learn(Table table, TAnnotation tableAnnotation, int column) throws KBSearchException {
        Pair<Integer, List<List<Integer>>> stopPosition =
                columnTagger.runPreliminaryColumnClassifier(table, tableAnnotation, column);

        cellTagger.runPreliminaryDisamb(
                stopPosition.getKey(),
                stopPosition.getValue(),
                table,
                tableAnnotation,
                column);
    }

    protected static void addCellAnnotation(
            Table table,
            TAnnotation tableAnnotation,
            List<Integer> rowBlock,
            int table_cell_col,
            List<Pair<Entity, Map<String, Double>>> entities_and_scoreMap) {

        Collections.sort(entities_and_scoreMap, (o1, o2) -> {
            Double o2_score = o2.getValue().get(TCellAnnotation.SCORE_FINAL);
            Double o1_score = o1.getValue().get(TCellAnnotation.SCORE_FINAL);
            return o2_score.compareTo(o1_score);
        });

        String cellText = table.getContentCell(rowBlock.get(0), table_cell_col).getText();
        for (int row : rowBlock) {
            TCellAnnotation[] annotationsForCell = new TCellAnnotation[entities_and_scoreMap.size()];
            for (int i = 0; i < entities_and_scoreMap.size(); i++) {
                Pair<Entity, Map<String, Double>> e = entities_and_scoreMap.get(i);
                double score = e.getValue().get(TCellAnnotation.SCORE_FINAL);
                annotationsForCell[i] = new TCellAnnotation(cellText,
                        e.getKey(), score, e.getValue());

            }
            tableAnnotation.setContentCellAnnotations(row, table_cell_col, annotationsForCell);
        }
    }
}
