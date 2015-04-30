/*
package uk.ac.shef.dcs.oak.lodie.table.interpreter.baseline;

import uk.ac.shef.dcs.oak.lodie.table.interpreter.content.KBSearcher_Freebase;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.interpret.ClassificationScorer_Vote;
import uk.ac.shef.dcs.oak.lodie.table.rep.*;
import uk.ac.shef.dcs.oak.lodie.test.TableMinerConstants;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.CollectionUtils;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.io.IOException;
import java.util.*;

*/
/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 25/02/14
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 *//*

public class Base_NameMatch_ColumnLearner_relDepend_v2 {
    private KBSearcher_Freebase fbSearcher;
    private Base_NameMatch_ColumnLearner column_learner;
    private int[] ignoreColumns;

    public Base_NameMatch_ColumnLearner_relDepend_v2(KBSearcher_Freebase fbSearcher,
                                                     Base_NameMatch_ColumnLearner learner,
                                                     int... ignoreColumns) {
        this.ignoreColumns=ignoreColumns;
        this.fbSearcher = fbSearcher;
        this.column_learner = learner;
    }

    public void interpret(LTable table, LTableAnnotation annotations) throws IOException {
        //for each column that has a relation with the subject column, infer its type
        Map<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                relationAnnotations = annotations.getRelationAnnotations_per_row();

        for (Map.Entry<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                e : relationAnnotations.entrySet()) {
            Key_SubjectCol_ObjectCol subcol_objcol = e.getKey();
            if(ignoreColumn(subcol_objcol.getObjectCol())) continue;

            System.out.println("\t>> Relation column " + subcol_objcol.getObjectCol());
            Map<Integer, List<CellBinaryRelationAnnotation>> rows_annotated_with_relation = e.getValue();
            //what is the main type of this column? if the main type happens to be entities...
            List<ObjObj<String, Double>> aggregated_scores_for_relations;
            if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 0)
                aggregated_scores_for_relations = score_columnBinaryRelations_best_contribute(rows_annotated_with_relation, table.getNumRows());
            else
                aggregated_scores_for_relations = score_columnBinaryRelations_all_contribute(rows_annotated_with_relation, table.getNumRows());

            Set<String> expected_types_of_relation_strings = new HashSet<String>();
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
                    expected_types_of_relation_strings.add(relation_name);
                }
            }
            Map<Integer, Double> rows_with_entities_and_matched_scores = new HashMap<Integer, Double>();
            Map<Integer, List<ObjObj<String, String>>> rows_with_entities_and_entity_ids =
                    new HashMap<Integer, List<ObjObj<String, String>>>();

            //for those candidates that belong to the major property/relation, are they entities?
            if(TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD==0)
                select_candidate_entities_by_best_relation(rows_annotated_with_relation,
                        expected_types_of_relation_strings,
                        rows_with_entities_and_matched_scores,
                        rows_with_entities_and_entity_ids);
            else{
                select_candidate_entities_by_all_relation(rows_annotated_with_relation,
                        expected_types_of_relation_strings,
                        rows_with_entities_and_matched_scores,
                        rows_with_entities_and_entity_ids);
            }
            if (rows_with_entities_and_entity_ids.size() > 0) {
                List<String[]> expected_types_of_relation;
                if(TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD==0){
                    expected_types_of_relation=create_candidate_type_objects_best_contribute(aggregated_scores_for_relations);
                }   else{
                    expected_types_of_relation=create_candidate_type_objects_all_contribute(aggregated_scores_for_relations);
                }
                interpret(
                        table,
                        annotations,
                        rows_with_entities_and_matched_scores,
                        rows_with_entities_and_entity_ids,
                        expected_types_of_relation,
                        subcol_objcol.getObjectCol()
                );
            } else {
                //the related column is not entity column, simply create header annotation using the most frequent
                //relation label
                HeaderAnnotation[] hAnnotations = new HeaderAnnotation[aggregated_scores_for_relations.size()];
                for (int i = 0; i < aggregated_scores_for_relations.size(); i++) {
                    ObjObj<String, Double> candidate = aggregated_scores_for_relations.get(i);
                    HeaderAnnotation hAnn = new HeaderAnnotation(table.getColumnHeader(subcol_objcol.getObjectCol()).getHeaderText(),
                            candidate.getMainObject(), candidate.getMainObject(),
                            candidate.getOtherObject());
                    hAnnotations[i] = hAnn;
                }

                annotations.setHeaderAnnotation(subcol_objcol.getObjectCol(), hAnnotations);
            }
            //}
        }

    }

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
            double score = binary_relation_base_score.get(k);
            double freq_score = (double) binary_relation_frequency.get(k) / tableRowsTotal;
            frequent_column_binary_relation_counting.put(k, score + freq_score);
        }
        */
/*double max = 0;
        String vote_with_max = null;
        for (Map.Entry<String, Double> e : frequent_column_binary_relation_counting.entrySet()) {
            if (e.getValue() > max) {
                vote_with_max = e.getKey();
                max = e.getValue();
            }
        }*//*

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
        */
/*double max = 0;
        String vote_with_max = null;
        for (Map.Entry<String, Double> e : frequent_column_binary_relation_counting.entrySet()) {
            if (e.getValue() > max) {
                vote_with_max = e.getKey();
                max = e.getValue();
            }
        }*//*

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

    private void select_candidate_entities_by_best_relation(
            Map<Integer, List<CellBinaryRelationAnnotation>> rows_annotated_with_relation,
            Set<String> expected_types_of_relation_strings,
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

                List<String[]> matched_values = cbr.getMatched_values();
                double score = cbr.getScore();

                for (String[] matched : matched_values) {
                    String prop_name = matched[0];
                    if (expected_types_of_relation_strings.contains(prop_name)) {
                        String name = matched[1];
                        String id = matched[2];
                        //if id is not null, it is likely to be an entity, so carry on to interpret the type of this column
                        if (id != null&&id.length()>0) {
                            rows_with_entities_and_matched_scores.put(row_entry.getKey(), score);
                            List<ObjObj<String, String>> entities_on_the_row =
                                    rows_with_entities_and_entity_ids.get(row_entry.getKey());
                            entities_on_the_row = entities_on_the_row == null ? new ArrayList<ObjObj<String, String>>() : entities_on_the_row;
                            ObjObj<String, String> toAdd =new ObjObj<String, String>(id, name);
                            if(!CollectionUtils.contains_ObjObj(entities_on_the_row, toAdd))
                                entities_on_the_row.add(toAdd);
                            rows_with_entities_and_entity_ids.put(row_entry.getKey(), entities_on_the_row);
                        }
                    }
                }
            }

        }
    }


    private void select_candidate_entities_by_all_relation(
            Map<Integer, List<CellBinaryRelationAnnotation>> rows_annotated_with_relation,
            Set<String> expected_types_of_relation_strings,
            Map<Integer, Double> rows_with_entities_and_matched_scores,
            Map<Integer, List<ObjObj<String, String>>> rows_with_entities_and_entity_ids
    ) {
        for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> row_entry : //key- row id
                rows_annotated_with_relation.entrySet()) {//key-row id; value:candidate binary relation detected on this row
            //Collections.sort(row_entry.getValue());
            for (CellBinaryRelationAnnotation cbr : row_entry.getValue()) {
                List<String[]> matched_values = cbr.getMatched_values();
                double score = cbr.getScore();
                for (String[] matched : matched_values) {
                    String prop_name = matched[0];
                    if (expected_types_of_relation_strings.contains(prop_name)) {
                        String name = matched[1];
                        String id = matched[2];
                        //if id is not null, it is likely to be an entity, so carry on to interpret the type of this column
                        if (id != null) {
                            rows_with_entities_and_matched_scores.put(row_entry.getKey(), score);
                            List<ObjObj<String, String>> entities_on_the_row =
                                    rows_with_entities_and_entity_ids.get(row_entry.getKey());
                            entities_on_the_row = entities_on_the_row == null ? new ArrayList<ObjObj<String, String>>() : entities_on_the_row;
                            ObjObj<String, String> toAdd =new ObjObj<String, String>(id, name);
                            if(!CollectionUtils.contains_ObjObj(entities_on_the_row, toAdd))
                                entities_on_the_row.add(toAdd);
                            rows_with_entities_and_entity_ids.put(row_entry.getKey(), entities_on_the_row);
                        }
                    }
                }
            }
        }
    }

    private List<String[]> create_candidate_type_objects_best_contribute(
            List<ObjObj<String, Double>> aggregated_scores_for_relations) throws IOException {
        List<String[]> expected_types_of_relation = new ArrayList<String[]>();
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
                expected_types_of_relation.addAll(types_of_relation);
            }
        }
        return expected_types_of_relation;
    }

    private List<String[]> create_candidate_type_objects_all_contribute(
            List<ObjObj<String, Double>> aggregated_scores_for_relations) throws IOException {
        List<String[]> expected_types_of_relation = new ArrayList<String[]>();
        for (ObjObj<String, Double> oo : aggregated_scores_for_relations) {
            String relation_name =oo.getMainObject();
            if (relation_name != null) {
                List<String[]> types_of_relation = fbSearcher.find_expected_types_of_relation(relation_name);
                expected_types_of_relation.addAll(types_of_relation);
            }
        }
        return expected_types_of_relation;
    }

    private void interpret(LTable table,
                           LTableAnnotation table_annotation,
                           Map<Integer, Double> rows_with_entities_mapped_scores,
                           Map<Integer, List<ObjObj<String, String>>> rows_with_entity_ids,
                           List<String[]> expected_types_of_relation,
                           int column) throws IOException {

        //Map<String, HeaderAnnotation> candidate_header_annotations = new HashMap<String, HeaderAnnotation>();
        //count types that are known for already mapped entities (using their ids)
        Set<EntityCandidate> reference_entities = new HashSet<EntityCandidate>();

        //1 initialise candidate entities on every row, create the "skip rows" list used by ColumnLearner_LEARN.learn method
        for (Map.Entry<Integer, Double> e : rows_with_entities_mapped_scores.entrySet()) {
            int row = e.getKey();
            double mapped_score = e.getValue();
            List<ObjObj<String, String>> entities = rows_with_entity_ids.get(row);
            System.out.println("\t>>> Row "+row+" candidates="+entities.size());
            for (ObjObj<String, String> entity : entities) {
                List<String[]> candidate_types = find_typesOfEntity(entity.getMainObject());

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
        Set<HeaderAnnotation> headerAnnotations = new HashSet<HeaderAnnotation>();
        for (String[] e : expected_types_of_relation) {
            String type = e[0];
            String label = e[1];
            HeaderAnnotation ha = new HeaderAnnotation(table.getColumnHeader(column).getHeaderText(),
                    type, label, 0.0); //the update process will update header score based on entities
            headerAnnotations.add(ha);
        }

        table_annotation.setHeaderAnnotation(column, headerAnnotations.toArray(new HeaderAnnotation[0]));
        column_learner.update_typing_annotations_best_candidate_contribute(new ArrayList<Integer>(rows_with_entity_ids.keySet()),
                column, table_annotation, table.getNumRows());

        column_learner.interpret(
                table,
                table_annotation,
                column,
                rows_with_entity_ids.keySet().toArray(new Integer[0]));
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

    public boolean ignoreColumn(Integer i){
        if(i!=null){
            for(int a: ignoreColumns){
                if(a==i)
                    return true;
            }
        }
        return false;
    }

}
*/
