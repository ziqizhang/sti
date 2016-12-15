package uk.ac.shef.dcs.websearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Search result parser coupled with a certain {@link WebSearch} implementation.
 * 
 * @author Ziqi Zhang
 */
public abstract class SearchResultParser {
  /**
   * Parses a search result.
   * 
   * @param resultStream input stream containing a {@link WebSearch} result
   * @return list of results
   * @throws IOException when I/O error happens during parsing
   */
  public abstract List<WebSearchResultDoc> parse(InputStream resultStream) throws IOException;
}
