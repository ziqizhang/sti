package uk.ac.shef.dcs.oak.sti.table.interpreter.interpret;

import uk.ac.shef.dcs.oak.sti.table.rep.LTable;
import uk.ac.shef.dcs.oak.sti.table.rep.LTableAnnotation;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 19/02/14
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public abstract class ColumnInterpreter_relDepend {
    public abstract void interpret(LTable table, LTableAnnotation annotations, Integer... en_columns) throws IOException;
}
