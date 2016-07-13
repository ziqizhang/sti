package uk.ac.shef.dcs.sti.core.algorithm.ji;

import cc.mallet.grmm.inference.Inferencer;
import cc.mallet.grmm.inference.LoopyBP;
import cc.mallet.grmm.types.AssignmentIterator;
import cc.mallet.grmm.types.Factor;
import cc.mallet.grmm.types.FactorGraph;
import cc.mallet.grmm.types.Variable;
import javafx.util.Pair;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.algorithm.ji.factorgraph.FactorGraphBuilder;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;

import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by zqz on 01/05/2015.
 */
public class JIInterpreter extends SemanticTableInterpreter {

    private static final Logger LOG = Logger.getLogger(JIInterpreter.class.getName());
    //main column finder is needed to generate data features of each column (e.g., data type in a column),
    //even though we do not use it to find the main column in SMP
    protected SubjectColumnDetector subjectColumnDetector;
    //if there are any columns we want to isValidAttribute
    protected int maxIteration;

    protected List<String> invalidCellValues = Arrays.asList("yes", "no", "away", "home");

    protected boolean useSubjectColumn = false;
    protected boolean debugMode = false;
    protected CandidateEntityGenerator neGenerator;
    protected CandidateConceptGenerator columnClazzClassifier;
    protected CandidateRelationGenerator relationGenerator;
    private FactorGraphBuilder graphBuilder;

    public JIInterpreter(SubjectColumnDetector subjectColumnDetector,
                         CandidateEntityGenerator neGenerator,
                         CandidateConceptGenerator columnClazzClassifier,
                         CandidateRelationGenerator relationGenerator,
                         boolean useSubjectColumn,
                         int[] ignoreColumns,
                         int[] mustdoColumns,
                         int maxIteration,
                         boolean debugMode
    ) {
        super(ignoreColumns, mustdoColumns);
        this.useSubjectColumn = useSubjectColumn;
        this.subjectColumnDetector = subjectColumnDetector;
        this.graphBuilder = new FactorGraphBuilder();
        this.neGenerator = neGenerator;
        this.columnClazzClassifier = columnClazzClassifier;
        this.relationGenerator = relationGenerator;
        this.maxIteration = maxIteration;
        this.debugMode = debugMode;
    }


    public TAnnotation start(Table table, boolean relationLearning) throws STIException {
        TAnnotationJI tableAnnotations = new TAnnotationJI(table.getNumRows(), table.getNumCols());

        //Main col finder finds main column. Although this is not needed by SMP, it also generates important features of
        //table data types to be used later
        try {
            Set<Integer> ignoreColumnsRevised = collectIgnoreColumns(table);
            int[] iignoreColumnsRevisedArray = new int[ignoreColumnsRevised.size()];
            int index = 0;
            for (Integer i : ignoreColumnsRevised) {
                iignoreColumnsRevisedArray[index] = i;
                index++;
            }

            LOG.info(">\t COLUMN FEATURE GENERATION AND SUBJECT COLUMN DETECTION (if enabled)...");
            List<Pair<Integer, Pair<Double, Boolean>>> subjectColumnScores = subjectColumnDetector.compute(table,
                    iignoreColumnsRevisedArray);
            tableAnnotations.setSubjectColumn(subjectColumnScores.get(0).getKey());

            LOG.info(">\t JOINT INFERENCE VARIABLE INIT");
            LOG.info(">\t named entity generator..."); //SMP begins with an initial NE ranker to rank candidate NEs for each cell
            boolean graphNonEmpty = generateEntityCandidates(table, tableAnnotations, ignoreColumnsRevised);

            LOG.info(">\t column class generator");
            generateClazzCandidates(tableAnnotations, table, ignoreColumnsRevised);
            if (relationLearning) {
                LOG.info(">\t column column relation generator");
                generateRelationCandidates(tableAnnotations, table, useSubjectColumn, ignoreColumnsRevised);
            }


            if (graphNonEmpty && hasAnnotation(tableAnnotations)) {
                LOG.info(">\t BUILDING FACTOR GRAPH");
                List<FactorGraph> subGraphs =
                        graphBuilder.build(tableAnnotations, relationLearning, table.getSourceId());
                LOG.info(">\t\t "+subGraphs.size()+" maximum connected sub-graphs");
                for (int i = 0; i < subGraphs.size(); i++) {
                    FactorGraph graph = subGraphs.get(i);
                    if (debugMode) {
                        DebuggingUtil.debugGraph(graph, i+"th_graph,"+table.getSourceId());
                        tableAnnotations.debugAffinity(i + "th_graph,"+table.getSourceId());
                    }

                    LOG.info(">\t RUNNING INFERENCE");
                    Inferencer infResidualBP;
                    if (maxIteration > 0)
                        infResidualBP = new LoopyBP(maxIteration);
                    else
                        infResidualBP = new LoopyBP();

                    try {
                        infResidualBP.computeMarginals(graph);
                    } catch (IndexOutOfBoundsException e) {
                        if (debugMode) {
                            LOG.error("\t Graph empty exception, but checking did not catch this. System exists:" + table.getSourceId());
                            LOG.error(graph.dumpToString());
                            Object[] debuggingResult = DebuggingUtil.debugAnnotations(tableAnnotations);
                            for (Object o : debuggingResult) {
                                LOG.info(o.toString());
                            }
                            LOG.warn(ExceptionUtils.getFullStackTrace(e));
                            System.exit(1);
                        } else
                            LOG.warn(ExceptionUtils.getFullStackTrace(e));
                    }
                    LOG.info(">\t COLLECTING MARGINAL PROB AND FINALIZING ANNOTATIONS");
                    boolean success = createAnnotations(graph, graphBuilder, infResidualBP, tableAnnotations);
                    if (!success)
                        throw new STIException("Invalid marginals, failed: " + table.getSourceId());
                }
            } else {
                LOG.warn("EMPTY TABLE:" + table.getSourceId());
            }
            return tableAnnotations;
        } catch (Exception e) {
            throw new STIException(e);
        }
    }

    private boolean hasAnnotation(TAnnotationJI tabAnnotations) {
        for (int col = 0; col < tabAnnotations.getCols(); col++) {
            TColumnHeaderAnnotation[] ha = tabAnnotations.getHeaderAnnotation(col);
            if (ha.length > 0)
                return true;
            for (int row = 0; row < tabAnnotations.getRows(); row++) {
                if (tabAnnotations.getContentCellAnnotations(row, col).length > 0)
                    return true;
            }
        }
        if (tabAnnotations.getColumncolumnRelations().size() > 0)
            return true;

        return false;
    }

    /**
     * Cell values like "yes", "no", "home", "away" in some domains have caused problems to JI.
     * Ignore these columns if they only contain such values
     *
     * @param table
     * @return
     */
    protected Set<Integer> collectIgnoreColumns(Table table) {
        Set<Integer> ignore = new HashSet<>();
        ignore.addAll(getIgnoreColumns());

        for (int c = 0; c < table.getNumCols(); c++) {
            Set<String> uniqueStrings = new HashSet<>();
            for (int r = 0; r < table.getNumRows(); r++) {
                TCell tcc = table.getContentCell(r, c);
                String text = tcc.getText().trim().replaceAll("[^a-zA-Z0-9]", "");
                if (text.length() > 1)
                    uniqueStrings.add(text);
            }
            if (uniqueStrings.size() < 4 && table.getNumRows() > 4) {
                uniqueStrings.removeAll(invalidCellValues);
                if (uniqueStrings.size() == 0) ignore.add(c);
            }
        }

        return ignore;
    }

    protected boolean generateEntityCandidates(Table table,
                                               TAnnotation tableAnnotations,
                                               Collection<Integer> ignoreColumns) throws KBSearchException {
        boolean graphNonEmpty = false;
        for (int col = 0; col < table.getNumCols(); col++) {
            if (getMustdoColumns().contains(col)) {
                LOG.info("\t\t>> column=(compulsory)" + col);
                for (int r = 0; r < table.getNumRows(); r++) {
                    neGenerator.generateInitialCellAnnotations(tableAnnotations, table, r, col);
                }
                graphNonEmpty = true;
            } else {
                if (ignoreColumns.contains(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                LOG.info("\t\t>> column=" + col);
                for (int r = 0; r < table.getNumRows(); r++) {
                    neGenerator.generateInitialCellAnnotations(tableAnnotations, table, r, col);
                }
                graphNonEmpty = true;
            }
        }
        return graphNonEmpty;
    }

    protected void generateClazzCandidates(TAnnotationJI tableAnnotations, Table table,
                                           Collection<Integer> ignoreColumnsLocal) throws KBSearchException, STIException {
        // ObjectMatrix1D ccFactors = new SparseObjectMatrix1D(table.getNumCols());
        for (int col = 0; col < table.getNumCols(); col++) {
            if (getMustdoColumns().contains(col)) {
                LOG.info("\t\t>> column=(compulsory)" + col);
                columnClazzClassifier.generateInitialColumnAnnotations(tableAnnotations, table, col);
            } else {
                if (ignoreColumnsLocal.contains(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                LOG.info("\t\t>> column=" + col);
                columnClazzClassifier.generateInitialColumnAnnotations(tableAnnotations, table, col);
            }
        }
    }

    protected void generateRelationCandidates(TAnnotationJI tabAnnotations, Table table,
                                              boolean useSubjectColumn,
                                              Collection<Integer> ignoreColumnsLocal) throws IOException, KBSearchException {
        relationGenerator.generateInitialColumnColumnRelations(tabAnnotations, table, useSubjectColumn, ignoreColumnsLocal);
    }

    protected boolean createAnnotations(FactorGraph graph,
                                        FactorGraphBuilder graphBuilder,
                                        Inferencer infResidualBP,
                                        TAnnotationJI tabAnnotations) {
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
                TCellAnnotation[] candidateCellAnnotations =
                        tabAnnotations.getContentCellAnnotations(position[0], position[1]);

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
                tabAnnotations.setContentCellAnnotations(position[0], position[1], candidateCellAnnotations);
            } else if (varType.equals(VariableType.HEADER.toString())) {
                Integer position = graphBuilder.getHeaderPosition(var);
                if (position == null)
                    continue;
                TColumnHeaderAnnotation[] candidateHeaderAnnotations = tabAnnotations.getHeaderAnnotation(position);
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
                tabAnnotations.setHeaderAnnotation(position, candidateHeaderAnnotations);
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
                        tabAnnotations.getColumncolumnRelations().get(direction);

                tabAnnotations.getColumncolumnRelations().remove(new RelationColumns(
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
                tabAnnotations.getColumncolumnRelations().put(direction,
                        relationCandidates);
                //go through again and collection only...

            } else {
                continue;
            }
        }
        return true;
    }

}
