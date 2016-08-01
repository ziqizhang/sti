package uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping;

import java.util.Map;

/**
 */
public abstract class StoppingCriteria {

    public abstract boolean stop(Map<Object, Double> state, int max);

}
