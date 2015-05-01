package uk.ac.shef.dcs.oak.sti.algorithm.tm;

import uk.ac.shef.dcs.oak.sti.algorithm.tm.maincol.ColumnFeature;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.maincol.MainColumnFinder;
import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.dcs.oak.websearch.bing.v2.APIKeysDepletedException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**

 */
@Deprecated
public class MainInterpreter_old {

    private MainColumnFinder main_col_finder;
    private ColumnInterpreter interpreter_column;
    private ColumnInterpreter_relDepend interpreter_column_with_knownReltaions;
    private BinaryRelationInterpreter interpreter_relation;
    //private static Logger log = Logger.getLogger(MainInterpreter.class.getName());
    private int[] ignoreColumns;
    private int[] forceInterpretColumn;


    public MainInterpreter_old(MainColumnFinder main_col_finder,
                           ColumnInterpreter interpreter_column,
                           ColumnInterpreter_relDepend interpreter_column_with_knownReltaions,
                           BinaryRelationInterpreter interpreter_relation,
                           int[] ignoreColumns, int[] forceInterpretColumn) {
        this.main_col_finder = main_col_finder;
        this.interpreter_column = interpreter_column;
        this.interpreter_column_with_knownReltaions = interpreter_column_with_knownReltaions;
        this.interpreter_relation = interpreter_relation;
        this.ignoreColumns = ignoreColumns;
        this.forceInterpretColumn = forceInterpretColumn;
    }

    public LTableAnnotation start(LTable table, boolean relationLearning) throws IOException, APIKeysDepletedException {
        //1. find the main subject column of this table
        System.out.println(">\t Detecting main column...");
        List<ObjObj<Integer, ObjObj<Double, Boolean>>> candidate_main_NE_columns = main_col_finder.compute(table, ignoreColumns);
        //ignore columns that are likely to be acronyms only, because they are highly ambiguous
        /*if (candidate_main_NE_columns.size() > 1) {
            Iterator<ObjObj<Integer, ObjObj<Double, Boolean>>> it = candidate_main_NE_columns.iterator();
            while (it.hasNext()) {
                ObjObj<Integer, ObjObj<Double, Boolean>> en = it.next();
                if (en.getOtherObject().getOtherObject() == true)
                    it.remove();
            }
        }*/

        LTableAnnotation tab_annotations = null;
        LTableAnnotation best_annotations = null;
        int main_subject_column = 0;
        if (relationLearning) {
            double best_solution_score = 0;

            main_subject_column = 0;
            for (ObjObj<Integer, ObjObj<Double, Boolean>> mainCol : candidate_main_NE_columns) {
                tab_annotations = new LTableAnnotation(table.getNumRows(), table.getNumCols());
                main_subject_column = mainCol.getMainObject();
                if (ignoreColumn(main_subject_column)) continue;

                //2. disambiguate that column and other columns
                System.out.println(">\t Classify and disambiguate main column " + main_subject_column);
                interpreter_column.interpret(table, tab_annotations, main_subject_column);

                //3. interpret relations
                System.out.println(">\t Interpret relations with the main column");
                int columns_having_relations_with_main_col = interpreter_relation.interpret(tab_annotations, table, main_subject_column);
                //does the main column has sufficient relations with other columns?   //if so, stop
                boolean interpretable = false;
                if (columns_having_relations_with_main_col > 0) {
                    interpretable = true;
                }
                //should return number of columns that have relation with the main subject column

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
                    tab_annotations = null;
                    System.err.println(">>\t Main column does not satisfy number of relations check, continue to the next main column...");
                    continue;
                }
            }
            if (tab_annotations == null && best_annotations != null) {
                tab_annotations = best_annotations;
                main_subject_column = best_annotations.getSubjectColumn();
            }

            //4. consolidation-for columns that have relation with main subject column, if the column is
            // entity column, do column typing and disambiguation; otherwise, simply create header annotation
            System.out.println(">\t Interpret columns in relation with main column");
            interpreter_column_with_knownReltaions.interpret(table, tab_annotations);
        }
        //5. for other NE columns that do not have relation with the main sub column, do column typing and disambiguation, infer possible relation between it
        //and subject column

        System.out.println(">\t Interpret other columns");
        if (!relationLearning) {
            tab_annotations = new LTableAnnotation(table.getNumRows(), table.getNumCols());
            main_subject_column = -1;
        }
        int count_interpreted=0;
        for (int col = 0; col < table.getNumCols(); col++) {
            /*if(col!=1)
                continue;*/
            if (forceInterpret(col)) {
                System.out.println("\t>> Column=(forced)" + col);
                interpreter_column.interpret(table, tab_annotations, col);
            } else {
                if (ignoreColumn(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                if (table.getColumnHeader(col).getFeature().isCode_or_Acronym())
                    continue;
                if (main_subject_column == col)
                    continue;

                if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                    System.out.println("\t>> Column=" + col);
                    interpreter_column.interpret(table, tab_annotations, col);
                }
            }
        }

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

    private double scoreSolution(LTableAnnotation tab_annotations, LTable table, int main_subject_column) {
        double entityScores = 0.0;
        for (int col = 0; col < table.getNumCols(); col++) {
            for (int row = 0; row < table.getNumRows(); row++) {
                CellAnnotation[] cAnns = tab_annotations.getContentCellAnnotations(row, col);
                if (cAnns != null && cAnns.length > 0) {
                    entityScores += cAnns[0].getFinalScore();
                }
            }
        }

        double relationScores = 0.0;
        for (Map.Entry<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>> entry : tab_annotations.getRelationAnnotations_across_columns().entrySet()) {
            Key_SubjectCol_ObjectCol key = entry.getKey();
            HeaderBinaryRelationAnnotation rel = entry.getValue().get(0);
            relationScores += rel.getFinalScore();
        }
        ColumnFeature cf = table.getColumnHeader(main_subject_column).getFeature();
        //relationScores = relationScores * cf.getValueDiversity();

        double diversity = cf.getCellValueDiversity() + cf.getTokenValueDiversity();
        return (entityScores + relationScores) * diversity * ((table.getNumRows() - cf.getEmptyCells()) / (double) table.getNumRows());
    }

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
