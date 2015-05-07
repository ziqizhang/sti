package uk.ac.shef.dcs.oak.sti.algorithm.tm;

import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.oak.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.io.IOException;
import java.util.*;

/**
 */
public class ColumnLearner_LEARN_Update {

    private Disambiguator disambiguator;
    private KnowledgeBaseSearcher kbSearcher;
    private ClassificationScorer classification_scorer;

    public ColumnLearner_LEARN_Update(KnowledgeBaseSearcher kbSearcher,
                                      Disambiguator disambiguator,
                                      ClassificationScorer classification_scorer) {
        this.kbSearcher = kbSearcher;
        this.disambiguator = disambiguator;
        this.classification_scorer = classification_scorer;
    }

    public void learn_consolidate(
            int anchor,
            List<List<Integer>> ranking,
            LTable table,
            LTableAnnotation current_iteration_annotation,
            int column,
            Set<EntityCandidate> reference_entities,
            Integer... skipRows) throws IOException {


        System.out.println("\t>> LEARN (Consolidate) begins");
        List<HeaderAnnotation> bestHeaderAnnotations = current_iteration_annotation.getBestHeaderAnnotations(column);
        Set<String> columnTypes = new HashSet<String>();
        for (HeaderAnnotation ha : bestHeaderAnnotations)
            columnTypes.add(ha.getAnnotation_url());

        int start = anchor;
        int end = ranking.size();


        List<Integer> updated = new ArrayList<Integer>();
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
            LTableContentCell sample = table.getContentCell(rows.get(0), column);
            /*if (sample.getType().equals(DataTypeClassifier.DataType.LONG_TEXT)) {
                System.out.println("\t\t>>> Long text cell skipped: " + rows + "," + column + " " + sample.getText());
                continue;
            }*/
            if (sample.getText().length() < 2) {
                System.out.println("\t\t>>> Long text cell skipped: " + rows + "," + column + " " + sample.getText());
                continue;
            }


            List<ObjObj<EntityCandidate, Map<String, Double>>>
                    candidates_and_scores_for_block =
                    disambiguate(sample,
                            table,
                            //current_iteration_annotation,
                            columnTypes,
                            rows, column, reference_entities.toArray(new EntityCandidate[0])
                    );

            if (candidates_and_scores_for_block.size() > 0) {
                update_entity_annotations(table, current_iteration_annotation, rows, column,
                        candidates_and_scores_for_block);
                updated.addAll(rows);
            }
        }
        //todo: one-name-PSPD, before create typing
        if (TableMinerConstants.ENFORCE_ONPSPD)
            ONPSPD_Enforcer.enforce(table, current_iteration_annotation, column);

        System.out.println("\t>> Classification-LEARN (consolidate " + updated.size() + " rows)");
        if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 0)
            update_typing_annotations_best_candidate_contribute(updated, column, current_iteration_annotation, table,
                    table.getNumRows()
                    //countRows
            );
        else
            update_typing_annotations_all_candidate_contribute(updated, column, current_iteration_annotation, table,
                    table.getNumRows()
                    //countRows
            );
    }


    //search candidates for the cell;
    //score candidates for the cell;
    //create annotation and update supportin header and header score (depending on the two params updateHeader_blah
    private List<ObjObj<EntityCandidate, Map<String, Double>>> disambiguate(LTableContentCell tcc,
                                                                            LTable table,
                                                                            Set<String> columnTypes,
                                                                            List<Integer> table_cell_rows,
                                                                            int table_cell_col,
                                                                            EntityCandidate... reference_disambiguated_entities) throws IOException {
        List<ObjObj<EntityCandidate, Map<String, Double>>> candidates_and_scores_for_block
                = new ArrayList<ObjObj<EntityCandidate, Map<String, Double>>>();

        List<EntityCandidate> candidates = kbSearcher.find_matchingEntities_with_type_forCell(tcc, columnTypes.toArray(new String[0]));
        if (candidates != null && candidates.size() != 0) {
        } else {
            candidates = kbSearcher.find_matchingEntities_with_type_forCell(tcc);
        }

        //now each candidate is given scores
        candidates_and_scores_for_block =
                disambiguator.disambiguate_learn_consolidate
                        (candidates, table, table_cell_rows, table_cell_col, columnTypes, true,reference_disambiguated_entities);

        return candidates_and_scores_for_block;
    }


    //disambiguate cells in a column, assuming the type is "column_type". supporting row info is added to headers
    //updateHeaderSupportingRow: weather the disamb result on each row should also update header's supporting row
    //updateHeaderScore: weather the disamb result score should be incremented to the header's score (e.g., those that contributed to the classification of the column in the first place shouldbe disregarded; while the remaining columns shouldbe considered
    private void update_entity_annotations(
            LTable table,
            LTableAnnotation table_annotation,
            List<Integer> table_cell_rows,
            int table_cell_col,
            List<ObjObj<EntityCandidate, Map<String, Double>>> candidates_and_scores_for_cell) {

        Collections.sort(candidates_and_scores_for_cell, new Comparator<ObjObj<EntityCandidate, Map<String, Double>>>() {
            @Override
            public int compare(ObjObj<EntityCandidate, Map<String, Double>> o1, ObjObj<EntityCandidate, Map<String, Double>> o2) {
                Double o2_score = o2.getOtherObject().get(CellAnnotation.SCORE_FINAL);
                Double o1_score = o1.getOtherObject().get(CellAnnotation.SCORE_FINAL);
                return o2_score.compareTo(o1_score);
            }
        });

        String sampleCellText = table.getContentCell(table_cell_rows.get(0), table_cell_col).getText();

        for (int row : table_cell_rows) {
            CellAnnotation[] annotationsForCell = new CellAnnotation[candidates_and_scores_for_cell.size()];
            for (int i = 0; i < candidates_and_scores_for_cell.size(); i++) {
                ObjObj<EntityCandidate, Map<String, Double>> e = candidates_and_scores_for_cell.get(i);
                annotationsForCell[i] = new CellAnnotation(sampleCellText,
                        e.getMainObject(), e.getOtherObject().get("final"), e.getOtherObject());
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
                                                                    LTableAnnotation table_annotations,
                                                                    LTable table,
                                                                    int tableRowsTotal) {
        HeaderAnnotation[] existing_header_annotations = table_annotations.getHeaderAnnotation(column);
        existing_header_annotations = existing_header_annotations == null ? new HeaderAnnotation[0] : existing_header_annotations;

        //supporting rows are only added if a header for the type of the cell annotation exists
        Set<HeaderAnnotation> add = new HashSet<HeaderAnnotation>();
        //any new headers due to disambiguation-update?
        for (int row : rowsUpdated) {
            List<CellAnnotation> bestCellAnnotations = table_annotations.getBestContentCellAnnotations(row, column);
            for (CellAnnotation ca : bestCellAnnotations) {
                HeaderAnnotationUpdater.add(ca, column, table, existing_header_annotations, add);
            }
        }
        //add or not?
        if (TableMinerConstants.ALLOW_NEW_HEADERS_AT_DISAMBIGUATION_UPDATE) {
            for (HeaderAnnotation eh : existing_header_annotations)
                add.add(eh);
            existing_header_annotations = add.toArray(new HeaderAnnotation[0]);
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
                                                                   LTableAnnotation table_annotations,
                                                                   LTable table,
                                                                   int tableRowsTotal) {
        HeaderAnnotation[] existing_header_annotations = table_annotations.getHeaderAnnotation(column);
        existing_header_annotations = existing_header_annotations == null ? new HeaderAnnotation[0] : existing_header_annotations;

        //supporting rows are only added if a header for the type of the cell annotation exists

        for (int row : rowsUpdated) {
            CellAnnotation[] cellAnnotations = table_annotations.getContentCellAnnotations(row, column);

            Map<String, Double> header_annotation_url_and_max_score = new HashMap<String, Double>();
            Map<String, String> header_annotation_url_and_label = new HashMap<String, String>();
            for (CellAnnotation ca : cellAnnotations) {
                List<String[]> types = ca.getAnnotation().getTypes();
                double disamb_score = ca.getFinalScore();
                for (String[] t : types) {
                    String url = t[0];
                    String label = t[1];
                    header_annotation_url_and_label.put(url, label);
                    Double score = header_annotation_url_and_max_score.get(url);
                    if (score == null) score = 0.0;
                    if (disamb_score > score) {
                        /*if(score!=0)
                        System.out.println();*/
                        score = disamb_score;
                    }
                    header_annotation_url_and_max_score.put(url, score);
                }
            }

            Set<HeaderAnnotation> new_header_annotation_placeholders = new HashSet<HeaderAnnotation>();
            HeaderAnnotationUpdater.add(header_annotation_url_and_label,
                    column,
                    table,
                    existing_header_annotations,
                    new_header_annotation_placeholders);
            if (TableMinerConstants.ALLOW_NEW_HEADERS_AT_DISAMBIGUATION_UPDATE) {
                for (HeaderAnnotation ha : existing_header_annotations)
                    new_header_annotation_placeholders.add(ha);
                existing_header_annotations = new_header_annotation_placeholders.toArray(new HeaderAnnotation[0]);
            }

            HeaderAnnotationUpdater.update_by_entity_contribution(
                    header_annotation_url_and_max_score, row, existing_header_annotations
            );
            //p.close();

        }

        Set<HeaderAnnotation> headers = new HashSet<HeaderAnnotation>(Arrays.asList(existing_header_annotations));
        headers = classification_scorer.score_context(
                headers, table, column, false);

        //final update to compute revised typing scores, then sort them
        List<HeaderAnnotation> resort = new ArrayList<HeaderAnnotation>();
        for (HeaderAnnotation ha : headers) {
            classification_scorer.compute_final_score(ha, tableRowsTotal);
            /* ha.setScoreElements(revised_score_elements);
            ha.setFinalScore(revised_score_elements.get(HeaderAnnotation.FINAL));*/
            resort.add(ha);
        }

        Collections.sort(resort);
        table_annotations.setHeaderAnnotation(column, resort.toArray(new HeaderAnnotation[0]));

    }
}
