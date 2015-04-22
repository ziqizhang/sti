package uk.ac.shef.dcs.oak.lodie.table.interpreter.stopping;

//import nl.peterbloem.powerlaws.Continuous;

import java.util.Map;

/**
 * TODO THIS CLASS IS NOT WORKING
 */
public class LongtailPatternConfirmation extends StoppingCriteria {
    private double minimum_exponent;
    private int minimum_data_elements;

    //minimum #
    public LongtailPatternConfirmation(double minimum_exponent, int minimum_data_elements) {
        this.minimum_exponent=minimum_exponent;
        this.minimum_data_elements=minimum_data_elements;
    }

    @Override
    public boolean stop(Map<Object, Double> state,int max) {
        if(state.size()<minimum_data_elements)
            return false;

       /* Continuous distribution = Continuous.fit(state.values()).fit();
        double exponent = distribution.exponent();
        double xMin = distribution.xMin();

        if(minimum_exponent!=0 && exponent<minimum_exponent)
            return false;

        double significance = distribution.significance(state.values(), 100);   //potentially expensive computation
        if(significance>0.01)
            return true;*/

        return false;
    }
}
