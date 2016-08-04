package uk.ac.shef.dcs.sti.parser.table.creator;

import cern.colt.matrix.ObjectMatrix2D;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.model.TContext;

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
    Table create(ObjectMatrix2D preTable, String tableId, String sourceId, TContext... contexts);
}
