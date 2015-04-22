/*
package uk.ac.shef.dcs.oak.lodie.table.interpreter.interpret;

import uk.ac.shef.dcs.oak.lodie.table.interpreter.content.KBSearcher_Freebase;
import uk.ac.shef.dcs.oak.lodie.table.rep.*;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.CollectionUtils;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

*/
/**
 * column types based on already interpreted entities in that column. interpretation continues same fashion using columnlearner_learn and update
 *//*

@Deprecated
public class ColumnInterpreter_relDepend_v1 extends ColumnInterpreter_relDepend {
    private static final Logger log = Logger.getLogger(ColumnInterpreter_relDepend_v1.class.getName());
    private KBSearcher_Freebase fbSearcher;
    private ColumnLearner_LEARN column_learner;
    private ColumnLearner_UPDATE column_updater;
    private boolean use_reference_entity;

    public ColumnInterpreter_relDepend_v1(KBSearcher_Freebase fbSearcher,
                                          ColumnLearner_LEARN learner,
                                          ColumnLearner_UPDATE updater, boolean use_reference_entity) {
        this.fbSearcher = fbSearcher;
        this.column_learner = learner;
        this.column_updater = updater;
        this.use_reference_entity = use_reference_entity;
    }

    public void interpret(LTable table, LTableAnnotation annotations) throws IOException {
        //for each column that has a relation with the subject column, infer its type
        Map<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                relationAnnotations = annotations.getRelationAnnotations_per_row();

        for (Map.Entry<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                e : relationAnnotations.entrySet()) {
            Key_SubjectCol_ObjectCol subcol_objcol = e.getKey();
            log.info(">>\tRelation column " + subcol_objcol.getObjectCol());
            Map<Integer, List<CellBinaryRelationAnnotation>> rows_annotated_with_relation = e.getValue();
            //what is the main type of this column? if the main type happens to be entities...
            List<ObjObj<String, Double>> aggregated_scores_for_relations =
                    vote_columnBinaryRelations(rows_annotated_with_relation, table.getNumRows());
            String majority_relation_name = aggregated_scores_for_relations.get(0).getMainObject().toString();

            Map<Integer, Double> rows_with_entities_and_matched_scores = new HashMap<Integer, Double>();
            Map<Integer, List<ObjObj<String, String>>> rows_with_entities_and_entity_ids =
                    new HashMap<Integer, List<ObjObj<String, String>>>();

            //for those candidates that belong to the major property/relation, are they entities?
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
                        if (prop_name.equals(majority_relation_name)) {
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

            if (rows_with_entities_and_entity_ids.size() > 0) {
                interpret(
                        table,
                        annotations,
                        rows_with_entities_and_matched_scores,
                        rows_with_entities_and_entity_ids,
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

    private List<ObjObj<String, Double>> vote_columnBinaryRelations(Map<Integer, List<CellBinaryRelationAnnotation>> candidate_binary_relations,
                                                                    int tableRowsTotal) {

        Map<String, Double> binary_relation_base_score = new HashMap<String, Double>();
        Map<String, Integer> binary_relation_frequency = new HashMap<String, Integer>();
        for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> row_entry :
                candidate_binary_relations.entrySet()) {
            Collections.sort(row_entry.getValue());
            CellBinaryRelationAnnotation best = row_entry.getValue().get(0);
            Double score = binary_relation_base_score.get(best.getAnnotation_url());
            score = score == null ? 0 : score;
            score = score + best.getScore();
            binary_relation_base_score.put(best.getAnnotation_url(), score);

            Integer freq = binary_relation_frequency.get(best.getAnnotation_url());
            freq = freq == null ? 0 : freq;
            freq++;
            binary_relation_frequency.put(best.getAnnotation_url(), freq);
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

    private void interpret(LTable table,
                           LTableAnnotation table_annotation,
                           Map<Integer, Double> rows_with_entities_mapped_scores,
                           Map<Integer, List<ObjObj<String, String>>> rows_with_entity_ids,
                           int column) throws IOException {

        //Map<String, HeaderAnnotation> candidate_header_annotations = new HashMap<String, HeaderAnnotation>();
        //count types that are known for already mapped entities (using their ids)

        //1 initialise candidate entities on every row, create the "skip rows" list used by ColumnLearner_LEARN.learn method
        for (Map.Entry<Integer, Double> e : rows_with_entities_mapped_scores.entrySet()) {
            int row = e.getKey();
            double mapped_score = e.getValue();
            List<ObjObj<String, String>> entities = rows_with_entity_ids.get(row);

            for (ObjObj<String, String> entity : entities) {
                List<String[]> candidate_types = find_typesOfEntity(entity.getMainObject());

                //update cell annotation
                EntityCandidate ec = new EntityCandidate(entity.getMainObject(), entity.getOtherObject());
                ec.getTypes().addAll(candidate_types);
                Map<String, Double> score_elements = new HashMap<String, Double>();
                score_elements.put("final", mapped_score);
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

                //update header annotation
                */
/*for (String[] ct : candidate_types) {
                    HeaderAnnotation hAnn = candidate_header_annotations.get(ct);
                    if (hAnn == null)
                        hAnn = new HeaderAnnotation(table.getColumnHeader(column).getHeaderText(), ct[0], ct[1], 0.0);
                    hAnn.addSupportingRow(row);
                    hAnn.setScore(hAnn.getScore() + mapped_score);
                    candidate_header_annotations.put(ct[0], hAnn);
                }*//*

            }
        }

        //2. run classification LEARN process; create initial typing annotation and disamb
        ObjObj<Integer, int[]> progress = column_learner.
                learn(table, table_annotation, column, rows_with_entity_ids.keySet().toArray(new Integer[0]));
        Set<EntityCandidate> reference_entities = new HashSet<EntityCandidate>();
        if (use_reference_entity) {
            for (int i = 0; i < progress.getMainObject(); i++) {
                int row = progress.getOtherObject()[i];
                CellAnnotation[] annotations = table_annotation.getContentCellAnnotations(row, column);
                if (annotations != null && annotations.length > 0)
                    reference_entities.add(annotations[0].getAnnotation());
            }
        }
        //3. run update process
        column_updater.update(progress.getMainObject(),
                progress.getOtherObject(),
                table,
                table_annotation,
                column,
                reference_entities,
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
}
*/
