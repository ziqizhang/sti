package uk.ac.shef.dcs.websearch;

import java.io.InputStream;
import java.util.Properties;

/**
 * Web search interface. In addition to implementing the abstract methods, potential implementors
 * are obliged to provide a single-parameter constructor accepting a {@link Properties} that contain
 * all the declared keys.
 * 
 * @author Ziqi Zhang
 */
public abstract class WebSearch {

  /**
   * Class name property key.
   */
  public static final String WEB_SEARCH_CLASS = "web.search.class";

  /**
   * Enacts the search.
   * 
   * @param string search query
   * @return input stream with the result
   * @throws Exception when something goes awry during the search
   */
  public abstract InputStream search(String string) throws Exception;

  /**
   * @return instance capable of parsing the result of {@link #search(String)} method
   */
  public abstract SearchResultParser getResultParser();
}
