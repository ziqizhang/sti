package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.filter;

import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 11/03/13
 * Time: 13:28
 */
public interface FilterPolicy {

    /**
     *
     * @param table
     * @param row
     * @param column
     * @return true if the corresponding cell in row and column should not be interpreted. when row=-1 or column =-1, it means
     * the entire column/row respectively. So each implementation must decide its own policy on "row=-1" or "column=-1"
     *
     */
    boolean discard(LTable table, int row, int column);
}
