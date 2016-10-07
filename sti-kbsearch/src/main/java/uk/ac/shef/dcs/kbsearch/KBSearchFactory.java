package uk.ac.shef.dcs.kbsearch;

import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearch;
import uk.ac.shef.dcs.kbsearch.sparql.DBpediaSearch;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

/**
 * Created by - on 17/03/2016.
 */
public class KBSearchFactory {
  private static final String PROPERTIES_SEPARATOR = "\\|";

  public Collection<KBSearch> createInstances(
          String kbSearchPropertyFiles,
          String cachesBasePath,
          String workingDirectory) throws KBSearchException {

    try {
      List<KBSearch> result = new ArrayList<>();
      String[] kbSearchPropertyFilesArray = kbSearchPropertyFiles.split(PROPERTIES_SEPARATOR);
      cachesBasePath = combinePaths(workingDirectory, cachesBasePath);

      for (String kbSearchPropertyFile : kbSearchPropertyFilesArray) {
        Properties properties = new Properties();
        properties.load(new FileInputStream(combinePaths(workingDirectory, kbSearchPropertyFile)));

        String className = properties.getProperty(KBSearch.KB_SEARCH_CLASS);
        boolean fuzzyKeywords = Boolean.valueOf(properties.getProperty(KBSearch.KB_SEARCH_TRY_FUZZY_KEYWORD, "false"));

        if (className.equals(FreebaseSearch.class.getName())) {
          result.add((KBSearch) Class.forName(className).
                  getDeclaredConstructor(Properties.class,
                          Boolean.class,
                          String.class).
                  newInstance(properties,
                          fuzzyKeywords, cachesBasePath));
        } else if (className.equals(DBpediaSearch.class.getName())) {
          KBDefinition definition = new KBDefinition();
          definition.load(properties, workingDirectory);

          result.add((KBSearch) Class.forName(className).
                  getDeclaredConstructor(KBDefinition.class,
                          Boolean.class,
                          String.class).
                  newInstance(definition,
                          fuzzyKeywords, cachesBasePath));
        } else {
          throw new KBSearchException("Class:" + className + " not supported");
        }
      }

      return result;
    } catch (Exception e) {
      throw new KBSearchException(e);
    }
  }
}
