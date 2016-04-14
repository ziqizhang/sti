package uk.ac.shef.dcs.sti.core.algorithm.ji;

import cc.mallet.grmm.inference.Inferencer;
import cc.mallet.grmm.inference.LoopyBP;
import cc.mallet.grmm.types.FactorGraph;
import javafx.util.Pair;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.ji.factorgraph.FactorGraphBuilderMultiple;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;

import java.util.List;
import java.util.Set;

/**
 * Created by zqz on 15/05/2015.
 */
public class JIInterpreterFailSafe extends JIInterpreter {

    private FactorGraphBuilderMultiple subGraphBuilder=new FactorGraphBuilderMultiple();

    public JIInterpreterFailSafe(SubjectColumnDetector main_col_finder,
                                 CandidateEntityGenerator neGenerator,
                                 CandidateConceptGenerator columnClassifier,
                                 CandidateRelationGenerator relationGenerator,
                                 boolean useSubjectColumn,
                                 int[] ignoreColumns,
                                 int[] forceInterpretColumn,
                                 int maxIteration,
                                 boolean debugMode) {
        super(main_col_finder, neGenerator, columnClassifier, relationGenerator, useSubjectColumn, ignoreColumns, forceInterpretColumn, maxIteration,
                debugMode);
    }

    public TAnnotation start(Table table, boolean relationLearning) throws STIException {

        try {
            int[] ignoreColumnsArray = new int[getIgnoreColumns().size()];

            int index = 0;
            for (Integer i : getIgnoreColumns()) {
                ignoreColumnsArray[index] = i;
                index++;
            }

            TAnnotationJI tab_annotations = new TAnnotationJI(table.getNumRows(), table.getNumCols());
            Set<Integer> ignoreColumnsLocal = collectIgnoreColumns(table);
            int[] ignoreColumnsLocalArray = new int[ignoreColumnsLocal.size()];
            for (int i = 0; i < ignoreColumnsLocal.size(); i++)
                ignoreColumnsLocalArray[i] = i;
            //Main col finder finds main column. Although this is not needed by SMP, it also generates important features of
            //table data types to be used later
            List<Pair<Integer, Pair<Double, Boolean>>> candidate_main_NE_columns = subjectColumnDetector.compute(table, ignoreColumnsLocalArray);
            if (useSubjectColumn)
                tab_annotations.setSubjectColumn(candidate_main_NE_columns.get(0).getKey());

            System.out.println(">\t INITIALIZATION");
            System.out.println(">\t\t NAMED ENTITY GENERATOR..."); //SMP begins with an initial NE ranker to rank candidate NEs for each cell
            for (int col = 0; col < table.getNumCols(); col++) {
            /*if(col!=1)
                continue;*/
                if (getMustdoColumns().contains(col)) {
                    System.out.println("\t\t>> Column=(forced)" + col);
                    for (int r = 0; r < table.getNumRows(); r++) {
                        neGenerator.generateInitialCellAnnotations(tab_annotations, table, r, col);
                    }
                } else {
                    if (ignoreColumnsLocal.contains(col)) continue;
                    if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                        continue;
                /*if (table.getColumnHeader(col).getFeature().isAcronymColumn())
                    continue;*/
                    //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                    System.out.println("\t\t>> Column=" + col);
                    for (int r = 0; r < table.getNumRows(); r++) {
                        neGenerator.generateInitialCellAnnotations(tab_annotations, table, r, col);
                    }
                }
            }

            System.out.println(">\t HEADER CLASSIFICATION GENERATOR");
            generateClazzCandidates(tab_annotations, table, ignoreColumnsLocal);
            if (relationLearning) {
                System.out.println(">\t RELATION GENERATOR");
                generateRelationCandidates(tab_annotations, table, useSubjectColumn, ignoreColumnsLocal);
            }

            //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

            //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
            System.out.println(">\t BUILDING FACTOR GRAPH");
            List<FactorGraph> subGraphs = subGraphBuilder.buildDisconnectedGraphs(tab_annotations, relationLearning, table.getSourceId());

            //================debug
            for (int i = 0; i < subGraphs.size(); i++) {
                FactorGraph graph = subGraphs.get(i);
                DebuggingUtil.debugGraph(graph, i + "th_graph," + table.getSourceId());
                tab_annotations.debugAffinity(i + "th_graph," + table.getSourceId());
                System.out.println(">\t " + i + "th graph RUNNING INFERENCE");
                Inferencer infResidualBP;
                if (maxIteration > 0)
                    infResidualBP = new LoopyBP(maxIteration);
                else
                    infResidualBP = new LoopyBP();
                infResidualBP.computeMarginals(graph);
                System.out.println(">\t COLLECTING MARGINAL PROB AND FINALIZING ANNOTATIONS");
                boolean success = createAnnotations(graph, subGraphBuilder, infResidualBP, tab_annotations);
                if (!success)
                    throw new STIException("Invalid marginals, failed: " + i + "th_graph in " + " " + table.getSourceId());
            }
            //===============debug


            return tab_annotations;
        }catch (Exception e){
            throw new STIException(e);
        }
    }
}
