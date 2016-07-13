package uk.ac.shef.dcs.sti.core.algorithm.smp;

import cern.colt.matrix.ObjectMatrix2D;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;

/**
 * Created by zqz on 20/04/2015.
 */
public class SMPInterpreter extends SemanticTableInterpreter {

    //main column finder is needed to generate data features of each column (e.g., data type in a column),
    //even though we do not use it to find the main column in SMP
    private static final Logger LOG = Logger.getLogger(SMPInterpreter.class.getName());
    private SubjectColumnDetector subjectColumnDetector;
    private TCellEntityRanker neRanker;
    private TColumnClassifier columnClassifier;
    private TColumnColumnRelationEnumerator relationLearner;
    private SemanticMessagePassing messagePassingCalculator;

    private static final int halting_num_of_iterations_middlepoint = 5;
    private static final double min_pc_of_change_messages_for_column_concept_update = 0.0;
    private static final double min_pc_of_change_messages_for_relation_update = 0.0;
    private static final int halting_num_of_iterations_max = 10;

    public SMPInterpreter(SubjectColumnDetector subjectColumnDetector,
                          TCellEntityRanker neRanker,
                          TColumnClassifier columnClassifier,
                          TColumnColumnRelationEnumerator relationLearner,
                          SemanticMessagePassing messagePassingCalculator,
                          int[] ignoreColumns,
                          int[] mustdoColumns
    ) {
        super(ignoreColumns, mustdoColumns);
        this.subjectColumnDetector = subjectColumnDetector;
        this.relationLearner = relationLearner;
        this.columnClassifier = columnClassifier;
        this.neRanker = neRanker;
        this.messagePassingCalculator = messagePassingCalculator;
    }

    public TAnnotation start(Table table, boolean relationLearning) throws STIException {
        TAnnotation tableAnnotations =
                new TAnnotationSMPFreebase(table.getNumRows(), table.getNumCols());

        //Main col finder finds main column. Although this is not needed by SMP, it also generates important features of
        //table data types to be used later
        int[] ignoreColumnsArray = new int[getIgnoreColumns().size()];

        int index = 0;
        for (Integer i : getIgnoreColumns()) {
            ignoreColumnsArray[index] = i;
            index++;
        }
        try {
            LOG.info(">\t COLUMN FEATURE GENERATION AND SUBJECT COLUMN DETECTION (if enabled)...");
            List<Pair<Integer, Pair<Double, Boolean>>> subjectColumnScores =
                    subjectColumnDetector.compute(table, ignoreColumnsArray);
            tableAnnotations.setSubjectColumn(subjectColumnScores.get(0).getKey());

            LOG.info(">\t NAMED ENTITY RANKER..."); //SMP begins with an initial NE ranker to rank candidate NEs for each cell
            for (int col = 0; col < table.getNumCols(); col++) {
            /*if(col!=1)
                continue;*/
                if (isCompulsoryColumn(col)) {
                    LOG.info("\t\t>> Column=(compulsory)" + col);
                    for (int r = 0; r < table.getNumRows(); r++) {
                        neRanker.rankCandidateNamedEntities(tableAnnotations, table, r, col);
                    }
                } else {
                    if (getIgnoreColumns().contains(col)) continue;
                    if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                        continue;
                /*if (table.getColumnHeader(col).getFeature().isAcronymColumn())
                    continue;*/
                    //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                    LOG.info("\t\t>> Column=" + col);
                    for (int r = 0; r < table.getNumRows(); r++) {
                        neRanker.rankCandidateNamedEntities(tableAnnotations, table, r, col);
                    }
                }
            }

            LOG.info(">\t COMPUTING Column CLASSIFICATION AND Column-column RELATION");
            columnClassification(columnClassifier, tableAnnotations, table, getMustdoColumns(), getIgnoreColumns());
            if (relationLearning) {
                LOG.info("\t> RELATION ENUMERATION");
                relationEnumeration(relationLearner, tableAnnotations, table, tableAnnotations.getSubjectColumn());
            }

            //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
            LOG.info(">\t SEMANTIC MESSAGE PASSING");
            if (relationLearning)
                messagePassingCalculator.start(table, tableAnnotations, columnClassifier,
                        relationLearner, getMustdoColumns(),getIgnoreColumns());
            else
                messagePassingCalculator.start(table, tableAnnotations, columnClassifier,
                        null, getMustdoColumns(),getIgnoreColumns());

            return tableAnnotations;
        } catch (Exception e) {
            throw new STIException(e);
        }
    }


    protected static void columnClassification(TColumnClassifier columnClassifier,
                                               TAnnotation tabAnnotations, Table table,
                                               Collection<Integer> mustDoColumns,
                                               Collection<Integer> ignoreColumns) throws KBSearchException {
        // ObjectMatrix1D ccFactors = new SparseObjectMatrix1D(table.getNumCols());
        for (int col = 0; col < table.getNumCols(); col++) {
            if (mustDoColumns.contains(col)) {
                LOG.info("\t\t>> Column=(compulsory)" + col);
                columnClassifier.classifyColumns(tabAnnotations, table, col);
            } else {
                if (ignoreColumns.contains(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                LOG.info("\t\t>> Column=" + col);
                columnClassifier.classifyColumns(tabAnnotations, table, col);
            }
        }
    }

    protected static void relationEnumeration(TColumnColumnRelationEnumerator relationLearner,
                                              TAnnotation tabAnnotations,
                                              Table table, int subjectColumnIndex) throws STIException {

        relationLearner.runRelationEnumeration(tabAnnotations, table, subjectColumnIndex);
    }

}
