package uk.ac.shef.dcs.sti.algorithm.tm;

import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.rep.TAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 19/02/14
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public abstract class DataLiteralColumnClassifier {
    public abstract void interpret(Table table, TAnnotation annotations, Integer... en_columns) throws KBSearchException;
}
