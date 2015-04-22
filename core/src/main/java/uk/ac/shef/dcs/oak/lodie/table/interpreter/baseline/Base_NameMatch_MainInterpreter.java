package uk.ac.shef.dcs.oak.lodie.table.interpreter.baseline;

import uk.ac.shef.dcs.oak.lodie.table.interpreter.interpret.ColumnInterpreter_relDepend;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.maincol.ColumnFeature;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.maincol.MainColumnFinder;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.lodie.table.rep.*;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.dcs.oak.websearch.bing.v2.APIKeysDepletedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 25/02/14
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
 */
public class Base_NameMatch_MainInterpreter {
    private MainColumnFinder main_col_finder;
    private Base_NameMatch_ColumnLearner interpreter_column;
    private Baseline_BinaryRelationInterpreter interpreter_relation;
    //private static Logger log = Logger.getLogger(MainInterpreter.class.getName());
    private int[] ignoreColumns;
    private int[] forceInterpretColumn;
    private ColumnInterpreter_relDepend interpreter_column_with_knownReltaions;


    public Base_NameMatch_MainInterpreter(MainColumnFinder main_col_finder,
                                          Base_NameMatch_ColumnLearner interpreter_column,
                                          Baseline_BinaryRelationInterpreter interpreter_relation,
                                          ColumnInterpreter_relDepend interpreter_column_with_knownReltaions,
                                          int[] ignoreColumns, int[] forceInterpretColumn) {
        this.main_col_finder = main_col_finder;
        this.interpreter_column = interpreter_column;
        this.interpreter_relation = interpreter_relation;
        this.ignoreColumns = ignoreColumns;
        this.forceInterpretColumn = forceInterpretColumn;
        this.interpreter_column_with_knownReltaions=interpreter_column_with_knownReltaions;
    }

    public LTableAnnotation start(LTable table, boolean relationLearning) throws IOException, APIKeysDepletedException {
        //1. find the main subject column of this table
        System.out.println(">\t Detecting main column...");
        List<ObjObj<Integer, ObjObj<Double, Boolean>>> candidate_main_NE_columns = main_col_finder.compute(table, ignoreColumns);
        //ignore columns that are likely to be acronyms only, because they are highly ambiguous
       /* if (candidate_main_NE_columns.size() > 1) {
            Iterator<ObjObj<Integer, ObjObj<Double, Boolean>>> it = candidate_main_NE_columns.iterator();
            while (it.hasNext()) {
                ObjObj<Integer, ObjObj<Double, Boolean>> en = it.next();
                if (en.getOtherObject().getOtherObject() == true)
                    it.remove();
            }
        }*/
        LTableAnnotation tab_annotations = new LTableAnnotation(table.getNumRows(), table.getNumCols());
        List<Integer> interpreted_columns = new ArrayList<Integer>();
        for (int col = 0; col < table.getNumCols(); col++) {
            /*if(col!=1)
                continue;*/
            if (forceInterpret(col)) {
                System.out.println("\t>> Column=(forced)" + col);
                interpreter_column.interpret(table, tab_annotations, col);
                interpreted_columns.add(col);
            } else {
                if (ignoreColumn(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                /*if (table.getColumnHeader(col).getFeature().isCode_or_Acronym())
                    continue;*/
                interpreted_columns.add(col);

                //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                System.out.println("\t>> Column=" + col);
                interpreter_column.interpret(table, tab_annotations, col);
                //}
            }
        }

        if(relationLearning){
            double best_solution_score = 0;
            int main_subject_column = -1;
            LTableAnnotation best_annotations = null;
            for (ObjObj<Integer, ObjObj<Double, Boolean>> mainCol : candidate_main_NE_columns) {
                //tab_annotations = new LTableAnnotation(table.getNumRows(), table.getNumCols());
                main_subject_column = mainCol.getMainObject();
                if (ignoreColumn(main_subject_column)) continue;

                System.out.println(">\t Interpret relations with the main column, ="+main_subject_column);
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

            //4. consolidation-for columns that have relation with main subject column, if the column is
            // entity column, do column typing and disambiguation; otherwise, simply create header annotation
            System.out.println(">\t Classify columns (non-NE) in relation with main column");
            interpreter_column_with_knownReltaions.interpret(table, tab_annotations, interpreted_columns.toArray(new Integer[0]));

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
