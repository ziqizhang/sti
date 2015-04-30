package uk.ac.shef.dcs.oak.sti.test;

import cc.mallet.grmm.inference.Inferencer;
import cc.mallet.grmm.inference.JunctionTreeInferencer;
import cc.mallet.grmm.types.*;

import java.util.Random;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 24/09/12
 * Time: 11:12
 */
public class TestGRMM {

    public static void main(String[] args) {
        FactorGraph mdl = new FactorGraph();
        Variable[] vars = new Variable[]{
                new Variable(2),
                new Variable(2),
                new Variable(3),
                new Variable(2),
                new Variable(2),
        };

        /* Create an edge potential looking like
            VARS[0]   VARS[1]    VALUE
               0         0        0.6
               0         1        1.3
               1         0        0.3
               1         1        2.3
        */
        double[] arr = new double[]{0.6, 1.3, 0.3, 2.3,};
        mdl.addFactor(vars[0], vars[1], arr);
        System.out.println("Model with one edge potential:");
        mdl.dump();

        /* Add a three-clique potential whose values are
     VARS[2]   VARS[3]  VARS[4]    VALUE
        0         0        0         1
        0         0        1         2
        0         1        0         3
        0         1        1         4
        1         0        0        11
        1         0        1        12
        1         1        0        13
        1         1        1        14
        2         0        0        21
        2         0        1        22
        2         1        0        23
        2         1        1        24
        */
        double[] arr2 = {1, 2, 3, 4, 11, 12, 13, 14, 21, 22, 23, 24};
        VarSet varSet = new HashVarSet(new Variable[]{vars[2], vars[3], vars[4]});
        Factor ptl = new TableFactor(varSet, arr2);
        mdl.addFactor(ptl);

        System.out.println("Model with a 3-clique added:");
        mdl.dump();


        /*Variable[] allVars = {
                new Variable(2),
                new Variable(2),
                new Variable(2),
                new Variable(2)
        };

        FactorGraph mdl = new FactorGraph(allVars);

        // Create a diamond graph, with random potentials
        Random r = new Random(42);
        for (int i = 0; i < allVars.length; i++) {
            double[] ptlarr = new double[4];
            for (int j = 0; j < ptlarr.length; j++)
                ptlarr[j] = Math.abs(r.nextDouble());

            Variable v1 = allVars[i];
            Variable v2 = allVars[(i + 1) % allVars.length];
            mdl.addFactor(v1, v2, ptlarr);
        }
        Inferencer inf = new JunctionTreeInferencer();
        inf.computeMarginals(mdl);


        for (int varnum = 0; varnum < allVars.length; varnum++) {
            Variable var = allVars[varnum];
            Factor ptl = inf.lookupMarginal(var);
            for (AssignmentIterator it = ptl.assignmentIterator(); it.hasNext(); ) {
                int outcome = it.indexOfCurrentAssn();
                System.out.println(var + "  " + outcome + "   " + ptl.value(it));
            }
            System.out.println();
        }

        System.out.println("end");*/
    }
}
