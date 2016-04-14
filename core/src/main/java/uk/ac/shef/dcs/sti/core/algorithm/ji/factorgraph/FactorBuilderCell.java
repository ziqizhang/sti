package uk.ac.shef.dcs.sti.core.algorithm.ji.factorgraph;

import cc.mallet.grmm.types.*;
import cc.mallet.types.LabelAlphabet;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.ji.JIAdaptedEntityScorer;
import uk.ac.shef.dcs.sti.core.algorithm.ji.VariableType;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;

import java.util.*;

/**
 * Created by zqz on 12/05/2015.
 */
class FactorBuilderCell extends FactorBuilder{

    protected Map<Variable, int[]> cellVarOutcomePosition = new HashMap<>();

    public Map<String, Variable> addFactors(TAnnotation annotation, FactorGraph graph,
                                            Map<Variable, String> typeOfVariable,
                                            Set<Integer> columns
    ) throws STIException {
        Map<String, Variable> variables = new HashMap<>();
        for (int row = 0; row < annotation.getRows(); row++) {
            for (int col = 0; col < annotation.getCols(); col++) {
                if(columns!=null&&!columns.contains(col)) continue;
                Variable dummyCell = createDummyVariable("dummyCell("+row+","+col+")");

                TCellAnnotation[] candidateEntityAnnotations = annotation.getContentCellAnnotations(row, col);
                if (candidateEntityAnnotations.length == 0)
                    continue;
                String cellPosition = String.valueOf(row) + "," + String.valueOf(col);

                LabelAlphabet candidateIndex_cell = new LabelAlphabet();
                List<Double> scores=new ArrayList<>();

                for (int i = 0; i < candidateEntityAnnotations.length; i++) {
                    TCellAnnotation ca = candidateEntityAnnotations[i];
                    double score=ca.getScoreElements().get(
                            JIAdaptedEntityScorer.SCORE_FINAL);
                    if(score==0)
                        continue;

                    candidateIndex_cell.lookupIndex(ca.getAnnotation().getId());
                    scores.add(score);
                }
                if(scores.size()==0)
                    continue;
                double[] compatibility = new double[scores.size()];
                for(int i=0; i<compatibility.length;i++)
                    compatibility[i]=scores.get(i);
                Variable variable_cell = new Variable(candidateIndex_cell);
                variable_cell.setLabel(VariableType.CELL.toString() + "." + cellPosition);
                typeOfVariable.put(variable_cell, VariableType.CELL.toString());
                cellVarOutcomePosition.put(variable_cell, new int[]{row, col});

                if (isValidGraphAffinity(compatibility, null)) {
                    //if(patchScores) compatibility= patchCompatibility(compatibility);
                    Variable[] vars = new Variable[]{dummyCell, variable_cell};
                    TableFactor factor = new TableFactor(vars, compatibility);
                    graph.addFactor(factor);
                    variables.put(row + "," + col, variable_cell);
                }else{
                    throw new STIException("Fatal: inconsistency detected on graph, while mapping affinity scores to potentials");
                }
            }
        }
        return variables;

    }
    public Map<String, Variable> addFactors(TAnnotation annotation, FactorGraph graph,
                                            Map<Variable, String> typeOfVariable
                                            ) throws STIException {
        return addFactors(annotation, graph, typeOfVariable, null);
    }
}
