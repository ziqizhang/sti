package uk.ac.shef.dcs.sti.algorithm.tm;

import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 */
public class TripleGenerator {

    private String namespace;
    private String dummyNamespace;

    public TripleGenerator(String namespace, String dummyNamespace) {
        this.namespace = namespace;
        this.dummyNamespace = dummyNamespace;
    }

    public List<LTableTriple> generate_newTriples(LTableAnnotation tab_annotation, LTable table) {
        List<LTableTriple> result = new ArrayList<LTableTriple>();

        //column typing instances
        for (int col = 0; col < table.getNumCols(); col++) {
            LTableColumnHeader header = table.getColumnHeader(col);

            List<HeaderAnnotation> bestHeaderAnnotations = tab_annotation.getBestHeaderAnnotations(col);
            if (bestHeaderAnnotations.size() == 0)
                continue;


            for (int row = 0; row < table.getNumRows(); row++) {
                for (HeaderAnnotation final_type_for_the_column : bestHeaderAnnotations) {

                    /*if (final_type_for_the_column.getSupportingRows().contains(row))
                        continue;*/
                    LTableContentCell tcc = table.getContentCell(row, col);
                    CellAnnotation[] cell_annotations = tab_annotation.getContentCellAnnotations(row, col);
                    if (cell_annotations == null || cell_annotations.length == 0) //no entity found for this cell
                        continue;

                    CellAnnotation final_cell_annotation = cell_annotations[0];
                    Entity entity = final_cell_annotation.getAnnotation();

                    //new triple
                    LTableTriple ltt = new LTableTriple();
                    ltt.setSubject_position(new int[]{row, col});
                    ltt.setSubject(tcc.getText());
                    ltt.setSubject_annotation(namespace + entity.getId());
                    ltt.setObject(header.getHeaderText());
                    ltt.setObject_annotation(namespace + final_type_for_the_column.getAnnotation_url());
                    ltt.setObject_position(new int[]{-1, -1});
                    ltt.setRelation_annotation("rdf:type");
                    result.add(ltt);
                }
            }
        }

        //across column relations at each row
        List<Integer> related_columns_with_subject = new ArrayList<Integer>();
        int main_subject_column = 0;
        Map<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>>
                relations_across_columns = tab_annotation.getRelationAnnotations_across_columns();
        for (Map.Entry<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>> entry :
                relations_across_columns.entrySet()) {
            Key_SubjectCol_ObjectCol the_two_columns = entry.getKey();
            int subCol = the_two_columns.getSubjectCol();
            int objCol = the_two_columns.getObjectCol();
            related_columns_with_subject.add(objCol);
            main_subject_column = subCol;

            Collections.sort(entry.getValue());
            HeaderBinaryRelationAnnotation relation_annotation = entry.getValue().get(0);

            for (int row = 0; row < table.getNumRows(); row++) {
                if (relation_annotation.getSupportingRows().contains(row))
                    continue;

                LTableContentCell subject_cell = table.getContentCell(row, subCol);
                LTableContentCell object_cell = table.getContentCell(row, objCol);
                CellAnnotation[] subject_cell_annotations = tab_annotation.getContentCellAnnotations(row, subCol);
                if (subject_cell_annotations == null || subject_cell_annotations.length == 0)
                    continue;
                CellAnnotation final_subject_cell_annotation = subject_cell_annotations[0];
                CellAnnotation[] object_cell_annotations = tab_annotation.getContentCellAnnotations(row, objCol);
                CellAnnotation final_object_cell_annotation = object_cell_annotations == null || object_cell_annotations.length == 0 ? null : object_cell_annotations[0];

                LTableTriple triple = new LTableTriple();
                triple.setSubject_position(new int[]{row, subCol});
                triple.setSubject(final_subject_cell_annotation.getTerm());
                triple.setSubject_annotation(namespace + final_subject_cell_annotation.getAnnotation().getId());

                triple.setObject_position(new int[]{row, objCol});
                if (final_object_cell_annotation != null) {
                    triple.setObject_annotation(namespace + final_object_cell_annotation.getAnnotation().getId());
                    triple.setObject(final_object_cell_annotation.getTerm());
                } else {
                    triple.setObject_annotation("'" + object_cell.getText() + "'");
                    triple.setObject(object_cell.getText());
                }
                triple.setRelation_annotation(namespace + relation_annotation.getAnnotation_url());
                result.add(triple);

            }
        }

        //remaining columns with subject column create dummy relations
        for (int col = 0; col < table.getNumCols(); col++) {
            if (col == main_subject_column || related_columns_with_subject.contains(col))
                continue;

            LTableColumnHeader header = table.getColumnHeader(col);
            if (header!=null&&header.getTypes() != null) {
                if (header.getTypes().get(0).getCandidateType().equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
                    continue;
            }
            else{
                continue;
            }

            for (int row = 0; row < table.getNumRows(); row++) {
                LTableContentCell subject_cell = table.getContentCell(row, main_subject_column);
                LTableContentCell object_cell = table.getContentCell(row, col);
                CellAnnotation[] subject_cell_annotations = tab_annotation.getContentCellAnnotations(row, main_subject_column);
                if (subject_cell_annotations == null || subject_cell_annotations.length == 0)
                    continue;
                CellAnnotation final_subject_cell_annotation = subject_cell_annotations[0];

                LTableTriple triple = new LTableTriple();
                triple.setSubject_position(new int[]{row, main_subject_column});
                triple.setSubject(final_subject_cell_annotation.getTerm());
                triple.setSubject_annotation(namespace + final_subject_cell_annotation.getAnnotation().getId());

                triple.setObject_position(new int[]{row, col});
                triple.setObject(object_cell.getText());
                triple.setObject_annotation("'" + object_cell.getText() + "'");
                triple.setRelation_annotation(dummyNamespace + "/" + header.getHeaderText());
                result.add(triple);
            }
        }

        return result;
    }
}
