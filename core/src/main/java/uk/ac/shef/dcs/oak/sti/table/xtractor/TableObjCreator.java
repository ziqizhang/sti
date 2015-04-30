package uk.ac.shef.dcs.oak.sti.table.xtractor;

import cern.colt.matrix.ObjectMatrix2D;
import uk.ac.shef.dcs.oak.sti.table.rep.LTable;
import uk.ac.shef.dcs.oak.sti.table.rep.LTableContext;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 05/10/12
 * Time: 15:40
 *
 * Takes the result from TableHODector and parses it into a Table object
 *
 * Must deal with at least:
 */
public interface TableObjCreator {
    LTable create(ObjectMatrix2D preTable, String tableId, String sourceId, LTableContext... contexts);
}
