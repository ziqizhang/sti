package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.types.Variable;
import cc.mallet.types.LabelAlphabet;

import java.util.Map;

/**
 * Created by zqz on 12/05/2015.
 */
abstract class FactorBuilder {

    protected void checkVariableOutcomeUsage(double potential, String key, Map<String, Boolean> varOutcomeHasNonZeroPotential) {
        Boolean hasNonZeroPotential =
                varOutcomeHasNonZeroPotential.
                        get(key);
        if (hasNonZeroPotential == null) {
            hasNonZeroPotential = false;
            varOutcomeHasNonZeroPotential.put(key, hasNonZeroPotential);
        }
        if (potential > 0) {
            if (!hasNonZeroPotential)
                varOutcomeHasNonZeroPotential.put(key, true);
        }
    }

    protected boolean isValidPotential(double[] potential1) {
        int countZero = 0;
        for (int i = 0; i < potential1.length; i++) {
            if (potential1[i] == 0)
                countZero++;
        }
        //System.out.println(note + ":" + countZero + "/" + potential1.length);
        if (countZero == potential1.length)
            return false;
        return true;
    }

    protected Variable createDummyVariable(String label){
        LabelAlphabet dummyLA = new LabelAlphabet();
        dummyLA.lookupLabel(label);
        Variable v = new Variable(dummyLA);
        v.setLabel(label);
        return v;
    }

    /**
     * @param affinity_values_between_variable_outcomes in the key, the first element (int) must correspond to the index in cellVar; the second must
     *                                                  correpsond to the index in headerVar
     * @param firstVar
     * @param secondVar
     * @return
     */
    protected double[] computePotential(
            Map<String, Double> affinity_values_between_variable_outcomes,
            Variable firstVar,
            Variable secondVar) {
        int dimensionFirstVar = firstVar.getNumOutcomes();
        int dimensionSecondVar = secondVar.getNumOutcomes();
        double[] res = new double[dimensionFirstVar * dimensionSecondVar];
        for (int first = 0; first < dimensionFirstVar; first++) {
            for (int second = 0; second < dimensionSecondVar; second++) {
                String key = first + "," + second;
                Double affinity = affinity_values_between_variable_outcomes.get(key);

                if (affinity == null)
                    affinity = 0.0;
                res[first * dimensionSecondVar + second] = affinity;
            }
        }
        return res;
    }

}
