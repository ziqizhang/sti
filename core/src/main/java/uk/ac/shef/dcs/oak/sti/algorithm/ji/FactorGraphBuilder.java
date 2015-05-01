package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.types.*;
import cc.mallet.types.LabelAlphabet;
import uk.ac.shef.dcs.oak.sti.misc.KB_InstanceFilter;
import uk.ac.shef.dcs.oak.sti.rep.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zqz on 01/05/2015.
 */
public class FactorGraphBuilder {

    public FactorGraph build(LTableAnnotation_JI_Freebase annotation, LTable table) {
        FactorGraph graph = new FactorGraph();
        //cell text and entity label
        Map<int[], Variable> cellAnnotations = addCellAnnotationFactors(annotation, table, graph);
        //column header and type label
        Map<Integer, Variable> columnHeaders = addColumnHeaderFactors(annotation, table, graph);
        //column type and cell entities
        addHeaderAndCellFactors(cellAnnotations,
                columnHeaders,
                annotation,
                table, graph);
        //relation and pair of column types
        addRelationAndHeaderFactors(cellAnnotations,
                columnHeaders,
                annotation,
                table, graph);

        //relation and entity pairs
        return graph;
    }

    private void addHeaderAndCellFactors(Map<int[], Variable> cellVariables,
                                         Map<Integer, Variable> headerVariables,
                                         LTableAnnotation_JI_Freebase annotation,
                                         LTable table,
                                         FactorGraph graph) {
        for (int row = 0; row < annotation.getRows(); row++) {
            for (int col = 0; col < annotation.getCols(); col++) {
                CellAnnotation[] candidateEntityAnnotations = annotation.getContentCellAnnotations(row, col);
                if (candidateEntityAnnotations.length == 0)
                    continue;

                Variable cellVar = cellVariables.get(new int[]{row, col});
                Variable headerVar = headerVariables.get(col);

                Map<int[], Double> affinity_values_between_variable_outcomes = new HashMap<int[], Double>();
                //go thru every candidate cell entity
                for (CellAnnotation ca : candidateEntityAnnotations) {
                    //which concept it has a relation with
                    String entId = ca.getAnnotation().getId();
                    int cellVarOutcomeIndex = cellVar.getLabelAlphabet().lookupIndex(entId, false);
                    if (cellVarOutcomeIndex < 0) continue;
                    for (String type : ca.getAnnotation().getTypeIds()) {
                        if (KB_InstanceFilter.ignoreType(type, type))
                            continue;
                        int headerVarOutcomeIndex = headerVar.getLabelAlphabet().lookupIndex(type, false);
                        if (headerVarOutcomeIndex < 0) continue;

                        affinity_values_between_variable_outcomes.put(
                                new int[]{cellVarOutcomeIndex, headerVarOutcomeIndex},
                                annotation.getScore_entityAndConcept(entId, type));

                    }
                }

                if (affinity_values_between_variable_outcomes.size() > 0) {
                    double[] potential = computePotential(affinity_values_between_variable_outcomes,
                            cellVar, headerVar);
                    VarSet varSet = new HashVarSet(new Variable[]{cellVar, headerVar});
                    TableFactor factor = new TableFactor(varSet, potential);
                    graph.addFactor(factor);
                }
            }
        }
    }

    /**
     *
     * @param affinity_values_between_variable_outcomes
     *        in the key, the first element (int) must correspond to the index in cellVar; the second must
     *        correpsond to the index in headerVar
     * @param firstVar
     * @param secondVar
     * @return
     */
    private double[] computePotential(
            Map<int[], Double> affinity_values_between_variable_outcomes,
            Variable firstVar,
            Variable secondVar) {
        int dimensionFirstVar = firstVar.getNumOutcomes();
        int dimensionSecondVar = secondVar.getNumOutcomes();
        double[] res = new double[dimensionFirstVar*dimensionSecondVar];
        for(int first = 0; first<dimensionFirstVar; first++){
            for(int second = 0; second<dimensionSecondVar; second++){
                int[] key = new int[]{first, second};
                Double affinity = affinity_values_between_variable_outcomes.get(key);

                if(affinity==null)
                     affinity=0.0;
                res[first+second]=affinity;
            }
        }
        return res;
    }

    private Map<int[], Variable> addCellAnnotationFactors(LTableAnnotation annotation, LTable table, FactorGraph graph) {
        Map<int[], Variable> variables = new HashMap<int[], Variable>();
        for (int row = 0; row < annotation.getRows(); row++) {
            for (int col = 0; col < annotation.getCols(); col++) {
                CellAnnotation[] candidateEntityAnnotations = annotation.getContentCellAnnotations(row, col);
                if (candidateEntityAnnotations.length == 0)
                    continue;
                String cellText = String.valueOf(row) + "," + String.valueOf(col);

                LabelAlphabet candidateIndex_cell = new LabelAlphabet();
                double[] potential = new double[candidateEntityAnnotations.length];
                for (int i = 0; i < candidateEntityAnnotations.length; i++) {
                    CellAnnotation ca = candidateEntityAnnotations[i];
                    candidateIndex_cell.lookupIndex(ca.getAnnotation().getId());
                    potential[i] = ca.getScore_element_map().get(
                            DisambiguationScorer.SCORE_CELL_FACTOR
                    );
                }
                Variable variable_cell = new Variable(candidateIndex_cell);
                variable_cell.setLabel(cellText);
                TableFactor factor = new TableFactor(variable_cell, potential);
                graph.addFactor(factor);
                variables.put(new int[]{row, col}, variable_cell);
            }
        }
        return variables;
    }

    protected Map<Integer, Variable> addColumnHeaderFactors(LTableAnnotation annotation, LTable table,
                                                            FactorGraph graph) {
        Map<Integer, Variable> variables = new HashMap<Integer, Variable>();
        for (int col = 0; col < annotation.getCols(); col++) {
            HeaderAnnotation[] candidateConcepts_header = annotation.getHeaderAnnotation(col);
            if (candidateConcepts_header.length == 0)
                continue;

            String headerText = String.valueOf(col);
            LabelAlphabet candidateIndex_header = new LabelAlphabet();

            double[] potential = new double[candidateConcepts_header.length];
            for (int i = 0; i < candidateConcepts_header.length; i++) {
                HeaderAnnotation ha = candidateConcepts_header[i];
                candidateIndex_header.lookupIndex(ha.getAnnotation_url());
                potential[i] = ha.getScoreElements().get(
                        ClassificationScorer.SCORE_HEADER_FACTOR
                );
            }
            Variable variable_header = new Variable(candidateIndex_header);
            variable_header.setLabel(headerText);
            TableFactor factor = new TableFactor(variable_header, potential);
            graph.addFactor(factor);
            variables.put(col, variable_header);
        }
        return variables;
    }
}
