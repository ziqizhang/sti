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
 * this class creates preliminary classification on a column
 */


public class LEARNINGPreliminaryClassify {
    private TContentCellRanker selector;
    private KBSearch kbSearch;
    private TCellDisambiguator cellDisambiguator;
    private TColumnClassifier columnClassifier;

    private String stopperClassname;
    private String[] stopperParams;
    private static final Logger LOG = Logger.getLogger(LEARNINGPreliminaryClassify.class.getName());

    public LEARNINGPreliminaryClassify(TContentCellRanker selector,
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

    public Pair<Integer, List<List<Integer>>> learn_seeding(Table table, TAnnotation tableAnnotation, int column, Integer... skipRows) throws KBSearchException {
        StoppingCriteria stopper = StoppingCriteriaInstantiator.instantiate(stopperClassname, stopperParams);

        //1. gather list of strings from this column to be interpreted, rank them (for sampling)
        List<List<Integer>> ranking = selector.select(table, column, tableAnnotation.getSubjectColumn());

        //2. score column and also disambiguate initial rows in the selected sample
        Map<Integer, List<Pair<Entity, Map<String, Double>>>> cellDisambEntityScores =
                new HashMap<>();
        Set<TColumnHeaderAnnotation> headerAnnotationCandidates = new HashSet<>();

        int countProcessed = 0, totalRows = 0;
        boolean stopped = false;
        Map<Object, Double> state = new HashMap<>();

        for (List<Integer> blockOfRows : ranking) {

            /* if(row_index<39)
            continue;*/
            /* if(row_index==13)
            System.out.println();*/
            countProcessed++;
            totalRows += blockOfRows.size();
            //find candidate entities
            TCell sample = table.getContentCell(blockOfRows.get(0), column);
            /*if (sample.getType().equals(DataTypeClassifier.DataType.LONG_TEXT)) {
                System.out.println("\t\t>>> Long text cell skipped: " + rows_indexes + "," + column + " " + sample.getText());
                continue;
            }*/
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
                entityScoresForBlock = collectScores(tableAnnotation, blockOfRows, column);
            } else {
                List<Entity> candidates = kbSearch.findEntityCandidates(sample.getText());
                //do disambiguation scoring
                entityScoresForBlock =
                        cellDisambiguator.coldstartDisambiguate(
                                candidates, table, blockOfRows, column
                        );
                for (int ri : blockOfRows) {
                    cellDisambEntityScores.put(ri, entityScoresForBlock);
                }
            }

            //run algorithm to learn column typing; header annotation scores are updated constantly, but supporting rows are not.
            state = createState(
                    columnClassifier.
                            score(entityScoresForBlock, headerAnnotationCandidates,
                                    table, blockOfRows, column), totalRows
            );
            boolean stop = stopper.stop(state, table.getNumRows());

            if (stop) {
                System.out.println("\t>> Classification-LEARN(seeding) converged, rows:" + totalRows);
                //state is stable. annotate using the type, and disambiguate entities
                create_typing_annotations(state, tableAnnotation, column);
                //then use classification results to revise disambiguation
                System.out.println("\t>> Disambiguation-LEARN(seeding revise " + cellDisambEntityScores.size() + " rows)");
                revise_disambiguation_and_create_annotations(tableAnnotation, table, cellDisambEntityScores, column);
                stopped = true;
                break;  //exit loop
            }
        }

        if (!stopped) {
            System.out.println("\t>> Classification-LEARN(seeding) no convergence");
            create_typing_annotations(state, tableAnnotation, column); //supporting rows not added
            revise_disambiguation_and_create_annotations(tableAnnotation, table, cellDisambEntityScores, column);
        }
        return new Pair<>(countProcessed, ranking);
    }

    private List<Pair<Entity, Map<String, Double>>> collectScores(TAnnotation tableAnnotation, List<Integer> blockOfRows, int column) {
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

    private Map<Object, Double> createState(
            Set<TColumnHeaderAnnotation> scores, int tableRowsTotal) {
        Map<Object, Double> state = new HashMap<Object, Double>();
        for (TColumnHeaderAnnotation ha : scores) {
            //Map<String, Double> scoreElements =ha.getScoreElements();
            ha.getScoreElements().put(
                    TColumnHeaderAnnotation.FINAL,
                    columnClassifier.computeFinal(ha, tableRowsTotal).get(TColumnHeaderAnnotation.FINAL)
            );
            state.put(ha, ha.getFinalScore());
        }
        return state;
    }


    private void revise_disambiguation_and_create_annotations(TAnnotation table_annotation,
                                                              Table table,
                                                              Map<Integer, List<Pair<Entity, Map<String, Double>>>> candidates_and_scores_for_each_row,
                                                              int column) {
        List<TColumnHeaderAnnotation> bestHeaderAnnotations = table_annotation.getBestHeaderAnnotations(column);
        List<String> types = new ArrayList<String>();
        for (TColumnHeaderAnnotation ha : bestHeaderAnnotations)
            types.add(ha.getAnnotation().getId());
        for (Map.Entry<Integer, List<Pair<Entity, Map<String, Double>>>> e :
                candidates_and_scores_for_each_row.entrySet()) {

            int row = e.getKey();
            List<Pair<Entity, Map<String, Double>>> entities_for_this_cell_and_scores = e.getValue();
            if (entities_for_this_cell_and_scores.size() == 0)
                continue;

            List<Pair<Entity, Map<String, Double>>> revised = cellDisambiguator.revise(entities_for_this_cell_and_scores, types);
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
                Double o2_score = o2.getValue().get(TCellAnnotation.SCORE_FINAL);
                Double o1_score = o1.getValue().get(TCellAnnotation.SCORE_FINAL);
                return o2_score.compareTo(o1_score);
            }
        });

        double max = 0.0;
        TCellAnnotation[] annotationsForCell = new TCellAnnotation[candidates_and_scores_for_cell.size()];
        for (int i = 0; i < candidates_and_scores_for_cell.size(); i++) {
            Pair<Entity, Map<String, Double>> e = candidates_and_scores_for_cell.get(i);
            double score = e.getValue().get(TCellAnnotation.SCORE_FINAL);
            if (score > max)
                max = score;
            annotationsForCell[i] = new TCellAnnotation(table.getContentCell(table_cell_row, table_cell_col).getText(),
                    e.getKey(), e.getValue().get(TCellAnnotation.SCORE_FINAL), e.getValue());

        }
        table_annotation.setContentCellAnnotations(table_cell_row, table_cell_col, annotationsForCell);

        List<Entity> best = new ArrayList<>();
        for (int i = 0; i < candidates_and_scores_for_cell.size(); i++) {
            Pair<Entity, Map<String, Double>> e = candidates_and_scores_for_cell.get(i);
            double score = e.getValue().get(TCellAnnotation.SCORE_FINAL);
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
