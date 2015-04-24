package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix1D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.maincol.MainColumnFinder;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.lodie.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.CellBinaryRelationAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.util.STIException;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.dcs.oak.websearch.bing.v2.APIKeysDepletedException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    private RelationLearner relationLearner;

    public int halting_num_of_iterations_middlepoint = 5;
    public double min_pc_of_change_messages_for_column_concept_update = 0.0;
    public double min_pc_of_change_messages_for_relation_update = 0.0;
    public int halting_num_of_iterations_max = 10;

    public TI_SemanticMessagePassing(NamedEntityRanker neRanker,
                                     ColumnClassifier columnClassifier,
                                     RelationLearner relationLearner,
                                     int[] ignoreColumns,
                                     int[] forceInterpretColumn
    ) {
        this.relationLearner = relationLearner;
        this.columnClassifier = columnClassifier;
        this.neRanker = neRanker;
        this.ignoreColumns = ignoreColumns;
        this.forceInterpretColumn = forceInterpretColumn;
    }

    public TI_SemanticMessagePassing(MainColumnFinder main_col_finder,
                                     NamedEntityRanker neRanker,
                                     ColumnClassifier columnClassifier,
                                     RelationLearner relationLearner,
                                     int[] ignoreColumns,
                                     int[] forceInterpretColumn
    ) {
        this.main_col_finder = main_col_finder;
        this.relationLearner = relationLearner;
        this.columnClassifier = columnClassifier;
        this.neRanker = neRanker;
        this.ignoreColumns = ignoreColumns;
        this.forceInterpretColumn = forceInterpretColumn;
    }

    public LTableAnnotation start(LTable table, boolean relationLearning) throws IOException, APIKeysDepletedException, STIException {
        LTableAnnotation_SMP_Freebase tab_annotations = new LTableAnnotation_SMP_Freebase(table.getNumRows(), table.getNumCols());

        //Main col finder finds main column. Although this is not needed by SMP, it also generates important features of
        //table data types to be used later
        List<ObjObj<Integer, ObjObj<Double, Boolean>>> candidate_main_NE_columns = main_col_finder.compute(table, ignoreColumns);
        tab_annotations.setSubjectColumn(candidate_main_NE_columns.get(0).getMainObject());

        System.out.println(">\t INITIALIZATION");
        System.out.println(">\t\t NAMED ENTITY RANKER...");
        for (int col = 0; col < table.getNumCols(); col++) {
            /*if(col!=1)
                continue;*/
            if (forceInterpret(col)) {
                System.out.println("\t\t>> Column=(forced)" + col);
                for (int r = 0; r < table.getNumRows(); r++) {
                    List<ObjObj<EntityCandidate, Double>> candidates = neRanker.rankCandidateNamedEntities(tab_annotations, table, r, col);
                }
            } else {
                if (ignoreColumn(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                /*if (table.getColumnHeader(col).getFeature().isCode_or_Acronym())
                    continue;*/
                //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                System.out.println("\t\t>> Column=" + col);
                for (int r = 0; r < table.getNumRows(); r++) {
                    List<ObjObj<EntityCandidate, Double>> candidates = neRanker.rankCandidateNamedEntities(tab_annotations, table, r, col);
                }
            }
        }

        System.out.println(">\t COMPUTING HEADER CLASSIFICATION AND COLUMN-COLUMN RELATION");
        computeClassesAndRelations(tab_annotations, table, 0);

        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        System.out.println(">\t SEMANTIC MESSAGE PASSING");
        LTableAnnotation_SMP_Freebase copy = new LTableAnnotation_SMP_Freebase(table.getNumRows(), table.getNumCols());
        CellAnnotationUpdater cellAnnotationUpdater = new CellAnnotationUpdater();
        for (int i = 1; i <= halting_num_of_iterations_max; i++) {
            LTableAnnotation.copy(tab_annotations, copy);
            System.out.println("\t\t>> ITERATION " + (i));
            //column concept and relation factors send message to entity factors
            ObjectMatrix2D messages = new ChangeMessageBroadcaster().computeChangeMessages(tab_annotations, table);
            //check middle-point halting condition
            boolean stop = false;
            if (i == halting_num_of_iterations_middlepoint) {
                stop = middlePointHaltingConditionReached(messages, tab_annotations, table);
            }
            if (stop) {
                System.out.println("\t\t>> Halting condition (middle point) reached after " + halting_num_of_iterations_middlepoint + " iterations.");
                break;
            }

            //re-compute cell annotations based on messages
            cellAnnotationUpdater.update(messages, tab_annotations);
            //re-compute header and relation annotations
            computeClassesAndRelations(tab_annotations, table, i + 1);

            //check stopping condition
            stop = haltingConditionReached(i, halting_num_of_iterations_max, copy, tab_annotations);
            if (stop) {
                System.out.println("\t\t>> Halting condition reached, iteration=" + i);
            }
        }
        return tab_annotations;
    }

    private void computeClassesAndRelations(LTableAnnotation_SMP_Freebase tab_annotations, LTable table, int iteration) throws IOException {
        System.out.println(">\t\t (itr=" + iteration + ") COLUMN SEMANTIC TYPE COMPUTING...");
       // ObjectMatrix1D ccFactors = new SparseObjectMatrix1D(table.getNumCols());
        for (int col = 0; col < table.getNumCols(); col++) {
            if (forceInterpret(col)) {
                System.out.println("\t\t>> Column=(forced)" + col);
                columnClassifier.rankColumnConcepts(tab_annotations, table, col);
            } else {
                if (ignoreColumn(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                System.out.println("\t\t>> Column=" + col);
                columnClassifier.rankColumnConcepts(tab_annotations, table, col);
            }
        }

        System.out.println(">\t\t (itr=" + iteration + ") RELATION COMPUTING...");
        relationLearner.inferRelation(tab_annotations, table);
    }

    private boolean middlePointHaltingConditionReached(ObjectMatrix2D messages, LTableAnnotation tab_annotations, LTable table) {
        int count_change_messages_from_header = 0, count_change_messages_from_relation = 0;
        for (int r = 0; r < messages.rows(); r++) {
            for (int c = 0; c < messages.columns(); c++) {
                if (tab_annotations.getContentCellAnnotations(r, c).length == 0) //if this cell has not NE annotation there is not point to upate it
                    continue;
                Object container = messages.get(r, c);
                if (container == null)
                    continue;

                List<ChangeMessage> messages_for_cell = (List<ChangeMessage>) container;
                CellAnnotationUpdater.checkMinConfidence(messages_for_cell);

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

        for (Map<Integer, List<CellBinaryRelationAnnotation>> relations : tab_annotations.getRelationAnnotations_per_row().values()) {
            count_cell_pairs_with_relation += relations.size();
        }

        if ((double) count_change_messages_from_header / count_cells_with_ne_annotations <
                min_pc_of_change_messages_for_column_concept_update)
            return true;
        if ((double) count_change_messages_from_header / count_cell_pairs_with_relation <
                min_pc_of_change_messages_for_relation_update)
            return true;
        return false;
    }

    private boolean haltingConditionReached(int currentIteration, int maxIterations, LTableAnnotation previous, LTableAnnotation current) {
        if (currentIteration == maxIterations)
            return true;

        return false;
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
