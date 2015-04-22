package uk.ac.shef.dcs.oak.lodie.table.interpreter.selector;

import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

import java.util.List;

/**
 */
public abstract class RowSelector {

    //return ranking of indexes of objects in the passed list object
    public abstract int[] select(LTable table);

}
