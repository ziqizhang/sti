package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.types.*;
import uk.ac.shef.dcs.oak.sti.rep.Key_SubjectCol_ObjectCol;

import java.io.File;
import java.util.*;

/**
 * Created by zqz on 12/05/2015.
 */
class FactorBuilderCellAndRelation extends FactorBuilder {

    public void addFactors(Map<String, Variable> relationVariables,
                           Map<String, Variable> cellVariables,
                           LTableAnnotation_JI_Freebase annotation,
                           FactorGraph graph,
                           Map<String, Key_SubjectCol_ObjectCol> relationVarOutcomeDirection,
                           String tableId, Set<Integer> columns) {
        List<String> processed = new ArrayList<String>();
        for (int c1 = 0; c1 < annotation.getCols(); c1++) {
            for (int c2 = 0; c2 < annotation.getCols(); c2++) {
                if (c1 == c2) continue;
                if(columns!=null&& !columns.contains(c1)&&columns.contains(c2)) continue;

                if (processed.contains(c1 + "," + c2) || processed.contains(c2 + "," + c1)) continue;
                Variable relation_var = relationVariables.get(c1 + "," + c2);
                if (relation_var == null)
                    relation_var = relationVariables.get(c2 + "," + c1);
                if (relation_var != null) {
                    //there is a relation between c1, c2, go thru each row, to create factor between the cell pair and relation
                    for (int r = 0; r < annotation.getRows(); r++) {
                        Variable sbj_cell_var = cellVariables.get(r + "," + c1);
                        Variable obj_cell_var = cellVariables.get(r + "," + c2);
                        createCellRelationFactor(sbj_cell_var, obj_cell_var, relation_var,
                                annotation, graph, relationVarOutcomeDirection, tableId);
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
                                          Map<String, Key_SubjectCol_ObjectCol> relationVarOutcomeDirection,
                                          String tableId) {
        if (sbjCellVar != null && objCellVar != null) {
            Map<String, Double> affinity_scores = new HashMap<String, Double>();
            Map<Integer, Boolean> relationIndex_forwardRelation = new HashMap<Integer, Boolean>();
            for (int s = 0; s < sbjCellVar.getNumOutcomes(); s++) {
                String sbj = sbjCellVar.getLabelAlphabet().lookupLabel(s).toString();
                for (int r = 0; r < relationVar.getNumOutcomes(); r++) {
                    String rel = relationVar.getLabelAlphabet().lookupLabel(r).toString();
                    Key_SubjectCol_ObjectCol direction = relationVarOutcomeDirection.get(rel);
                    boolean forwardRelation = true;
                    if (direction.getObjectCol() < direction.getSubjectCol()) forwardRelation = false;
                    if (forwardRelation)
                        relationIndex_forwardRelation.put(r, true);
                    else
                        relationIndex_forwardRelation.put(r, false);

                    for (int o = 0; o < objCellVar.getNumOutcomes(); o++) {
                        String obj = objCellVar.getLabelAlphabet().lookupLabel(o).toString();
                        double score;
                        if (forwardRelation) {
                            score = annotation.getScore_entityPairAndRelation(sbj, obj, rel);
                            score = score + annotation.getScore_entityAndRelation(sbj, rel);
                            if (score > 0)
                                updateAffinity(affinity_scores, sbjCellVar, s, objCellVar, o, r, score);
                        } else {
                            score = annotation.getScore_entityPairAndRelation(obj, sbj, rel);
                            score = score + annotation.getScore_entityAndRelation(obj, rel);
                            if (score > 0)
                                updateAffinity(affinity_scores, sbjCellVar, s, objCellVar, o, r, score);
                        }
                    }
                }
            }
            if (affinity_scores.size() > 0) {
                double[] potential;
                if (sbjCellVar.getIndex() < objCellVar.getIndex())
                    potential = computePotential(affinity_scores,
                            sbjCellVar, objCellVar, relationVar, relationIndex_forwardRelation);
                else
                    potential = computePotential(affinity_scores,
                            objCellVar, sbjCellVar, relationVar, relationIndex_forwardRelation);
                if (isValidPotential(potential, affinity_scores)) {
                    VarSet varSet;
                    if(sbjCellVar.getIndex() < objCellVar.getIndex())
                        varSet= new HashVarSet(new Variable[]{sbjCellVar, objCellVar, relationVar});
                    else
                        varSet= new HashVarSet(new Variable[]{objCellVar, sbjCellVar, relationVar});
                    TableFactor factor = new TableFactor(varSet, potential);
                    GraphCheckingUtil.checkFactorAgainstAffinity(factor, affinity_scores, tableId);
                    graph.addFactor(factor);
                }
            }
        }
    }

    private void updateAffinity(Map<String, Double> affinity_scores,
                                Variable sbjCellVar, int sbjVarOutcomeIndex,
                                Variable objCellVar, int objVarOutcomeIndex,
                                int relationVarOutcomeIndex, double score) {
        if (sbjCellVar.getIndex() < objCellVar.getIndex()) {
            affinity_scores.put(sbjVarOutcomeIndex + ">" + objVarOutcomeIndex + ">" + relationVarOutcomeIndex, score);
        } else {
            affinity_scores.put(objVarOutcomeIndex + ">" + sbjVarOutcomeIndex + ">" + relationVarOutcomeIndex, score);
        }
    }

    public void addFactors(Map<String, Variable> relationVariables,
                           Map<String, Variable> cellVariables,
                           LTableAnnotation_JI_Freebase annotation,
                           FactorGraph graph,
                           Map<String, Key_SubjectCol_ObjectCol> relationVarOutcomeDirection,
                           String tableId) {
        addFactors(relationVariables, cellVariables, annotation, graph,
                relationVarOutcomeDirection, tableId, null);
    }
}
