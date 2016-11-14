package cz.cuni.mff.xrg.odalic.tasks.executions;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.KBSearchFactory;
import uk.ac.shef.dcs.sti.STIException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

/**
 * Created by Jan
 */
public class DefaultKnowledgeBaseSearchFactory implements KnowledgeBaseSearchFactory {

  private static final String PROPERTY_HOME = "sti.home";
  private static final String PROPERTY_KBSEARCH_PROP_FILE = "sti.kbsearch.propertyfile";
  private static final String PROPERTY_CACHE_FOLDER = "sti.cache.main.dir";

  private static final Logger logger = LoggerFactory.getLogger(DefaultKnowledgeBaseSearchFactory.class);

  private Map<String, KBSearch> kbSearches;
  private Lock initLock = new ReentrantLock();
  private boolean isInitialized = false;

  private final String propertyFilePath;
  private Properties properties;

  public DefaultKnowledgeBaseSearchFactory(String propertyFilePath) {
    Preconditions.checkNotNull(propertyFilePath);

    this.propertyFilePath = propertyFilePath;
  }

  public DefaultKnowledgeBaseSearchFactory() {
    this(System.getProperty("cz.cuni.mff.xrg.odalic.sti"));
  }

  @Override
  public Map<String, KBSearch> getKBSearches() {
    if (kbSearches == null) {
      try {
        initComponents();
      } catch (STIException | IOException e) {
        e.printStackTrace();
      }
    }
    return kbSearches;
  }

  private void initComponents() throws STIException, IOException {
    initLock.lock();
    try {
      if (isInitialized) {
        return;
      }

      properties = new Properties();
      properties.load(new FileInputStream(propertyFilePath));

      // object to fetch things from KB
      Collection<KBSearch> kbSearchInstances = initKBSearch();

      for (KBSearch kbSearch : kbSearchInstances){
        initKBCache(kbSearch);
      }

      kbSearches = kbSearchInstances.stream().collect(Collectors.toMap(KBSearch::getName, item -> item));

      isInitialized = true;
    } finally {
      initLock.unlock();
    }
  }

  private Collection<KBSearch> initKBSearch() throws STIException {
    logger.info("Initializing KBSearch ...");
    try {
      KBSearchFactory fbf = new KBSearchFactory();
      return fbf.createInstances(
          properties.getProperty(PROPERTY_KBSEARCH_PROP_FILE),
          properties.getProperty(PROPERTY_CACHE_FOLDER),
          properties.getProperty(PROPERTY_HOME));
    } catch (Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException(
          "Failed initializing KBSearch: " + getAbsolutePath(PROPERTY_KBSEARCH_PROP_FILE), e);
    }
  }

  private void initKBCache(KBSearch kbSearch) throws STIException {
    logger.info("Initializing KB cache ...");
    try {
      kbSearch.initializeCaches();
    }
    catch (KBSearchException e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing KBSearch cache.", e);
    }
  }

  private String getAbsolutePath(String propertyName) {
    return combinePaths(properties.getProperty(PROPERTY_HOME), properties.getProperty(propertyName));
  }
}
