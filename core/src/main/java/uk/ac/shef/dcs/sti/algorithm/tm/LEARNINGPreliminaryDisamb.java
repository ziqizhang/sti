package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.*;

import java.util.*;
import java.util.logging.Logger;

/**
 */
public class LEARNINGPreliminaryDisamb {

    private static final Logger LOG = Logger.getLogger(LEARNINGPreliminaryDisamb.class.getName());
    private TCellDisambiguator disambiguator;
    private KBSearch kbSearch;
    private ClazzScorer classification_scorer;

    public LEARNINGPreliminaryDisamb(KBSearch kbSearch,
                                     TCellDisambiguator disambiguator,
                                     ClazzScorer classification_scorer) {
        this.kbSearch = kbSearch;
        this.disambiguator = disambiguator;
        this.classification_scorer = classification_scorer;
    }

    public void runPreliminaryDisamb(
            int stopPointByPreColumnClassifier,
            List<List<Integer>> ranking,
            Table table,
            TAnnotation tableAnnotation,
            int column,
            Integer... skipRows) throws KBSearchException {

        LOG.info("\t>> (LEARNING) Preliminary Disambiguation begins");
        List<TColumnHeaderAnnotation> winningColumnClazz = tableAnnotation.getBestHeaderAnnotations(column);
        Set<String> winningColumnClazzIds = new HashSet<>();
        for (TColumnHeaderAnnotation ha : winningColumnClazz)
            winningColumnClazzIds.add(ha.getAnnotation().getId());

        //for those cells already processed by pre column classification, update their cell annotations
        LOG.info("\t\t>> re-annotate cells involved in cold start disambiguation");
        reselect(tableAnnotation, stopPointByPreColumnClassifier, ranking, winningColumnClazzIds, column);

        //for remaining...
        LOG.info("\t\t>> constrained cell disambiguation");
        int start = stopPointByPreColumnClassifier;
        int end = ranking.size();


        List<Integer> updated = new ArrayList<>();
        for (int bi = start; bi < end; bi++) {
            List<Integer> rows = ranking.get(bi);

            boolean skip = false;
            for (int i : skipRows) {
                if (rows.contains(i)) {
                    skip = true;
                    break;
                }
            }
            if (skip)
                continue;


            //System.out.println(">>\tUPDATE: Classifying and disambiguating remaining rows, row " + row + ", reference entities:" + reference_entities.size());
            //find candidate entities
            TCell sample = table.getContentCell(rows.get(0), column);
            /*if (sample.getType().equals(DataTypeClassifier.DataType.LONG_TEXT)) {
                System.out.println("\t\t>>> Long text cell skipped: " + rows + "," + column + " " + sample.getText());
                continue;
            }*/
            if (sample.getText().length() < 2) {
                System.out.println("\t\t>>> Long text cell skipped: " + rows + "," + column + " " + sample.getText());
                continue;
            }


            List<Pair<Entity, Map<String, Double>>>
                    candidates_and_scores_for_block =
                    disambiguate(sample,
                            table,
                            //current_iteration_annotation,
                            winningColumnClazzIds,
                            rows, column
                    );

            if (candidates_and_scores_for_block.size() > 0) {
                update_entity_annotations(table, tableAnnotation, rows, column,
                        candidates_and_scores_for_block);
                updated.addAll(rows);
            }
        }
        //todo: one-name-PSPD, before create typing
        if (TableMinerConstants.ENFORCE_ONPSPD)
            ONPSPD_Enforcer.enforce(table, tableAnnotation, column);

        System.out.println("\t>> Classification-LEARN (consolidate " + updated.size() + " rows)");

        update_typing_annotations_best_candidate_contribute(updated, column, tableAnnotation, table,
                table.getNumRows()
                //countRows
        );

    }

    //for those cells already processed in preliminary column classification,
    //preliminary disamb simply reselects entities whose type overlap with winning column clazz annotation
    private void reselect(TAnnotation tableAnnotation,
                          int stopPointByPreColumnClassifier,
                          List<List<Integer>> cellBlockRanking,
                          Collection<String> winningClazzIds,
                          int column) {
        TColumnHeaderAnnotation[] headers = tableAnnotation.getHeaderAnnotation(column);
        for(int index=0; index<stopPointByPreColumnClassifier; index++) {
            List<Integer> cellBlock = cellBlockRanking.get(index);
            for(int row: cellBlock) {
                TCellAnnotation[] cellAnnotations=
                        tableAnnotation.getContentCellAnnotations(row, column);
                TCellAnnotation[] revised =
                        disambiguator.reselect(cellAnnotations, winningClazzIds);
                if (revised.length != 0)
                    tableAnnotation.setContentCellAnnotations(row, column, revised);

                //now update supporting rows for the elected column clazz
                if (headers != null) {
                    for (TColumnHeaderAnnotation ha : headers) {
                        for (TCellAnnotation tca : revised) {
                            if (tca.getAnnotation().getTypes().contains(ha.getAnnotation())) {
                                ha.addSupportingRow(row);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    //search candidates for the cell;
    //computeElementScores candidates for the cell;
    //create annotation and update supportin header and header computeElementScores (depending on the two params updateHeader_blah
    private List<Pair<Entity, Map<String, Double>>> disambiguate(TCell tcc,
                                                                 Table table,
                                                                 Set<String> columnTypes,
                                                                 List<Integer> table_cell_rows,
                                                                 int table_cell_col) throws KBSearchException {
        List<Pair<Entity, Map<String, Double>>> candidates_and_scores_for_block;

        List<Entity> candidates = kbSearch.findEntityCandidatesOfTypes(tcc.getText(), columnTypes.toArray(new String[0]));
        if (candidates != null && candidates.size() != 0) {
        } else {
            candidates = kbSearch.findEntityCandidatesOfTypes(tcc.getText());
        }

        //now each candidate is given scores
        candidates_and_scores_for_block =
                disambiguator.disambiguate_learn_consolidate
                        (candidates, table, table_cell_rows, table_cell_col, columnTypes, true
                        );

        return candidates_and_scores_for_block;
    }


    //disambiguate cells in a column, assuming the type is "column_type". supporting row info is added to headers
    //updateHeaderSupportingRow: weather the disamb result on each row should also update header's supporting row
    //updateHeaderScore: weather the disamb result computeElementScores should be incremented to the header's computeElementScores (e.g., those that contributed to the classification of the column in the first place shouldbe disregarded; while the remaining columns shouldbe considered
    private void update_entity_annotations(
            Table table,
            TAnnotation table_annotation,
            List<Integer> table_cell_rows,
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

        String sampleCellText = table.getContentCell(table_cell_rows.get(0), table_cell_col).getText();

        for (int row : table_cell_rows) {
            TCellAnnotation[] annotationsForCell = new TCellAnnotation[candidates_and_scores_for_cell.size()];
            for (int i = 0; i < candidates_and_scores_for_cell.size(); i++) {
                Pair<Entity, Map<String, Double>> e = candidates_and_scores_for_cell.get(i);
                annotationsForCell[i] = new TCellAnnotation(sampleCellText,
                        e.getKey(), e.getValue().get("final"), e.getValue());
                /*if(table_cell_row==5 &&table_cell_col==4)
                System.out.println(i);*/
            }

            table_annotation.setContentCellAnnotations(row, table_cell_col, annotationsForCell);
        }
        /* if (table_cell_row == 5 && table_cell_col == 4)
        System.out.println("end");*/
    }

    //WARNING: CURRENTLY updating does not ADD new headers
    public void update_typing_annotations_best_candidate_contribute(List<Integer> rowsUpdated,
                                                                    int column,
                                                                    TAnnotation table_annotations,
                                                                    Table table,
                                                                    int tableRowsTotal) {
        TColumnHeaderAnnotation[] existing_header_annotations = table_annotations.getHeaderAnnotation(column);
        existing_header_annotations = existing_header_annotations == null ? new TColumnHeaderAnnotation[0] : existing_header_annotations;

        //supporting rows are only added if a header for the type of the cell annotation exists
        Set<TColumnHeaderAnnotation> add = new HashSet<TColumnHeaderAnnotation>();
        //any new headers due to disambiguation-update?
        for (int row : rowsUpdated) {
            List<TCellAnnotation> bestCellAnnotations = table_annotations.getWinningContentCellAnnotation(row, column);
            for (TCellAnnotation ca : bestCellAnnotations) {
                HeaderAnnotationUpdater.add(ca, column, table, existing_header_annotations, add);
            }
        }
        //add or not?
        if (TableMinerConstants.ALLOW_NEW_HEADERS_AT_DISAMBIGUATION_UPDATE) {
            for (TColumnHeaderAnnotation eh : existing_header_annotations)
                add.add(eh);
            existing_header_annotations = add.toArray(new TColumnHeaderAnnotation[0]);
        }

        existing_header_annotations = HeaderAnnotationUpdater.update_best_entity_contribute(
                rowsUpdated.toArray(new Integer[0]),
                column,
                tableRowsTotal,
                existing_header_annotations,
                table,
                table_annotations,
                classification_scorer
        );
        table_annotations.setHeaderAnnotation(column, existing_header_annotations);

    }

    //WARNING: CURRENTLY updating does not ADD new headers
    public void update_typing_annotations_all_candidate_contribute(List<Integer> rowsUpdated,
                                                                   int column,
                                                                   TAnnotation table_annotations,
                                                                   Table table,
                                                                   int tableRowsTotal) {
        TColumnHeaderAnnotation[] existing_header_annotations = table_annotations.getHeaderAnnotation(column);
        existing_header_annotations = existing_header_annotations == null ? new TColumnHeaderAnnotation[0] : existing_header_annotations;

        //supporting rows are only added if a header for the type of the cell annotation exists

        for (int row : rowsUpdated) {
            TCellAnnotation[] cellAnnotations = table_annotations.getContentCellAnnotations(row, column);

            Map<String, Double> header_annotation_url_and_max_score = new HashMap<String, Double>();
            Map<String, String> header_annotation_url_and_label = new HashMap<String, String>();
            for (TCellAnnotation ca : cellAnnotations) {
                List<Clazz> types = ca.getAnnotation().getTypes();
                double disamb_score = ca.getFinalScore();
                for (Clazz t : types) {
                    String url = t.getId();
                    String label = t.getLabel();
                    header_annotation_url_and_label.put(url, label);
                    Double score = header_annotation_url_and_max_score.get(url);
                    if (score == null) score = 0.0;
                    if (disamb_score > score) {
                        /*if(computeElementScores!=0)
                        System.out.println();*/
                        score = disamb_score;
                    }
                    header_annotation_url_and_max_score.put(url, score);
                }
            }

            Set<TColumnHeaderAnnotation> new_header_annotation_placeholders = new HashSet<TColumnHeaderAnnotation>();
            HeaderAnnotationUpdater.add(header_annotation_url_and_label,
                    column,
                    table,
                    existing_header_annotations,
                    new_header_annotation_placeholders);
            if (TableMinerConstants.ALLOW_NEW_HEADERS_AT_DISAMBIGUATION_UPDATE) {
                for (TColumnHeaderAnnotation ha : existing_header_annotations)
                    new_header_annotation_placeholders.add(ha);
                existing_header_annotations = new_header_annotation_placeholders.toArray(new TColumnHeaderAnnotation[0]);
            }

            HeaderAnnotationUpdater.update_by_entity_contribution(
                    header_annotation_url_and_max_score, row, existing_header_annotations
            );
            //p.close();

        }

        Set<TColumnHeaderAnnotation> headers = new HashSet<TColumnHeaderAnnotation>(Arrays.asList(existing_header_annotations));
        headers = classification_scorer.computeCCScore(
                headers, table, column);

        //final update to compute revised typing scores, then sort them
        List<TColumnHeaderAnnotation> resort = new ArrayList<TColumnHeaderAnnotation>();
        for (TColumnHeaderAnnotation ha : headers) {
            classification_scorer.computeFinal(ha, tableRowsTotal);
            /* ha.setScoreElements(revised_score_elements);
            ha.setFinalScore(revised_score_elements.get(TColumnHeaderAnnotation.FINAL));*/
            resort.add(ha);
        }

        Collections.sort(resort);
        table_annotations.setHeaderAnnotation(column, resort.toArray(new TColumnHeaderAnnotation[0]));

    }
}
