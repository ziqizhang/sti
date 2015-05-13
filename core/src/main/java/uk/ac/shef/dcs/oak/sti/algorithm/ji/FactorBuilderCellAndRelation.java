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
                        Variable obj_cell_var = cellVariables.get(r + "," + c2);
                        createCellRelationFactor(sbj_cell_var, obj_cell_var, relation_var,
                                annotation, graph,varOutcomeHasNonZeroPotential);
                    }
                } else {
                    relation_var = relationVariables.get(c2 + "," + c1);
                    if (relation_var != null) {
                        for (int r = 0; r < annotation.getRows(); r++) {
                            Variable sbj_cell_var = cellVariables.get(r + "," + c2);
                            Variable obj_cell_var = cellVariables.get(r + "," + c1);
                            createCellRelationFactor(obj_cell_var, sbj_cell_var, relation_var,
                                    annotation, graph,varOutcomeHasNonZeroPotential);
                        }
                    }
                }
                processed.add(c1 + "," + c2);
                processed.add(c2 + "," + c1);
            }
        }

    }

    private void createCellRelationFactor(Variable sbjCellVar,
                                          Variable objCellVar,
                                          Variable relationVar,
                                          LTableAnnotation_JI_Freebase annotation,
                                          FactorGraph graph,
                                          Map<String, Boolean> varOutcomeHasNonZeroPotential) {
        if (sbjCellVar != null && objCellVar!=null) {
            Map<String, Double> affinity_scores = new HashMap<String, Double>();
            for (int s = 0; s < sbjCellVar.getNumOutcomes(); s++) {
                String sbj = sbjCellVar.getLabelAlphabet().lookupLabel(s).toString();
                for (int r = 0; r < relationVar.getNumOutcomes(); r++) {
                    String rel = relationVar.getLabelAlphabet().lookupLabel(r).toString();
                    for(int o=0; o<objCellVar.getNumOutcomes(); o++){
                        String obj = objCellVar.getLabelAlphabet().lookupLabel(o).toString();
                        double score = annotation.getScore_entityPairAndRelation(sbj, obj, rel);
                        score = score + annotation.getScore_entityAndRelation(sbj, rel);
                        if (score > 0) {
                            affinity_scores.put(s + ">" +r+">"+o , score);
                        }
                        checkVariableOutcomeUsage(score, sbjCellVar.getLabel() + "." + sbj,varOutcomeHasNonZeroPotential);
                        checkVariableOutcomeUsage(score, relationVar.getLabel() + "." + rel,varOutcomeHasNonZeroPotential);
                    }
                }
            }
            if (affinity_scores.size() > 0) {
                double[] potential = computePotential(affinity_scores,
                        sbjCellVar, relationVar, objCellVar);
                if (isValidPotential(potential)) {
                    VarSet varSet = new HashVarSet(new Variable[]{sbjCellVar, relationVar, objCellVar});
                    TableFactor factor = new TableFactor(varSet, potential);
                    graph.addFactor(factor);
                }
            }
        }
    }




}
