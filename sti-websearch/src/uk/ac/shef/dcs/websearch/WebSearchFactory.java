package uk.ac.shef.dcs.websearch;

import uk.ac.shef.dcs.websearch.bing.v5.BingSearch;

import java.io.FileInputStream;
import java.util.Properties;

import com.google.common.base.Preconditions;

/**
 * Creates a {@link WebSearch instance} dynamically, using reflection.
 * 
 * @author Ziqi Zhang
 */
public final class WebSearchFactory {

  /**
   * Creates a new {@link WebSearch} instance.
   * 
   * @param propertyFileName property file name
   * @return {@link WebSearch} instance
   * @throws WebSearchException when the instance cannot be created
   */
  public WebSearch createInstance(final String propertyFileName) throws WebSearchException {
    try {
      Preconditions.checkNotNull(propertyFileName);

      final Properties properties = new Properties();
      properties.load(new FileInputStream(propertyFileName));

      final String className = properties.getProperty(WebSearch.WEB_SEARCH_CLASS);
      if (className == null) {
        throw new IllegalArgumentException("Class name not defined!");
      }

      if (!className.equals(BingSearch.class.getName())) {
        throw new WebSearchException("Class: " + className + " not supported!");
      }

      return (WebSearch) Class.forName(className).getDeclaredConstructor(Properties.class)
          .newInstance(properties);
    } catch (final Exception e) {
      throw new WebSearchException(e);
    }
  }
}
