package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPClazzScorer;
import uk.ac.shef.dcs.sti.core.model.*;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;

import java.util.*;
import java.util.List;

/**
 * Created by - on 04/04/2016.
 */
public class TColumnClassifier {

    private ClazzScorer clazzScorer;
    private static final Logger LOG = LoggerFactory.getLogger(TColumnClassifier.class.getName());

    public TColumnClassifier(ClazzScorer scorer){
        this.clazzScorer=scorer;
    }

    public Map<TColumnHeaderAnnotation, Double> generateCandidateClazz(
            List<Pair<Entity, Map<String, Double>>> entityScoresForBlock,
            List<TColumnHeaderAnnotation> existingColumnClazzCandidates,
            Table table,
            List<Integer> blockOfRows,
            int column
            , int tableRowsTotal) throws STIException {
        Collection<TColumnHeaderAnnotation> candidateHeaderAnnotations=clazzScorer.
                computeElementScores(entityScoresForBlock, existingColumnClazzCandidates,
                        table, blockOfRows, column);

        LOG.info("\t\t>> update candidate clazz on column, existing="+existingColumnClazzCandidates.size());
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
     * @param resetCESums if true, the sum_ce and sum_vote will be set to 0, before the newly disambiguated rows in rowsUpdated are counted
     */
    protected void updateColumnClazz(List<Integer> rowsUpdated,
                                            int column,
                                            TAnnotation tableAnnotations,
                                            Table table,
                                     boolean resetCESums) throws STIException {
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
                for (TColumnHeaderAnnotation ha : selectNew(ca, column, table, existingColumnClazzAnnotations)) {
                    if (!toAdd.contains(ha))
                        toAdd.add(ha);
                }
            }
        }

        toAdd.addAll(existingColumnClazzAnnotations);
        TColumnHeaderAnnotation[] result = updateColumnClazzAnnotationScores(
                rowsUpdated,
                column,
                table.getNumRows(),
                existingColumnClazzAnnotations,
                table,
                tableAnnotations,
                clazzScorer,
                resetCESums
        );
        tableAnnotations.setHeaderAnnotation(column, result);
    }


    protected void updateClazzScoresByDC(TAnnotation currentAnnotation, List<String> domanRep,
                                       List<Integer> interpretedColumns) throws STIException {
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


    /**
     * given a cell annotation, and existing TColumnHeaderAnnotations on the column,
     * go through the types (clazz) of that cell annotation, select the ones that are
     * not included in the existing TColumnHeaderAnnotations
     *
     * @param ca
     * @param column
     * @param table
     * @param existingColumnClazzAnnotations
     * @return
     */
    private List<TColumnHeaderAnnotation> selectNew(TCellAnnotation ca, int column,
                                                          Table table,
                                                          Collection<TColumnHeaderAnnotation> existingColumnClazzAnnotations
    ) {

        List<Clazz> types = ca.getAnnotation().getTypes();

        List<TColumnHeaderAnnotation> selected = new ArrayList<>();
        for (int index = 0; index < types.size(); index++) {
            boolean found = false;
            Clazz type = types.get(index);
            for (TColumnHeaderAnnotation ha : existingColumnClazzAnnotations) {
                if (type.equals(ha.getAnnotation())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                TColumnHeaderAnnotation ha = new TColumnHeaderAnnotation(table.getColumnHeader(column).getHeaderText(),
                        type, 0.0);
                selected.add(ha);
            }
        }
        return selected;
    }

    /**
     * Used after disamb, to update candidate column clazz annotations on the column
     * @param updatedRows
     * @param column
     * @param totalRows
     * @param candidateColumnClazzAnnotations
     * @param table
     * @param tableAnnotations
     * @param clazzScorer
     * @param resetCESums if true, the sum_ce and sum_vote will be set to 0, before the newly disambiguated rows in rowsUpdated are counted
     * @return
     */
    private TColumnHeaderAnnotation[] updateColumnClazzAnnotationScores(Collection<Integer> updatedRows,
                                                                              int column,
                                                                              int totalRows,
                                                                              Collection<TColumnHeaderAnnotation> candidateColumnClazzAnnotations,
                                                                              Table table,
                                                                              TAnnotation tableAnnotations,
                                                                              ClazzScorer clazzScorer,
                                                                        boolean resetCESums) throws STIException {
        //for the candidate column clazz annotations compute CC score
        candidateColumnClazzAnnotations = clazzScorer.computeCCScore(candidateColumnClazzAnnotations,table, column);

        if(resetCESums){
            for (TColumnHeaderAnnotation ha : candidateColumnClazzAnnotations) {
                ha.getScoreElements().put(TMPClazzScorer.SUM_CELL_VOTE, 0.0);
                ha.getScoreElements().put(TMPClazzScorer.SUM_CE, 0.0);
            }
        }

        for (int row : updatedRows) {
            List<TCellAnnotation> winningEntities = tableAnnotations.getWinningContentCellAnnotation(row, column);
            Set<String> votedClazzIdsByThisCell = new HashSet<>();
            for (TCellAnnotation ca : winningEntities) {
                //go thru each candidate column clazz annotation, check if this winning entity has a type that is this clazz
                for (TColumnHeaderAnnotation ha : candidateColumnClazzAnnotations) {
                    if (ca.getAnnotation().getTypes().contains(ha.getAnnotation())) {
                        ha.addSupportingRow(row);
                        if (!votedClazzIdsByThisCell.contains(ha.getAnnotation().getId())) {
                            //update the CE score elements for this column clazz annotation
                            Double sum_votes = ha.getScoreElements().get(TMPClazzScorer.SUM_CELL_VOTE);
                            if(sum_votes==null) sum_votes=0.0;
                            sum_votes++;
                            ha.getScoreElements().put(TMPClazzScorer.SUM_CELL_VOTE, sum_votes);

                            Double sum_ce = ha.getScoreElements().get(TMPClazzScorer.SUM_CE);
                            if(sum_ce==null) sum_ce=0.0;
                            sum_ce += ca.getFinalScore();
                            ha.getScoreElements().put(TMPClazzScorer.SUM_CE, sum_ce);
                            votedClazzIdsByThisCell.add(ha.getAnnotation().getId());
                        }
                    } else if (votedClazzIdsByThisCell.contains(ha.getAnnotation().getId())){}

                }
            }
        }

        //finally recompute final scores, because CE scores could have changed
        List<TColumnHeaderAnnotation> revised = new ArrayList<>();
        for (TColumnHeaderAnnotation ha : candidateColumnClazzAnnotations) {
            clazzScorer.computeFinal(ha, totalRows);
            revised.add(ha);
        }

        Collections.sort(revised);
        return revised.toArray(new TColumnHeaderAnnotation[0]);
    }

}
