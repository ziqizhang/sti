package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.algorithm.tm.subjectcol.TColumnFeature;
import uk.ac.shef.dcs.sti.algorithm.tm.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.sti.rep.*;
import uk.ac.shef.dcs.websearch.bing.v2.APIKeysDepletedException;

import java.io.IOException;
import java.util.*;

/**

 */
public class TMPInterpreter {

    private SubjectColumnDetector subjectColumnDetector;
    private LEARNING learning;
    private DataLiteralColumnClassifier interpreter_column_with_knownReltaions;
    private BinaryRelationInterpreter interpreter_relation;
    private HeaderBinaryRelationScorer hbr_scorer;
    //private static Logger LOG = Logger.getLogger(MainInterpreter.class.getName());
    private Set<Integer> ignoreCols;
    private int[] mustdoColumns;
    private UPDATE update;

    private static final Logger LOG = Logger.getLogger(TMPInterpreter.class.getName());

    public TMPInterpreter(SubjectColumnDetector subjectColumnDetector,
                          LEARNING learning,
                          UPDATE update,
                          BinaryRelationInterpreter interpreter_relation,
                          HeaderBinaryRelationScorer hbr_scorer,
                          DataLiteralColumnClassifier interpreter_column_with_knownReltaions,
                          int[] ignoreColumns,
                          int[] mustdoColumns
    ) {
        this.subjectColumnDetector = subjectColumnDetector;
        this.learning = learning;
        this.interpreter_column_with_knownReltaions = interpreter_column_with_knownReltaions;
        this.interpreter_relation = interpreter_relation;
        this.ignoreCols = new HashSet<>();
        for (int i : ignoreColumns)
            ignoreCols.add(i);
        this.mustdoColumns = mustdoColumns;
        this.update = update;
        this.hbr_scorer = hbr_scorer;
    }

    public TAnnotation start(Table table, boolean relationLearning) throws IOException, APIKeysDepletedException, KBSearchException, STIException {
        //1. find the main subject column of this table
        LOG.info(">\t Detecting subject column...");
        int[] ignoreColumnsArray = new int[ignoreCols.size()];

        int index = 0;
        for (Integer i : ignoreCols) {
            ignoreColumnsArray[index] = i;
            index++;
        }
        List<Pair<Integer, Pair<Double, Boolean>>> subjectColumnScores =
                subjectColumnDetector.compute(table, ignoreColumnsArray);
        //isValidAttribute columns that are likely to be acronyms only, because they are highly ambiguous
        /*if (candidate_main_NE_columns.size() > 1) {
            Iterator<ObjObj<Integer, ObjObj<Double, Boolean>>> it = candidate_main_NE_columns.iterator();
            while (it.hasNext()) {
                ObjObj<Integer, ObjObj<Double, Boolean>> en = it.next();
                if (en.getOtherObject().getOtherObject() == true)
                    it.remove();
            }
        }*/
        TAnnotation tableAnnotations = new TAnnotation(table.getNumRows(), table.getNumCols());
        tableAnnotations.setSubjectColumn(subjectColumnScores.get(0).getKey());

        List<Integer> annotatedColumns = new ArrayList<>();
        LOG.info(">\t LEARNING phrase ...");
        for (int col = 0; col < table.getNumCols(); col++) {
            /*if(col!=1)
                continue;*/
            if (isCompulsoryColumn(col)) {
                LOG.info("\t>> Column=(compulsory)" + col);
                annotatedColumns.add(col);
                learning.learn(table, tableAnnotations, col);
            } else {
                if (ignoreColumn(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                /*if (table.getColumnHeader(col).getFeature().isAcronymColumn())
                    continue;*/
                annotatedColumns.add(col);

                //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                LOG.info("\t>> Column=" + col);
                learning.learn(table, tableAnnotations, col);
                //}
            }
        }

        if (update != null) {
            LOG.info(">\t UPDATE phase ...");
            update.update(annotatedColumns, table, tableAnnotations);
        }

        if (relationLearning) {
            double best_solution_score = 0;
            int main_subject_column = -1;
            TAnnotation best_annotations = null;
            for (Pair<Integer, Pair<Double, Boolean>> mainCol : subjectColumnScores) {
                //tab_annotations = new TAnnotation(table.getNumRows(), table.getNumCols());
                main_subject_column = mainCol.getKey();
                if (ignoreColumn(main_subject_column)) continue;

                LOG.info(">\t Interpret relations with the main column, =" + main_subject_column);
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
                    LOG.error(">>\t Main column does not satisfy number of relations check, continue to the next main column...");
                    continue;
                }
            }
            if (tableAnnotations == null && best_annotations != null) {
                tableAnnotations = best_annotations;
            }

            if (TableMinerConstants.REVISE_HBR_BY_DC && update != null) {
                List<String> domain_rep = update.createDomainRep(table, tableAnnotations, annotatedColumns);
                revise_header_binary_relations(tableAnnotations, domain_rep);
            }

            //4. consolidation-for columns that have relation with main subject column, if the column is
            // entity column, do column typing and disambiguation; otherwise, simply create header annotation
            LOG.info(">\t Classify columns (non-NE) in relation with main column");
            interpreter_column_with_knownReltaions.interpret(table, tableAnnotations, annotatedColumns.toArray(new Integer[0]));

        }


        return tableAnnotations;
    }

    protected static void addCellAnnotation(
            Table table,
            TAnnotation tableAnnotation,
            List<Integer> rowBlock,
            int table_cell_col,
            List<Pair<Entity, Map<String, Double>>> entities_and_scoreMap) {

        Collections.sort(entities_and_scoreMap, (o1, o2) -> {
            Double o2_score = o2.getValue().get(TCellAnnotation.SCORE_FINAL);
            Double o1_score = o1.getValue().get(TCellAnnotation.SCORE_FINAL);
            return o2_score.compareTo(o1_score);
        });

        String cellText = table.getContentCell(rowBlock.get(0), table_cell_col).getText();
        for (int row : rowBlock) {
            TCellAnnotation[] annotationsForCell = new TCellAnnotation[entities_and_scoreMap.size()];
            for (int i = 0; i < entities_and_scoreMap.size(); i++) {
                Pair<Entity, Map<String, Double>> e = entities_and_scoreMap.get(i);
                double score = e.getValue().get(TCellAnnotation.SCORE_FINAL);
                annotationsForCell[i] = new TCellAnnotation(cellText,
                        e.getKey(), score, e.getValue());

            }
            tableAnnotation.setContentCellAnnotations(row, table_cell_col, annotationsForCell);
        }
    }

    /**
     * after disamb on the column, go thru the cells that have been newly disambiguated (i.e., in addition to cold start
     * disamb) update class annotation for the column due to these new cells
     *
     * @param rowsUpdated
     * @param column
     * @param tableAnnotations
     * @param table
     */
    public static void updateColumnClazz(List<Integer> rowsUpdated,
                                         int column,
                                         TAnnotation tableAnnotations,
                                         Table table,
                                         ClazzScorer clazzScorer) {
        List<TColumnHeaderAnnotation> existingColumnClazzAnnotations;
        existingColumnClazzAnnotations = tableAnnotations.getHeaderAnnotation(column) == null
                ? new ArrayList<>() : new ArrayList<>(Arrays.asList(tableAnnotations.getHeaderAnnotation(column)));

        //supporting rows are added if a header for the type of the cell annotation exists
        List<TColumnHeaderAnnotation> toAdd = new ArrayList<>();
        //deal with newly disambiguated cells (that is, in addition to cold start disamb)
        for (int row : rowsUpdated) {
            List<TCellAnnotation> winningEntities =
                    tableAnnotations.getWinningContentCellAnnotation(row, column);
            for (TCellAnnotation ca : winningEntities) {
                for (TColumnHeaderAnnotation ha : HeaderAnnotationUpdater.selectNew(ca, column, table, existingColumnClazzAnnotations)) {
                    if (!toAdd.contains(ha))
                        toAdd.add(ha);
                }
            }
        }

        toAdd.addAll(existingColumnClazzAnnotations);
        TColumnHeaderAnnotation[] result = HeaderAnnotationUpdater.updateColumnClazzAnnotationScores(
                rowsUpdated,
                column,
                table.getNumRows(),
                existingColumnClazzAnnotations,
                table,
                tableAnnotations,
                clazzScorer
        );
        tableAnnotations.setHeaderAnnotation(column, result);
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
        for (Map.Entry<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>> entry : tab_annotations.getRelationAnnotations_across_columns().entrySet()) {
            Key_SubjectCol_ObjectCol key = entry.getKey();
            HeaderBinaryRelationAnnotation rel = entry.getValue().get(0);
            relationScores += rel.getFinalScore();
        }
        TColumnFeature cf = table.getColumnHeader(main_subject_column).getFeature();
        //relationScores = relationScores * cf.getValueDiversity();

        double diversity = cf.getUniqueCellCount() + cf.getUniqueTokenCount();
        return (entityScores + relationScores) * diversity * ((table.getNumRows() - cf.getEmptyCellCount()) / (double) table.getNumRows());
    }

    private boolean ignoreColumn(Integer i) {
        if (i != null) {
            for (int a : ignoreCols) {
                if (a == i)
                    return true;
            }
        }
        return false;
    }

    private boolean isCompulsoryColumn(Integer i) {
        if (i != null) {
            for (int a : mustdoColumns) {
                if (a == i)
                    return true;
            }
        }
        return false;
    }

    private void revise_header_binary_relations(TAnnotation annotation, List<String> domain_representation
    ) {
        for (Map.Entry<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>>
                entry : annotation.getRelationAnnotations_across_columns().entrySet()) {

            for (HeaderBinaryRelationAnnotation hbr : entry.getValue()) {
                double domain_consensus = hbr_scorer.score_domain_consensus(hbr, domain_representation);

                hbr.setFinalScore(hbr.getFinalScore() + domain_consensus);
            }
            Collections.sort(entry.getValue());
        }

    }

}
