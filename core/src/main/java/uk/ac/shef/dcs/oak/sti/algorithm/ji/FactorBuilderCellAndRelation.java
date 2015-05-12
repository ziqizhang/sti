package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.types.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zqz on 12/05/2015.
 */
class FactorBuilderCellAndRelation extends FactorBuilder {

    public void addFactors(Map<String, Variable> relationVariables,
                                           Map<String, Variable> cellVariables,
                                           LTableAnnotation_JI_Freebase annotation,
                                           FactorGraph graph,
                                           Map<String, Boolean> varOutcomeHasNonZeroPotential) {
        List<String> processed = new ArrayList<String>();
        for (int c1 = 0; c1 < annotation.getCols(); c1++) {
            for (int c2 = 0; c2 < annotation.getCols(); c2++) {
                if (c1 == c2) continue;
                if (processed.contains(c1 + "," + c2) || processed.contains(c2 + "," + c1)) continue;
                Variable relation_var = relationVariables.get(c1 + "," + c2);
                if (relation_var != null) {
                    //there is a relation between c1, c2, go thru each row, to create factor between the cell pair and relation
                    for (int r = 0; r < annotation.getRows(); r++) {
                        Variable sbj_cell_var = cellVariables.get(r + "," + c1);
                        createCellRelationFactor(sbj_cell_var, relation_var, annotation, graph,varOutcomeHasNonZeroPotential);
                        Variable obj_cell_var = cellVariables.get(r + "," + c2);
                        createCellRelationFactor(obj_cell_var, relation_var, annotation, graph,varOutcomeHasNonZeroPotential);
                    }
                } else {
                    relation_var = relationVariables.get(c2 + "," + c1);
                    if (relation_var != null) {
                        for (int r = 0; r < annotation.getRows(); r++) {
                            Variable sbj_cell_var = cellVariables.get(r + "," + c2);
                            createCellRelationFactor(sbj_cell_var, relation_var, annotation, graph,varOutcomeHasNonZeroPotential);
                            Variable obj_cell_var = cellVariables.get(r + "," + c1);
                            createCellRelationFactor(obj_cell_var, relation_var, annotation, graph,varOutcomeHasNonZeroPotential);
                        }
                    }
                }
                processed.add(c1 + "," + c2);
                processed.add(c2 + "," + c1);
            }
        }

    }

    private void createCellRelationFactor(Variable cellVar, Variable relationVar,
                                          LTableAnnotation_JI_Freebase annotation,
                                          FactorGraph graph,
                                          Map<String, Boolean> varOutcomeHasNonZeroPotential) {
        if (cellVar != null) {
            Map<String, Double> affinity_scores = new HashMap<String, Double>();
            for (int i = 0; i < cellVar.getNumOutcomes(); i++) {
                String sbj = cellVar.getLabelAlphabet().lookupLabel(i).toString();
                for (int j = 0; j < relationVar.getNumOutcomes(); j++) {
                    String rel = relationVar.getLabelAlphabet().lookupLabel(j).toString();
                    double score = annotation.getScore_entityAndRelation(sbj, rel);
                    /*if(score==0)
                        score=Math.pow(10.0,-12);*/
                    if (score > 0) {
                        affinity_scores.put(i + "," + j, score);
                    }
                    checkVariableOutcomeUsage(score, cellVar.getLabel() + "." + sbj,varOutcomeHasNonZeroPotential);
                    checkVariableOutcomeUsage(score, relationVar.getLabel() + "." + rel,varOutcomeHasNonZeroPotential);
                }
            }
            if (affinity_scores.size() > 0) {
                double[] potential = computePotential(affinity_scores,
                        cellVar, relationVar);
                if (isValidPotential(potential)) {
                    VarSet varSet = new HashVarSet(new Variable[]{cellVar, relationVar});
                    TableFactor factor = new TableFactor(varSet, potential);
                    graph.addFactor(factor);
                }
            }
        }
    }




}
