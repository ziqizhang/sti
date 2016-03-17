package uk.ac.shef.dcs.sti.algorithm.tm.sampler;

import uk.ac.shef.dcs.sti.rep.Table;

/**
 */
public abstract class TContentRowRanker {

    //return ranking of indexes of objects in the passed list object
    public abstract int[] select(Table table);

}
