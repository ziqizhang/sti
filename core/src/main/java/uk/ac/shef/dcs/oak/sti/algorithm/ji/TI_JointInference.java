package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.inference.Inferencer;
import cc.mallet.grmm.inference.LoopyBP;
import cc.mallet.grmm.types.AssignmentIterator;
import cc.mallet.grmm.types.Factor;
import cc.mallet.grmm.types.FactorGraph;
import cc.mallet.grmm.types.Variable;
import uk.ac.shef.dcs.oak.sti.STIException;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.maincol.MainColumnFinder;
import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.dcs.oak.websearch.bing.v2.APIKeysDepletedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zqz on 01/05/2015.
 */
public class TI_JointInference {

    //main column finder is needed to generate data features of each column (e.g., data type in a column),
    //even though we do not use it to find the main column in SMP
    protected MainColumnFinder main_col_finder;
    //if there are any columns we want to ignore
    protected int[] ignoreColumns;
    protected int[] forceInterpretColumn;
    protected int maxIteration;

    protected boolean useSubjectColumn = false;
    protected CandidateEntityGenerator neGenerator;
    protected CandidateConceptGenerator columnClassifier;
    protected CandidateRelationGenerator relationGenerator;
    protected FactorGraphBuilder graphBuilder;

    public TI_JointInference(MainColumnFinder main_col_finder,
                             CandidateEntityGenerator neGenerator,
                             CandidateConceptGenerator columnClassifier,
                             CandidateRelationGenerator relationGenerator,
                             FactorGraphBuilder graphBuilder,
                             boolean useSubjectColumn,
                             int[] ignoreColumns,
                             int[] forceInterpretColumn,
                             int maxIteration
    ) {
        this.useSubjectColumn = useSubjectColumn;
        this.main_col_finder = main_col_finder;
        this.graphBuilder = graphBuilder;
        this.neGenerator = neGenerator;
        this.columnClassifier = columnClassifier;
        this.relationGenerator = relationGenerator;
        this.ignoreColumns = ignoreColumns;
        this.forceInterpretColumn = forceInterpretColumn;
        this.maxIteration = maxIteration;
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
        System.out.println(">\t BUILDING FACTOR GRAPH");
        FactorGraph graph = graphBuilder.build(tab_annotations,relationLearning, table.getTableId());

        //================debug
        GraphCheckingUtil.checkGraph(graph, table.getTableId());
        tab_annotations.checkAffinityUsage(table.getTableId());
        //===============debug

        System.out.println(">\t RUNNING INFERENCE");
        Inferencer infResidualBP;
        if (maxIteration > 0)
            infResidualBP = new LoopyBP(maxIteration);
        else
            infResidualBP = new LoopyBP();
        infResidualBP.computeMarginals(graph);
        System.out.println(">\t COLLECTING MARGINAL PROB AND FINALIZING ANNOTATIONS");
        boolean success=createFinalAnnotations(graph, graphBuilder, infResidualBP, tab_annotations);
        if(!success)
            throw new STIException("Invalid marginals, failed: "+table.getSourceId());


        return tab_annotations;
    }

    protected void computeClassCandidates(LTableAnnotation_JI_Freebase tab_annotations, LTable table) throws IOException {
        // ObjectMatrix1D ccFactors = new SparseObjectMatrix1D(table.getNumCols());
        for (int col = 0; col < table.getNumCols(); col++) {
            if (forceInterpret(col)) {
                System.out.println("\t\t>> Column=(forced)" + col);
                columnClassifier.generateCandidateConcepts(tab_annotations, table, col);
            } else {
                if (ignoreColumn(col, ignoreColumns)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostDataType().getCandidateType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                System.out.println("\t\t>> Column=" + col);
                columnClassifier.generateCandidateConcepts(tab_annotations, table, col);
            }
        }
    }

    protected void computeRelationCandidates(LTableAnnotation_JI_Freebase tab_annotations, LTable table, boolean useMainSubjectColumn) throws IOException {
        relationGenerator.generateCandidateRelation(tab_annotations, table, useMainSubjectColumn, ignoreColumns);
    }

    protected boolean createFinalAnnotations(FactorGraph graph,
                                        FactorGraphBuilder graphBuilder,
                                        Inferencer infResidualBP,
                                        LTableAnnotation_JI_Freebase tab_annotations) {
        for (int i = 0; i < graph.numVariables(); i++) {
            Variable var = graph.get(i);
            Factor ptl = infResidualBP.lookupMarginal(var);

            String varType = graphBuilder.getTypeOfVariable(var);
            if (varType == null)
                continue;

            if (varType.equals(VariableType.CELL.toString())) {
                int[] position = graphBuilder.getCellPosition(var);
                if (position == null)
                    continue;
                CellAnnotation[] candidateCellAnnotations = tab_annotations.getContentCellAnnotations(position[0], position[1]);

                for (CellAnnotation ca : candidateCellAnnotations) {
                    AssignmentIterator it = ptl.assignmentIterator();
                    boolean found = false;
                    while (it.hasNext()) {
                        int outcome = it.indexOfCurrentAssn();
                        String assignedId = var.getLabelAlphabet().lookupLabel(outcome).toString();
                        if (assignedId.equals(ca.getAnnotation().getId())) {
                            found = true;
                            double score = ptl.value(it);
                            ca.setFinalScore(score);
                            break;
                        }
                        it.next();
                    }
                    if (!found) //this should not happen
                        ca.setFinalScore(0.0);
                }
                Arrays.sort(candidateCellAnnotations);
                tab_annotations.setContentCellAnnotations(position[0], position[1], candidateCellAnnotations);
            } else if (varType.equals(VariableType.HEADER.toString())) {
                Integer position = graphBuilder.getHeaderPosition(var);
                if (position == null)
                    continue;
                HeaderAnnotation[] candidateHeaderAnnotations = tab_annotations.getHeaderAnnotation(position);
                for (HeaderAnnotation ha : candidateHeaderAnnotations) {
                    AssignmentIterator it = ptl.assignmentIterator();
                    boolean found = false;
                    while (it.hasNext()) {
                        int outcome = it.indexOfCurrentAssn();
                        String assignedId = var.getLabelAlphabet().lookupLabel(outcome).toString();
                        if (assignedId.equals(ha.getAnnotation_url())) {
                            found = true;
                            double score = ptl.value(it);
                            if(Double.isNaN(score)) return false;
                            ha.setFinalScore(score);
                            break;
                        }
                        it.next();
                    }
                    if (!found) //this should not happen
                        ha.setFinalScore(0.0);
                }
                Arrays.sort(candidateHeaderAnnotations);
                tab_annotations.setHeaderAnnotation(position, candidateHeaderAnnotations);
            } else if (varType.equals(VariableType.RELATION.toString())) {
                double maxScore = 0.0;
                AssignmentIterator it = ptl.assignmentIterator();
                Key_SubjectCol_ObjectCol direction = null;
                while (it.hasNext()) {
                    double score = ptl.value(it);
                    if(Double.isNaN(score)) return false;
                    int outcome = it.indexOfCurrentAssn();
                    String assignedId = var.getLabelAlphabet().lookupLabel(outcome).toString();
                    if (score >= maxScore) {
                        maxScore = score;
                        direction = graphBuilder.getRelationDirection(assignedId);
                    }
                    it.next();
                }

                List<HeaderBinaryRelationAnnotation> relationCandidates =
                        tab_annotations.getRelationAnnotations_across_columns().get(direction);

                tab_annotations.getRelationAnnotations_across_columns().remove(new Key_SubjectCol_ObjectCol(
                        direction.getObjectCol(), direction.getSubjectCol()
                ));

                for (HeaderBinaryRelationAnnotation hbr : relationCandidates) {
                    AssignmentIterator itr = ptl.assignmentIterator();
                    boolean found = false;
                    while (itr.hasNext()) {
                        int outcome = itr.indexOfCurrentAssn();
                        String assignedId = var.getLabelAlphabet().lookupLabel(outcome).toString();
                        if (assignedId.equals(hbr.getAnnotation_url())) {
                            found = true;
                            double score = ptl.value(itr);
                            if(Double.isNaN(score)) return false;
                            hbr.setFinalScore(score);
                            break;
                        }
                        itr.next();
                    }
                    if (!found) //this should not happen
                        hbr.setFinalScore(0.0);
                }
                tab_annotations.getRelationAnnotations_across_columns().put(direction,
                        relationCandidates);
                //go through again and collection only...

            } else {
                continue;
            }
        }
        return true;
    }

    protected static boolean ignoreColumn(Integer i, int[] ignoreColumns) {
        if (i != null) {
            for (int a : ignoreColumns) {
                if (a == i)
                    return true;
            }
        }
        return false;
    }

    protected boolean forceInterpret(Integer i) {
        if (i != null) {
            for (int a : forceInterpretColumn) {
                if (a == i)
                    return true;
            }
        }
        return false;
    }
}
