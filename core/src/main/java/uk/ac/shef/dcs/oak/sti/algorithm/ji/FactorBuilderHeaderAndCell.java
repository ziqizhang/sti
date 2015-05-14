package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.types.*;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseFreebaseFilter;
import uk.ac.shef.dcs.oak.sti.rep.CellAnnotation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zqz on 12/05/2015.
 */
class FactorBuilderHeaderAndCell extends FactorBuilder {

    public void addFactors(Map<String, Variable> cellVariables,
                           Map<Integer, Variable> headerVariables,
                           LTableAnnotation_JI_Freebase annotation,
                           FactorGraph graph) {
        for (int col = 0; col < annotation.getCols(); col++) {
            Variable headerVar = headerVariables.get(col);
            if (headerVar == null) continue;

            for (int row = 0; row < annotation.getRows(); row++) {
                CellAnnotation[] candidateEntityAnnotations = annotation.getContentCellAnnotations(row, col);
                if (candidateEntityAnnotations.length == 0) continue;
                Variable cellVar = cellVariables.get(row + "," + col);
                if (cellVar == null) continue;

                Map<String, Double> affinity_values_between_variable_outcomes = new HashMap<String, Double>();
                //go thru every candidate cell entity
                for (CellAnnotation ca : candidateEntityAnnotations) {
                    //which concept it has a relation with
                    String entId = ca.getAnnotation().getId();
                    int cellVarOutcomeIndex = cellVar.getLabelAlphabet().lookupIndex(entId, false);
                    if (cellVarOutcomeIndex < 0) continue;
                    for (int headerVarOutcomeIndex=0; headerVarOutcomeIndex<headerVar.getNumOutcomes(); headerVarOutcomeIndex++) {
                        String conceptURL = headerVar.getLabelAlphabet().lookupLabel(headerVarOutcomeIndex).toString();
                        if (headerVarOutcomeIndex < 0) continue;

                        double score = annotation.getScore_entityAndConcept(entId, conceptURL);
                        if (score > 0) {
                            affinity_values_between_variable_outcomes.put(
                                    cellVarOutcomeIndex + "," +headerVarOutcomeIndex , score
                            );
                        }
                    }
                }

                if (affinity_values_between_variable_outcomes.size() > 0) {
                    double[] potential = computePotential(affinity_values_between_variable_outcomes,
                            cellVar, headerVar);
                    if (isValidPotential(potential, affinity_values_between_variable_outcomes)) {
                        VarSet varSet = new HashVarSet(new Variable[]{cellVar, headerVar});
                        TableFactor factor = new TableFactor(varSet, potential);
                        GraphCheckingUtil.checkFactorAgainstAffinity(factor, affinity_values_between_variable_outcomes);
                        graph.addFactor(factor);
                    }
                }
            }
        }
    }
}
