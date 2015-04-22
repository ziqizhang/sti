package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.filter;

import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 14/03/13
 * Time: 12:16
 *
 * only those columns reolsved in the groud truth are kept.
 *
 * todo
 */
public class FilterPolicyGroudTruth implements FilterPolicy {
    @Override
    public boolean discard(LTable table, int row, int column) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
