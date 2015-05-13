package uk.ac.shef.dcs.oak.sti.experiment;

import cc.mallet.grmm.inference.Inferencer;
import cc.mallet.grmm.inference.JunctionTreeInferencer;
import cc.mallet.grmm.inference.LoopyBP;
import cc.mallet.grmm.inference.ResidualBP;
import cc.mallet.grmm.types.*;
import cc.mallet.types.LabelAlphabet;

import java.util.Random;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 26/09/12
 * Time: 11:42
 */
public class TestGRMMToy {

    public static void main (String[] args)
    {

        // STEP 1: Create the graph

        Variable[] allVars = new Variable[100];
        for(int i=0; i<allVars.length; i++){
            int it =new Random().nextInt(100);
            if(it<0)
                it=0-it;
            if(it==0)
                    it=10;
            allVars[i] = new Variable(it);
        }


        // STEP 1: Create the graph

        /*Variable[] allVars = {
                new Variable (2),
                new Variable (2),
                new Variable (2),
                new Variable (2)
        };*/

        FactorGraph mdl = new FactorGraph (allVars);

        // Create a diamond graph, with random potentials
        Random r = new Random (42);
        for (int i = 0; i < allVars.length; i++) {
            Variable v1 = allVars[i];
            Variable v2 = allVars[(i + 1) % allVars.length];

            double[] ptlarr=null;
                    try {
                        ptlarr = new double[v1.getNumOutcomes() * v2.getNumOutcomes()];
                    }catch(Exception e){
                        e.printStackTrace();
                    }
            for (int j = 0; j < ptlarr.length; j++) {
                if (i % 2 == 0)
                    ptlarr[j] = Math.abs(r.nextInt());
                else
                    ptlarr[j] = Math.abs(r.nextDouble());
            }

            mdl.addFactor (v1, v2, ptlarr);
        }

        mdl.dump();

        // STEP 2: Compute marginals

        Inferencer inf = new LoopyBP ();
        inf.computeMarginals (mdl);

        // STEP 3: Collect the results
        //   We'll just print them out

        for (int varnum = 0; varnum < allVars.length; varnum++) {
            Variable var = allVars[varnum];
            Factor ptl = inf.lookupMarginal (var);
            for (AssignmentIterator it = ptl.assignmentIterator (); it.hasNext ();) {
                int outcome = it.indexOfCurrentAssn ();
                System.out.println (var+"  "+outcome+"   "+ptl.value (it));
                it.next();
            }
            System.out.println ();
        }

    }
}
