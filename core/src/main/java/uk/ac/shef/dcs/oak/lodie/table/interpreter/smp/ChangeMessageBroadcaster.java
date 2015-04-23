package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.table.rep.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by zqz on 23/04/2015.
 */
public class ChangeMessageBroadcaster {

    public ObjectMatrix2D computeChangeMessages(LTableAnnotation tableAnnotation, LTable table){
        ObjectMatrix2D messages = new SparseObjectMatrix2D(table.getNumRows(), table.getNumCols());
        //messages by column header
        for(int col=0; col<table.getNumCols(); col++){
            List<HeaderAnnotation> headerAnnotations = tableAnnotation.getBestHeaderAnnotations(col);
            if(headerAnnotations.size()==0)
                continue;

            for(int row=0; row<table.getNumRows();row++){
                List<CellAnnotation> cellAnnotations = tableAnnotation.getBestContentCellAnnotations(row, col);
                if(cellAnnotations.size()==0)
                    continue;

                List<HeaderAnnotation> copy = new ArrayList<HeaderAnnotation>(headerAnnotations);
                copy.retainAll(cellAnnotations.get(0).getAnnotation().getTypeIds());
                if(copy.size()==0){
                    ChangeMessage m = new ChangeMessage();
                    m.setConfidence(headerAnnotations.get(0).getFinalScore());
                    m.setLabel(headerAnnotations.get(0).getAnnotation_label());
                    updateMessageForCell(messages, row, col, m);
                }
            }
        }

        //messages by relations
        Map<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>> relations = tableAnnotation.getRelationAnnotations_across_columns();
        for(Map.Entry<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>> e: relations.entrySet()){
            Key_SubjectCol_ObjectCol subobj_col_ids = e.getKey();
            List<HeaderBinaryRelationAnnotation> relationAnnotations = e.getValue();
            Collections.sort(relationAnnotations);
            double maxScore_of_relation_across_columns = relationAnnotations.get(0).getFinalScore();
            List<String> topRelations = new ArrayList<String>();
            for(HeaderBinaryRelationAnnotation hba: relationAnnotations){
                if(hba.getFinalScore()==maxScore_of_relation_across_columns)
                    topRelations.add(hba.getAnnotation_label());
            }

            Map<Integer, List<CellBinaryRelationAnnotation>>
                relationAnnotations_per_row=tableAnnotation.getRelationAnnotations_per_row().get(subobj_col_ids);

            for(Map.Entry<Integer, List<CellBinaryRelationAnnotation>> ent: relationAnnotations_per_row.entrySet()){
                int row = ent.getKey();
                List<CellBinaryRelationAnnotation> relations_on_row = ent.getValue();
                if(relations_on_row.size()!=0) {
                    Collections.sort(relations_on_row);
                    double maxScore = relations_on_row.get(0).getScore();
                    boolean hasMatch = false;
                    for(CellBinaryRelationAnnotation cra: relations_on_row){
                        if(cra.getScore()==maxScore && topRelations.contains(cra.getAnnotation_label()))
                            hasMatch=true;
                    }
                    if(!hasMatch){
                        ChangeMessageFromColumnsRelation forSubjectCell = new ChangeMessageFromColumnsRelation();
                        forSubjectCell.setLabel(relations_on_row.get(0).getAnnotation_label());
                        forSubjectCell.setConfidence(relations_on_row.get(0).getScore());
                        forSubjectCell.setFlag_subOrObj(0);
                        updateMessageForCell(messages,row, subobj_col_ids.getSubjectCol(), forSubjectCell);

                        ChangeMessageFromColumnsRelation forObjectCell = new ChangeMessageFromColumnsRelation();
                        forObjectCell.setLabel(relations_on_row.get(0).getAnnotation_label());
                        forObjectCell.setConfidence(relations_on_row.get(0).getScore());
                        forObjectCell.setFlag_subOrObj(1);
                        updateMessageForCell(messages,row, subobj_col_ids.getObjectCol(), forObjectCell);
                    }
                }
            }
        }
        return messages;
    }

    private void updateMessageForCell(ObjectMatrix2D messages, int row, int col, ChangeMessage m) {
        Object container = messages.get(row, col);
        List<ChangeMessage> messages_at_cell=null;
        if(container==null){
            messages_at_cell = new ArrayList<ChangeMessage>();
        }else {
            messages_at_cell = (List<ChangeMessage>) container;
        }

        messages_at_cell.add(m);
        messages.set(row, col, m);
    }


}
