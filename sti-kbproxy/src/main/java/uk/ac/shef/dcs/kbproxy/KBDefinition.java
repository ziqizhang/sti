package uk.ac.shef.dcs.kbproxy;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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

  private static final String INSERT_SUPPORTED = "kb.insert.supported";
  private static final String INSERT_PREFIX_SCHEMA_ELEMENT = "kb.insert.prefix.schema.element";
  private static final String INSERT_PREFIX_DATA_ELEMENT = "kb.insert.prefix.data.element";
  private static final String INSERT_ROOT_CLASS = "kb.insert.root.class";
  private static final String INSERT_LABEL = "kb.insert.label";
  private static final String INSERT_ALTERNATIVE_LABEL = "kb.insert.alternative.label";
  private static final String INSERT_SUBCLASS_OF = "kb.insert.subclass.of";
  private static final String INSERT_INSTANCE_OF = "kb.insert.instance.of";
  private static final String INSERT_CLASS_TYPE = "kb.insert.class.type";
  private static final String INSERT_GRAPH = "kb.insert.graph";

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

  private boolean insertSupported;
  private URI insertSchemaElementPrefix;
  private URI insertDataElementPrefix;
  private String insertRootClass;
  private String insertLabel;
  private String insertAlternativeLabel;
  private String insertSubclassOf;
  private String insertInstanceOf;
  private String insertClassType;
  private String insertGraph;

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

  public boolean isInsertSupported() {
    return insertSupported;
  }

  private void setInsertSupported(boolean insertSupported) {
    this.insertSupported = insertSupported;
  }

  public URI getInsertSchemaElementPrefix() {
    return insertSchemaElementPrefix;
  }

  private void setInsertSchemaElementPrefix(URI insertSchemaElementPrefix) {
    this.insertSchemaElementPrefix = insertSchemaElementPrefix;
  }

  public URI getInsertDataElementPrefix() {
    return insertDataElementPrefix;
  }

  private void setInsertDataElementPrefix(URI insertDataElementPrefix) {
    this.insertDataElementPrefix = insertDataElementPrefix;
  }

  public String getInsertRootClass() {
    return insertRootClass;
  }

  private void setInsertRootClass(String insertRootClass) {
    this.insertRootClass = insertRootClass;
  }

  public String getInsertLabel() {
    return insertLabel;
  }

  private void setInsertLabel(String insertLabel) {
    this.insertLabel = insertLabel;
  }

  public String getInsertAlternativeLabel() {
    return insertAlternativeLabel;
  }

  private void setInsertAlternativeLabel(String insertAlternativeLabel) {
    this.insertAlternativeLabel = insertAlternativeLabel;
  }

  public String getInsertSubclassOf() {
    return insertSubclassOf;
  }

  private void setInsertSubclassOf(String insertSubclassOf) {
    this.insertSubclassOf = insertSubclassOf;
  }

  public String getInsertInstanceOf() {
    return insertInstanceOf;
  }

  private void setInsertInstanceOf(String insertInstanceOf) {
    this.insertInstanceOf = insertInstanceOf;
  }

  public String getInsertClassType() {
    return insertClassType;
  }

  private void setInsertClassType(String insertClassType) {
    this.insertClassType = insertClassType;
  }

  public String getInsertGraph() {
    return insertGraph;
  }

  private void setInsertGraph(String insertGraph) {
    this.insertGraph = insertGraph;
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
   * @throws URISyntaxException
   */
  public void load(Properties kbProperties, String workingDirectory) throws IOException, URISyntaxException {
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

    // Loading predicates
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

    // SPARQL insert
    if (kbProperties.containsKey(INSERT_SUPPORTED)) {
      setInsertSupported(Boolean.parseBoolean(kbProperties.getProperty(INSERT_SUPPORTED)));
    }

    if (isInsertSupported()) {
      setInsertSchemaElementPrefix(new URI(kbProperties.getProperty(INSERT_PREFIX_SCHEMA_ELEMENT)));
      setInsertDataElementPrefix(new URI(kbProperties.getProperty(INSERT_PREFIX_DATA_ELEMENT)));
      setInsertLabel(kbProperties.getProperty(INSERT_LABEL));
      setInsertAlternativeLabel(kbProperties.getProperty(INSERT_ALTERNATIVE_LABEL));
      setInsertRootClass(kbProperties.getProperty(INSERT_ROOT_CLASS));
      setInsertSubclassOf(kbProperties.getProperty(INSERT_SUBCLASS_OF));
      setInsertInstanceOf(kbProperties.getProperty(INSERT_INSTANCE_OF));
      setInsertClassType(kbProperties.getProperty(INSERT_CLASS_TYPE));
      setInsertGraph(kbProperties.getProperty(INSERT_GRAPH));
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
