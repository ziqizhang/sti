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
public class SMPInterpreter extends SemanticTableInterpreter{

    //main column finder is needed to generate data features of each column (e.g., data type in a column),
    //even though we do not use it to find the main column in SMP
    private static final Logger LOG = Logger.getLogger(SMPInterpreter.class.getName());
    private SubjectColumnDetector subjectColumnDetector;
    private TCellEntityRanker neRanker;
    private TColumnClassifier columnClassifier;
    private RelationLearner relationLearner;
    private boolean useSubjectColumn =false;

    private static final int halting_num_of_iterations_middlepoint = 5;
    private static final double min_pc_of_change_messages_for_column_concept_update = 0.0;
    private static final double min_pc_of_change_messages_for_relation_update = 0.0;
    private static final int halting_num_of_iterations_max = 10;

    public SMPInterpreter(SubjectColumnDetector subjectColumnDetector,
                          boolean useSubjectColumn,
                          TCellEntityRanker neRanker,
                          TColumnClassifier columnClassifier,
                          RelationLearner relationLearner,
                          int[] ignoreColumns,
                          int[] mustdoColumns
    ) {
        super(ignoreColumns, mustdoColumns);
        this.useSubjectColumn = useSubjectColumn;
        this.subjectColumnDetector = subjectColumnDetector;
        this.relationLearner = relationLearner;
        this.columnClassifier = columnClassifier;
        this.neRanker = neRanker;
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
            List<Pair<Integer, Pair<Double, Boolean>>> candidate_main_NE_columns =
                    subjectColumnDetector.compute(table, ignoreColumnsArray);
            if (useSubjectColumn)
                tableAnnotations.setSubjectColumn(candidate_main_NE_columns.get(0).getKey());

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
            columnClassification(tableAnnotations, table);
            if (relationLearning)
                relationEnumeration(tableAnnotations, table, useSubjectColumn, getIgnoreColumns());

            //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
            LOG.info(">\t SEMANTIC MESSAGE PASSING");
            TAnnotationSMPFreebase copy;
            CellAnnotationUpdater cellAnnotationUpdater = new CellAnnotationUpdater();
            for (int i = 1; i <= halting_num_of_iterations_max; i++) {
                System.out.println("\t\t>> [ITERATION] " + i);
                copy = new TAnnotationSMPFreebase(table.getNumRows(), table.getNumCols());
                TAnnotation.copy(tableAnnotations, copy);
                //column concept and relation factors send message to entity factors
                System.out.println("\t\t>> COMPUTING MESSAGES");
                ObjectMatrix2D messages = new ChangeMessageBroadcaster().computeChangeMessages(tableAnnotations, table);
                //check middle-point halting condition
                boolean stop = false;
                if (i == halting_num_of_iterations_middlepoint) {
                    stop = middlePointHaltingConditionReached(messages, tableAnnotations, table);
                }
                if (stop) {
                    System.out.println("\t\t>> Halting condition (middle point) reached after " + halting_num_of_iterations_middlepoint + " iterations.");
                    break;
                }

                //re-compute cell annotations based on messages
                System.out.println("\t\t>> NAMED ENTITY UPDATE");
                int[] updateResult = cellAnnotationUpdater.update(messages, tableAnnotations);
                System.out.println("\t\t   (requiredUpdate=" + updateResult[1] + ", updated=" + updateResult[0] + ")");

                //check stopping condition
                stop = haltingConditionReached(i, halting_num_of_iterations_max, copy, tableAnnotations);
                if (stop) {
                    System.out.println(">\t Halting condition reached, iteration=" + i);
                    break;
                } else {
                    //re-compute header and relation annotations
                    resetClassesAndRelations(tableAnnotations);
                    columnClassification(tableAnnotations, table);
                    if (relationLearning)
                        relationEnumeration(tableAnnotations, table, useSubjectColumn, getIgnoreColumns());
                }
            }
            return tableAnnotations;
        }catch (Exception e){
            throw new STIException(e);
        }
    }

    private void resetClassesAndRelations(TAnnotation tab_annotations) {
        tab_annotations.resetHeaderAnnotations();
        tab_annotations.resetRelationAnnotations();
    }

    private void columnClassification(TAnnotation tabAnnotations, Table table) throws KBSearchException {
        LOG.info("\t\t>> column classification...");
        // ObjectMatrix1D ccFactors = new SparseObjectMatrix1D(table.getNumCols());
        for (int col = 0; col < table.getNumCols(); col++) {
            if (isCompulsoryColumn(col)) {
                LOG.info("\t\t>> Column=(compulsory)" + col);
                columnClassifier.rankColumnConcepts(tabAnnotations, table, col);
            } else {
                if (getIgnoreColumns().contains(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                LOG.info("\t\t>> Column=" + col);
                columnClassifier.rankColumnConcepts(tabAnnotations, table, col);
            }
        }
    }

    private void relationEnumeration(TAnnotation tab_annotations, Table table, boolean useMainSubjectColumn, Collection<Integer> ignoreColumns){
        System.out.println("\t\t>> RELATION COMPUTING...");
        relationLearner.inferRelation(tab_annotations, table, useMainSubjectColumn, ignoreColumns);
    }

    private boolean middlePointHaltingConditionReached(ObjectMatrix2D messages, TAnnotation tab_annotations, Table table) {
        int count_change_messages_from_header = 0, count_change_messages_from_relation = 0;
        for (int r = 0; r < messages.rows(); r++) {
            for (int c = 0; c < messages.columns(); c++) {
                if (tab_annotations.getContentCellAnnotations(r, c).length == 0) //if this cell has not NE annotation there is not point to upate it
                    continue;
                Object container = messages.get(r, c);
                if (container == null)
                    continue;

                List<ChangeMessage> messages_for_cell = (List<ChangeMessage>) container;

                for (ChangeMessage m : messages_for_cell) {
                    if (m instanceof ChangeMessageFromColumnsRelation)
                        count_change_messages_from_relation++;
                    else
                        count_change_messages_from_header++;
                }
            }
        }

        int count_cells_with_ne_annotations = 0, count_cell_pairs_with_relation = 0;
        for (int r = 0; r < table.getNumRows(); r++) {
            for (int c = 0; c < table.getNumCols(); c++) {
                if (tab_annotations.getContentCellAnnotations(r, c).length != 0)
                    count_cells_with_ne_annotations++;
            }
        }

        for (Map<Integer, List<TCellCellRelationAnotation>> relations : tab_annotations.getCellcellRelations().values()) {
            count_cell_pairs_with_relation += relations.size();
        }

        if ((double) count_change_messages_from_header / count_cells_with_ne_annotations <
                min_pc_of_change_messages_for_column_concept_update)
            return true;
        if ((double) count_change_messages_from_relation / count_cell_pairs_with_relation <
                min_pc_of_change_messages_for_relation_update)
            return true;
        return false;
    }

    private boolean haltingConditionReached(int currentIteration, int maxIterations, TAnnotation previous, TAnnotation current) {
        if (currentIteration == maxIterations)
            return true;

        //check header annotations
        int header_converged_count = 0;
        boolean header_converged = false;
        for (int c=0; c<previous.getCols(); c++) {
            List<TColumnHeaderAnnotation> header_annotations_prev_iteration = previous.getWinningHeaderAnnotations(c);
            List<TColumnHeaderAnnotation> header_annotations_current_iteration = current.getWinningHeaderAnnotations(c);
            if(header_annotations_prev_iteration==null&&header_annotations_current_iteration==null) {
                header_converged_count++;
                continue;
            }
            if (header_annotations_current_iteration.size() == header_annotations_prev_iteration.size()) {
                header_annotations_current_iteration.retainAll(header_annotations_prev_iteration);
                if (header_annotations_current_iteration.size() == header_annotations_prev_iteration.size())
                    header_converged_count++;
                else
                    return false;
            } else
                return false;
        }
        if (header_converged_count == previous.getCols()) {
            header_converged = true;
        }

        //check cell annotations
        boolean cell_converged = true;
        for (int c=0; c<previous.getCols(); c++) {
            for (int row = 0; row < previous.getRows(); row++) {
                List<TCellAnnotation> cell_prev_annotations = previous.getWinningContentCellAnnotation(row, c);
                List<TCellAnnotation> cell_current_annotations = current.getWinningContentCellAnnotation(row, c);
                if (cell_current_annotations.size() == cell_prev_annotations.size()) {
                    cell_current_annotations.retainAll(cell_prev_annotations);
                    if (cell_current_annotations.size() != cell_prev_annotations.size())
                        return false;
                }
            }
        }

        //check relation annotations
        int relation_converged_count = 0;
        boolean relation_converged = false;
        Map<RelationColumns, List<TColumnColumnRelationAnnotation>> prev_relations=previous.getColumncolumnRelations();
        Map<RelationColumns, List<TColumnColumnRelationAnnotation>> current_relation = current.getColumncolumnRelations();
        Set<RelationColumns> tmp_keys = new HashSet<RelationColumns>(prev_relations.keySet());
        tmp_keys.retainAll(current_relation.keySet());
        if(tmp_keys.size()!=prev_relations.keySet().size()|| tmp_keys.size()!=current_relation.keySet().size())
            return false;
        for(RelationColumns subobj: tmp_keys){
            List<TColumnColumnRelationAnnotation> prev_candidates = previous.getWinningRelationAnnotationsBetween(subobj);
            List<TColumnColumnRelationAnnotation> current_candidates = current.getWinningRelationAnnotationsBetween(subobj);
            List<TColumnColumnRelationAnnotation> tmp = new ArrayList<TColumnColumnRelationAnnotation>(prev_candidates);
            tmp.retainAll(current_candidates);
            if(tmp.size()==prev_candidates.size()&& tmp.size()==current_candidates.size()){
                relation_converged_count++;
            }
        }
        if(relation_converged_count == tmp_keys.size())
            relation_converged=true;

        return header_converged && cell_converged && relation_converged;
    }


}
