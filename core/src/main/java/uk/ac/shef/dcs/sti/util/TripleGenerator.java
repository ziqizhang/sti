package uk.ac.shef.dcs.sti.util;

import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 */
public class TripleGenerator {

    private String kbNamespace;
    private String defaultNamespace;

    public TripleGenerator(String kbNamespace, String dummyNamespace) {
        this.kbNamespace = kbNamespace;
        this.defaultNamespace = dummyNamespace;
    }

    public List<TableTriple> generate_newTriples(TAnnotation tab_annotation, Table table) {
        List<TableTriple> result = new ArrayList<>();

        //column typing instances
        for (int col = 0; col < table.getNumCols(); col++) {
            TColumnHeader header = table.getColumnHeader(col);

            List<TColumnHeaderAnnotation> bestHeaderAnnotations = tab_annotation.getWinningHeaderAnnotations(col);
            if (bestHeaderAnnotations.size() == 0)
                continue;


            for (int row = 0; row < table.getNumRows(); row++) {
                for (TColumnHeaderAnnotation final_type_for_the_column : bestHeaderAnnotations) {

                    /*if (final_type_for_the_column.getSupportingRows().contains(row))
                        continue;*/
                    TCell tcc = table.getContentCell(row, col);
                    TCellAnnotation[] cell_annotations = tab_annotation.getContentCellAnnotations(row, col);
                    if (cell_annotations == null || cell_annotations.length == 0) //no entity found for this cell
                        continue;

                    TCellAnnotation final_cell_annotation = cell_annotations[0];
                    Entity entity = final_cell_annotation.getAnnotation();

                    //new triple
                    TableTriple ltt = new TableTriple();
                    ltt.setSubject_position(new int[]{row, col});
                    ltt.setSubject(tcc.getText());
                    ltt.setSubject_annotation(kbNamespace + entity.getId());
                    ltt.setObject(header.getHeaderText());
                    ltt.setObject_annotation(kbNamespace + final_type_for_the_column.getAnnotation().getId());
                    ltt.setObject_position(new int[]{-1, -1});
                    ltt.setRelation_annotation("rdf:type");
                    result.add(ltt);
                }
            }
        }

        //across column relations at each row
        List<Integer> related_columns_with_subject = new ArrayList<Integer>();
        int main_subject_column = 0;
        Map<RelationColumns, List<TColumnColumnRelationAnnotation>>
                relations_across_columns = tab_annotation.getColumncolumnRelations();
        for (Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> entry :
                relations_across_columns.entrySet()) {
            RelationColumns the_two_columns = entry.getKey();
            int subCol = the_two_columns.getSubjectCol();
            int objCol = the_two_columns.getObjectCol();
            related_columns_with_subject.add(objCol);
            main_subject_column = subCol;

            Collections.sort(entry.getValue());
            TColumnColumnRelationAnnotation relation_annotation = entry.getValue().get(0);

            for (int row = 0; row < table.getNumRows(); row++) {
                if (relation_annotation.getSupportingRows().contains(row))
                    continue;

                TCell subject_cell = table.getContentCell(row, subCol);
                TCell object_cell = table.getContentCell(row, objCol);
                TCellAnnotation[] subject_cell_annotations = tab_annotation.getContentCellAnnotations(row, subCol);
                if (subject_cell_annotations == null || subject_cell_annotations.length == 0)
                    continue;
                TCellAnnotation final_subject_cell_annotation = subject_cell_annotations[0];
                TCellAnnotation[] object_cell_annotations = tab_annotation.getContentCellAnnotations(row, objCol);
                TCellAnnotation final_object_cell_annotation = object_cell_annotations == null || object_cell_annotations.length == 0 ? null : object_cell_annotations[0];

                TableTriple triple = new TableTriple();
                triple.setSubject_position(new int[]{row, subCol});
                triple.setSubject(final_subject_cell_annotation.getTerm());
                triple.setSubject_annotation(kbNamespace + final_subject_cell_annotation.getAnnotation().getId());

                triple.setObject_position(new int[]{row, objCol});
                if (final_object_cell_annotation != null) {
                    triple.setObject_annotation(kbNamespace + final_object_cell_annotation.getAnnotation().getId());
                    triple.setObject(final_object_cell_annotation.getTerm());
                } else {
                    triple.setObject_annotation("'" + object_cell.getText() + "'");
                    triple.setObject(object_cell.getText());
                }
                triple.setRelation_annotation(kbNamespace + relation_annotation.getRelationURI());
                result.add(triple);

            }
        }

        //remaining columns with subject column create dummy relations
        for (int col = 0; col < table.getNumCols(); col++) {
            if (col == main_subject_column || related_columns_with_subject.contains(col))
                continue;

            TColumnHeader header = table.getColumnHeader(col);
            if (header!=null&&header.getTypes() != null) {
                if (header.getTypes().get(0).getType().equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
                    continue;
            }
            else{
                continue;
            }

            for (int row = 0; row < table.getNumRows(); row++) {
                TCell subject_cell = table.getContentCell(row, main_subject_column);
                TCell object_cell = table.getContentCell(row, col);
                TCellAnnotation[] subject_cell_annotations = tab_annotation.getContentCellAnnotations(row, main_subject_column);
                if (subject_cell_annotations == null || subject_cell_annotations.length == 0)
                    continue;
                TCellAnnotation final_subject_cell_annotation = subject_cell_annotations[0];

                TableTriple triple = new TableTriple();
                triple.setSubject_position(new int[]{row, main_subject_column});
                triple.setSubject(final_subject_cell_annotation.getTerm());
                triple.setSubject_annotation(kbNamespace + final_subject_cell_annotation.getAnnotation().getId());

                triple.setObject_position(new int[]{row, col});
                triple.setObject(object_cell.getText());
                triple.setObject_annotation("'" + object_cell.getText() + "'");
                triple.setRelation_annotation(defaultNamespace + "/" + header.getHeaderText());
                result.add(triple);
            }
        }

        return result;
    }
}
