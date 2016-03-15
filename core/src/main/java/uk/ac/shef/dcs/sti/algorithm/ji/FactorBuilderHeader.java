package uk.ac.shef.dcs.sti.algorithm.ji;

import cc.mallet.grmm.types.*;
import cc.mallet.types.LabelAlphabet;
import uk.ac.shef.dcs.sti.rep.HeaderAnnotation;
import uk.ac.shef.dcs.sti.rep.LTableAnnotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zqz on 12/05/2015.
 */
class FactorBuilderHeader extends FactorBuilder {

    protected Map<Variable, Integer> headerVarOutcomePosition = new HashMap<Variable, Integer>();

    public Map<Integer, Variable> addFactors(LTableAnnotation annotation,
                                             FactorGraph graph,
                                             Map<Variable, String> typeOfVariable,
                                             Set<Integer> columns) {
        Map<Integer, Variable> variables = new HashMap<Integer, Variable>();
        for (int col = 0; col < annotation.getCols(); col++) {
            if(columns!=null && !columns.contains(col)) continue;
            Variable dummyHeader = createDummyVariable("dummyHeader("+col+")");

            HeaderAnnotation[] candidateConcepts_header = annotation.getHeaderAnnotation(col);
            if (candidateConcepts_header.length == 0)
                continue;

            String headerPosition = String.valueOf(col);
            LabelAlphabet candidateIndex_header = new LabelAlphabet();

            double[] compatibility = new double[candidateConcepts_header.length];
            for (int i = 0; i < candidateConcepts_header.length; i++) {
                HeaderAnnotation ha = candidateConcepts_header[i];
                candidateIndex_header.lookupIndex(ha.getAnnotation_url());

                compatibility[i] = ha.getScoreElements().get(
                        ClassificationScorer_JI_adapted.SCORE_HEADER_FACTOR
                );
            }
            Variable variable_header = new Variable(candidateIndex_header);
            variable_header.setLabel(VariableType.HEADER.toString() + "." + headerPosition);
            typeOfVariable.put(variable_header, VariableType.HEADER.toString());
            headerVarOutcomePosition.put(variable_header, col);
            variables.put(col, variable_header);

            if (isValidCompatibility(compatibility, null)) {
                if(patchScores) compatibility= patchCompatibility(compatibility);
                Variable[] vars = new Variable[]{dummyHeader, variable_header};
                //VarSet varSet = new HashVarSet(new Variable[]{dummyHeader, variable_header});
                TableFactor factor = new TableFactor(vars, compatibility);
                graph.addFactor(factor);
            }
        }
        return variables;
    }

    public Map<Integer, Variable> addFactors(LTableAnnotation annotation,
                                             FactorGraph graph,
                                             Map<Variable, String> typeOfVariable) {
        return addFactors(annotation, graph, typeOfVariable, null);
    }
}
