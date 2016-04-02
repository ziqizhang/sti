package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;
import uk.ac.shef.dcs.sti.rep.*;

import java.util.*;

/**
 * this simply chooses column type based on relations' expected types
 */
public class LiteralColumnTagger_exclude_entity_col extends LiteralColumnTagger {
    //private static final Logger LOG = Logger.getLogger(ColumnInterpreter_relDepend_v1.class.getName());
    private int[] ignoreColumns;

    public LiteralColumnTagger_exclude_entity_col(
            int... ignoreColumns) {
        this.ignoreColumns = ignoreColumns;

    }

    public void annotate(Table table, TAnnotation annotations, Integer... ne_columns) throws KBSearchException {
        //for each column that has a relation with the subject column, infer its type
        Map<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>>
                relationAnnotations = annotations.getCellcellRelations();

        for (Map.Entry<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>>
                e : relationAnnotations.entrySet()) {
            RelationColumns subcol_objcol = e.getKey();
            if (ignoreColumn(subcol_objcol.getObjectCol())) continue;
            /*if (table.getColumnHeader(subcol_objcol.getObjectCol()).getFeature()
                    .getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY) &&
                    !table.getColumnHeader(subcol_objcol.getObjectCol()).getFeature().isAcronymColumn()) {
                if (annotations.getHeaderAnnotation(subcol_objcol.getObjectCol()) != null &&
                        annotations.getHeaderAnnotation(subcol_objcol.getObjectCol()).length > 0)
                    continue;

            }*/
            System.out.println("\t>> Relation column " + subcol_objcol.getObjectCol());
            boolean skip = false;

            for (int i : ne_columns) {
                boolean isColumn_acronym_or_code = table.getColumnHeader(i).getFeature().isAcronymColumn();
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

            Map<Integer, List<TCellCellRelationAnotation>> rows_annotated_with_relation = e.getValue();
            //what is the main type of this column? if the main type happens to be entities...
            List<Pair<String, Double>> sorted_scores_for_relations = new ArrayList<>();
            /*if (TableMinerConstants.CLASSIFICATION_CANDIDATE_CONTRIBUTION_METHOD == 0)
                aggregated_scores_for_relations = score_columnBinaryRelations_best_contribute(rows_annotated_with_relation, table.getNumRows());
            else
                aggregated_scores_for_relations = score_columnBinaryRelations_all_contribute(rows_annotated_with_relation, table.getNumRows());*/

            //the related column is not entity column, simply create header annotation using the most frequent
            //relation label
            Set<TColumnHeaderAnnotation> candidates = new HashSet<TColumnHeaderAnnotation>();
            List<TColumnColumnRelationAnnotation> relations =
                    annotations.getColumncolumnRelations().
                            get(subcol_objcol);
            for (TColumnColumnRelationAnnotation hbr : relations) {
                TColumnHeaderAnnotation hAnn = new TColumnHeaderAnnotation(table.getColumnHeader(subcol_objcol.getObjectCol()).getHeaderText(),
                        new Clazz(hbr.getRelationURI(), hbr.getRelationLabel()),
                        hbr.getFinalScore());
                candidates.add(hAnn);
            }
            /* classification_scorer.computeCCScore(candidates, table, subcol_objcol.getObjectCol(), false);
                            for (TColumnHeaderAnnotation ha : candidates)
                                classification_scorer.computeFinal(ha, table.getNumRows());
                            List<TColumnHeaderAnnotation> sorted = new ArrayList<TColumnHeaderAnnotation>(candidates);
                            Collections.sort(sorted);
                            TColumnHeaderAnnotation[] hAnnotations = new TColumnHeaderAnnotation[aggregated_scores_for_relations.size()];
                            for (int i = 0; i < hAnnotations.length; i++) {
                                hAnnotations[i] = sorted.get(i);
                            }

            */
            List<TColumnHeaderAnnotation> sorted = new ArrayList<TColumnHeaderAnnotation>(candidates);
            Collections.sort(sorted);
            annotations.setHeaderAnnotation(subcol_objcol.getObjectCol(), sorted.toArray(new TColumnHeaderAnnotation[0]));

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
