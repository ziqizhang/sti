package uk.ac.shef.dcs.websearch;

import java.io.Serializable;

import com.google.common.base.Preconditions;

/**
 * Encapsulates a result obtained from a web search engine.
 * 
 * @author Ziqi Zhang
 * @author Václav Brodec
 */
public final class WebSearchResultDoc implements Serializable {

  private static final long serialVersionUID = 7919024447245010089L;

  private String id;
  private String title;
  private String description;
  private String url;

  /**
   * Creates a new web search result document.
   * 
   * @param id document ID (may not be unique across engines)
   * @param title document title
   * @param description document description
   * @param url document URL
   */
  public WebSearchResultDoc(String id, String title, String description, String url) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(title);
    Preconditions.checkNotNull(description);
    Preconditions.checkNotNull(url);

    this.id = id;
    this.title = title;
    this.description = description;
    this.url = url;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getUrl() {
    return url;
  }

  public String getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "WebSearchResultDoc [id=" + id + ", title=" + title + ", description=" + description
        + ", url=" + url + "]";
  }
}
