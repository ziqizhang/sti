package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.*;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;

import java.util.*;

/**
 * Created by - on 04/04/2016.
 */
public class TColumnClassifier {

    private ClazzScorer clazzScorer;
    private static final Logger LOG = Logger.getLogger(TColumnClassifier.class.getName());

    public TColumnClassifier(ClazzScorer scorer){
        this.clazzScorer=scorer;
    }

    public Map<TColumnHeaderAnnotation, Double> generateCandidateClazz(
            List<Pair<Entity, Map<String, Double>>> entityScoresForBlock,
            List<TColumnHeaderAnnotation> existingColumnClazzCandidates,
            Table table,
            List<Integer> blockOfRows,
            int column
            , int tableRowsTotal) {
        Collection<TColumnHeaderAnnotation> candidateHeaderAnnotations=clazzScorer.
                computeElementScores(entityScoresForBlock, existingColumnClazzCandidates,
                        table, blockOfRows, column);

        Map<TColumnHeaderAnnotation, Double> state = new HashMap<>();
        for (TColumnHeaderAnnotation ha : candidateHeaderAnnotations) {
            //Map<String, Double> scoreElements =ha.getScoreElements();
            clazzScorer.computeFinal(ha, tableRowsTotal);
            state.put(ha, ha.getFinalScore());
        }
        return state;
    }

    /**
     * after disamb on the column, go thru the cells that have been newly disambiguated (i.e., in addition to cold start
     * disamb) update class annotation for the column due to these new cells
     *
     * @param rowsUpdated
     * @param column
     * @param tableAnnotations
     * @param table
     */
    protected void updateColumnClazz(List<Integer> rowsUpdated,
                                            int column,
                                            TAnnotation tableAnnotations,
                                            Table table) {
        List<TColumnHeaderAnnotation> existingColumnClazzAnnotations;
        existingColumnClazzAnnotations = tableAnnotations.getHeaderAnnotation(column) == null
                ? new ArrayList<>() : new ArrayList<>(Arrays.asList(tableAnnotations.getHeaderAnnotation(column)));

        //supporting rows are added if a header for the type of the cell annotation exists
        List<TColumnHeaderAnnotation> toAdd = new ArrayList<>();
        //deal with newly disambiguated cells (that is, in addition to cold start disamb)
        for (int row : rowsUpdated) {
            List<TCellAnnotation> winningEntities =
                    tableAnnotations.getWinningContentCellAnnotation(row, column);
            for (TCellAnnotation ca : winningEntities) {
                for (TColumnHeaderAnnotation ha : TColumnHeaderAnnotationUpdater.selectNew(ca, column, table, existingColumnClazzAnnotations)) {
                    if (!toAdd.contains(ha))
                        toAdd.add(ha);
                }
            }
        }

        toAdd.addAll(existingColumnClazzAnnotations);
        TColumnHeaderAnnotation[] result = TColumnHeaderAnnotationUpdater.updateColumnClazzAnnotationScores(
                rowsUpdated,
                column,
                table.getNumRows(),
                existingColumnClazzAnnotations,
                table,
                tableAnnotations,
                clazzScorer
        );
        tableAnnotations.setHeaderAnnotation(column, result);
    }


    protected void updateClazzScoresByDC(TAnnotation currentAnnotation, List<String> domanRep,
                                       List<Integer> interpretedColumns) {
        for (int c : interpretedColumns) {
            List<TColumnHeaderAnnotation> headers = new ArrayList<>(
                    Arrays.asList(currentAnnotation.getHeaderAnnotation(c)));

            for (TColumnHeaderAnnotation ha : headers) {
                double dc = clazzScorer.computeDC(ha, domanRep);
                ha.setFinalScore(ha.getFinalScore() + dc);
            }

            Collections.sort(headers);
            currentAnnotation.setHeaderAnnotation(c, headers.toArray(new TColumnHeaderAnnotation[0]));
        }
    }
}
