package uk.ac.shef.dcs.sti.core.algorithm.ji;

import cc.mallet.grmm.inference.Inferencer;
import cc.mallet.grmm.inference.LoopyBP;
import cc.mallet.grmm.types.AssignmentIterator;
import cc.mallet.grmm.types.Factor;
import cc.mallet.grmm.types.FactorGraph;
import cc.mallet.grmm.types.Variable;
import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.misc.TableAnnotationChecker;
import uk.ac.shef.dcs.sti.core.model.*;
import uk.ac.shef.dcs.websearch.bing.v2.APIKeysDepletedException;

import java.io.IOException;
import java.util.*;

/**
 * Created by zqz on 01/05/2015.
 */
public class TI_JointInference {

    //main column finder is needed to generate data features of each column (e.g., data type in a column),
    //even though we do not use it to find the main column in SMP
    protected SubjectColumnDetector main_col_finder;
    //if there are any columns we want to isValidAttribute
    protected int[] ignoreCols;
    protected int[] forceInterpretColumn;
    protected int maxIteration;
    protected List<String> stoplist = Arrays.asList("yes", "no", "away", "home");

    protected boolean useSubjectColumn = false;
    protected CandidateEntityGenerator neGenerator;
    protected CandidateConceptGenerator columnClassifier;
    protected CandidateRelationGenerator relationGenerator;
    private FactorGraphBuilder graphBuilder;

    public TI_JointInference(SubjectColumnDetector main_col_finder,
                             CandidateEntityGenerator neGenerator,
                             CandidateConceptGenerator columnClassifier,
                             CandidateRelationGenerator relationGenerator,
                             boolean useSubjectColumn,
                             int[] ignoreColumns,
                             int[] forceInterpretColumn,
                             int maxIteration
    ) {
        this.useSubjectColumn = useSubjectColumn;
        this.main_col_finder = main_col_finder;
        this.graphBuilder = new FactorGraphBuilder(false);
        this.neGenerator = neGenerator;
        this.columnClassifier = columnClassifier;
        this.relationGenerator = relationGenerator;
        this.ignoreCols = ignoreColumns;
        this.forceInterpretColumn = forceInterpretColumn;
        this.maxIteration = maxIteration;
    }

    public TAnnotation start(Table table, boolean relationLearning) throws IOException, KBSearchException,APIKeysDepletedException, STIException {
        TAnnotation_JI_Freebase tab_annotations = new TAnnotation_JI_Freebase(table.getNumRows(), table.getNumCols());
        List<Integer> ignoreColumnsLocal = new ArrayList<Integer>(updateIgnoreColumns(table, ignoreCols));
        int[] ignoreColumnsLocalArray = new int[ignoreColumnsLocal.size()];
        Collections.sort(ignoreColumnsLocal);
        for(int i=0; i<ignoreColumnsLocal.size(); i++)
            ignoreColumnsLocalArray[i]=ignoreColumnsLocal.get(i);
        //Main col finder finds main column. Although this is not needed by SMP, it also generates important features of
        //table data types to be used later
        List<Pair<Integer, Pair<Double, Boolean>>> candidate_main_NE_columns = main_col_finder.compute(table,
                ignoreColumnsLocalArray);
        if (useSubjectColumn)
            tab_annotations.setSubjectColumn(candidate_main_NE_columns.get(0).getKey());

        System.out.println(">\t INITIALIZATION");
        System.out.println(">\t\t NAMED ENTITY GENERATOR..."); //SMP begins with an initial NE ranker to rank candidate NEs for each cell
        boolean graphNonEmpty=false;
        for (int col = 0; col < table.getNumCols(); col++) {
            /*if(col!=1)
                continue;*/
            if (forceInterpret(col)) {
                System.out.println("\t\t>> Column=(forced)" + col);
                for (int r = 0; r < table.getNumRows(); r++) {
                    neGenerator.generateCandidateEntity(tab_annotations, table, r, col);
                }
                graphNonEmpty=true;
            } else {
                if (ignoreColumn(col, ignoreColumnsLocal)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                                /*if (table.getColumnHeader(col).getFeature().isAcronymColumn())
                    continue;*/
                //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                System.out.println("\t\t>> Column=" + col);
                for (int r = 0; r < table.getNumRows(); r++) {
                    neGenerator.generateCandidateEntity(tab_annotations, table, r, col);
                }
                graphNonEmpty=true;
            }
        }

        System.out.println(">\t HEADER CLASSIFICATION GENERATOR");
        computeClassCandidates(tab_annotations, table, ignoreColumnsLocal);
        if (relationLearning) {
            System.out.println(">\t RELATION GENERATOR");
            computeRelationCandidates(tab_annotations, table, useSubjectColumn, ignoreColumnsLocal);
        }

        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        System.out.println(">\t BUILDING FACTOR GRAPH");
        if(graphNonEmpty && TableAnnotationChecker.hasAnnotation(tab_annotations)) {
            FactorGraph graph = graphBuilder.build(tab_annotations, relationLearning, table.getSourceId());

            //================debug
            GraphCheckingUtil.checkGraph(graph, table.getSourceId());
            tab_annotations.checkAffinityUsage(table.getSourceId());
            //===============debug

            System.out.println(">\t RUNNING INFERENCE");
            Inferencer infResidualBP;
            if (maxIteration > 0)
                infResidualBP = new LoopyBP(maxIteration);
            else
                infResidualBP = new LoopyBP();

            try {
                infResidualBP.computeMarginals(graph);
            }catch(IndexOutOfBoundsException e){
                System.err.println(">>>>FUCK! graph empty exception, but checking did not catch this:"+table.getSourceId());
                System.err.println(">>>>");
                System.err.println(graph.dumpToString());
                System.err.println(">>>>");
                TableAnnotationChecker.checkAnnotation(tab_annotations);
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println(">\t COLLECTING MARGINAL PROB AND FINALIZING ANNOTATIONS");
            boolean success = createFinalAnnotations(graph, graphBuilder, infResidualBP, tab_annotations);
            if (!success)
                throw new STIException("Invalid marginals, failed: " + table.getSourceId());

        }
        else{
            System.err.println("EMPTY_TABLE:"+table.getSourceId());
        }
        return tab_annotations;
    }

    protected Set<Integer> updateIgnoreColumns(Table table, int[] ignoreColumns) {
        Set<Integer> ignore = new HashSet<Integer>();
        for (int c = 0; c < table.getNumCols(); c++) {
            Set<String> uniqueStrings = new HashSet<String>();
            for (int r = 0; r < table.getNumRows(); r++) {
                TCell tcc = table.getContentCell(r, c);
                String text = tcc.getText().trim().replaceAll("[^a-zA-Z0-9]", "");
                if(text.length()>1)
                    uniqueStrings.add(text);
            }
            if (uniqueStrings.size() < 4 && table.getNumRows() > 4) {
                uniqueStrings.removeAll(stoplist);
                if (uniqueStrings.size() == 0) ignore.add(c);
            }
        }
        for (int i : ignoreColumns)
            ignore.add(i);
        return ignore;
    }

    protected void computeClassCandidates(TAnnotation_JI_Freebase tab_annotations, Table table,
                                          Collection<Integer> ignoreColumnsLocal) throws KBSearchException {
        // ObjectMatrix1D ccFactors = new SparseObjectMatrix1D(table.getNumCols());
        for (int col = 0; col < table.getNumCols(); col++) {
            if (forceInterpret(col)) {
                System.out.println("\t\t>> Column=(forced)" + col);
                columnClassifier.generateCandidateConcepts(tab_annotations, table, col);
            } else {
                if (ignoreColumn(col,ignoreColumnsLocal )) continue;
                if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                System.out.println("\t\t>> Column=" + col);
                columnClassifier.generateCandidateConcepts(tab_annotations, table, col);
            }
        }
    }

    protected void computeRelationCandidates(TAnnotation_JI_Freebase tab_annotations, Table table,
                                             boolean useMainSubjectColumn,
                                             Collection<Integer> ignoreColumnsLocal) throws IOException,KBSearchException {
        relationGenerator.generateCandidateRelation(tab_annotations, table, useMainSubjectColumn, ignoreColumnsLocal);
    }

    protected boolean createFinalAnnotations(FactorGraph graph,
                                             FactorGraphBuilder graphBuilder,
                                             Inferencer infResidualBP,
                                             TAnnotation_JI_Freebase tab_annotations) {
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
                TCellAnnotation[] candidateCellAnnotations = tab_annotations.getContentCellAnnotations(position[0], position[1]);

                for (TCellAnnotation ca : candidateCellAnnotations) {
                    AssignmentIterator it = ptl.assignmentIterator();
                    boolean found = false;
                    while (it.hasNext()) {
                        int outcome = it.indexOfCurrentAssn();
                        String assignedId = var.getLabelAlphabet().lookupLabel(outcome).toString();
                        if (assignedId.equals(ca.getAnnotation().getId())) {
                            found = true;
                            double score = ptl.value(it);
                            if (Double.isNaN(score))
                                return false;
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
                TColumnHeaderAnnotation[] candidateHeaderAnnotations = tab_annotations.getHeaderAnnotation(position);
                for (TColumnHeaderAnnotation ha : candidateHeaderAnnotations) {
                    AssignmentIterator it = ptl.assignmentIterator();
                    boolean found = false;
                    while (it.hasNext()) {
                        int outcome = it.indexOfCurrentAssn();
                        String assignedId = var.getLabelAlphabet().lookupLabel(outcome).toString();
                        if (assignedId.equals(ha.getAnnotation().getId())) {
                            found = true;
                            double score = ptl.value(it);
                            if (Double.isNaN(score))
                                return false;
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
                RelationColumns direction = null;
                while (it.hasNext()) {
                    double score = ptl.value(it);
                    if (Double.isNaN(score))
                        return false;
                    int outcome = it.indexOfCurrentAssn();
                    String assignedId = var.getLabelAlphabet().lookupLabel(outcome).toString();
                    if (score >= maxScore) {
                        maxScore = score;
                        direction = graphBuilder.getRelationDirection(assignedId);
                    }
                    it.next();
                }

                List<TColumnColumnRelationAnnotation> relationCandidates =
                        tab_annotations.getColumncolumnRelations().get(direction);

                tab_annotations.getColumncolumnRelations().remove(new RelationColumns(
                        direction.getObjectCol(), direction.getSubjectCol()
                ));

                for (TColumnColumnRelationAnnotation hbr : relationCandidates) {
                    AssignmentIterator itr = ptl.assignmentIterator();
                    boolean found = false;
                    while (itr.hasNext()) {
                        int outcome = itr.indexOfCurrentAssn();
                        String assignedId = var.getLabelAlphabet().lookupLabel(outcome).toString();
                        if (assignedId.equals(hbr.getRelationURI())) {
                            found = true;
                            double score = ptl.value(itr);
                            if (Double.isNaN(score)) return false;
                            hbr.setFinalScore(score);
                            break;
                        }
                        itr.next();
                    }
                    if (!found) //this should not happen
                        hbr.setFinalScore(0.0);
                }
                tab_annotations.getColumncolumnRelations().put(direction,
                        relationCandidates);
                //go through again and collection only...

            } else {
                continue;
            }
        }
        return true;
    }

    protected static boolean ignoreColumn(Integer i, Collection<Integer> ignoreColumns) {
        /*if (i != null) {
            for (int a : ignoreColumns) {
                if (a == i)
                    return true;
            }
        }*/
        return ignoreColumns.contains(i);
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
