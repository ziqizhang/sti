package uk.ac.shef.dcs.oak.sti.algorithm.tm;

import uk.ac.shef.dcs.oak.sti.kb.KBSearcher_Freebase;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.selector.CellSelector;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.oak.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.CollectionUtils;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.io.IOException;
import java.util.*;

/**
 * this simply chooses column type based on relations' expected types
 */
@Deprecated
public class ColumnInterpreter_relDepend_include_entity_col extends ColumnInterpreter_relDepend {
    //private static final Logger log = Logger.getLogger(ColumnInterpreter_relDepend_v1.class.getName());
    private KBSearcher_Freebase fbSearcher;
    private ClassificationScorer classification_scorer;
    private ColumnLearner_LEARN_Update column_updater;
    private CellSelector selector;
    private int max_reference_entity_for_disambiguation;
    private int[] ignoreColumns;

    public ColumnInterpreter_relDepend_include_entity_col(KBSearcher_Freebase fbSearcher,
                                                          ClassificationScorer scorer,
                                                          ColumnLearner_LEARN_Update updater,
                                                          CellSelector selector,
                                                          int max_reference_entity_for_disambiguation,
                                                          int... ignoreColumns) {
        this.ignoreColumns = ignoreColumns;
        this.fbSearcher = fbSearcher;
        this.classification_scorer = scorer;
        this.column_updater = updater;
        this.max_reference_entity_for_disambiguation = max_reference_entity_for_disambiguation;
        this.selector=selector;
    }

    public void interpret(LTable table, LTableAnnotation annotations, Integer... ne_columns) throws IOException {
        //for each column that has a relation with the subject column, infer its type
        Map<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                relationAnnotations = annotations.getRelationAnnotations_per_row();

        for (Map.Entry<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                e : relationAnnotations.entrySet()) {
            Key_SubjectCol_ObjectCol subcol_objcol = e.getKey();
            if (ignoreColumn(subcol_objcol.getObjectCol())) continue;

            System.out.println("\t>> Relation column " + subcol_objcol.getObjectCol());
            Map<Integer, List<CellBinaryRelationAnnotation>> rows_annotated_with_relation = e.getValue();
            //what is the main type of this column? if the main type happens to be entities...
            List<ObjObj<String, Double>> sorted_scores_for_relations=new ArrayList<ObjObj<String, Double>>();
            /*if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 0)
                aggregated_scores_for_relations = score_columnBinaryRelations_best_contribute(rows_annotated_with_relation, table.getNumRows());
            else
                aggregated_scores_for_relations = score_columnBinaryRelations_all_contribute(rows_annotated_with_relation, table.getNumRows());*/
            List<HeaderBinaryRelationAnnotation> header_relations =
                    annotations.getRelationAnnotations_across_columns().get(subcol_objcol);
            for(HeaderBinaryRelationAnnotation hra: header_relations){
                ObjObj<String, Double> entry = new ObjObj<String, Double>(hra.getAnnotation_url(), hra.getFinalScore());
                sorted_scores_for_relations.add(entry);
            }

            Set<String> highest_scoring_relation_annotations = new HashSet<String>();
            double prevMax = 0.0;
            for (ObjObj<String, Double> oo : sorted_scores_for_relations) {
                String relation_name = null;
                if (oo.getOtherObject() > prevMax) {
                    prevMax = oo.getOtherObject();
                    relation_name = oo.getMainObject();
                } else if (oo.getOtherObject() == prevMax) {
                    relation_name = oo.getMainObject();
                } else {
                    break;
                }
                if (relation_name != null) {
                    highest_scoring_relation_annotations.add(relation_name);
                }
            }
            Map<Integer, Double> rows_with_entities_and_matched_scores = new HashMap<Integer, Double>();
            Map<Integer, List<ObjObj<String, String>>> rows_with_entities_and_entity_ids =
                    new HashMap<Integer, List<ObjObj<String, String>>>();

            //for those candidates that belong to the major property/relation, are they entities?
            if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 0)
                select_candidate_entities_by_best_relation(rows_annotated_with_relation,
                        highest_scoring_relation_annotations,
                        rows_with_entities_and_matched_scores,
                        rows_with_entities_and_entity_ids);
            else {
                select_candidate_entities_by_all_relation(rows_annotated_with_relation,
                        highest_scoring_relation_annotations,
                        rows_with_entities_and_matched_scores,
                        rows_with_entities_and_entity_ids);
            }
            if (rows_with_entities_and_entity_ids.size() > 0) {
                Map<String[], Double> expected_types_of_relation;
                if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 0) {
                    expected_types_of_relation = create_candidate_type_objects_best_contribute(sorted_scores_for_relations);
                } else {
                    expected_types_of_relation = create_candidate_type_objects_all_contribute(sorted_scores_for_relations);
                }
                interpret(
                        table,
                        annotations,
                        rows_with_entities_and_matched_scores,
                        rows_with_entities_and_entity_ids,
                        expected_types_of_relation,
                        subcol_objcol.getObjectCol(),
                        TableMinerConstants.RELATED_COLUMN_HEADER_TYPING_ONLY_FROM_MAIN_COL_RELATIONS
                );
            } else {
                //the related column is not entity column, simply create header annotation using the most frequent
                //relation label
                Set<HeaderAnnotation> candidates = new HashSet<HeaderAnnotation>();
                List<HeaderBinaryRelationAnnotation> relations =
                        annotations.getRelationAnnotations_across_columns().
                                get(subcol_objcol);
                for (HeaderBinaryRelationAnnotation hbr : relations) {
                    HeaderAnnotation hAnn = new HeaderAnnotation(table.getColumnHeader(subcol_objcol.getObjectCol()).getHeaderText(),
                            hbr.getAnnotation_url(), hbr.getAnnotation_label(),
                            hbr.getFinalScore());
                    candidates.add(hAnn);
                }
                /* classification_scorer.score_context(candidates, table, subcol_objcol.getObjectCol(), false);
                                for (HeaderAnnotation ha : candidates)
                                    classification_scorer.compute_final_score(ha, table.getNumRows());
                                List<HeaderAnnotation> sorted = new ArrayList<HeaderAnnotation>(candidates);
                                Collections.sort(sorted);
                                HeaderAnnotation[] hAnnotations = new HeaderAnnotation[aggregated_scores_for_relations.size()];
                                for (int i = 0; i < hAnnotations.length; i++) {
                                    hAnnotations[i] = sorted.get(i);
                                }

                */
                List<HeaderAnnotation> sorted = new ArrayList<HeaderAnnotation>(candidates);
                Collections.sort(sorted);
                annotations.setHeaderAnnotation(subcol_objcol.getObjectCol(), sorted.toArray(new HeaderAnnotation[0]));
            }
            //}
        }

    }

    @Deprecated
    private List<ObjObj<String, Double>> score_columnBinaryRelations_best_contribute(Map<Integer, List<CellBinaryRelationAnnotation>> candidate_binary_relations,
                                                                                     int tableRowsTotal) {

        Map<String, Double> binary_relation_base_score = new HashMap<String, Double>();
        Map<String, Integer> binary_relation_frequency = new HashMap<String, Integer>();
        for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> row_entry :
                candidate_binary_relations.entrySet()) {
            Collections.sort(row_entry.getValue());

            List<CellBinaryRelationAnnotation> candidates = new ArrayList<CellBinaryRelationAnnotation>();
            double prevMax = 0.0;
            for (CellBinaryRelationAnnotation cbr : row_entry.getValue()) {
                if (cbr.getScore() != 0.0 && cbr.getScore() >= prevMax) {
                    prevMax = cbr.getScore();
                    candidates.add(cbr);
                } else if (cbr.getScore() != 0.0 && cbr.getScore() < prevMax)
                    break;
            }


            for (CellBinaryRelationAnnotation cbr : candidates) {
                Double score = binary_relation_base_score.get(cbr.getAnnotation_url());
                score = score == null ? 0 : score;
                score = score + cbr.getScore();
                binary_relation_base_score.put(cbr.getAnnotation_url(), score);

                Integer freq = binary_relation_frequency.get(cbr.getAnnotation_url());
                freq = freq == null ? 0 : freq;
                freq++;
                binary_relation_frequency.put(cbr.getAnnotation_url(), freq);
            }
        }


        Map<String, Double> frequent_column_binary_relation_counting = new HashMap<String, Double>();
        for (String k : binary_relation_base_score.keySet()) {
            double match_score = binary_relation_base_score.get(k);
            Integer freq = binary_relation_frequency.get(k);
            match_score = freq == null ? match_score : match_score / freq;

            double freq_score = (double) freq / tableRowsTotal;
            frequent_column_binary_relation_counting.put(k, match_score * freq_score);
        }
        /*double max = 0;
        String vote_with_max = null;
        for (Map.Entry<String, Double> e : frequent_column_binary_relation_counting.entrySet()) {
            if (e.getValue() > max) {
                vote_with_max = e.getKey();
                max = e.getValue();
            }
        }*/
        List<ObjObj<String, Double>> rs = new ArrayList<ObjObj<String, Double>>();
        for (Map.Entry<String, Double> e : frequent_column_binary_relation_counting.entrySet()) {
            ObjObj<String, Double> member = new ObjObj<String, Double>();
            member.setMainObject(e.getKey());
            member.setOtherObject(e.getValue());
            rs.add(member);
        }
        Collections.sort(rs, new Comparator<ObjObj<String, Double>>() {
            @Override
            public int compare(ObjObj<String, Double> o1, ObjObj<String, Double> o2) {
                return o2.getOtherObject().compareTo(o1.getOtherObject());
            }
        });
        return rs;

    }

    @Deprecated
    private List<ObjObj<String, Double>> score_columnBinaryRelations_all_contribute(Map<Integer, List<CellBinaryRelationAnnotation>> candidate_binary_relations,
                                                                                    int tableRowsTotal) {

        Map<String, Double> binary_relation_disamb_score = new HashMap<String, Double>();
        Map<String, Integer> binary_relation_sum_vote = new HashMap<String, Integer>();
        for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> row_entry :
                candidate_binary_relations.entrySet()) {
            List<CellBinaryRelationAnnotation> candidates = row_entry.getValue();

            for (CellBinaryRelationAnnotation cbr : candidates) {

                Double score = binary_relation_disamb_score.get(cbr.getAnnotation_url());
                score = score == null ? 0 : score;
                score = score + cbr.getScore();
                binary_relation_disamb_score.put(cbr.getAnnotation_url(), score);

                Integer freq = binary_relation_sum_vote.get(cbr.getAnnotation_url());
                freq = freq == null ? 0 : freq;
                freq++;
                binary_relation_sum_vote.put(cbr.getAnnotation_url(), freq);
            }
        }


        Map<String, Double> frequent_column_binary_relation_counting = new HashMap<String, Double>();
        for (String k : binary_relation_disamb_score.keySet()) {
            double score_entity_disamb = binary_relation_disamb_score.get(k);
            double sum_entity_vote = (double) binary_relation_sum_vote.get(k);
            double final_score = ClassificationScorer_Vote.compute_typing_base_score(score_entity_disamb, sum_entity_vote, tableRowsTotal);

            frequent_column_binary_relation_counting.put(k, final_score);
        }
        /*double max = 0;
        String vote_with_max = null;
        for (Map.Entry<String, Double> e : frequent_column_binary_relation_counting.entrySet()) {
            if (e.getValue() > max) {
                vote_with_max = e.getKey();
                max = e.getValue();
            }
        }*/
        List<ObjObj<String, Double>> rs = new ArrayList<ObjObj<String, Double>>();
        for (Map.Entry<String, Double> e : frequent_column_binary_relation_counting.entrySet()) {
            ObjObj<String, Double> member = new ObjObj<String, Double>();
            member.setMainObject(e.getKey());
            member.setOtherObject(e.getValue());
            rs.add(member);
        }
        Collections.sort(rs, new Comparator<ObjObj<String, Double>>() {
            @Override
            public int compare(ObjObj<String, Double> o1, ObjObj<String, Double> o2) {
                return o2.getOtherObject().compareTo(o1.getOtherObject());
            }
        });
        return rs;

    }

    //go through only HIGHEST RANKED CellBinaryRelationAnnotation each row, if an annotation is the same with "highest scoring annotation
    //on this column,
    private void select_candidate_entities_by_best_relation(
            Map<Integer, List<CellBinaryRelationAnnotation>> rows_annotated_with_relation,
            Set<String> highest_scoring_relation_annotations,
            Map<Integer, Double> rows_with_entities_and_matched_scores,
            Map<Integer, List<ObjObj<String, String>>> rows_with_entities_and_entity_ids
    ) {
        for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> row_entry : //key- row id
                rows_annotated_with_relation.entrySet()) {//key-row id; value:candidate binary relation detected on this row
            //Collections.sort(row_entry.getValue());
            double prevMaxScore = 0.0;
            for (CellBinaryRelationAnnotation cbr : row_entry.getValue()) {
                if (prevMaxScore == 0.0) {
                    prevMaxScore = cbr.getScore();
                } else {
                    if (cbr.getScore() != prevMaxScore)
                        break;
                }
                /*if(prevMaxScore!=1.0)
                    continue;*/

                List<String[]> matched_values = cbr.getMatched_values();
                double score = cbr.getScore();

                for (String[] matched : matched_values) {
                    if(matched[3].equals("y"))
                        continue;
                    String prop_name = matched[0];
                    if (highest_scoring_relation_annotations.contains(prop_name)) {
                        String name = matched[1];
                        String id = matched[2];
                        //if id is not null, it is likely to be an entity, so carry on to interpret the type of this column
                        if (id != null && id.length() > 0) {
                            rows_with_entities_and_matched_scores.put(row_entry.getKey(), score);
                            List<ObjObj<String, String>> entities_on_the_row =
                                    rows_with_entities_and_entity_ids.get(row_entry.getKey());
                            entities_on_the_row = entities_on_the_row == null ? new ArrayList<ObjObj<String, String>>() : entities_on_the_row;
                            ObjObj<String, String> toAdd = new ObjObj<String, String>(id, name);
                            if (!CollectionUtils.contains_ObjObj(entities_on_the_row, toAdd))
                                entities_on_the_row.add(toAdd);
                            rows_with_entities_and_entity_ids.put(row_entry.getKey(), entities_on_the_row);
                        }
                    }
                }
            }

        }
    }

    //go through every CellBinaryRelationAnnotation each row, if an annotation is the same with "highest scoring annotation
    //on this column, it contributes to the classification of column
    private void select_candidate_entities_by_all_relation(
            Map<Integer, List<CellBinaryRelationAnnotation>> rows_annotated_with_relation,
            Set<String> highest_scoring_relation_annotation_for_column,
            Map<Integer, Double> rows_with_entities_and_matched_scores,
            Map<Integer, List<ObjObj<String, String>>> rows_with_entities_and_entity_ids
    ) {
        for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> row_entry : //key- row id
                rows_annotated_with_relation.entrySet()) {//key-row id; value:candidate binary relation detected on this row
            //Collections.sort(row_entry.getValue());
            for (CellBinaryRelationAnnotation cbr : row_entry.getValue()) {
                List<String[]> matched_values = cbr.getMatched_values();
                double score = cbr.getScore();
                if(score!=1.0)
                    continue;
                for (String[] matched : matched_values) {
                    String prop_name = matched[0];
                    if (highest_scoring_relation_annotation_for_column.contains(prop_name)) {
                        String name = matched[1];
                        String id = matched[2];
                        //if id is not null, it is likely to be an entity, so carry on to interpret the type of this column
                        if (id != null) {
                            rows_with_entities_and_matched_scores.put(row_entry.getKey(), score);
                            List<ObjObj<String, String>> entities_on_the_row =
                                    rows_with_entities_and_entity_ids.get(row_entry.getKey());
                            entities_on_the_row = entities_on_the_row == null ? new ArrayList<ObjObj<String, String>>() : entities_on_the_row;
                            ObjObj<String, String> toAdd = new ObjObj<String, String>(id, name);
                            if (!CollectionUtils.contains_ObjObj(entities_on_the_row, toAdd))
                                entities_on_the_row.add(toAdd);
                            rows_with_entities_and_entity_ids.put(row_entry.getKey(), entities_on_the_row);
                        }
                    }
                }
            }
        }
    }

    private Map<String[], Double> create_candidate_type_objects_best_contribute(
            List<ObjObj<String, Double>> aggregated_scores_for_relations) throws IOException {
        Map<String[], Double> expected_types_of_relation = new HashMap<String[], Double>();
        double prevMax = 0.0;
        for (ObjObj<String, Double> oo : aggregated_scores_for_relations) {
            String relation_name = null;
            if (oo.getOtherObject() > prevMax) {
                prevMax = oo.getOtherObject();
                relation_name = oo.getMainObject();
            } else if (oo.getOtherObject() == prevMax) {
                relation_name = oo.getMainObject();
            } else {
                break;
            }
            if (relation_name != null) {
                List<String[]> types_of_relation = fbSearcher.find_expected_types_of_relation(relation_name);
                for(String[] tor: types_of_relation)
                    expected_types_of_relation.put(tor, oo.getOtherObject());
            }
        }
        return expected_types_of_relation;
    }

    private Map<String[], Double> create_candidate_type_objects_all_contribute(
            List<ObjObj<String, Double>> aggregated_scores_for_relations) throws IOException {
        Map<String[], Double> expected_types_of_relation = new HashMap<String[], Double>();
        for (ObjObj<String, Double> oo : aggregated_scores_for_relations) {
            String relation_name = oo.getMainObject();
            if (relation_name != null) {
                List<String[]> types_of_relation = fbSearcher.find_expected_types_of_relation(relation_name);
                for(String[] tor: types_of_relation)
                    expected_types_of_relation.put(tor, oo.getOtherObject());
            }
        }
        return expected_types_of_relation;
    }

    private void interpret(LTable table,
                           LTableAnnotation table_annotation,
                           Map<Integer, Double> rows_with_entities_mapped_scores,
                           Map<Integer, List<ObjObj<String, String>>> rows_with_entity_ids,
                           Map<String[], Double> expected_types_of_relation,
                           int column,
                           boolean use_only_typing_candidates_from_relations_with_main_col) throws IOException {

        //Map<String, HeaderAnnotation> candidate_header_annotations = new HashMap<String, HeaderAnnotation>();
        //count types that are known for already mapped entities (using their ids)
        Set<EntityCandidate> reference_entities = new HashSet<EntityCandidate>();

        //0. apply one sense per discourse
        Map<Integer, Integer> map_rows_to_already_solved_rows_if_any = new HashMap<Integer, Integer>();
        Set<Integer> already_selected_cells_at_row = new HashSet<Integer>(rows_with_entities_mapped_scores.keySet());
        for(int r=0; r<table.getNumRows(); r++){
            LTableContentCell tcc = table.getContentCell(r,column);
            String text = tcc.getText().trim();

            for(int already_selected_row: already_selected_cells_at_row){
                LTableContentCell a_tcc = table.getContentCell(already_selected_row,column);
                String a_text = a_tcc.getText().trim();
                if(text.equals(a_text)){
                    rows_with_entities_mapped_scores.put(r, rows_with_entities_mapped_scores.get(already_selected_row));
                    rows_with_entity_ids.put(r, rows_with_entity_ids.get(already_selected_row));
                    map_rows_to_already_solved_rows_if_any.put(r, already_selected_row);
                }
            }
        }


        //1 initialise candidate entities on every row, create the "skip rows" list used by ColumnLearner_LEARN.learn method
        Map<String, HeaderAnnotation> header_annotation_contributed_from_cells = new HashMap<String, HeaderAnnotation>();
        for (Map.Entry<Integer, Double> e : rows_with_entities_mapped_scores.entrySet()) {
            int row = e.getKey();
            double mapped_score = e.getValue();
            List<ObjObj<String, String>> entities = rows_with_entity_ids.get(row);

            if(map_rows_to_already_solved_rows_if_any.get(row)==null)
                System.out.println("\t>>> Row " + row + " candidates=" + entities.size());
            else
                System.out.println("\t>>> Row "+ row+ "(apply OSPD)");
            for (ObjObj<String, String> entity : entities) {
                List<String[]> candidate_types = find_typesOfEntity(entity.getMainObject());

                if (!use_only_typing_candidates_from_relations_with_main_col)
                    initialize_candidate_header_typings(
                            header_annotation_contributed_from_cells,
                            candidate_types,
                            table.getColumnHeader(column).getHeaderText());

                //update cell annotation
                EntityCandidate ec = new EntityCandidate(entity.getMainObject(), entity.getOtherObject());
                reference_entities.add(ec);

                ec.getTypes().addAll(candidate_types);
                Map<String, Double> score_elements = new HashMap<String, Double>();
                score_elements.put(CellAnnotation.SCORE_FINAL, mapped_score);
                CellAnnotation ca = new CellAnnotation(table.getContentCell(row, column).getText(),
                        ec, mapped_score, score_elements
                );
                CellAnnotation[] cAnns = table_annotation.getContentCellAnnotations(row, column);
                List<CellAnnotation> new_cAnns = new ArrayList<CellAnnotation>();
                if (cAnns != null) {
                    for (CellAnnotation c : cAnns)
                        new_cAnns.add(c);
                }
                new_cAnns.add(ca);
                Collections.sort(new_cAnns);
                table_annotation.setContentCellAnnotations(row, column, new_cAnns.toArray(new CellAnnotation[0]));
            }
        }

        //2. run classification LEARN process; create initial typing annotation and disamb
        //create header annotations based on the major types
        //do update
        List<List<Integer>> rankings = selector.select(table, column, table_annotation.getSubjectColumn());

        Set<HeaderAnnotation> headerAnnotations = new HashSet<HeaderAnnotation>();
        if (!use_only_typing_candidates_from_relations_with_main_col)
            headerAnnotations.addAll(header_annotation_contributed_from_cells.values());

        for (String[] e : expected_types_of_relation.keySet()) {
            String type = e[0];
            String label = e[1];
            double score_from_relation = expected_types_of_relation.get(e);
            HeaderAnnotation ha = new HeaderAnnotation(table.getColumnHeader(column).getHeaderText(),
                    type, label, 0.0);
            boolean found=false;
            for(HeaderAnnotation added: headerAnnotations){
                if(added.equals(ha)&&TableMinerConstants.RELATION_ALSO_CONTRIBUTES_TO_COLUMN_HEADER_SCORE){
                    added.getScoreElements().put(HeaderAnnotation.SCORE_CTX_RELATION_IF_ANY, score_from_relation);
                    found=true;
                    break;
                }
            }
            if(!found){
                if(TableMinerConstants.RELATION_ALSO_CONTRIBUTES_TO_COLUMN_HEADER_SCORE)
                    ha.getScoreElements().put(HeaderAnnotation.SCORE_CTX_RELATION_IF_ANY, score_from_relation);
                headerAnnotations.add(ha);
            }
        }

        headerAnnotations = classification_scorer.score_context(headerAnnotations, table, column, false);
        table_annotation.setHeaderAnnotation(column, headerAnnotations.toArray(new HeaderAnnotation[0]));
        //this is updating header annotations given by relation
        if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 0) {
            column_updater.update_typing_annotations_best_candidate_contribute(
                    new ArrayList<Integer>(rows_with_entity_ids.keySet()),
                    column, table_annotation, table, table.getNumRows());
        } else {
            column_updater.update_typing_annotations_all_candidate_contribute(
                    new ArrayList<Integer>(rows_with_entity_ids.keySet()),
                    column, table_annotation, table, table.getNumRows());
        }

        if (max_reference_entity_for_disambiguation == 0)
            reference_entities = new HashSet<EntityCandidate>();
        else
            reference_entities = ColumnInterpreter.selectReferenceEntities(table, table_annotation, column, max_reference_entity_for_disambiguation);
        column_updater.learn_consolidate(0,
                rankings,
                table,
                table_annotation,
                column,
                reference_entities,
                rows_with_entity_ids.keySet().toArray(new Integer[0]));
    }

    private void initialize_candidate_header_typings(Map<String, HeaderAnnotation> contribution_from_cells, List<String[]> candidate_types, String headerText) {
        for (String[] ct : candidate_types) {
            String url = ct[0];
            String label = ct[1];
            HeaderAnnotation ha = contribution_from_cells.get(url);
            ha = ha == null ? new HeaderAnnotation(headerText, url, label, 0.0) : ha;
            contribution_from_cells.put(url, ha);
        }
    }

    private List<String[]> find_typesOfEntity(String entity_id) throws IOException {
        List<String[]> types = new ArrayList<String[]>();
        List<String[]> facts = fbSearcher.find_typesForEntityId(entity_id);
        for (String[] f : facts) {
            String type = f[2]; //this is the id of the type
            types.add(new String[]{type, f[1]});

        }
        return types;
    }

    public boolean ignoreColumn(Integer i) {
        if (i != null) {
            for (int a : ignoreColumns) {
                if (a == i)
                    return true;
            }
        }
        return false;
    }

}
