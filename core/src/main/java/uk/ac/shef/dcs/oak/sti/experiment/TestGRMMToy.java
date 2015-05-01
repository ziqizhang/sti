package uk.ac.shef.dcs.oak.sti.experiment;

import cc.mallet.grmm.inference.Inferencer;
import cc.mallet.grmm.inference.LoopyBP;
import cc.mallet.grmm.inference.ResidualBP;
import cc.mallet.grmm.types.*;
import cc.mallet.types.LabelAlphabet;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 26/09/12
 * Time: 11:42
 */
public class TestGRMMToy {

    /*
               Table

                 f1                      f2
                  |                       |
              (v)City      --f12--       (v)Mayor
                  |                       |
                 f13, f14                f25,f26
                  |                       |
         f3 --(v)sheffield --f35--       (v)personA--  f5



         f4 --(v)new york  --f46--       (v)personB--  f6

     */
    public static void main(String[] args) {
        LabelAlphabet headerCity = new LabelAlphabet();
        //two possible values
        headerCity.lookupIndex("dbpedia-owl:City");
        headerCity.lookupIndex("dbpedia-owl:Citi_group");
        //create a variable that takes the two possible values
        Variable varHeaderCity = new Variable(headerCity);
        varHeaderCity.setLabel("TableHeader_City");

        LabelAlphabet headerMayor = new LabelAlphabet();
        headerMayor.lookupIndex("dbpedia-owl:Mayor");
        headerMayor.lookupIndex("dbpedia-owl:Mayor_Surname");
        Variable varHeaderMayor = new Variable(headerMayor);
        varHeaderMayor.setLabel("TableHeader_Mayor");


        LabelAlphabet cellCity1 = new LabelAlphabet();
        cellCity1.lookupIndex("dbpedia-owl:shefielduk");
        cellCity1.lookupIndex("dbpedia-owl:shefieldus");
        cellCity1.lookupIndex("dbpedia-owl:shefieldukaustralia");
        Variable varCellCity1 = new Variable(cellCity1);
        varCellCity1.setLabel("TableCellCity1");

        LabelAlphabet cellCity2 = new LabelAlphabet();
        cellCity2.lookupIndex("dbpedia-owl:newyorkUS");
        cellCity2.lookupIndex("dbpedia-owl:newyorkNewsPaper");
        cellCity2.lookupIndex("dbpedia-owl:newyorkMovie");
        Variable varCellCity2 = new Variable(cellCity2);
        varCellCity2.setLabel("TableCellCity2");

        LabelAlphabet cellMayor1 = new LabelAlphabet();
        cellMayor1.lookupIndex("dbpedia-owl:personA_1");
        cellMayor1.lookupIndex("dbpedia-owl:personA_2");
        Variable varCellMayor1 = new Variable(cellMayor1);
        varCellMayor1.setLabel("TableCellMayor1");

        LabelAlphabet cellMayor2 = new LabelAlphabet();
        cellMayor2.lookupIndex("dbpedia-owl:personB_1");
        cellMayor2.lookupIndex("dbpedia-owl:personB_2");
        cellMayor2.lookupIndex("dbpedia-owl:personB_3");
        cellMayor2.lookupIndex("dbpedia-owl:personB_4");
        Variable varCellMayor2 = new Variable(cellMayor2);
        varCellMayor2.setLabel("TableCellMayor2");

        /*
                  Table

                    f1                      f2
                     |                       |
                 (v)City      --f12--       (v)Mayor
                     |                       |
                    f13, f14                f25,f26
                     |                       |
            f3 --(v)sheffield --f35--       (v)personA--  f5



            f4 --(v)new york  --f46--       (v)personB--  f6

        */
        /*prior prob of seeing each value of varHeaderCity in the KB*/
        double[] potential1 = new double[]{0.65, 0.35};  //meaning prob of seeing the first value (city) is 0.65; prob of the 2nd (citi_group) is0.35
        TableFactor f1 = new TableFactor(varHeaderCity, potential1);

        double[] potential2 = new double[]{0.75, 0.25};
        TableFactor f2 = new TableFactor(varHeaderMayor, potential2);

        double[] potential3 = new double[]{0.5, 0.15, 0.35};
        TableFactor f3 = new TableFactor(varCellCity1, potential3);
        double[] potential4 = new double[]{0.8, 0.05, 0.15};
        TableFactor f4 = new TableFactor(varCellCity2, potential4);
        double[] potential5 = new double[]{0.5, 0.5};
        TableFactor f5 = new TableFactor(varCellMayor1, potential5);
        double[] potential6 = new double[]{0.3, 0.25, 0.25, 0.2};
        TableFactor f6 = new TableFactor(varCellMayor2, potential6);


        //influences between variables
        /*              v1                  v2
                      headerCity        headerMayor
        v1_1st,v2_1st    city               mayor             0.65
        v1_1st,v2_2nd    city               mayor_surname     0.1
        v1_2nd,v2_1st    citigroup          mayor             0.15
        v1_2nd,v2_2nd    citigroup          mayor_surname     0.1

         */
        double[] potential12 = {0.65, 0.1, 0.15, 0.1};
        VarSet varSet12 = new HashVarSet(new Variable[]{varHeaderCity, varHeaderMayor});
        TableFactor f12 = new TableFactor(varSet12, potential12);

        double[] potential13 = new double[]{0.9, 0.85, 0.91, 0.01, 0.005, 0.11};
        VarSet varSet13 = new HashVarSet(new Variable[]{varHeaderCity, varCellCity1});
        TableFactor f13 = new TableFactor(varSet13, potential13);

        double[] potential14 = new double[]{0.9, 0.12, 0.23, 0.56, 0.775, 0.01};
        VarSet varSet14 = new HashVarSet(new Variable[]{varHeaderCity, varCellCity2});
        TableFactor f14 = new TableFactor(varSet14, potential14);

        double[] potential25 = new double[]{0.35, 0.77, 0.55, 0.1};
        VarSet varSet25 = new HashVarSet(new Variable[]{varHeaderMayor, varCellMayor1});
        TableFactor f25 = new TableFactor(varSet25, potential25);

        double[] potential26 = new double[]{0.8, 0.25, 0.7, 0.01, 0.001, 0.002, 0.32, 0.01};
        VarSet varSet26 = new HashVarSet(new Variable[]{varHeaderMayor, varCellMayor2});
        TableFactor f26 = new TableFactor(varSet26, potential26);

        double[] potential35 = new double[]{0.45, 0.17, 0.35, 0.1, 0.7, 0.01};
        VarSet varSet35 = new HashVarSet(new Variable[]{varCellCity1, varCellMayor1});
        TableFactor f35 = new TableFactor(varSet35, potential35);

        double[] potential46 = new double[]{0.2, 0.01, 0.03, 0.1, 0.5, 0.01, 0.3, 0.02, 0.2, 0.2, 0.01, 0.29};
        VarSet varSet46 = new HashVarSet(new Variable[]{varCellCity2, varCellMayor2});
        TableFactor f46 = new TableFactor(varSet46, potential46);

        FactorGraph mdl = new FactorGraph();
        mdl.addFactor(f1);
        mdl.addFactor(f2);
        mdl.addFactor(f3);
        mdl.addFactor(f4);
        mdl.addFactor(f5);
        mdl.addFactor(f6);
        mdl.addFactor(f12);
        mdl.addFactor(f13);
        mdl.addFactor(f14);
        mdl.addFactor(f25);
        mdl.addFactor(f26);
        mdl.addFactor(f35);
        mdl.addFactor(f46);

        Inferencer infLoopyBP = new LoopyBP();
        Inferencer infResidualBP = new ResidualBP();

        infLoopyBP.computeMarginals(mdl);
        infResidualBP.computeMarginals(mdl);

        for(int i=0; i<mdl.numVariables(); i++){
            Variable var = mdl.get(i);

            Factor ptl  =infResidualBP.lookupMarginal(var);


            AssignmentIterator it = ptl.assignmentIterator ();
            while(it.hasNext()){
                int outcome = it.indexOfCurrentAssn ();
                System.out.println (var+"  "+outcome+"   "+ptl.value (it));

                it.next();
            }
            System.out.println ();
        }

        System.out.println();


    }
}
