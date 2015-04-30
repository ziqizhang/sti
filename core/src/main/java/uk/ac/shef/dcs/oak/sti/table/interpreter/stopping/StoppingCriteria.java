package uk.ac.shef.dcs.oak.sti.table.interpreter.stopping;

import java.util.Map;

/**
 */
public abstract class StoppingCriteria {

    public abstract boolean stop(Map<Object, Double> state, int max);

}
