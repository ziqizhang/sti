package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 */
public interface LiteralColumnTagger {
    void annotate(Table table, TAnnotation annotations, Integer... enColumnIndexes) throws KBProxyException;
    
    void setIgnoreColumns(int... ignoreCols);
}
