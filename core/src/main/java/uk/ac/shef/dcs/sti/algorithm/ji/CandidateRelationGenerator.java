package uk.ac.shef.dcs.sti.algorithm.ji;

import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.rep.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by zqz on 01/05/2015.
 */
public class CandidateRelationGenerator {
    public static boolean allowRelationCandidatesFromRows=true;
    private RelationTextMatcher_Scorer_JI_adapted matcher;
    private KBSearch kbSearch;

    public CandidateRelationGenerator(RelationTextMatcher_Scorer_JI_adapted matcher,
                                      KBSearch kbSearch,
                                      boolean allowRelationCandidatesFromRows) {
        this.matcher = matcher;
        this.kbSearch = kbSearch;
        this.allowRelationCandidatesFromRows=allowRelationCandidatesFromRows;
    }

    public void generateCandidateRelation(LTableAnnotation_JI_Freebase tableAnnotations,
                                          Table table, boolean useMainSubjectColumn,
                                          Collection<Integer> ignoreColumns) throws IOException {
        //RelationDataStructure result = new RelationDataStructure();

        //mainColumnIndexes contains indexes of columns that are possible NEs
        Map<Integer, DataTypeClassifier.DataType> colTypes
                = new HashMap<Integer, DataTypeClassifier.DataType>();
        for (int c = 0; c < table.getNumCols(); c++) {
            DataTypeClassifier.DataType type =
                    table.getColumnHeader(c).getTypes().get(0).getCandidateType();
            colTypes.put(c, type);
        }

        //candidate relations between any pairs of columns
        List<Integer> subjectColumnsToConsider = new ArrayList<Integer>();
        if (useMainSubjectColumn)
            subjectColumnsToConsider.add(tableAnnotations.getSubjectColumn());
        else {
            for (int c = 0; c < table.getNumCols(); c++) {
                if (!TI_JointInference.ignoreColumn(c, ignoreColumns))
                    subjectColumnsToConsider.add(c);
            }
        }
        System.out.println("\t\t>>Relations derived from rows");
        for (int subjectColumn : subjectColumnsToConsider) {  //choose a column to be subject column (must be NE column)
            if (!table.getColumnHeader(subjectColumn).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                continue;

            for (int objectColumn = 0; objectColumn < table.getNumCols(); objectColumn++) { //choose a column to be object column (any data type)
                if (subjectColumn == objectColumn||TI_JointInference.ignoreColumn(objectColumn, ignoreColumns))
                    continue;
                DataTypeClassifier.DataType columnDataType = table.getColumnHeader(objectColumn).getFeature().getMostDataType().getCandidateType();
                if (!columnDataType.equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                System.out.print("(" + subjectColumn + "-" + objectColumn + ",");
                for (int r = 0; r < table.getNumRows(); r++) {
                    //in JI, all candidate NEs (the disambiguated NE) is needed from each cell to aggregate candidate relation
                    CellAnnotation[] subjectCells = tableAnnotations.getContentCellAnnotations(r, subjectColumn);
                    CellAnnotation[] objectCells = tableAnnotations.getContentCellAnnotations(r, objectColumn);
                    //matches obj of facts of subject entities against object cell text and candidate entity labels.
                    //also create evidence for entity-relation, concept-relation
                    matcher.match_cellPairs(r,
                            Arrays.asList(subjectCells),
                            subjectColumn,
                            Arrays.asList(objectCells),
                            objectColumn,
                            colTypes.get(objectColumn),
                            tableAnnotations);
                }
            }
        }
        System.out.println(")");

        //aggregate scores for relations on each column pairs and populate relation annotation object
        if(allowRelationCandidatesFromRows)
            aggregateRelationsAcrossColumns(tableAnnotations);

        //using pairs of column headers to compute candidate relations between columns, update tableannotation object
        System.out.println("\t\t>>Relations derived from headers");
        for (int subjectColumn : subjectColumnsToConsider) {  //choose a column to be subject column (must be NE column)
            if (!table.getColumnHeader(subjectColumn).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                continue;

            for (int objectColumn = 0; objectColumn < table.getNumCols(); objectColumn++) { //choose a column to be object column (any data type)
                if (subjectColumn == objectColumn||TI_JointInference.ignoreColumn(objectColumn, ignoreColumns)) continue;
                DataTypeClassifier.DataType columnDataType = table.getColumnHeader(objectColumn).getFeature().getMostDataType().getCandidateType();
                if (!columnDataType.equals(DataTypeClassifier.DataType.NAMED_ENTITY)) continue;
                System.out.print("(" + subjectColumn + "-" + objectColumn + ",");
                createRelationCandidateBetweenConceptCandidates(subjectColumn,
                        objectColumn,
                        tableAnnotations,
                        colTypes, kbSearch
                );
            }
        }
        System.out.println(")");
        System.out.println("\t\t>>Update entity-relation scores");
        //further, update entity-relation scores, to account for 1:n relations
        for (int subjectColumn : subjectColumnsToConsider) {  //choose a column to be subject column (must be NE column)
            if (!table.getColumnHeader(subjectColumn).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                continue;
            for (int objectColumn = 0; objectColumn < table.getNumCols(); objectColumn++) { //choose a column to be object column (any data type)
                if (subjectColumn == objectColumn||TI_JointInference.ignoreColumn(objectColumn, ignoreColumns)) continue;
                DataTypeClassifier.DataType columnDataType = table.getColumnHeader(objectColumn).getFeature().getMostDataType().getCandidateType();
                if (!columnDataType.equals(DataTypeClassifier.DataType.NAMED_ENTITY)) continue;
                System.out.print("(" + subjectColumn + "-" + objectColumn + ",");
                for (int r = 0; r < table.getNumRows(); r++) {
                    //in JI, all candidate NEs (the disambiguated NE) is needed from each cell to aggregate candidate relation
                    CellAnnotation[] subjectCells = tableAnnotations.getContentCellAnnotations(r, subjectColumn);
                    CellAnnotation[] objectCells = tableAnnotations.getContentCellAnnotations(r,objectColumn);
                    if(objectCells.length==0 || subjectCells.length==0) continue;
                    //matches obj of facts of subject entities against object cell text and candidate entity labels.
                    //also create evidence for entity-relation, concept-relation
                    matcher.match_sbjCellsAndRelation(
                            Arrays.asList(subjectCells),
                            subjectColumn,
                            objectColumn,
                            tableAnnotations);
                }
            }
        }
        System.out.println(")");
    }

    private void aggregateRelationsAcrossColumns(
            LTableAnnotation_JI_Freebase tableAnnotation
    ) throws IOException {
        for (Map.Entry<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>> e :
                tableAnnotation.getRelationAnnotations_per_row().entrySet()) {

            //simply create dummy header relation annotations
            Key_SubjectCol_ObjectCol current_relationKey = e.getKey(); //key indicating the directional relationship (subject col, object col)

            Set<String> candidateRelationURLs = new HashSet<String>();
            Map<Integer, List<CellBinaryRelationAnnotation>> relation_on_rows = e.getValue();
            for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> entry :
                    relation_on_rows.entrySet()) {
                for (CellBinaryRelationAnnotation cbr : entry.getValue()) {
                    candidateRelationURLs.add(cbr.getAnnotation_url());
                }
            }

            for (String url : candidateRelationURLs) {
                tableAnnotation.addRelationAnnotation_across_column(
                        new HeaderBinaryRelationAnnotation(current_relationKey, url,
                                url, 0.0)
                );
            }
        }
    }

    private void createRelationCandidateBetweenConceptCandidates(int sbjCol, int objCol,
                                                                 LTableAnnotation_JI_Freebase annotation,
                                                                 Map<Integer, DataTypeClassifier.DataType> colTypes,
                                                                 KBSearch kbSearch) throws IOException {
        HeaderAnnotation[] candidates_col1 = annotation.getHeaderAnnotation(sbjCol);
        HeaderAnnotation[] candidates_col2 = annotation.getHeaderAnnotation(objCol);
        matcher.match_headerPairs(
                Arrays.asList(candidates_col1),
                sbjCol,
                Arrays.asList(candidates_col2),
                objCol,
                colTypes.get(objCol),
                annotation,
                kbSearch);

    }
}
