package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearch;
import uk.ac.shef.dcs.kbsearch.rep.Attribute;
import uk.ac.shef.dcs.sti.algorithm.tm.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.*;
import uk.ac.shef.dcs.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

/**
 * this simply chooses column type based on relations' expected types
 */
@Deprecated
public class DataLiteralColumnClassifier_include_entity_col extends DataLiteralColumnClassifier {
    //private static final Logger LOG = Logger.getLogger(ColumnInterpreter_relDepend_v1.class.getName());
    private FreebaseSearch fbSearcher;
    private TColumnClassifier classification_scorer;
    private LEARNINGPreliminaryDisamb column_updater;
    private TContentCellRanker selector;
    private int max_reference_entity_for_disambiguation;
    private int[] ignoreColumns;

    public DataLiteralColumnClassifier_include_entity_col(FreebaseSearch fbSearcher,
                                                          TColumnClassifier scorer,
                                                          LEARNINGPreliminaryDisamb updater,
                                                          TContentCellRanker selector,
                                                          int max_reference_entity_for_disambiguation,
                                                          int... ignoreColumns) {
        this.ignoreColumns = ignoreColumns;
        this.fbSearcher = fbSearcher;
        this.classification_scorer = scorer;
        this.column_updater = updater;
        this.max_reference_entity_for_disambiguation = max_reference_entity_for_disambiguation;
        this.selector=selector;
    }

    public void interpret(Table table, TAnnotation annotations, Integer... ne_columns) throws KBSearchException {
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
            List<Pair<String, Double>> sorted_scores_for_relations=new ArrayList<>();
            /*if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 0)
                aggregated_scores_for_relations = score_columnBinaryRelations_best_contribute(rows_annotated_with_relation, table.getNumRows());
            else
                aggregated_scores_for_relations = score_columnBinaryRelations_all_contribute(rows_annotated_with_relation, table.getNumRows());*/
            List<HeaderBinaryRelationAnnotation> header_relations =
                    annotations.getRelationAnnotations_across_columns().get(subcol_objcol);
            for(HeaderBinaryRelationAnnotation hra: header_relations){
                Pair<String, Double> entry = new Pair<String, Double>(hra.getAnnotation_url(), hra.getFinalScore());
                sorted_scores_for_relations.add(entry);
            }

            Set<String> highest_scoring_relation_annotations = new HashSet<String>();
            double prevMax = 0.0;
            for (Pair<String, Double> oo : sorted_scores_for_relations) {
                String relation_name = null;
                if (oo.getValue() > prevMax) {
                    prevMax = oo.getValue();
                    relation_name = oo.getKey();
                } else if (oo.getValue() == prevMax) {
                    relation_name = oo.getKey();
                } else {
                    break;
                }
                if (relation_name != null) {
                    highest_scoring_relation_annotations.add(relation_name);
                }
            }
            Map<Integer, Double> rows_with_entities_and_matched_scores = new HashMap<Integer, Double>();
            Map<Integer, List<Pair<String, String>>> rows_with_entities_and_entity_ids =
                    new HashMap<>();

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
                Map<Clazz, Double> expected_types_of_relation;
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
                                    classification_scorer.computeFinal(ha, table.getNumRows());
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
    private List<Pair<String, Double>> score_columnBinaryRelations_best_contribute(Map<Integer, List<CellBinaryRelationAnnotation>> candidate_binary_relations,
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
        List<Pair<String, Double>> rs = new ArrayList<>();
        for (Map.Entry<String, Double> e : frequent_column_binary_relation_counting.entrySet()) {
            Pair<String, Double> member = new Pair<>(e.getKey(), e.getValue());
            rs.add(member);
        }
        Collections.sort(rs, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        return rs;

    }

    @Deprecated
    private List<Pair<String, Double>> score_columnBinaryRelations_all_contribute(Map<Integer, List<CellBinaryRelationAnnotation>> candidate_binary_relations,
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
            double final_score = TMPTColumnClassifier.compute_typing_base_score(score_entity_disamb, sum_entity_vote, tableRowsTotal);

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
        List<Pair<String, Double>> rs = new ArrayList<>();
        for (Map.Entry<String, Double> e : frequent_column_binary_relation_counting.entrySet()) {
            Pair<String, Double> member = new Pair<>(e.getKey(), e.getValue());
            rs.add(member);
        }
        Collections.sort(rs, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
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
            Map<Integer, List<Pair<String, String>>> rows_with_entities_and_entity_ids
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

                List<Attribute> matched_values = cbr.getMatched_values();
                double score = cbr.getScore();

                for (Attribute matched : matched_values) {
                    if(!matched.isDirect())
                        continue;
                    String prop_name = matched.getRelation();
                    if (highest_scoring_relation_annotations.contains(prop_name)) {
                        String name = matched.getValue();
                        String id = matched.getValueURI();
                        //if id is not null, it is likely to be an entity, so carry on to score the type of this column
                        if (id != null && id.length() > 0) {
                            rows_with_entities_and_matched_scores.put(row_entry.getKey(), score);
                            List<Pair<String, String>> entities_on_the_row =
                                    rows_with_entities_and_entity_ids.get(row_entry.getKey());
                            entities_on_the_row = entities_on_the_row == null ? new ArrayList<Pair<String, String>>() : entities_on_the_row;
                            Pair<String, String> toAdd = new Pair<>(id, name);
                            if (!CollectionUtils.containsPair(entities_on_the_row, toAdd))
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
            Map<Integer, List<Pair<String, String>>> rows_with_entities_and_entity_ids
    ) {
        for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> row_entry : //key- row id
                rows_annotated_with_relation.entrySet()) {//key-row id; value:candidate binary relation detected on this row
            //Collections.sort(row_entry.getValue());
            for (CellBinaryRelationAnnotation cbr : row_entry.getValue()) {
                List<Attribute> matched_values = cbr.getMatched_values();
                double score = cbr.getScore();
                if(score!=1.0)
                    continue;
                for (Attribute matched : matched_values) {
                    String prop_name = matched.getRelation();
                    if (highest_scoring_relation_annotation_for_column.contains(prop_name)) {
                        String name = matched.getValue();
                        String id = matched.getValueURI();
                        //if id is not null, it is likely to be an entity, so carry on to score the type of this column
                        if (id != null) {
                            rows_with_entities_and_matched_scores.put(row_entry.getKey(), score);
                            List<Pair<String, String>> entities_on_the_row =
                                    rows_with_entities_and_entity_ids.get(row_entry.getKey());
                            entities_on_the_row = entities_on_the_row == null ? new ArrayList<Pair<String, String>>() : entities_on_the_row;
                            Pair<String, String> toAdd = new Pair<>(id, name);
                            if (!CollectionUtils.containsPair(entities_on_the_row, toAdd))
                                entities_on_the_row.add(toAdd);
                            rows_with_entities_and_entity_ids.put(row_entry.getKey(), entities_on_the_row);
                        }
                    }
                }
            }
        }
    }

    private Map<Clazz, Double> create_candidate_type_objects_best_contribute(
            List<Pair<String, Double>> aggregated_scores_for_relations) throws KBSearchException {
        Map<Clazz, Double> expected_types_of_relation = new HashMap<>();
        double prevMax = 0.0;
        for (Pair<String, Double> oo : aggregated_scores_for_relations) {
            String relation_name = null;
            if (oo.getValue() > prevMax) {
                prevMax = oo.getValue();
                relation_name = oo.getKey();
            } else if (oo.getValue() == prevMax) {
                relation_name = oo.getKey();
            } else {
                break;
            }
            if (relation_name != null) {
                List<Clazz> types_of_relation = fbSearcher.findRangeOfRelation(relation_name);
                for(Clazz tor: types_of_relation)
                    expected_types_of_relation.put(tor, oo.getValue());
            }
        }
        return expected_types_of_relation;
    }

    private Map<Clazz, Double> create_candidate_type_objects_all_contribute(
            List<Pair<String, Double>> aggregated_scores_for_relations) throws KBSearchException {
        Map<Clazz, Double> expected_types_of_relation = new HashMap<>();
        for (Pair<String, Double> oo : aggregated_scores_for_relations) {
            String relation_name = oo.getKey();
            if (relation_name != null) {
                List<Clazz> types_of_relation = fbSearcher.findRangeOfRelation(relation_name);
                for(Clazz tor: types_of_relation)
                    expected_types_of_relation.put(tor, oo.getValue());
            }
        }
        return expected_types_of_relation;
    }

    private void interpret(Table table,
                           TAnnotation table_annotation,
                           Map<Integer, Double> rows_with_entities_mapped_scores,
                           Map<Integer, List<Pair<String, String>>> rows_with_entity_ids,
                           Map<Clazz, Double> expected_types_of_relation,
                           int column,
                           boolean use_only_typing_candidates_from_relations_with_main_col) throws KBSearchException {

        //Map<String, HeaderAnnotation> candidate_header_annotations = new HashMap<String, HeaderAnnotation>();
        //count types that are known for already mapped entities (using their ids)
        Set<Entity> reference_entities = new HashSet<>();

        //0. apply one sense per discourse
        Map<Integer, Integer> map_rows_to_already_solved_rows_if_any = new HashMap<Integer, Integer>();
        Set<Integer> already_selected_cells_at_row = new HashSet<Integer>(rows_with_entities_mapped_scores.keySet());
        for(int r=0; r<table.getNumRows(); r++){
            TContentCell tcc = table.getContentCell(r,column);
            String text = tcc.getText().trim();

            for(int already_selected_row: already_selected_cells_at_row){
                TContentCell a_tcc = table.getContentCell(already_selected_row,column);
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
            List<Pair<String, String>> entities = rows_with_entity_ids.get(row);

            if(map_rows_to_already_solved_rows_if_any.get(row)==null)
                System.out.println("\t>>> Row " + row + " candidates=" + entities.size());
            else
                System.out.println("\t>>> Row "+ row+ "(apply OSPD)");
            for (Pair<String, String> entity : entities) {
                List<Clazz> candidate_types = fbSearcher.find_typesForEntity_filtered(entity.getKey());

                if (!use_only_typing_candidates_from_relations_with_main_col)
                    initialize_candidate_header_typings(
                            header_annotation_contributed_from_cells,
                            candidate_types,
                            table.getColumnHeader(column).getHeaderText());

                //update cell annotation
                Entity ec = new Entity(entity.getKey(), entity.getValue());
                reference_entities.add(ec);

                ec.getTypes().addAll(candidate_types);
                Map<String, Double> score_elements = new HashMap<String, Double>();
                score_elements.put(TCellAnnotation.SCORE_FINAL, mapped_score);
                TCellAnnotation ca = new TCellAnnotation(table.getContentCell(row, column).getText(),
                        ec, mapped_score, score_elements
                );
                TCellAnnotation[] cAnns = table_annotation.getContentCellAnnotations(row, column);
                List<TCellAnnotation> new_cAnns = new ArrayList<TCellAnnotation>();
                if (cAnns != null) {
                    for (TCellAnnotation c : cAnns)
                        new_cAnns.add(c);
                }
                new_cAnns.add(ca);
                Collections.sort(new_cAnns);
                table_annotation.setContentCellAnnotations(row, column, new_cAnns.toArray(new TCellAnnotation[0]));
            }
        }

        //2. run classification LEARN process; create initial typing annotation and disamb
        //create header annotations based on the major types
        //do update
        List<List<Integer>> rankings = selector.select(table, column, table_annotation.getSubjectColumn());

        Set<HeaderAnnotation> headerAnnotations = new HashSet<HeaderAnnotation>();
        if (!use_only_typing_candidates_from_relations_with_main_col)
            headerAnnotations.addAll(header_annotation_contributed_from_cells.values());

        for (Clazz e : expected_types_of_relation.keySet()) {
            String type = e.getId();
            String label = e.getLabel();
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
                    new ArrayList<>(rows_with_entity_ids.keySet()),
                    column, table_annotation, table, table.getNumRows());
        }

        if (max_reference_entity_for_disambiguation == 0)
            reference_entities = new HashSet<Entity>();
        else
            reference_entities = LEARNING.selectReferenceEntities(table, table_annotation, column, max_reference_entity_for_disambiguation);
        column_updater.learn_consolidate(0,
                rankings,
                table,
                table_annotation,
                column,
                reference_entities,
                rows_with_entity_ids.keySet().toArray(new Integer[0]));
    }

    private void initialize_candidate_header_typings(Map<String, HeaderAnnotation> contribution_from_cells,
                                                     List<Clazz> candidate_types, String headerText) {
        for (Clazz ct : candidate_types) {
            String url = ct.getId();
            String label = ct.getLabel();
            HeaderAnnotation ha = contribution_from_cells.get(url);
            ha = ha == null ? new HeaderAnnotation(headerText, url, label, 0.0) : ha;
            contribution_from_cells.put(url, ha);
        }
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
