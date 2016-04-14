package uk.ac.shef.dcs.sti.core.algorithm.ji.factorgraph;

import cc.mallet.grmm.types.*;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.ji.DebuggingUtil;
import uk.ac.shef.dcs.sti.core.algorithm.ji.TAnnotationJI;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zqz on 12/05/2015.
 */
class FactorBuilderHeaderAndCell extends FactorBuilder {

    public void addFactors(Map<String, Variable> cellVariables,
                           Map<Integer, Variable> headerVariables,
                           TAnnotationJI annotation,
                           FactorGraph graph,
                           String tableId, Set<Integer> columns) throws STIException {
        for (int col = 0; col < annotation.getCols(); col++) {
            if(columns!=null&& !columns.contains(col)) continue;

            Variable headerVar = headerVariables.get(col);
            if (headerVar == null) continue;

            for (int row = 0; row < annotation.getRows(); row++) {
                TCellAnnotation[] candidateEntityAnnotations = annotation.getContentCellAnnotations(row, col);
                if (candidateEntityAnnotations.length == 0) continue;
                Variable cellVar = cellVariables.get(row + "," + col);
                if (cellVar == null) continue;

                Map<String, Double> affinity_values_between_variable_outcomes = new HashMap<>();
                //go thru every candidate cell entity
                for (TCellAnnotation ca : candidateEntityAnnotations) {
                    //which concept it has a relation with
                    String entId = ca.getAnnotation().getId();
                    int cellVarOutcomeIndex = cellVar.getLabelAlphabet().lookupIndex(entId, false);
                    if (cellVarOutcomeIndex < 0) continue;
                    for (int headerVarOutcomeIndex=0; headerVarOutcomeIndex<headerVar.getNumOutcomes(); headerVarOutcomeIndex++) {
                        String conceptURL = headerVar.getLabelAlphabet().lookupLabel(headerVarOutcomeIndex).toString();
                        if (headerVarOutcomeIndex < 0) continue;

                        double score = annotation.getScoreEntityAndConceptSimilarity(entId, conceptURL);
                        if (score > 0) {
                            affinity_values_between_variable_outcomes.put(
                                    cellVarOutcomeIndex + "," +headerVarOutcomeIndex , score
                            );
                        }
                    }
                }

                if (affinity_values_between_variable_outcomes.size() > 0) {
                    double[] compatibility = computePotential(affinity_values_between_variable_outcomes,
                            cellVar, headerVar);
                    if (isValidGraphAffinity(compatibility, affinity_values_between_variable_outcomes)) {
                        Variable[] vars = new Variable[]{cellVar, headerVar};
                        //VarSet varSet = new HashVarSet(new Variable[]{cellVar, headerVar});
                        TableFactor factor = new TableFactor(vars, compatibility);
                        DebuggingUtil.debugFactorAndAffinity(factor, affinity_values_between_variable_outcomes, tableId);
                        graph.addFactor(factor);
                    }
                    else{
                        throw new STIException("Fatal: inconsistency detected on graph, while mapping affinity scores to potentials");
                    }
                }
            }
        }
    }

    public void addFactors(Map<String, Variable> cellVariables,
                           Map<Integer, Variable> headerVariables,
                           TAnnotationJI annotation,
                           FactorGraph graph,
                           String tableId) throws STIException {
        addFactors(cellVariables, headerVariables, annotation, graph, tableId, null);
    }
}
