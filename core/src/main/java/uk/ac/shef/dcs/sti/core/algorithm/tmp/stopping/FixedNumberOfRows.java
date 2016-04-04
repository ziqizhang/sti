package uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping;

import java.util.Map;

/**
 */
public class FixedNumberOfRows extends StoppingCriteria {

    private int stop_at_row_counter=0;
    private int current_iteration;

    public FixedNumberOfRows(int rows){
        stop_at_row_counter=rows;
    }

    @Override
    public boolean stop(Map<Object, Double> state, int max) {
        current_iteration++;

        if (current_iteration < stop_at_row_counter)
            return false;

        return true;
    }
}
