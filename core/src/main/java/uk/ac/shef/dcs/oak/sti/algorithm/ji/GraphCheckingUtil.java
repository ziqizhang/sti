package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.types.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by zqz on 14/05/2015.
 */
public class GraphCheckingUtil {

    public static void checkFactorAgainstAffinity(Factor f, Map<String, Double> affinity, String tableId) {
        Set<String> factorValues = new HashSet<String>();
        AssignmentIterator it = f.assignmentIterator();
        while (it.hasNext()) {
            Assignment assignment = it.assignment();
            //assignment.numVariables();
            String[] splits = assignment.dumpToString().trim().split("[\r\n]");
            String indexString = splits[splits.length - 1].trim();
            String[] indexes = indexString.split("\\s+");

            double score = f.value(assignment);
            if (score > 0) {
                String line = "";
                for (int v = 0; v < assignment.numVariables(); v++) {
                    Variable var = assignment.getVariable(v);
                    //System.out.print(indexes[v]+",");
                    int idx = 0;
                    try {
                        idx = Integer.valueOf(indexes[v]);
                    } catch (NumberFormatException e) {
                        System.err.println(assignment.dumpToString() + ">>>>" + indexes[v]);
                        System.exit(1);
                    }
                    //String label = var.getLabelAlphabet().lookupLabel(idx).toString();
                    line = line + idx + " ";
                }
                factorValues.add(line.trim());
            }
            it.next();
        }
        Set<String> affinityValues = new HashSet<String>();
        for (String k : affinity.keySet()) {
            affinityValues.add(k.replaceAll("[^0-9]", " ").trim());
        }

        List<String> factorValuesCopy = new ArrayList<String>(factorValues);
        List<String> affinityValuesCopy = new ArrayList<String>(affinityValues);
        factorValuesCopy.removeAll(affinityValues);
        affinityValuesCopy.removeAll(factorValues);

        Collections.sort(factorValuesCopy);
        Collections.sort(affinityValuesCopy);
        if (factorValuesCopy.size() != 0) {
            //System.err.println(tableId+"-"+f.toString() + " factorValuesRemain:" + factorValuesCopy);
        }
        if (affinityValuesCopy.size() != 0) {
            System.err.println(tableId + "-" + f.toString() + " affinityValuesRemain:" + affinityValuesCopy);
        }
    }

    public static void checkGraph(FactorGraph graph, String tableId) throws FileNotFoundException {
        PrintWriter p = new PrintWriter("graph.txt");
        graph.dump(p);
        p.close();

        boolean checkSuccess = true;
        Map<Variable, Set<Integer>> variableNonzerooutcome_collectedFromFactor = new HashMap<Variable, Set<Integer>>();
        Iterator it = graph.factorsIterator();
        while (it.hasNext()) {
            Factor f = (Factor) it.next();
            AssignmentIterator ait = f.assignmentIterator();
            while (ait.hasNext()) {
                Assignment assignment = ait.assignment();
                //assignment.numVariables();
                String[] splits = assignment.dumpToString().trim().split("[\r\n]");
                String indexString = splits[splits.length - 1].trim();
                String[] indexes = indexString.split("\\s+");

                double score = f.value(assignment);
                if (score > 0) {
                    for (int v = 0; v < assignment.numVariables(); v++) {
                        Variable var = assignment.getVariable(v);

                        int idx = 0;
                        try {
                            idx = Integer.valueOf(indexes[v]);
                        } catch (NumberFormatException e) {
                            System.err.println(assignment.dumpToString() + ">>>>" + indexes[v]);
                            System.exit(1);
                        }
                        String label = var.getLabelAlphabet().lookupLabel(idx).toString();

                        Set<Integer> nonzerooutcomes = variableNonzerooutcome_collectedFromFactor.get(var);
                        if (nonzerooutcomes == null)
                            nonzerooutcomes = new HashSet<Integer>();
                        nonzerooutcomes.add(idx);
                        variableNonzerooutcome_collectedFromFactor.put(var, nonzerooutcomes);
                    }

                }

                ait.next();
            }
        }

        //compare
        for (int i = 0; i < graph.numVariables(); i++) {
            Variable v = graph.get(i);
            Set<Integer> allOutcomesOfVariable = new HashSet<Integer>();
            for (int j = 0; j < v.getNumOutcomes(); j++)
                allOutcomesOfVariable.add(j);

            try {
                Set<Integer> nonzerooutcomes = variableNonzerooutcome_collectedFromFactor.get(v);
                allOutcomesOfVariable.removeAll(nonzerooutcomes);
            }catch(NullPointerException npe){
                System.err.println(">>>FUCK nullpointer "+tableId);
                System.exit(1);
            }
            if (allOutcomesOfVariable.size() > 0) {
                List<Integer> missing = new ArrayList<Integer>(allOutcomesOfVariable);
                Collections.sort(missing);

                checkSuccess = false;
                for (int m : missing) {
                    System.err.println("MISSING:" + v.getLabel() + "\t" + v.getLabelAlphabet().lookupLabel(m) + " in " + tableId);
                }
            }
        }
        /*List<String> keys = new ArrayList<String>(varOutcomeHasNonZeroPotential.keySet());
        Collections.sort(keys);
        for (String k : keys) {
            if (!varOutcomeHasNonZeroPotential.get(k))
                System.out.println("\tmissed: " + k);
        }*/
    }
}
