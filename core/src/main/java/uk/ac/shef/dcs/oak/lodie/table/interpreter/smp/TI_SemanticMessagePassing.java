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
    private RelationLearner relationLearner;

    public int halting_num_of_iterations_middlepoint=5;
    public double mim_pc_of_change_messages_for_column_concept_update=0.5;
    public double min_pc_of_change_messages_for_relation_update=0.5;
    public int halting_num_of_iterations_max=10;

    public TI_SemanticMessagePassing(NamedEntityRanker neRanker,
                                     ColumnClassifier columnClassifier,
                                     RelationLearner relationLearner,
                                     int[] ignoreColumns,
                                     int[] forceInterpretColumn
    ) {
        this.relationLearner=relationLearner;
        this.columnClassifier=columnClassifier;
        this.neRanker = neRanker;
        this.ignoreColumns = ignoreColumns;
        this.forceInterpretColumn = forceInterpretColumn;
    }

    public TI_SemanticMessagePassing(MainColumnFinder main_col_finder,
                                     ColumnClassifier columnClassifier,
                                     RelationLearner relationLearner,
                                     NamedEntityRanker neRanker,
                                     int[] ignoreColumns,
                                     int[] forceInterpretColumn
    ) {
        this.main_col_finder = main_col_finder;
        this.relationLearner=relationLearner;
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

        System.out.println(">\t INITIALIZATION");
        System.out.println(">\t\t NAMED ENTITY RANKER...");
        ObjectMatrix2D neFactors = new SparseObjectMatrix2D(table.getNumRows(), table.getNumCols());
        for (int col = 0; col < table.getNumCols(); col++) {
            /*if(col!=1)
                continue;*/
            if (forceInterpret(col)) {
                System.out.println("\t\t>> Column=(forced)" + col);
                for(int r=0; r<table.getNumRows(); r++){
                    List<ObjObj<EntityCandidate, Double>> candidates =neRanker.rankCandidateNamedEntities(tab_annotations,table, r, col);
                    neFactors.set(r, col, candidates);
                }
            } else {
                if (ignoreColumn(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                /*if (table.getColumnHeader(col).getFeature().isCode_or_Acronym())
                    continue;*/
                //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                System.out.println("\t\t>> Column=" + col);
                for(int r=0; r<table.getNumRows(); r++){
                    List<ObjObj<EntityCandidate, Double>> candidates =neRanker.rankCandidateNamedEntities(tab_annotations,table, r, col);
                    neFactors.set(r, col, candidates);
                }
            }
        }

        System.out.println(">\t\t COLUMN SEMANTIC TYPE COMPUTING...");
        ObjectMatrix1D ccFactors = new SparseObjectMatrix1D(table.getNumCols());
        for (int col = 0; col < table.getNumCols(); col++) {
            if (forceInterpret(col)) {
                System.out.println("\t\t>> Column=(forced)" + col);
                columnClassifier.rankColumnConcepts(tab_annotations,table,col);
            } else {
                if (ignoreColumn(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                System.out.println("\t\t>> Column=" + col);
                columnClassifier.rankColumnConcepts(tab_annotations,table, col);
            }
        }

        if (relationLearning) {
            System.out.println(">\t\t RELATION COMPUTING...");
            relationLearner.inferRelation(tab_annotations, table);

        }


        System.out.println(">\t SEMANTIC MESSAGE PASSING");
        create_copy_of_table_annotation
        for(int i=0; i<halting_num_of_iterations_max; i++){
            System.out.println("\t\t>> ITERATION "+(i+1));
            //column concept and relation factors send message to entity factors
            ObjectMatrix2D messages =  new ChangeMessageBroadcaster().computeChangeMessages(tab_annotations, table);
            //check middle-point halting condition
            boolean stop=false;
            if(i==halting_num_of_iterations_middlepoint){
                stop = middlePointHaltReached(messages, tab_annotations);
            }
            if(stop){
                System.out.println("\t\t>> Halting condition (middle point) reached after "+halting_num_of_iterations_middlepoint+" iterations.");
                break;
            }

            //re-compute cell annotations based on messages


            //re-compute header and relation annotations


            //check stopping condition
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
