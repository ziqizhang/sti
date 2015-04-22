package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix1D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.maincol.MainColumnFinder;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.lodie.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.dcs.oak.websearch.bing.v2.APIKeysDepletedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zqz on 20/04/2015.
 */
public class TI_SemanticMessagePassing {

    private MainColumnFinder main_col_finder;
    //private static Logger log = Logger.getLogger(MainInterpreter.class.getName());
    private int[] ignoreColumns;
    private int[] forceInterpretColumn;
    private NamedEntityRanker neRanker;
    private ColumnClassifier columnClassifier;

    public TI_SemanticMessagePassing(NamedEntityRanker neRanker,
                                     ColumnClassifier columnClassifier,
                                     int[] ignoreColumns,
                                     int[] forceInterpretColumn
    ) {
        this.columnClassifier=columnClassifier;
        this.neRanker = neRanker;
        this.ignoreColumns = ignoreColumns;
        this.forceInterpretColumn = forceInterpretColumn;
    }

    public TI_SemanticMessagePassing(MainColumnFinder main_col_finder,
                                     ColumnClassifier columnClassifier,
                                     NamedEntityRanker neRanker,
                                     int[] ignoreColumns,
                                     int[] forceInterpretColumn
    ) {
        this.main_col_finder = main_col_finder;
        this.columnClassifier=columnClassifier;
        this.neRanker = neRanker;
        this.ignoreColumns = ignoreColumns;
        this.forceInterpretColumn = forceInterpretColumn;
    }

    public LTableAnnotation start(LTable table, boolean relationLearning) throws IOException, APIKeysDepletedException {
        //1. find the main subject column of this table
        LTableAnnotation tab_annotations = new LTableAnnotation(table.getNumRows(), table.getNumCols());

        if(main_col_finder!=null) {
            System.out.println(">\t Detecting main column...");
            List<ObjObj<Integer, ObjObj<Double, Boolean>>> candidate_main_NE_columns = main_col_finder.compute(table, ignoreColumns);
            tab_annotations.setSubjectColumn(candidate_main_NE_columns.get(0).getMainObject());
        }
        //ignore columns that are likely to be acronyms only, because they are highly ambiguous
        /*if (candidate_main_NE_columns.size() > 1) {
            Iterator<ObjObj<Integer, ObjObj<Double, Boolean>>> it = candidate_main_NE_columns.iterator();
            while (it.hasNext()) {
                ObjObj<Integer, ObjObj<Double, Boolean>> en = it.next();
                if (en.getOtherObject().getOtherObject() == true)
                    it.remove();
            }
        }*/

        List<Integer> interpreted_columns = new ArrayList<Integer>();
        System.out.println(">\t INITIALIZATION");
        System.out.println(">\t\t NAMED ENTITY RANKER...");
        ObjectMatrix2D neFactors = new SparseObjectMatrix2D(table.getNumRows(), table.getNumCols());
        for (int col = 0; col < table.getNumCols(); col++) {
            /*if(col!=1)
                continue;*/
            if (forceInterpret(col)) {
                System.out.println("\t\t>> Column=(forced)" + col);
                interpreted_columns.add(col);
                for(int r=0; r<table.getNumRows(); r++){
                    List<ObjObj<EntityCandidate, Double>> candidates =neRanker.rankCandidateNamedEntities(table, r, col);
                    neFactors.set(r, col, candidates);
                }
            } else {
                if (ignoreColumn(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                /*if (table.getColumnHeader(col).getFeature().isCode_or_Acronym())
                    continue;*/
                interpreted_columns.add(col);
                //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                System.out.println("\t\t>> Column=" + col);
                for(int r=0; r<table.getNumRows(); r++){
                    List<ObjObj<EntityCandidate, Double>> candidates =neRanker.rankCandidateNamedEntities(table, r, col);
                    neFactors.set(r, col, candidates);
                }
            }
        }

        System.out.println(">\t\t COLUMN SEMANTIC TYPE COMPUTING...");
        ObjectMatrix1D ccFactors = new SparseObjectMatrix1D(table.getNumCols());
        for (int col = 0; col < table.getNumCols(); col++) {
            if (forceInterpret(col)) {
                System.out.println("\t\t>> Column=(forced)" + col);
                columnClassifier.rankColumnConcepts(col, neFactors);
            } else {
                if (ignoreColumn(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                System.out.println("\t\t>> Column=" + col);
                columnClassifier.rankColumnConcepts(col, neFactors);
            }
        }

        /*if (relationLearning) {
            double best_solution_score = 0;
            int main_subject_column = -1;
            LTableAnnotation best_annotations = null;
            for (ObjObj<Integer, ObjObj<Double, Boolean>> mainCol : candidate_main_NE_columns) {
                //tab_annotations = new LTableAnnotation(table.getNumRows(), table.getNumCols());
                main_subject_column = mainCol.getMainObject();
                if (ignoreColumn(main_subject_column)) continue;

                System.out.println(">\t Interpret relations with the main column, =" + main_subject_column);
                int columns_having_relations_with_main_col = interpreter_relation.interpret(tab_annotations, table, main_subject_column);
                boolean interpretable = false;
                if (columns_having_relations_with_main_col > 0) {
                    interpretable = true;
                }
                if (interpretable) {
                    tab_annotations.setSubjectColumn(main_subject_column);
                    break;
                } else {
                    //the current subject column could be wrong, try differently
                    double overall_score_of_current_solution = scoreSolution(tab_annotations, table, main_subject_column);
                    if (overall_score_of_current_solution > best_solution_score) {
                        tab_annotations.setSubjectColumn(main_subject_column);
                        best_annotations = tab_annotations;
                        best_solution_score = overall_score_of_current_solution;
                    }
                    tab_annotations.resetRelationAnnotations();
                    System.err.println(">>\t Main column does not satisfy number of relations check, continue to the next main column...");
                    continue;
                }
            }
            if (tab_annotations == null && best_annotations != null) {
                tab_annotations = best_annotations;
            }

            if (TableMinerConstants.REVISE_HBR_BY_DC && backward_updater != null) {
                List<String> domain_rep = backward_updater.construct_domain_represtation(table, tab_annotations, interpreted_columns);
                revise_header_binary_relations(tab_annotations, domain_rep);
            }

            //4. consolidation-for columns that have relation with main subject column, if the column is
            // entity column, do column typing and disambiguation; otherwise, simply create header annotation
            System.out.println(">\t Classify columns (non-NE) in relation with main column");
            interpreter_column_with_knownReltaions.interpret(table, tab_annotations, interpreted_columns.toArray(new Integer[0]));

        }*/


        return tab_annotations;
    }

    /*private boolean isInterpretable(int columns_having_relations_with_main_col, LTable table) {
        int totalColumns = 0;
        for (int col = 0; col < table.getNumCols(); col++) {
            DataTypeClassifier.DataType cType = table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType();
            if (cType.equals(DataTypeClassifier.DataType.ORDERED_NUMBER) ||
                    cType.equals(DataTypeClassifier.DataType.EMPTY) ||
                    cType.equals(DataTypeClassifier.DataType.LONG_TEXT))
                continue;
            totalColumns++;
        }

        return columns_having_relations_with_main_col >=
                totalColumns * interpreter_relation.getThreshold_minimum_binary_relations_in_table();
    }*/



    private boolean ignoreColumn(Integer i) {
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
