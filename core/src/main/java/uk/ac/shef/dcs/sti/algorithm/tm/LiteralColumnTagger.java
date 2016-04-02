package uk.ac.shef.dcs.sti.algorithm.tm;

import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.rep.TAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;

import java.io.IOException;

/**
 */
public interface LiteralColumnTagger {
    void annotate(Table table, TAnnotation annotations, Integer... enColumnIndexes) throws KBSearchException;
}
