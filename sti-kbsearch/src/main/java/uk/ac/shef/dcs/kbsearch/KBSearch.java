package uk.ac.shef.dcs.kbsearch;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.atlas.io.IO;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;

import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.util.SolrCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

/**
 */
public abstract class KBSearch {

  protected SolrCache cacheEntity;
  protected SolrCache cacheConcept;
  protected SolrCache cacheProperty;
  protected SolrCache cacheSimilarity;
  protected boolean fuzzyKeywords;
  private String cachesBasePath;
  private static final Map<String, CoreContainer> cacheCores = new HashMap<>();

  protected static final String KB_SEARCH_RESULT_STOPLIST = "kb.search.result.stoplistfile";
  protected static final String KB_SEARCH_CLASS = "kb.search.class";
  protected static final String KB_SEARCH_TRY_FUZZY_KEYWORD = "kb.search.tryfuzzykeyword";

  private static final String ENTITY_CACHE = "entity";
  private static final String PROPERTY_CACHE = "property";
  private static final String CONCEPT_CACHE = "concept";
  private static final String SIMILARITY_CACHE = "similarity";

  protected static final boolean AUTO_COMMIT = true;
  protected static final boolean ALWAYS_CALL_REMOTE_SEARCHAPI = false;

  protected KBSearchResultFilter resultFilter;

  protected final Logger log = Logger.getLogger(getClass());

  protected KBDefinition kbDefinition;

  /**
   * @param kbDefinition    the knowledge base definition
   * @param fuzzyKeywords   given a query string, kbsearch will firstly try to fetch results
   *                        matching the exact query. when no match is found, you can set
   *                        fuzzyKeywords to true, to let kbsearch to break the query string based
   *                        on conjunective words. So if the query string is "tom and jerry", it
   *                        will try "tom" and "jerry"
   * @param cachesBasePath  Base path for the initialized solr caches.
   */
  public KBSearch(KBDefinition kbDefinition,
                  Boolean fuzzyKeywords,
                  String cachesBasePath) throws IOException {

    this.kbDefinition = kbDefinition;
    this.cachesBasePath = cachesBasePath;
    this.fuzzyKeywords = fuzzyKeywords;
  }

  public void initializeCaches() throws KBSearchException {
    cacheEntity = new SolrCache(getSolrServer(ENTITY_CACHE));
    cacheProperty = new SolrCache(getSolrServer(ENTITY_CACHE));
    cacheConcept = new SolrCache(getSolrServer(ENTITY_CACHE));
    cacheSimilarity = new SolrCache(getSolrServer(ENTITY_CACHE));
  }

  public String getName() {
    return kbDefinition.getName();
  }

  public KBDefinition getKbDefinition() { return kbDefinition; }

  public EmbeddedSolrServer getSolrServer(String cacheIdentifier) throws KBSearchException {
    Path cachePath = Paths.get(cachesBasePath, kbDefinition.getName());
    EmbeddedSolrServer cacheServer;

    if (!cacheCores.containsKey(cachePath.toString())) {
      cacheServer = initializeSolrServer(cacheIdentifier, cachePath, kbDefinition.getCacheTemplatePath());
      cacheCores.put(cachePath.toString(), cacheServer.getCoreContainer());
    }
    else {
      cacheServer = new EmbeddedSolrServer(cacheCores.get(cachePath.toString()), cacheIdentifier);
    }

    return cacheServer;
  }

  /**
   * Given a string, fetch candidate entities (resources) from the KB
   * Candidate entities are those resources for which label or part of the label matches the given content
   * @param content
   * @return
   * @throws IOException
   */
  public abstract List<Entity> findEntityCandidates(String content) throws KBSearchException;

  /**
   * Given a string,  fetch candidate entities (resources) from the KB that only match certain types
   */
  public abstract List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws KBSearchException;

  /**
   * Get attributes of the entity candidate
   * (all predicates and object values of the triples where the candidate entity is the subject).
   *
   * Note: Certain predicates may be blacklisted.
   */
  public abstract List<Attribute> findAttributesOfEntities(Entity ec) throws KBSearchException;

  /**
   * get attributes of the class
   */
  public abstract List<Attribute> findAttributesOfClazz(String clazzId) throws KBSearchException;

  /**
   * get attributes of the property
   */
  public abstract List<Attribute> findAttributesOfProperty(String propertyId) throws KBSearchException;

  /**
   * @return the granularity of the class in the KB.
   * @throws KBSearchException if the method is not supported
   */
  public double findGranularityOfClazz(String clazz) throws KBSearchException {
    return 0;
  }

  /**
   * compute the seamntic similarity between an entity and a class
   */
  public double findEntityClazzSimilarity(String entity_id, String clazz_url) throws KBSearchException {
    return 0;
  }

  /**
   * save the computed semantic similarity between the entity and class
   */
  public void cacheEntityClazzSimilarity(String entity_id, String clazz_url, double score, boolean biDirectional,
                                         boolean commit) throws KBSearchException {
    String query = createSolrCacheQuery_findEntityClazzSimilarity(entity_id, clazz_url);
    try {
      cacheSimilarity.cache(query, score, commit);
      log.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
      if (biDirectional) {
        query = clazz_url + "<>" + entity_id;
        cacheSimilarity.cache(query, score, commit);
        log.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
      }
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
    }
  }

  public void commitChanges() throws KBSearchException {

    try {
      if (cacheConcept != null) {
        cacheConcept.commit();
      }
      if (cacheEntity != null) {
        cacheEntity.commit();
      }
      if (cacheProperty != null) {
        cacheProperty.commit();
      }
      if (cacheSimilarity != null) {
        cacheSimilarity.commit();
      }
    } catch (Exception e) {
      throw new KBSearchException(e);
    }
  }


  public void closeConnection() throws KBSearchException {

    try {
      if (cacheEntity != null) {
        cacheEntity.shutdown();
      }
      if (cacheConcept != null) {
        cacheConcept.shutdown();
      }
      if (cacheProperty != null) {
        cacheProperty.shutdown();
      }
      if (cacheSimilarity != null) {
        cacheSimilarity.shutdown();
      }
    } catch (Exception e) {
      throw new KBSearchException(e);
    }

  }


  //TODO the properties below should be moved to a different class (SolrCacheHelper?) and renamed properly
    /*
    createSolrCacheQuery_XXX defines how a solr query should be constructed. If your implementing class
     want to benefit from solr cache, you should call these methods to generate a query string, which will
     be considered as the id of a record in the solr index. that query will be performed, to attempt to retrieve
     previously saved results if any.

     If there are no previously cached results, you have to perform your remote call to the KB, obtain the results,
     then cache the results in solr. Again you should call these methods to create a query string, which should be
     passed as the id of the record to be added to solr
     */
  protected String createSolrCacheQuery_findResources(String content) {
    return content;
  }

  protected String createSolrCacheQuery_findAttributesOfResource(String resource) {
    return "ATTR_" + resource;
  }

  protected String createSolrCacheQuery_findGranularityOfClazz(String clazz) {
    return "GRANULARITY_" + clazz;
  }

  protected String createSolrCacheQuery_findEntityClazzSimilarity(String entity, String concept) {
    return entity + "<>" + concept;
  }

  private EmbeddedSolrServer initializeSolrServer(String cacheIdentifier, Path cachePath, String templatePathString) throws KBSearchException {
    if (!Files.exists(cachePath)) {
      Path templatePath = Paths.get(templatePathString);
      if (!Files.exists(templatePath)) {
        String error = "Cannot proceed: the cache dir is not set or does not exist: "
                + templatePathString;
        log.error(error);
        throw new KBSearchException(error);
      }

      try {
        FileUtils.copyDirectory(templatePath.toFile(), cachePath.toFile());
      }
      catch (IOException exception) {
        String error = "Cannot proceed: the cache template cannot be copied. source: "
                + templatePath
                + "target: "
                + cachePath;

        log.error(error);
        throw new KBSearchException(error, exception);
      }
    }

    return new EmbeddedSolrServer(cachePath, cacheIdentifier);
  }
}
