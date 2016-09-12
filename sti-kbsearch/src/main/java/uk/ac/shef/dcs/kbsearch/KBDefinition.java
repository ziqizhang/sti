package uk.ac.shef.dcs.kbsearch;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

/**
 * Information about the knowledge base.
 * Created by Jan
 */
public class KBDefinition {
  //region Consts

  private static final String PATH_SEPARATOR = "\\|";
  private static final String URL_SEPARATOR = " ";

  private static final String NAME_PROPERTY_NAME = "kb.name";

  private static final String SPARQL_ENDPOINT_PROPERTY_NAME = "kb.endpoint";
  private static final String ONTOLOGY_URI_PROPERTY_NAME = "kb.ontologyURI";
  private static final String STOP_LIST_FILE_PROPERTY_NAME = "kb.stopListFile";

  private static final String CACHE_TEMPLATE_PATH_PROPERTY_NAME = "kb.cacheTemplatePath";

  private static final String PREDICATES_PROPERTY_NAME = "kb.predicates";
  private static final String LANGUAGE_SUFFIX = "kb.languageSuffix";
  private static final String USE_BIF_CONTAINS = "kb.useBifContains";

  private static final String PREDICATE_NAME_PROPERTY_NAME = "kb.predicate.name";
  private static final String PREDICATE_LABEL_PROPERTY_NAME = "kb.predicate.label";
  private static final String PREDICATE_DESCRIPTION_PROPERTY_NAME = "kb.predicate.description";
  private static final String PREDICATE_TYPE_PROPERTY_NAME = "kb.predicate.type";

  //endregion

  //region Fields

  private final Map<String, Set<String>> predicates = new HashMap<>();
  protected final Logger log = Logger.getLogger(getClass());

  private String name;
  private String sparqlEndpoint;
  private String ontologyUri;
  private String stopListFile;

  private String languageSuffix;
  private boolean useBifContains;

  private String cacheTemplatePath;

  //endregion

  //region Properties

  /**
   * Returns the name of the knowledge base
   * @return
   */
  public String getName() {
    return name;
  }

  private void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the SPARQL endpoint used for connecting to the knowledge base
   * @return
   */
  public String getSparqlEndpoint() {
    return sparqlEndpoint;
  }

  private void setSparqlEndpoint(String sparqlEndpoint) {
    this.sparqlEndpoint = sparqlEndpoint;
  }

  public String getOntologyUri() {
    return ontologyUri;
  }

  private void setOntologyUri(String ontologyUri) {
    this.ontologyUri = ontologyUri;
  }

  public String getStopListFile() {
    return stopListFile;
  }

  private void setStopListFile(String stopListFile) {
    this.stopListFile = stopListFile;
  }

  public String getLanguageSuffix() {
    return languageSuffix;
  }

  private void setLanguageSuffix(String languageSuffix) {
    this.languageSuffix = languageSuffix;
  }

  public String getCacheTemplatePath() {
    return cacheTemplatePath;
  }

  private void setCacheTemplatePath(String cacheTemplatePath) {
    this.cacheTemplatePath = cacheTemplatePath;
  }

  public boolean getUseBifContains() {
    return useBifContains;
  }

  private void setUseBifContains(boolean useBifContains) {
    this.useBifContains = useBifContains;
  }

  public Set<String> getPredicateName() {
    return predicates.get(PREDICATE_NAME_PROPERTY_NAME);
  }

  public Set<String> getPredicateLabel() {
    return predicates.get(PREDICATE_LABEL_PROPERTY_NAME);
  }

  public Set<String> getPredicateDescription() {
    return predicates.get(PREDICATE_DESCRIPTION_PROPERTY_NAME);
  }

  public Set<String> getPredicateType() {
    return predicates.get(PREDICATE_TYPE_PROPERTY_NAME);
  }

  //endregion

  //region constructor

  public KBDefinition() {
    predicates.put(PREDICATE_NAME_PROPERTY_NAME, new HashSet<>());
    predicates.put(PREDICATE_LABEL_PROPERTY_NAME, new HashSet<>());
    predicates.put(PREDICATE_DESCRIPTION_PROPERTY_NAME, new HashSet<>());
    predicates.put(PREDICATE_TYPE_PROPERTY_NAME, new HashSet<>());
  }

  //endregion

  //region Methods

  /**
   * Loads KB definition from the knowledge base properties.
   * @param kbProperties Properties of the knowledge base.
   * @throws IOException
   */
  public void load(Properties kbProperties, String workingDirectory) throws IOException {
    // Name
    setName(kbProperties.getProperty(NAME_PROPERTY_NAME));

    // Endpoint and ontology
    setSparqlEndpoint(kbProperties.getProperty(SPARQL_ENDPOINT_PROPERTY_NAME));
    setOntologyUri(kbProperties.getProperty(ONTOLOGY_URI_PROPERTY_NAME));
    setStopListFile(combinePaths(workingDirectory, kbProperties.getProperty(STOP_LIST_FILE_PROPERTY_NAME)));

    setCacheTemplatePath(combinePaths(workingDirectory, kbProperties.getProperty(CACHE_TEMPLATE_PATH_PROPERTY_NAME)));

    // Language preferences
    if (kbProperties.containsKey(LANGUAGE_SUFFIX)) {
      setLanguageSuffix(kbProperties.getProperty(LANGUAGE_SUFFIX));
    }

    // Vistuoso specific settings
    if (kbProperties.containsKey(USE_BIF_CONTAINS)) {
      setUseBifContains(Boolean.parseBoolean(kbProperties.getProperty(USE_BIF_CONTAINS)));
    }

    // Loading predicate
    // Individual paths to definition files are separated by ";"
    String predicates = kbProperties.getProperty(PREDICATES_PROPERTY_NAME);
    String[] predicatesArray = predicates.split(PATH_SEPARATOR);

    for (String predicateFile : predicatesArray) {
      String predicateFileNormalized = combinePaths(workingDirectory, predicateFile);

      File file = new File(predicateFileNormalized);
      if (!file.exists() || file.isDirectory()) {
        log.error("The specified properties file does not exist: " + predicateFileNormalized);
        continue;
      }

      Properties properties = new Properties();
      try (InputStream fileStream = new FileInputStream(predicateFileNormalized)) {
        properties.load(fileStream);
        loadPredicates(properties);
      }
    }
  }

  private void loadPredicates(Properties properties) {
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      String key = (String) entry.getKey();
      Set<String> predicateValues = predicates.getOrDefault(key, null);

      if (predicateValues == null) {
        log.error("Unknown predicate key: " + key);
        continue;
      }

      String values = (String) entry.getValue();
      String[] valuesArray = values.split(URL_SEPARATOR);

      for (String value : valuesArray) {
        boolean valueAdded = predicateValues.add(value.toLowerCase());

        if (!valueAdded) {
          log.warn(String.format("Predicate value %1$s for the key %2$s is already loaded.", value, key));
        }
      }
    }
  }

  //endregion
}
