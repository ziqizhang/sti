package uk.ac.shef.dcs.sti.core.algorithm.ji.factorgraph;

import cc.mallet.grmm.types.*;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.ji.DebuggingUtil;
import uk.ac.shef.dcs.sti.core.algorithm.ji.TAnnotationJI;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;

import java.util.*;

/**
 * Created by zqz on 12/05/2015.
 */
class FactorBuilderCellAndRelation extends FactorBuilder {

    public void addFactors(Map<String, Variable> relationVariables,
                           Map<String, Variable> cellVariables,
                           TAnnotationJI annotation,
                           FactorGraph graph,
                           Map<String, RelationColumns> relationVarOutcomeDirection,
                           String tableId, Set<Integer> columns) throws STIException {
        List<String> processed = new ArrayList<>();
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
                                          TAnnotationJI annotation,
                                          FactorGraph graph,
                                          Map<String, RelationColumns> relationVarOutcomeDirection,
                                          String tableId) throws STIException {
        if (sbjCellVar != null && objCellVar != null) {
            Map<String, Double> affinity_scores = new HashMap<>();
            Map<Integer, Boolean> relationIndex_forwardRelation = new HashMap<>();
            for (int s = 0; s < sbjCellVar.getNumOutcomes(); s++) {
                String sbj = sbjCellVar.getLabelAlphabet().lookupLabel(s).toString();
                for (int r = 0; r < relationVar.getNumOutcomes(); r++) {
                    String rel = relationVar.getLabelAlphabet().lookupLabel(r).toString();
                    RelationColumns direction = relationVarOutcomeDirection.get(rel);
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
                            score = annotation.getScoreEntityPairAndRelation(sbj, obj, rel);
                            score = score + annotation.getScoreEntityAndRelation(sbj, rel);
                            if (score > 0)
                                updateAffinity(affinity_scores, sbjCellVar, s, objCellVar, o, r, score);
                        } else {
                            score = annotation.getScoreEntityPairAndRelation(obj, sbj, rel);
                            score = score + annotation.getScoreEntityAndRelation(obj, rel);
                            if (score > 0)
                                updateAffinity(affinity_scores, sbjCellVar, s, objCellVar, o, r, score);
                        }
                    }
                }
            }
            if (affinity_scores.size() > 0) {
                double[] compatibility;
                if (sbjCellVar.getIndex() < objCellVar.getIndex())
                    compatibility = computePotential(affinity_scores,
                            sbjCellVar, objCellVar, relationVar, relationIndex_forwardRelation);
                else
                    compatibility = computePotential(affinity_scores,
                            objCellVar, sbjCellVar, relationVar, relationIndex_forwardRelation);
                if (isValidGraphAffinity(compatibility, affinity_scores)) {
                    Variable[] vars;
                    if(sbjCellVar.getIndex() < objCellVar.getIndex())
                        vars= new Variable[]{sbjCellVar, objCellVar, relationVar};
                    else
                        vars= new Variable[]{objCellVar, sbjCellVar, relationVar};

                    TableFactor factor = new TableFactor(vars, compatibility);
                    DebuggingUtil.debugFactorAndAffinity(factor, affinity_scores, tableId);
                    graph.addFactor(factor);
                }
            }
            else{
                throw new STIException("Fatal: inconsistency detected on graph, while mapping affinity scores to potentials");
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
                           TAnnotationJI annotation,
                           FactorGraph graph,
                           Map<String, RelationColumns> relationVarOutcomeDirection,
                           String tableId) throws STIException {
        addFactors(relationVariables, cellVariables, annotation, graph,
                relationVarOutcomeDirection, tableId, null);
    }
}
