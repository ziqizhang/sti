package uk.ac.shef.dcs.websearch.bing.v5;

import com.google.common.base.Preconditions;
import uk.ac.shef.dcs.websearch.SearchResultParser;
import uk.ac.shef.dcs.websearch.WebSearch;
import uk.ac.shef.dcs.websearch.WebSearchResultDoc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/**
 * Bing search class.
 * 
 * @author Ziqi Zhang
 * @author Václav Brodec
 */
public final class BingSearch extends WebSearch {

  public static final String BING_SUBSCRIPTION_KEY_PROPERTY_KEY = "bing.key";
  public static final String BING_BASE_URL_PROPERTY_KEY = "bing.url";

  private static final String SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";
  private static final int LIMIT = 20;
  private static final String KEY_PROPERTY_DESCRIPTION = "key";
  private static final String BASE_URL_PROPERTY_DESCRIPTION = "base URL";

  private final String subscriptionKey;
  private final URL baseURL;

  /**
   * Creates a search instance.
   * 
   * @param properties properties with the required keys
   * @throws MalformedURLException when the provided base URL is malformed
   */
  public BingSearch(Properties properties) throws MalformedURLException {
    this(readKeyProperty(properties), new URL(readBaseUrlProperty(properties)));
  }

  private static String readKeyProperty(Properties properties) {
    return readProperty(properties, BING_SUBSCRIPTION_KEY_PROPERTY_KEY, KEY_PROPERTY_DESCRIPTION);
  }

  private static String readBaseUrlProperty(Properties properties) {
    return readProperty(properties, BING_BASE_URL_PROPERTY_KEY, BASE_URL_PROPERTY_DESCRIPTION);
  }

  private static String readProperty(final Properties properties, final String propertyKey,
      final String description) {
    final String value = properties.getProperty(propertyKey);
    Preconditions.checkArgument(value != null, String.format("No %s property found!", description));

    return value;
  }

  public BingSearch(final String subscriptionKey, final URL baseUrl) {
    Preconditions.checkNotNull(subscriptionKey);
    Preconditions.checkNotNull(baseUrl);
    Preconditions.checkArgument(!subscriptionKey.isEmpty());

    this.subscriptionKey = subscriptionKey;
    this.baseURL = baseUrl;
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.shef.dcs.websearch.WebSearch#search(java.lang.String)
   */
  public InputStream search(final String query) throws IOException {
    Preconditions.checkNotNull(query);

    final String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
    final String queryUrlString = baseURL + encodedQuery + "&$top=" + LIMIT;

    final URL url = new URL(queryUrlString);
    final URLConnection urlConnection = url.openConnection();
    urlConnection.setRequestProperty(SUBSCRIPTION_KEY_HEADER, subscriptionKey);

    return urlConnection.getInputStream();
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.shef.dcs.websearch.WebSearch#getResultParser()
   */
  @Override
  public SearchResultParser getResultParser() {
    return new BingSearchResultParser();
  }

  public static void main(String[] args) throws IOException {
    final BingSearch searcher = new BingSearch("e413ab08c2e74283b12205f9453350ee",
        new URL("https://api.cognitive.microsoft.com/bing/v5.0/search?q="));

    final InputStream resultsStream = searcher.search("Sheffield University Sheffield");

    final BingSearchResultParser parser = new BingSearchResultParser();
    final List<WebSearchResultDoc> documents = parser.parse(resultsStream);

    System.out.println(documents);
  }
}
