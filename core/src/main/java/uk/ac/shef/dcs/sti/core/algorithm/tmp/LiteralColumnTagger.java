package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 */
public interface LiteralColumnTagger {
    void annotate(Table table, TAnnotation annotations, Integer... enColumnIndexes) throws KBSearchException;
    
    void setIgnoreColumns(int... ignoreCols);
}
