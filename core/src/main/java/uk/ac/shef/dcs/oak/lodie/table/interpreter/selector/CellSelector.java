package uk.ac.shef.dcs.oak.lodie.table.interpreter.selector;

import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 20/03/14
 * Time: 12:06
 * To change this template use File | Settings | File Templates.
 */
public abstract class CellSelector {
    public abstract List<List<Integer>> select(LTable table, int fromCol, int subCol);
}
