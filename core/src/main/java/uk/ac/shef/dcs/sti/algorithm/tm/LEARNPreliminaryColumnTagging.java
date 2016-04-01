package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.algorithm.tm.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.algorithm.tm.stopping.StoppingCriteria;
import uk.ac.shef.dcs.sti.algorithm.tm.stopping.StoppingCriteriaInstantiator;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.*;

import java.util.*;

/**
 * this class creates preliminary classification and disambiguation on a column
 */


public class LEARNPreliminaryColumnTagging {
    private TContentCellRanker selector;
    private KBSearch kbSearch;
    private TCellDisambiguator cellDisambiguator;
    private ClazzScorer columnClassifier;

    private String stopperClassname;
    private String[] stopperParams;
    private static final Logger LOG = Logger.getLogger(LEARNPreliminaryColumnTagging.class.getName());

    public LEARNPreliminaryColumnTagging(TContentCellRanker selector,
                                         String stoppingCriteriaClassname,
                                         String[] stoppingCriteriaParams,
                                         KBSearch candidateFinder,
                                         TCellDisambiguator cellDisambiguator,
                                         ClazzScorer columnClassifier) {
        this.selector = selector;
        this.kbSearch = candidateFinder;
        this.cellDisambiguator = cellDisambiguator;
        this.columnClassifier = columnClassifier;

        this.stopperClassname = stoppingCriteriaClassname;
        this.stopperParams = stoppingCriteriaParams;
    }

    public Pair<Integer, List<List<Integer>>> learn(Table table, TAnnotation tableAnnotation, int column, Integer... skipRows) throws KBSearchException {
        StoppingCriteria stopper = StoppingCriteriaInstantiator.instantiate(stopperClassname, stopperParams);

        //1. gather list of strings from this column to be interpreted, rank them (for sampling)
        List<List<Integer>> ranking = selector.select(table, column, tableAnnotation.getSubjectColumn());

        //2. computeElementScores column and also disambiguate initial rows in the selected sample
        Map<Integer, List<Pair<Entity, Map<String, Double>>>> cellDisambEntityScores =
                new HashMap<>();
        Set<TColumnHeaderAnnotation> headerClazzScores = new HashSet<>();

        int countProcessed = 0, totalRows = 0;
        boolean stopped = false;
        Map<Object, Double> state = new HashMap<>();

        for (List<Integer> blockOfRows : ranking) {
            countProcessed++;
            totalRows += blockOfRows.size();
            //find candidate entities
            TCell sample = table.getContentCell(blockOfRows.get(0), column);
            if (sample.getText().length() < 2) {
                LOG.debug("\t\t>>> Very short text cell skipped: " + blockOfRows + "," + column + " " + sample.getText());
                continue;
            }

            LOG.info("\t>> Preliminary Column Classification - cold start disambiguation, row(s) " + blockOfRows + "," + sample);

            boolean skip = false;
            for (int row : skipRows) {
                if (blockOfRows.contains(row)) {
                    skip = true;
                    break;
                }
            }

            List<Pair<Entity, Map<String, Double>>> entityScoresForBlock;
            if (skip) {
                entityScoresForBlock = toScoreMap(tableAnnotation, blockOfRows, column);
            } else {
                List<Entity> candidates = kbSearch.findEntityCandidates(sample.getText());
                //do cold start disambiguation
                entityScoresForBlock =
                        cellDisambiguator.coldstartDisambiguate(
                                candidates, table, blockOfRows, column
                        );
                for (int ri : blockOfRows) {
                    cellDisambEntityScores.put(ri, entityScoresForBlock);
                }
            }

            //run algorithm to learn column classification; header annotation scores are updated constantly, but supporting rows are not.
            state = updateState(
                    columnClassifier.
                            computeElementScores(entityScoresForBlock, headerClazzScores,
                                    table, blockOfRows, column), totalRows
            );
            boolean stop = stopper.stop(state, table.getNumRows());

            if (stop) {
                System.out.println("\t>> Preliminary Column Classification converged, rows:" + totalRows);
                //state is stable. annotate using the type, and disambiguate entities
                generatePreliminaryColumnClazz(state, tableAnnotation, column);
                //then use classification results to preliminaryDisambiguate disambiguation
                System.out.println("\t>> Preliminary Disambiguation begins (preliminaryDisambiguate " + cellDisambEntityScores.size() + " rows)");
                preliminaryDisambiguate(tableAnnotation, table, cellDisambEntityScores, column);
                stopped = true;
                break;  //exit loop
            }
        }

        if (!stopped) {
            System.out.println("\t>> Preliminary Column Classification no convergence");
            generatePreliminaryColumnClazz(state, tableAnnotation, column); //supporting rows not added
            preliminaryDisambiguate(tableAnnotation, table, cellDisambEntityScores, column);
        }
        return new Pair<>(countProcessed, ranking);
    }


    //
    private List<Pair<Entity, Map<String, Double>>> toScoreMap(TAnnotation tableAnnotation,
                                                               List<Integer> blockOfRows,
                                                               int column) {
        List<Pair<Entity, Map<String, Double>>> candidates = new ArrayList<>();
        for (int row : blockOfRows) {
            TCellAnnotation[] annotations = tableAnnotation.getContentCellAnnotations(row, column);
            for (TCellAnnotation can : annotations) {
                Entity ec = can.getAnnotation();
                Map<String, Double> scoreElements = can.getScoreElements();
                scoreElements.put(TCellAnnotation.SCORE_FINAL, can.getFinalScore());
                candidates.add(new Pair<>(ec, scoreElements));
            }

        }
        return candidates;
    }


    private Map<Object, Double> updateState(
            Set<TColumnHeaderAnnotation> candidateHeaderAnnotations, int tableRowsTotal) {
        Map<Object, Double> state = new HashMap<>();
        for (TColumnHeaderAnnotation ha : candidateHeaderAnnotations) {
            //Map<String, Double> scoreElements =ha.getScoreElements();
            columnClassifier.computeFinal(ha, tableRowsTotal);
            state.put(ha, ha.getFinalScore());
        }
        return state;
    }

    //assigns highest scoring clazz to the column;
    private void generatePreliminaryColumnClazz(final Map<Object, Double> state,
                                                TAnnotation tableAnnotation,
                                                int column) {
        if (state.size() > 0) {
            List<Object> candidateClazz = new ArrayList<>(state.keySet());
            Collections.sort(candidateClazz, (o1, o2)
                    -> state.get(o2).compareTo(state.get(o1)));
            //insert column type annotations
            TColumnHeaderAnnotation[] preliminaryRankedCandidateClazz
                    = new TColumnHeaderAnnotation[candidateClazz.size()];
            for (int i = 0; i < candidateClazz.size(); i++)
                preliminaryRankedCandidateClazz[i] =
                        (TColumnHeaderAnnotation) candidateClazz.get(i);
            tableAnnotation.setHeaderAnnotation(column, preliminaryRankedCandidateClazz);
        }
    }

    //given the winning clazz for this column generate preliminary entity annotations for cells in the column
    private void preliminaryDisambiguate(TAnnotation tableAnnotation,
                                         Table table,
                                         Map<Integer, List<Pair<Entity, Map<String, Double>>>>
                                                 rowIndex_and_candidateEntities,
                                         int column) {
        List<TColumnHeaderAnnotation> winningClazz = tableAnnotation.getBestHeaderAnnotations(column);
        List<String> winningClazzIds = new ArrayList<>();
        for (TColumnHeaderAnnotation ha : winningClazz)
            winningClazzIds.add(ha.getAnnotation().getId());
        for (Map.Entry<Integer, List<Pair<Entity, Map<String, Double>>>> e :
                rowIndex_and_candidateEntities.entrySet()) {

            int row = e.getKey();
            List<Pair<Entity, Map<String, Double>>> entityScoresForThisCell = e.getValue();
            if (entityScoresForThisCell.size() == 0)
                continue;
            //this will remove any entities whose types do not overlap within winning clazz
            List<Pair<Entity, Map<String, Double>>> revised =
                    cellDisambiguator.preliminaryDisambiguate(entityScoresForThisCell, winningClazzIds);
            if (revised.size() != 0)
                entityScoresForThisCell = revised;
            List<Entity> winningEntities =
                    createCellAnnotation(table, tableAnnotation, row, column, entityScoresForThisCell
                    );
            updateSupportingRowsForColumnClazz(winningEntities, row, column, tableAnnotation);
        }
    }


    private List<Entity> createCellAnnotation(
            Table table,
            TAnnotation tableAnnotation,
            int table_cell_row,
            int table_cell_col,
            List<Pair<Entity, Map<String, Double>>> entities_and_scoreMap) {

        Collections.sort(entities_and_scoreMap, (o1, o2) -> {
            Double o2_score = o2.getValue().get(TCellAnnotation.SCORE_FINAL);
            Double o1_score = o1.getValue().get(TCellAnnotation.SCORE_FINAL);
            return o2_score.compareTo(o1_score);
        });

        double max = 0.0;
        TCellAnnotation[] annotationsForCell = new TCellAnnotation[entities_and_scoreMap.size()];
        for (int i = 0; i < entities_and_scoreMap.size(); i++) {
            Pair<Entity, Map<String, Double>> e = entities_and_scoreMap.get(i);
            double score = e.getValue().get(TCellAnnotation.SCORE_FINAL);
            if (score > max)
                max = score;
            annotationsForCell[i] = new TCellAnnotation(table.getContentCell(table_cell_row, table_cell_col).getText(),
                    e.getKey(), e.getValue().get(TCellAnnotation.SCORE_FINAL), e.getValue());

        }
        tableAnnotation.setContentCellAnnotations(table_cell_row, table_cell_col, annotationsForCell);

        List<Entity> best = new ArrayList<>();
        for (int i = 0; i < entities_and_scoreMap.size(); i++) {
            Pair<Entity, Map<String, Double>> e = entities_and_scoreMap.get(i);
            double score = e.getValue().get(TCellAnnotation.SCORE_FINAL);
            if (score == max)
                best.add(e.getKey());
        }
        return best;
    }

    //check each winning entity in this cell (multiple winning possible) for its types. If
    //the types contains the winning type for this column, this cell is a supporting cell
    //for that winning type
    private void updateSupportingRowsForColumnClazz(List<Entity> winningEntities,
                                                    int row,
                                                    int column,
                                                    TAnnotation tableAnnotation) {
        TColumnHeaderAnnotation[] headers = tableAnnotation.getHeaderAnnotation(column);
        if (headers != null) {
            for (TColumnHeaderAnnotation ha : headers) {
                for (Entity ec : winningEntities) {
                    if (ec.getTypes().contains(ha.getAnnotation())) {
                        ha.addSupportingRow(row);
                        break;
                    }
                }
            }
        }
    }
}
