package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.algorithm.smp.RelationTextMatch_Scorer;
import uk.ac.shef.dcs.oak.sti.algorithm.smp.TI_SemanticMessagePassing;
import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.util.*;

/**
 * Created by zqz on 01/05/2015.
 */
public class CandidateRelationGenerator {
    //todo: collect candidate relations; also score them for building factor graph
    private RelationTextMatch_Scorer matcher;

    public CandidateRelationGenerator(RelationTextMatch_Scorer matcher) {
        this.matcher = matcher;
    }

    public void generateCandidateRelation(LTableAnnotation_JI_Freebase tableAnnotations, LTable table, boolean useMainSubjectColumn, int[] ignoreColumns) {
        //RelationDataStructure result = new RelationDataStructure();

        //mainColumnIndexes contains indexes of columns that are possible NEs
        Map<Integer, DataTypeClassifier.DataType> colTypes
                = new HashMap<Integer, DataTypeClassifier.DataType>();
        for (int c = 0; c < table.getNumCols(); c++) {
            DataTypeClassifier.DataType type =
                    table.getColumnHeader(c).getTypes().get(0).getCandidateType();
            colTypes.put(c, type);
        }

        //aggregate candidate relations between any pairs of columns
        List<Integer> subjectColumnsToConsider = new ArrayList<Integer>();
        if(useMainSubjectColumn)
            subjectColumnsToConsider.add(tableAnnotations.getSubjectColumn());
        else {
            for(int c=0; c<table.getNumCols(); c++) {
                if(!TI_JointInference.ignoreColumn(c, ignoreColumns))
                    subjectColumnsToConsider.add(c);
            }
        }
        for (int subjectColumn :subjectColumnsToConsider) {  //choose a column to be subject column (must be NE column)
            if (!table.getColumnHeader(subjectColumn).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                continue;

            for (int objectColumn = 0; objectColumn < table.getNumCols(); objectColumn++) { //choose a column to be object column (any data type)
                if (subjectColumn == objectColumn)
                    continue;
                DataTypeClassifier.DataType columnDataType = table.getColumnHeader(subjectColumn).getFeature().getMostDataType().getCandidateType();
                if (columnDataType.equals(DataTypeClassifier.DataType.EMPTY) || columnDataType.equals(DataTypeClassifier.DataType.LONG_TEXT) ||
                        columnDataType.equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
                    continue;

                for (int r = 0; r < table.getNumRows(); r++) {
                    //in JI, all candidate NEs (the disambiguated NE) is needed from each cell to aggregate candidate relation
                    CellAnnotation[] subjectCells = tableAnnotations.getContentCellAnnotations(r, subjectColumn);
                    LTableContentCell subjectCellText = table.getContentCell(r, subjectColumn);
                    CellAnnotation[] objectCells = tableAnnotations.getContentCellAnnotations(r, objectColumn);
                    LTableContentCell objectCellText = table.getContentCell(r, objectColumn);

                    //matches obj of facts of subject entities against object cell text and candidate entity labels.
                    //also create evidence for entity-relation, concept-relation
                    matcher.match(r,
                            Arrays.asList(subjectCells),
                            subjectColumn,
                            Arrays.asList(objectCells),
                            objectColumn,
                            subjectCellText, objectCellText, colTypes.get(objectColumn),
                            tableAnnotations);
                }
            }
        }

        //aggregate overall scores for relations on each column pairs and populate relation annotation object
        aggregate(tableAnnotations, table);
    }

    private void aggregate(
            LTableAnnotation tableAnnotation, LTable table) {

        List<Key_SubjectCol_ObjectCol> processed = new ArrayList<Key_SubjectCol_ObjectCol>();
        for (Map.Entry<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>> e :
                tableAnnotation.getRelationAnnotations_per_row().entrySet()) {

            //simply create dummy header relation annotations
            Key_SubjectCol_ObjectCol current_relationKey = e.getKey(); //key indicating the directional relationship (subject col, object col)

            Set<String> candidateRelationURLs = new HashSet<String>();
            Map<Integer, List<CellBinaryRelationAnnotation>> relation_on_rows =e.getValue();
            for(Map.Entry<Integer, List<CellBinaryRelationAnnotation>> entry:
                    relation_on_rows.entrySet()){
                for(CellBinaryRelationAnnotation cbr: entry.getValue()){
                    candidateRelationURLs.add(cbr.getAnnotation_url());
                }
            }

            for(String url: candidateRelationURLs){
                tableAnnotation.addRelationAnnotation_across_column(
                        new HeaderBinaryRelationAnnotation(current_relationKey, url,
                                url, 0.0)
                );
            }
        }
    }

}
