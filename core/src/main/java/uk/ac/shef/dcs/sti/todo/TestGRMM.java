package uk.ac.shef.dcs.sti.todo;

import cc.mallet.grmm.inference.Inferencer;
import cc.mallet.grmm.inference.LoopyBP;
import cc.mallet.grmm.types.*;
import cc.mallet.types.LabelAlphabet;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 26/09/12
 * Time: 11:42
 */
public class TestGRMM {

    /*
               Table

                  f                             f
                  |                             |
              (v)City      --f--  R  --f--     (v)Mayor
                 |                             |
                f                              f
                |                              |
         f --(v)sheffield --f--  R   --f-- (v)personA--  f



         f --(v)new york  --f-- R   --f--  (v)personB--  f

     */
    public static void main(String[] args) {
        FactorGraph mdl = new FactorGraph();

        LabelAlphabet header_city = new LabelAlphabet();
        //two possible values
        header_city.lookupIndex("dbpedia-owl:City");
        header_city.lookupIndex("dbpedia-owl:Citi_group");
        //create a variable that takes the two possible values
        Variable var_header_city = new Variable(header_city);    //0
        /*prior prob of seeing each value of varHeaderCity in the KB*/
        double[] potential1 = new double[]{0, 0};  //meaning prob of seeing the first value (city) is 0.65; prob of the 2nd (citi_group) is0.35
        TableFactor f1 = new TableFactor(var_header_city, potential1);
        mdl.addFactor(f1);

        LabelAlphabet header_mayor = new LabelAlphabet();
        header_mayor.lookupIndex("dbpedia-owl:Mayor");
        header_mayor.lookupIndex("dbpedia-owl:Mayor_Surname");
        Variable var_header_mayor = new Variable(header_mayor);   //1
        double[] potential2 = new double[]{1.75, 0};
        TableFactor f2 = new TableFactor(var_header_mayor, potential2);
        mdl.addFactor(f2);

        LabelAlphabet cell_city1 = new LabelAlphabet();
        cell_city1.lookupIndex("dbpedia-owl:shefielduk");
        cell_city1.lookupIndex("dbpedia-owl:shefieldus");
        cell_city1.lookupIndex("dbpedia-owl:shefieldukaustralia");
        Variable var_cell_city1 = new Variable(cell_city1);         //2
        double[] potential3 = new double[]{0.5, 2.15, 0.35};
        TableFactor f3 = new TableFactor(var_cell_city1, potential3);
        mdl.addFactor(f3);

        LabelAlphabet cell_City2 = new LabelAlphabet();
        cell_City2.lookupIndex("dbpedia-owl:newyorkUS");
        cell_City2.lookupIndex("dbpedia-owl:newyorkNewsPaper");
        cell_City2.lookupIndex("dbpedia-owl:newyorkMovie");
        Variable var_cell_city2 = new Variable(cell_City2);            //3
        double[] potential4 = new double[]{0.8, 1.05, 0.15};
        TableFactor f4 = new TableFactor(var_cell_city2, potential4);
        mdl.addFactor(f4);

        LabelAlphabet cell_mayor1 = new LabelAlphabet();
        cell_mayor1.lookupIndex("dbpedia-owl:personA_1");
        cell_mayor1.lookupIndex("dbpedia-owl:personA_2");
        Variable var_cell_mayor1 = new Variable(cell_mayor1);           //4
        double[] potential5 = new double[]{1.5, 1.5};
        TableFactor f5 = new TableFactor(var_cell_mayor1, potential5);
         mdl.addFactor(f5);

        LabelAlphabet cell_mayor2 = new LabelAlphabet();
        cell_mayor2.lookupIndex("dbpedia-owl:personB_1");
        cell_mayor2.lookupIndex("dbpedia-owl:personB_2");
        cell_mayor2.lookupIndex("dbpedia-owl:personB_3");
        cell_mayor2.lookupIndex("dbpedia-owl:personB_4");
        Variable var_cell_mayor2 = new Variable(cell_mayor2);            //5
        double[] potential6 = new double[]{2.3, 2.25, 2.25, 1.2};
        TableFactor f6 = new TableFactor(var_cell_mayor2, potential6);
        mdl.addFactor(f6);

        LabelAlphabet relation = new LabelAlphabet();
        relation.lookupIndex("dbpp:manage");
        relation.lookupIndex("dbpp:liveIn");
        relation.lookupIndex("dbpp:hometown");
        relation.lookupIndex("dbpp:famousPerson");
        Variable var_relation = new Variable(relation);            //5
        double[] potential_headerCity_relation = new double[]{3, 0, 1, 0, 2, 0,1,0};
        VarSet varSet17 = new HashVarSet(new Variable[]{var_header_city, var_relation});
        TableFactor f17 = new TableFactor(varSet17, potential_headerCity_relation);
        mdl.addFactor(f17);

        double[] potential_headerMayor_relation = new double[]{2, 0, 1, 0, 1, 0,0,0};
        VarSet varSet27 = new HashVarSet(new Variable[]{var_header_mayor, var_relation});
        TableFactor f27 = new TableFactor(varSet27, potential_headerMayor_relation);
        mdl.addFactor(f27);

        double[] potential_headerCity_cellCity1 = new double[]{0.9, 0.85, 0.91, 0.01, 0.005, 0.11};
        VarSet varSet13 = new HashVarSet(new Variable[]{var_header_city, var_cell_city1});
        TableFactor f13 = new TableFactor(varSet13, potential_headerCity_cellCity1);
        mdl.addFactor(f13);

        double[] potential_headerCity_cellCity2 = new double[]{0.9, 0.12, 0.23, 0.56, 0.775, 0.01};
        VarSet varSet14 = new HashVarSet(new Variable[]{var_header_city, var_cell_city2});
        TableFactor f14 = new TableFactor(varSet14, potential_headerCity_cellCity2);
        mdl.addFactor(f14);

        double[] potential_headerMayor_cellMayor1 = new double[]{0.35, 0.77, 0.55, 0.1};
        VarSet varSet25 = new HashVarSet(new Variable[]{var_header_mayor, var_cell_mayor1});
        TableFactor f25 = new TableFactor(varSet25, potential_headerMayor_cellMayor1);
        mdl.addFactor(f25);

        double[] potential_headerMayor_cellMayor2 = new double[]{0.8, 0.25, 0.7, 0.01, 0.001, 0.002, 0.32, 0.01};
        VarSet varSet26 = new HashVarSet(new Variable[]{var_header_mayor, var_cell_mayor2});
        TableFactor f26 = new TableFactor(varSet26, potential_headerMayor_cellMayor2);
        mdl.addFactor(f26);

        mdl.dump();

        Inferencer infLoopyBP = new LoopyBP();
        //Inferencer infResidualBP = new ResidualBP();

        infLoopyBP.computeMarginals(mdl);
       // infResidualBP.computeMarginals(mdl);

        for(int i=0; i<mdl.numVariables(); i++){
            Variable var = mdl.get(i);

            Factor ptl  =infLoopyBP.lookupMarginal(var);


            AssignmentIterator it = ptl.assignmentIterator ();
            while(it.hasNext()){
                int outcome = it.indexOfCurrentAssn ();
                System.out.println (var+"  "+outcome+"   "+ptl.value (it));

                it.next();
            }
            System.out.println ();
        }

    }
}
