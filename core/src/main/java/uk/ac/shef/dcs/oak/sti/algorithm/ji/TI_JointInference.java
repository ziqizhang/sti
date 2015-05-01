package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.STIException;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.maincol.MainColumnFinder;
import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.dcs.oak.websearch.bing.v2.APIKeysDepletedException;

import java.io.IOException;
import java.util.List;

/**
 * Created by zqz on 01/05/2015.
 */
public class TI_JointInference {

    //main column finder is needed to generate data features of each column (e.g., data type in a column),
    //even though we do not use it to find the main column in SMP
    private MainColumnFinder main_col_finder;
    //if there are any columns we want to ignore
    private int[] ignoreColumns;
    private int[] forceInterpretColumn;
    private boolean useSubjectColumn =false;
    private CandidateEntityGenerator neGenerator;
    private CandidateConceptGenerator columnClassifier;
    private CandidateRelationGenerator relationGenerator;
    public TI_JointInference(MainColumnFinder main_col_finder,
                             CandidateEntityGenerator neGenerator,
                             CandidateConceptGenerator columnClassifier,
                                     boolean useSubjectColumn,
                                     int[] ignoreColumns,
                                     int[] forceInterpretColumn
    ) {
        this.useSubjectColumn = useSubjectColumn;
        this.main_col_finder = main_col_finder;
        this.neGenerator=neGenerator;
        this.columnClassifier=columnClassifier;
        this.relationGenerator=relationGenerator;
        this.ignoreColumns = ignoreColumns;
        this.forceInterpretColumn = forceInterpretColumn;
    }

    public LTableAnnotation start(LTable table, boolean relationLearning) throws IOException, APIKeysDepletedException, STIException {
        LTableAnnotation_JI_Freebase tab_annotations = new LTableAnnotation_JI_Freebase(table.getNumRows(), table.getNumCols());

        //Main col finder finds main column. Although this is not needed by SMP, it also generates important features of
        //table data types to be used later
        List<ObjObj<Integer, ObjObj<Double, Boolean>>> candidate_main_NE_columns = main_col_finder.compute(table, ignoreColumns);
        if(useSubjectColumn)
            tab_annotations.setSubjectColumn(candidate_main_NE_columns.get(0).getMainObject());

        System.out.println(">\t INITIALIZATION");
        System.out.println(">\t\t NAMED ENTITY GENERATOR..."); //SMP begins with an initial NE ranker to rank candidate NEs for each cell
        for (int col = 0; col < table.getNumCols(); col++) {
            /*if(col!=1)
                continue;*/
            if (forceInterpret(col)) {
                System.out.println("\t\t>> Column=(forced)" + col);
                for (int r = 0; r < table.getNumRows(); r++) {
                    neGenerator.generateCandidateEntity(tab_annotations, table, r, col);
                }
            } else {
                if (ignoreColumn(col, ignoreColumns)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                /*if (table.getColumnHeader(col).getFeature().isCode_or_Acronym())
                    continue;*/
                //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                System.out.println("\t\t>> Column=" + col);
                for (int r = 0; r < table.getNumRows(); r++) {
                    neGenerator.generateCandidateEntity(tab_annotations, table, r, col);
                }
            }
        }

        System.out.println(">\t HEADER CLASSIFICATION GENERATOR");
        computeClasses(tab_annotations, table);
        if(relationLearning)
            computeRelations(tab_annotations, table, useSubjectColumn);

        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        System.out.println(">\t BUILDING FACTOR GRAPH");
        //TODO
        System.out.println(">\t RUNNING INFERENCE");
        //TODO
        System.out.println(">\t COLLECTING MARGINAL PROB AND FINALIZING ANNOTATIONS");
        //TODO
        return tab_annotations;
    }


    private void computeClasses(LTableAnnotation_JI_Freebase tab_annotations, LTable table) throws IOException {
        // ObjectMatrix1D ccFactors = new SparseObjectMatrix1D(table.getNumCols());
        for (int col = 0; col < table.getNumCols(); col++) {
            if (forceInterpret(col)) {
                System.out.println("\t\t>> Column=(forced)" + col);
                columnClassifier.generateCandidateConcepts(tab_annotations, table, col);
            } else {
                if (ignoreColumn(col, ignoreColumns)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                System.out.println("\t\t>> Column=" + col);
                columnClassifier.generateCandidateConcepts(tab_annotations, table, col);
            }
        }
    }

    private void computeRelations(LTableAnnotation_JI_Freebase tab_annotations, LTable table, boolean useMainSubjectColumn){
        relationGenerator.generateCandidateRelation(tab_annotations, table, useMainSubjectColumn, ignoreColumns);
    }

    protected static boolean ignoreColumn(Integer i, int[] ignoreColumns) {
        if (i != null) {
            for (int a : ignoreColumns) {
                if (a == i)
                    return true;
            }
        }
        return false;
    }

    private boolean forceInterpret(Integer i) {
        if (i != null) {
            for (int a : forceInterpretColumn) {
                if (a == i)
                    return true;
            }
        }
        return false;
    }
}
