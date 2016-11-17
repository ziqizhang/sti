package uk.ac.shef.dcs.kbproxy.sparql;

import uk.ac.shef.dcs.kbproxy.KBSearchResultFilter;

import java.io.IOException;

/**
 *
 */
public class DBpediaSearchResultFilter extends KBSearchResultFilter {
    public DBpediaSearchResultFilter(String property) throws IOException {
        super(property);
    }
}
