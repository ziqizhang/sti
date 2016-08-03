package uk.ac.shef.dcs.kbsearch.sparql;

import uk.ac.shef.dcs.kbsearch.KBSearchResultFilter;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseEnum;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class DBpediaSearchResultFilter extends KBSearchResultFilter {
    public DBpediaSearchResultFilter(String property) throws IOException {
        super(property);
    }
}
