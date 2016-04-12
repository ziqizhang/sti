package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTagger;
import uk.ac.shef.dcs.sti.core.subjectcol.TColumnFeature;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 25/02/14
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
 */
public class BaselineNameMatchInterpreter extends SemanticTableInterpreter {
    private static final Logger LOG = Logger.getLogger(BaselineNameMatchInterpreter.class.getName());
    private SubjectColumnDetector subjectColumnDetector;
    private Base_NameMatch_ColumnLearner interpreter_column;
    private Baseline_BinaryRelationInterpreter interpreter_relation;
    private LiteralColumnTagger interpreter_column_with_knownReltaions;


    public BaselineNameMatchInterpreter(SubjectColumnDetector subjectColumnDetector,
                                        Base_NameMatch_ColumnLearner interpreter_column,
                                        Baseline_BinaryRelationInterpreter interpreter_relation,
                                        LiteralColumnTagger interpreter_column_with_knownReltaions,
                                        int[] ignoreColumns, int[] mustdoColumns) {
        super(ignoreColumns, mustdoColumns);
        this.subjectColumnDetector = subjectColumnDetector;
        this.interpreter_column = interpreter_column;
        this.interpreter_relation = interpreter_relation;
        this.interpreter_column_with_knownReltaions=interpreter_column_with_knownReltaions;
    }

    public TAnnotation start(Table table, boolean relationLearning) throws STIException {
        try {
            LOG.info(">\t Detecting subject column...");
            int[] ignoreColumnsArray = new int[getIgnoreColumns().size()];

            int index = 0;
            for (Integer i : getIgnoreColumns()) {
                ignoreColumnsArray[index] = i;
                index++;
            }
            //1. find the main subject column of this table
            List<Pair<Integer, Pair<Double, Boolean>>> subjectColumnScores =
                    subjectColumnDetector.compute(table, ignoreColumnsArray);

            TAnnotation tableAnnotations = new TAnnotation(table.getNumRows(), table.getNumCols());
            List<Integer> annotatedColumns = new ArrayList<>();
            LOG.info(">\t Cell Annotation and Column Classification...");
            for (int col = 0; col < table.getNumCols(); col++) {
                if (getMustdoColumns().contains(col)) {
                    LOG.info("\t>> Column=(compulsory)" + col);
                    interpreter_column.interpret(table, tableAnnotations, col);
                    annotatedColumns.add(col);
                } else {
                    if (getIgnoreColumns().contains(col)) continue;
                    if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                        continue;
                /*if (table.getColumnHeader(col).getFeature().isAcronymColumn())
                    continue;*/
                    annotatedColumns.add(col);

                    //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                    System.out.println("\t>> Column=" + col);
                    interpreter_column.interpret(table, tableAnnotations, col);
                    //}
                }
            }

            if (relationLearning) {
                double best_solution_score = 0;
                int main_subject_column = -1;
                TAnnotation best_annotations = null;
                for (Pair<Integer, Pair<Double, Boolean>> mainCol : subjectColumnScores) {
                    //tab_annotations = new TAnnotation(table.getNumRows(), table.getNumCols());
                    main_subject_column = mainCol.getKey();
                    if (getIgnoreColumns().contains(main_subject_column)) continue;

                    System.out.println(">\t Interpret relations with the main column, =" + main_subject_column);
                    int columns_having_relations_with_main_col = interpreter_relation.interpret(tableAnnotations, table, main_subject_column);
                    boolean interpretable = false;
                    if (columns_having_relations_with_main_col > 0) {
                        interpretable = true;
                    }
                    if (interpretable) {
                        tableAnnotations.setSubjectColumn(main_subject_column);
                        break;
                    } else {
                        //the current subject column could be wrong, try differently
                        double overall_score_of_current_solution = scoreSolution(tableAnnotations, table, main_subject_column);
                        if (overall_score_of_current_solution > best_solution_score) {
                            tableAnnotations.setSubjectColumn(main_subject_column);
                            best_annotations = tableAnnotations;
                            best_solution_score = overall_score_of_current_solution;
                        }
                        tableAnnotations.resetRelationAnnotations();
                        System.err.println(">>\t Main column does not satisfy number of relations check, continue to the next main column...");
                        continue;
                    }
                }
                if (tableAnnotations == null && best_annotations != null) {
                    tableAnnotations = best_annotations;
                }

                //4. consolidation-for columns that have relation with main subject column, if the column is
                // entity column, do column typing and disambiguation; otherwise, simply create header annotation
                System.out.println(">\t Classify columns (non-NE) in relation with main column");
                interpreter_column_with_knownReltaions.annotate(table, tableAnnotations, annotatedColumns.toArray(new Integer[0]));

            }

            return tableAnnotations;
        }catch (Exception e){
            throw new STIException(e);
        }
    }

    /*private boolean isInterpretable(int columns_having_relations_with_main_col, Table table) {
        int totalColumns = 0;
        for (int col = 0; col < table.getNumCols(); col++) {
            DataTypeClassifier.DataType cType = table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType();
            if (cType.equals(DataTypeClassifier.DataType.ORDERED_NUMBER) ||
                    cType.equals(DataTypeClassifier.DataType.EMPTY) ||
                    cType.equals(DataTypeClassifier.DataType.LONG_TEXT))
                continue;
            totalColumns++;
        }

        return columns_having_relations_with_main_col >=
                totalColumns * interpreter_relation.getThreshold_minimum_binary_relations_in_table();
    }*/

    private double scoreSolution(TAnnotation tab_annotations, Table table, int main_subject_column) {
        double entityScores = 0.0;
        for (int col = 0; col < table.getNumCols(); col++) {
            for (int row = 0; row < table.getNumRows(); row++) {
                TCellAnnotation[] cAnns = tab_annotations.getContentCellAnnotations(row, col);
                if (cAnns != null && cAnns.length > 0) {
                    entityScores += cAnns[0].getFinalScore();
                }
            }
        }

        double relationScores = 0.0;
        for (Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> entry : tab_annotations.getColumncolumnRelations().entrySet()) {
            RelationColumns key = entry.getKey();
            TColumnColumnRelationAnnotation rel = entry.getValue().get(0);
            relationScores += rel.getFinalScore();
        }
        TColumnFeature cf = table.getColumnHeader(main_subject_column).getFeature();
        //relationScores = relationScores * cf.getValueDiversity();

        double diversity = cf.getUniqueCellCount() + cf.getUniqueTokenCount();
        return (entityScores + relationScores) * diversity * ((table.getNumRows() - cf.getEmptyCellCount()) / (double) table.getNumRows());
    }
}
