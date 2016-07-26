package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping.StoppingCriteria;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping.StoppingCriteriaInstantiator;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;

/**
 * this class creates preliminary classification and disambiguation on a column
 */


public class LEARNINGPreliminaryColumnClassifier {
    private TContentCellRanker selector;
    private KBSearch kbSearch;
    private TCellDisambiguator cellDisambiguator;
    private TColumnClassifier columnClassifier;

    private String stopperClassname;
    private String[] stopperParams;
    private static final Logger LOG = Logger.getLogger(LEARNINGPreliminaryColumnClassifier.class.getName());

    public LEARNINGPreliminaryColumnClassifier(TContentCellRanker selector,
                                               String stoppingCriteriaClassname,
                                               String[] stoppingCriteriaParams,
                                               KBSearch candidateFinder,
                                               TCellDisambiguator cellDisambiguator,
                                               TColumnClassifier columnClassifier) {
        this.selector = selector;
        this.kbSearch = candidateFinder;
        this.cellDisambiguator = cellDisambiguator;
        this.columnClassifier = columnClassifier;

        this.stopperClassname = stoppingCriteriaClassname;
        this.stopperParams = stoppingCriteriaParams;
    }

    /**
     * @param table
     * @param tableAnnotation
     * @param column
     * @param skipRows
     * @return pair: key is the index of the cell by which the classification stopped. value is the re-ordered
     * indexes of cells based on the sampler
     * @throws KBSearchException
     */
    public Pair<Integer, List<List<Integer>>> runPreliminaryColumnClassifier(Table table, TAnnotation tableAnnotation, int column,Integer... skipRows) throws KBSearchException, ClassNotFoundException, STIException {
        StoppingCriteria stopper = StoppingCriteriaInstantiator.instantiate(stopperClassname, stopperParams);

        //1. gather list of strings from this column to be interpreted, rank them (for sampling)
        List<List<Integer>> ranking = selector.select(table, column, tableAnnotation.getSubjectColumn());

        //2. computeElementScores column and also disambiguate initial rows in the selected sample
        List<TColumnHeaderAnnotation> headerClazzScores = new ArrayList<>();

        int countProcessed = 0, totalRows = 0;
        boolean stopped = false;
        Map<Object, Double> state = new HashMap<>();

        LOG.info("\t>> (LEANRING) Preliminary Column Classification begins");
        for (List<Integer> blockOfRows : ranking) {
            countProcessed++;
            totalRows += blockOfRows.size();
            //find candidate entities
            TCell sample = table.getContentCell(blockOfRows.get(0), column);
            if (sample.getText().length() < 2) {
                LOG.debug("\t\t>>> Very short text cell skipped: " + blockOfRows + "," + column + " " + sample.getText());
                continue;
            }

            LOG.info("\t\t>> cold start disambiguation, row(s) " + blockOfRows + "/"+ranking.size()+"," + sample);

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
                cellDisambiguator.addCellAnnotation(table,
                        tableAnnotation, blockOfRows, column, entityScoresForBlock);
            }

            //run algorithm to runPreliminaryColumnClassifier column classification; header annotation scores are updated constantly, but supporting rows are not.
            Map<TColumnHeaderAnnotation, Double> scores=columnClassifier.generateCandidateClazz(
                    entityScoresForBlock, headerClazzScores, table, blockOfRows, column, totalRows
            );
            headerClazzScores.clear();
            headerClazzScores.addAll(scores.keySet());
            state=new HashMap<>();
            state.putAll(scores);

            boolean stop = stopper.stop(state, table.getNumRows());

            if (stop) {
                LOG.info("\t>> (LEARNING) Preliminary Column Classification converged, rows:" + totalRows+"/"+ranking.size());
                //state is stable. annotate using the type, and disambiguate entities
                generatePreliminaryColumnClazz(state, tableAnnotation, column);
                stopped = true;
                break;  //exit loop
            }
        }

        if (!stopped) {
            LOG.info("\t>> Preliminary Column Classification no convergence");
            generatePreliminaryColumnClazz(state, tableAnnotation, column); //supporting rows not added
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




}
