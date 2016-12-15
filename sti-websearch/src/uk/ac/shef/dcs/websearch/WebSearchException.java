package uk.ac.shef.dcs.websearch;

/**
 * An exception that happened during web search or its commencement.
 * 
 * @author Ziqi Zhang
 */
public class WebSearchException extends Exception {

  private static final long serialVersionUID = 3542024823438925198L;

  public WebSearchException(Exception e) {
    super(e);
  }

  public WebSearchException(String e) {
    super(e);
  }
}
