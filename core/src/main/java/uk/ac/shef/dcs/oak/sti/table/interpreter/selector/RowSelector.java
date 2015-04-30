package uk.ac.shef.dcs.oak.sti.table.interpreter.selector;

import uk.ac.shef.dcs.oak.sti.table.rep.LTable;

/**
 */
public abstract class RowSelector {

    //return ranking of indexes of objects in the passed list object
    public abstract int[] select(LTable table);

}
