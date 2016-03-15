package uk.ac.shef.dcs.sti.algorithm.ji;

import cc.mallet.grmm.types.Variable;
import cc.mallet.types.LabelAlphabet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by zqz on 12/05/2015.
 */
abstract class FactorBuilder {

    protected boolean patchScores=false;

    public void setPatchScores(boolean yes){
        patchScores=yes;
    }

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

    protected double[] patchCompatibility(double[] potential1) {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < potential1.length; i++) {
            if (potential1[i]!=0&& potential1[i] < min)
                min = potential1[i];
        }

        //debug this is a cheat to arbitrarily fill up zero values to ensure a dense graph
        for (int i = 0; i < potential1.length; i++) {
            if (potential1[i] == 0)
                potential1[i] = min / 5000.0;
        }
        //
        return potential1;
    }

    protected boolean isValidCompatibility(double[] potential1, Map<String, Double> affinityValues) {
        int countZero = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < potential1.length; i++) {
            if (potential1[i] == 0)
                countZero++;
            if (potential1[i] < min)
                min = potential1[i];
        }

        //System.out.println(note + ":" + countZero + "/" + potential1.length);
        if (countZero == potential1.length)
            return false;

        if (affinityValues != null && potential1.length - countZero != affinityValues.size())
            return false;
        return true;
    }

    protected Variable createDummyVariable(String label) {
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


    /**
     * correpsond to the index in headerVar
     *
     * @param firstHeaderVar
     * @param relationVar
     * @return
     */
    protected double[] computePotential(
            Map<String, Double> affinity_values,
            Variable firstHeaderVar,
            Variable relationVar,
            Variable secondHeaderVar,
            Map<Integer, Boolean> relationIndex_forwardRelation) {
        int dimensionFirstHeaderVar = firstHeaderVar.getNumOutcomes();
        int dimensionSecondVar = relationVar.getNumOutcomes();
        int dimensionThirdVar = secondHeaderVar.getNumOutcomes();
        double[] res = new double[dimensionFirstHeaderVar * dimensionSecondVar * dimensionThirdVar];
        Set<Integer> indexes = new HashSet<Integer>();
        for (int f = 0; f < dimensionFirstHeaderVar; f++) {
            for (int s = 0; s < dimensionSecondVar; s++) {
                for (int t = 0; t < dimensionThirdVar; t++) {
                    Double affinity = affinity_values.get(f + ">" + s + ">" + t);
                    if (affinity == null) affinity = 0.0;
                    int index = f * dimensionSecondVar * dimensionThirdVar +
                            s * dimensionThirdVar + t;
                    res[index] = affinity;
                    if (affinity != 0)
                        indexes.add(index);
                }
            }
        }
        //System.out.println(cc);
        return res;
    }

    /*
    for (int f = 0; f < dimensionFirstHeaderVar; f++) {
            for (int r = 0; r < dimensionRelationVar; r++) {
                for (int s = 0; s < dimensionSecondHeaderVar; s++) {
                    Double affinity = affinity_values.get(f + ">" + r+">"+s);
                    if(affinity==null) affinity=affinity_values.get(s+">"+r+">"+f);
                    if (affinity == null) affinity = 0.0;
                    int index=f * dimensionRelationVar * dimensionSecondHeaderVar +
                            r*dimensionSecondHeaderVar + s;
                    res[index] = affinity;
                    if(affinity!=0) {
                        cc++;
                        indexes.add(index);
                    }
                }
            }
        }
     */
}
