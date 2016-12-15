package uk.ac.shef.dcs.websearch.bing.v5;

import uk.ac.shef.dcs.websearch.SearchResultParser;
import uk.ac.shef.dcs.websearch.WebSearchResultDoc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

/**
 * A {@link SearchResultParser} implementation parsing result from the revamped Bing Search API.
 * 
 * @author Václav Brodec
 */
public final class BingSearchResultParser extends SearchResultParser {

  /**
   * Microsoft Azure Bing Search API result (partial implementation, covering only the needed
   * attributes).
   * 
   * @see <a href="https://msdn.microsoft.com/en-us/library/dn760794.aspx#searchresponse">object
   *      specification</a>
   */
  private static final class SearchResponse {
    private WebAnswer webPages;
  }

  private static final class WebAnswer {
    private WebPage[] value;
  }

  private static final class WebPage {
    private String name;
    private String snippet;
    private String url;
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.shef.dcs.websearch.SearchResultParser#parse(java.io.InputStream)
   */
  public List<WebSearchResultDoc> parse(final InputStream resultStream) throws IOException {
    Preconditions.checkNotNull(resultStream);

    final Gson gson = new Gson();
    final SearchResponse searchResponse =
        gson.fromJson(new InputStreamReader(resultStream), SearchResponse.class);

    final List<WebSearchResultDoc> result = new ArrayList<>();

    final WebAnswer webAnswer = searchResponse.webPages;
    for (final WebPage webPage : webAnswer.value) {
      result.add(
          /*
           * Version 2 ID was removed (the version 5 ID stands for something different, so I use url
           * as the ID instead.
           */
          new WebSearchResultDoc(webPage.url, webPage.name, webPage.snippet, webPage.url));
    }

    return result;
  }
}
