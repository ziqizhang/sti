package uk.ac.shef.dcs.oak.lodie.table.validator;

import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 08/10/12
 * Time: 13:10
 */
public interface TableValidator {

    boolean validate(LTable table);
}
