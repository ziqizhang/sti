package uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler;

import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 20/03/14
 * Time: 12:06
 * To change this template use File | Settings | File Templates.
 */
public abstract class TContentCellRanker {
    public abstract List<List<Integer>> select(Table table, int fromCol, int subCol);
}
