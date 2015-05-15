package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.inference.Inferencer;
import cc.mallet.grmm.inference.LoopyBP;
import cc.mallet.grmm.types.FactorGraph;
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
 * Created by zqz on 15/05/2015.
 */
public class TI_JointInferenceFailSafe extends TI_JointInference {

    private FactorGraphBuilderMultiple subGraphBuilder=new FactorGraphBuilderMultiple(true);

    public TI_JointInferenceFailSafe(MainColumnFinder main_col_finder,
                                     CandidateEntityGenerator neGenerator,
                                     CandidateConceptGenerator columnClassifier,
                                     CandidateRelationGenerator relationGenerator,
                                     boolean useSubjectColumn,
                                     int[] ignoreColumns,
                                     int[] forceInterpretColumn,
                                     int maxIteration) {
        super(main_col_finder, neGenerator, columnClassifier, relationGenerator, useSubjectColumn, ignoreColumns, forceInterpretColumn, maxIteration);
    }

    public LTableAnnotation start(LTable table, boolean relationLearning) throws IOException, APIKeysDepletedException, STIException {
        LTableAnnotation_JI_Freebase tab_annotations = new LTableAnnotation_JI_Freebase(table.getNumRows(), table.getNumCols());

        //Main col finder finds main column. Although this is not needed by SMP, it also generates important features of
        //table data types to be used later
        List<ObjObj<Integer, ObjObj<Double, Boolean>>> candidate_main_NE_columns = main_col_finder.compute(table, ignoreColumns);
        if (useSubjectColumn)
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
        computeClassCandidates(tab_annotations, table);
        if (relationLearning) {
            System.out.println(">\t RELATION GENERATOR");
            computeRelationCandidates(tab_annotations, table, useSubjectColumn);
        }

        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        System.out.println(">\t BUILDING FACTOR GRAPH");
        List<FactorGraph> subGraphs = subGraphBuilder.buildDisconnectedGraphs(tab_annotations, relationLearning, table.getSourceId());

        //================debug
        for(int i=0; i<subGraphs.size(); i++) {
            FactorGraph graph = subGraphs.get(i);
            GraphCheckingUtil.checkGraph(graph, i+"th_graph,"+table.getSourceId());
            tab_annotations.checkAffinityUsage(i+"th_graph,"+table.getSourceId());
            System.out.println(">\t "+i+"th graph RUNNING INFERENCE");
            Inferencer infResidualBP;
            if (maxIteration > 0)
                infResidualBP = new LoopyBP(maxIteration);
            else
                infResidualBP = new LoopyBP();
            infResidualBP.computeMarginals(graph);
            System.out.println(">\t COLLECTING MARGINAL PROB AND FINALIZING ANNOTATIONS");
            boolean success=createFinalAnnotations(graph, subGraphBuilder, infResidualBP, tab_annotations);
            if(!success)
                throw new STIException("Invalid marginals, failed: "+i+"th_graph in "+" "+table.getSourceId());
        }
        //===============debug


        return tab_annotations;
    }
}
