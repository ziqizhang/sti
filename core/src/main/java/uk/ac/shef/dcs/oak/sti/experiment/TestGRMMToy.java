package uk.ac.shef.dcs.oak.sti.experiment;

import cc.mallet.grmm.inference.Inferencer;
import cc.mallet.grmm.inference.JunctionTreeInferencer;
import cc.mallet.grmm.inference.LoopyBP;
import cc.mallet.grmm.inference.ResidualBP;
import cc.mallet.grmm.types.*;
import cc.mallet.types.LabelAlphabet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 26/09/12
 * Time: 11:42
 */
public class TestGRMMToy {

    /*public static void main (String[] args)
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

        *//*Variable[] allVars = {
                new Variable (2),
                new Variable (2),
                new Variable (2),
                new Variable (2)
        };*//*

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

    }*/

    public static void main(String[] args) {
        //the order that variables are created are critical as
        //VarSet sorts variables according to their index, which seems to be the
        //order they are created
        Variable var0 = new Variable(3);
        Variable var1 = new Variable(2);
        Variable var2 = new Variable(2);

        double[] compatibility = {1, 2, 3, 4, 11, 12, 13, 14, 21, 22, 23, 24};
        //create a Variable array that takes the three variables in the order they are created
        Variable[] varArray_defaultOrder = new Variable[]{var0, var1, var2};
        VarSet varSet_defaultOrder = new HashVarSet(varArray_defaultOrder);

      /* The above "scores" and the Variable array represents a three-clique potential look like this:
      VARS[0]   VARS[1]  VARS[2]    VALUE
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


        //NOTE THAT we swap var2 and var1 in the variable array, but using the same
        //"compatibility", I thought we create a potential that look like this:
        Variable[] varArray_customOrder = new Variable[]{var1, var0, var2};
        VarSet varSet_customOrder = new HashVarSet(varArray_customOrder);
        /*
        VARS[1]   VARS[0]  VARS[2]    VALUE
         0         0        0         1
         0         0        1         2
         0         1        0         3
         0         1        1         4
         0         2        0        11
         0         2        1        12
         1         0        0        13
         1         0        1        14
         1         1        0        21
         1         1        1        22
         1         2        0        23
         1         2        1        24
         */


        //whether my expectation is wrong or not, I was expecting that the two
        //VarSet will have the same variables but DIFFERENT orders. However
        //as you can see the output is the same. Because HashVarSet re-orders
        //elements in the Variable[] array by their order of instantiation
        System.out.println(varSet_defaultOrder);
        System.out.println(varSet_customOrder);


        //If we use "varSet_defaultOrder" with "compatibility" to create a factor,
        //we do get the potential as illustrated above (from line 108)
        System.out.println("<<< correct factor as expected >>>");
        Factor factor_corret = new TableFactor(varSet_defaultOrder, compatibility);
        AssignmentIterator it = factor_corret.assignmentIterator();
        while (it.hasNext()) {
            Assignment assignment = it.assignment();
            //assignment.numVariables();
            String[] splits = assignment.dumpToString().trim().split("\r");
            String indexString = splits[splits.length - 1].trim();
            String[] indexes = indexString.split("\\s+");

            double score = factor_corret.value(assignment);
            String line="";
            for (int v = 0; v < assignment.numVariables(); v++) {
                Variable var = assignment.getVariable(v);
                int idx = Integer.valueOf(indexes[v]);
                String label = var.getLabelAlphabet().lookupLabel(idx).toString();
                line=line+var.getLabel()+"="+label+"\t";
            }
            line=line+score;
            System.out.println(line);
            it.next();
        }


        //HOWEVER, If we use "varSet_customeOrder" with "compatibility" to create a factor,
        //we DO NOT get the potential as illustrated above (from line 129). For example
        //you can notice the 3rd line of the printout is not what we want in the illustration
        System.out.println("\n\n<<< wrong factor unexpected >>>");
        Factor factor_wrong = new TableFactor(varSet_customOrder, compatibility);
        AssignmentIterator wit = factor_wrong.assignmentIterator();
        while (wit.hasNext()) {
            Assignment assignment = wit.assignment();
            //assignment.numVariables();
            String[] splits = assignment.dumpToString().trim().split("\r");
            String indexString = splits[splits.length - 1].trim();
            String[] indexes = indexString.split("\\s+");

            double score = factor_corret.value(assignment);
            String line="";
            for (int v = 0; v < assignment.numVariables(); v++) {
                Variable var = assignment.getVariable(v);
                int idx = Integer.valueOf(indexes[v]);
                String label = var.getLabelAlphabet().lookupLabel(idx).toString();
                line=line+var.getLabel()+"="+label+"\t";
            }
            line=line+score;
            System.out.println(line);
            wit.next();
        }

    }
}

