package uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler;

import uk.ac.shef.dcs.sti.core.model.Table;

/**
 */
public abstract class TContentRowRanker {

    //return ranking of indexes of objects in the passed list object
    public abstract int[] select(Table table);

}
