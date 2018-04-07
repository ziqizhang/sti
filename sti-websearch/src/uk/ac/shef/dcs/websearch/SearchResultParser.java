package uk.ac.shef.dcs.websearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by - on 17/03/2016.
 */
public abstract class SearchResultParser {
    public abstract  List<WebSearchResultDoc> parse(InputStream is) throws IOException;
}
