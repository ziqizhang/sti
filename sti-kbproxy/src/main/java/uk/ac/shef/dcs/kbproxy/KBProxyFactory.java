package uk.ac.shef.dcs.kbproxy;

import uk.ac.shef.dcs.kbproxy.freebase.FreebaseSearch;
import uk.ac.shef.dcs.kbproxy.sparql.DBpediaProxy;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

/**
 * Created by - on 17/03/2016.
 */
public class KBProxyFactory {
  private static final String PROPERTIES_SEPARATOR = "\\|";

  public Collection<KBProxy> createInstances(
          String kbPropertyFiles,
          String cachesBasePath,
          String workingDirectory) throws KBProxyException {

    try {
      List<KBProxy> result = new ArrayList<>();
      String[] kbProxyPropertyFilesArray = kbPropertyFiles.split(PROPERTIES_SEPARATOR);
      cachesBasePath = combinePaths(workingDirectory, cachesBasePath);

      for (String kbProxyPropertyFile : kbProxyPropertyFilesArray) {
        Properties properties = new Properties();
        properties.load(new FileInputStream(combinePaths(workingDirectory, kbProxyPropertyFile)));

        String className = properties.getProperty(KBProxy.KB_SEARCH_CLASS);
        boolean fuzzyKeywords = Boolean.valueOf(properties.getProperty(KBProxy.KB_SEARCH_TRY_FUZZY_KEYWORD, "false"));

        if (className.equals(FreebaseSearch.class.getName())) {
          result.add((KBProxy) Class.forName(className).
                  getDeclaredConstructor(Properties.class,
                          Boolean.class,
                          String.class).
                  newInstance(properties,
                          fuzzyKeywords, cachesBasePath));
        } else if (className.equals(DBpediaProxy.class.getName())) {
          KBDefinition definition = new KBDefinition();
          definition.load(properties, workingDirectory);

          result.add((KBProxy) Class.forName(className).
                  getDeclaredConstructor(KBDefinition.class,
                          Boolean.class,
                          String.class).
                  newInstance(definition,
                          fuzzyKeywords, cachesBasePath));
        } else {
          throw new KBProxyException("Class:" + className + " not supported");
        }
      }

      return result;
    } catch (Exception e) {
      throw new KBProxyException(e);
    }
  }
}
