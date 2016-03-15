package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.sti.rep.*;

import java.io.IOException;
import java.util.*;

/**
 * this simply chooses column type based on relations' expected types
 */
public class ColumnInterpreter_relDepend_exclude_entity_col extends ColumnInterpreter_relDepend {
    //private static final Logger log = Logger.getLogger(ColumnInterpreter_relDepend_v1.class.getName());
    private int[] ignoreColumns;

    public ColumnInterpreter_relDepend_exclude_entity_col(
            int... ignoreColumns) {
        this.ignoreColumns = ignoreColumns;

    }

    public void interpret(LTable table, LTableAnnotation annotations, Integer... ne_columns) throws IOException {
        //for each column that has a relation with the subject column, infer its type
        Map<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                relationAnnotations = annotations.getRelationAnnotations_per_row();

        for (Map.Entry<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                e : relationAnnotations.entrySet()) {
            Key_SubjectCol_ObjectCol subcol_objcol = e.getKey();
            if (ignoreColumn(subcol_objcol.getObjectCol())) continue;
            /*if (table.getColumnHeader(subcol_objcol.getObjectCol()).getFeature()
                    .getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY) &&
                    !table.getColumnHeader(subcol_objcol.getObjectCol()).getFeature().isCode_or_Acronym()) {
                if (annotations.getHeaderAnnotation(subcol_objcol.getObjectCol()) != null &&
                        annotations.getHeaderAnnotation(subcol_objcol.getObjectCol()).length > 0)
                    continue;

            }*/
            System.out.println("\t>> Relation column " + subcol_objcol.getObjectCol());
            boolean skip = false;

            for (int i : ne_columns) {
                boolean isColumn_acronym_or_code = table.getColumnHeader(i).getFeature().isCode_or_Acronym();
                if (i == subcol_objcol.getObjectCol() && !isColumn_acronym_or_code) {
                    if (annotations.getHeaderAnnotation(i) != null &&
                            annotations.getHeaderAnnotation(i).length > 0) {
                        skip = true;
                        break;
                    }
                }
            }
            if (skip) {
                System.out.println("\t>>> Skipping relation column " + subcol_objcol.getObjectCol());
                continue;
            }

            Map<Integer, List<CellBinaryRelationAnnotation>> rows_annotated_with_relation = e.getValue();
            //what is the main type of this column? if the main type happens to be entities...
            List<Pair<String, Double>> sorted_scores_for_relations = new ArrayList<>();
            /*if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 0)
                aggregated_scores_for_relations = score_columnBinaryRelations_best_contribute(rows_annotated_with_relation, table.getNumRows());
            else
                aggregated_scores_for_relations = score_columnBinaryRelations_all_contribute(rows_annotated_with_relation, table.getNumRows());*/

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

            //}
        }

    }
    //go through only HIGHEST RANKED CellBinaryRelationAnnotation each row, if an annotation is the same with "highest scoring annotation
    //on this column,

    //go through every CellBinaryRelationAnnotation each row, if an annotation is the same with "highest scoring annotation
    //on this column, it contributes to the classification of column

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
