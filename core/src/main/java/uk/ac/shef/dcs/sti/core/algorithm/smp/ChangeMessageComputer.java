package uk.ac.shef.dcs.sti.core.algorithm.smp;

import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
class ChangeMessageComputer {

    protected double minConfidence = 0.5;

    public ChangeMessageComputer(){}

    public ChangeMessageComputer(double minConfidence){
        this.minConfidence=minConfidence;
    }

    public ObjectMatrix2D computeChangeMessages(TAnnotation tableAnnotation, Table table) {
        ObjectMatrix2D messages = new SparseObjectMatrix2D(table.getNumRows(), table.getNumCols());
        //messages by column header
        for (int col = 0; col < table.getNumCols(); col++) {
            List<TColumnHeaderAnnotation> winningColumnClazz = tableAnnotation.getWinningHeaderAnnotations(col);
            if (winningColumnClazz.size() == 0)
                continue;

            for (int row = 0; row < table.getNumRows(); row++) {
                List<TCellAnnotation> cellAnnotations = tableAnnotation.getWinningContentCellAnnotation(row, col);
                if (cellAnnotations.size() == 0)
                    continue;

                List<String> headerAnnotationStrings = new ArrayList<>();
                for (TColumnHeaderAnnotation ha : winningColumnClazz)
                    headerAnnotationStrings.add(ha.getAnnotation().getId());
                boolean sendChange = false;
                for (TCellAnnotation best : cellAnnotations) { //this cell can have multiple annotations with the same highest computeElementScores
                    //we need to check everyone of them. if any one's type does not overlap with the header annotations, it need changing
                    List<String> copy = new ArrayList<>(headerAnnotationStrings);
                    copy.retainAll(best.getAnnotation().getTypeIds());
                    if (copy.size() == 0) {
                        sendChange = true;
                        break;
                    }
                }
                if (sendChange) {
                    for (TColumnHeaderAnnotation ha : winningColumnClazz) {
                        ChangeMessage m = new ChangeMessage();
                        m.setConfidence(ha.getFinalScore());
                        m.addLabel(ha.getAnnotation().getId());
                        updateMessageForCell(messages, row, col, m);
                    }
                }
            }
        }

        //messages by relations
        Map<RelationColumns, List<TColumnColumnRelationAnnotation>> relations = tableAnnotation.getColumncolumnRelations();
        //go thru every directional relations
        for (Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> e : relations.entrySet()) {
            RelationColumns subobj_col_ids = e.getKey(); //sub-obj key tells us the subject column id, and the obj column id
            List<TColumnColumnRelationAnnotation> relationAnnotations = e.getValue();
            Collections.sort(relationAnnotations);
            double maxScore_of_relation_across_columns = relationAnnotations.get(0).getFinalScore(); //what is the top relation's computeElementScores
            List<String> highestScoringRelationStrings = new ArrayList<>();
            for (TColumnColumnRelationAnnotation hba : relationAnnotations) {  //what are the top scoring relaions. these are the currently assigned relations for the two columns
                if (hba.getFinalScore() == maxScore_of_relation_across_columns)
                    highestScoringRelationStrings.add(hba.getRelationLabel());
            }
            //next go thru every row and check if the current top scored relations apply
            Map<Integer, List<TCellCellRelationAnotation>>
                    relationAnnotations_per_row = tableAnnotation.getCellcellRelations().get(subobj_col_ids);

            List<Integer> rows_annotated_with_relations = new ArrayList<>(relationAnnotations_per_row.keySet());
            for (int row = 0; row < tableAnnotation.getRows(); row++) {
                boolean hasMatch = false;
                //do we know if this row is annotated with relations?
                if (rows_annotated_with_relations.contains(row)) {
                    List<TCellCellRelationAnotation> relations_on_row = relationAnnotations_per_row.get(row);
                    //if so, take the highest scoring relation for the current row, if it is the same as the one assigned
                    //for the two columns, we are ok
                    if (relations_on_row.size() != 0) {
                        Collections.sort(relations_on_row);
                        double maxScore = relations_on_row.get(0).getWinningAttributeMatchScore();
                        for (TCellCellRelationAnotation cra : relations_on_row) {
                            if (cra.getWinningAttributeMatchScore() == maxScore && highestScoringRelationStrings.contains(cra.getRelationURI())) {
                                hasMatch = true;
                                break;
                            }
                        }
                    }
                }
                //if for this row no relation is present or no relation matches with the annotations for the two columns,
                //we prepare change messages
                if (!hasMatch) {
                    ChangeMessageFromRelation forSubjectCell = new ChangeMessageFromRelation();
                    forSubjectCell.setLabels(highestScoringRelationStrings);
                    forSubjectCell.setConfidence(maxScore_of_relation_across_columns);
                    forSubjectCell.setSubobjIndicator(0);
                    updateMessageForCell(messages, row, subobj_col_ids.getSubjectCol(), forSubjectCell);

                    ChangeMessageFromRelation forObjectCell = new ChangeMessageFromRelation();
                    forObjectCell.setLabels(highestScoringRelationStrings);
                    forObjectCell.setConfidence(maxScore_of_relation_across_columns);
                    forObjectCell.setSubobjIndicator(1);
                    updateMessageForCell(messages, row, subobj_col_ids.getObjectCol(), forObjectCell);
                }
            }
        }
        return messages;
    }

    @SuppressWarnings("unchecked")
    private void updateMessageForCell(ObjectMatrix2D messages, int row, int col, ChangeMessage m) {
        if (m.getConfidence() >= minConfidence) {
            Object container = messages.get(row, col);
            List<ChangeMessage> messages_at_cell;
            if (container == null) {
                messages_at_cell = new ArrayList<>();
            } else {
                messages_at_cell = (List<ChangeMessage>) container;
            }

            messages_at_cell.add(m);
            messages.set(row, col, messages_at_cell);
        }
    }


}
