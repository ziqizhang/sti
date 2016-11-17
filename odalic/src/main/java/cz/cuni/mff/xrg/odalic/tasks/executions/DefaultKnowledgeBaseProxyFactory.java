package cz.cuni.mff.xrg.odalic.tasks.executions;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.KBProxyFactory;
import uk.ac.shef.dcs.sti.STIException;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

/**
 * Created by Jan
 */
public class DefaultKnowledgeBaseProxyFactory implements KnowledgeBaseProxyFactory {

  private static final String PROPERTY_HOME = "sti.home";
  private static final String PROPERTY_PROXY_PROP_FILE = "sti.kbproxy.propertyfile";
  private static final String PROPERTY_CACHE_FOLDER = "sti.cache.main.dir";

  private static final Logger logger = LoggerFactory.getLogger(DefaultKnowledgeBaseProxyFactory.class);

  private Map<String, KBProxy> kbProxies;
  private Lock initLock = new ReentrantLock();
  private boolean isInitialized = false;

  private final String propertyFilePath;
  private Properties properties;

  public DefaultKnowledgeBaseProxyFactory(String propertyFilePath) {
    Preconditions.checkNotNull(propertyFilePath);

    this.propertyFilePath = propertyFilePath;
  }

  public DefaultKnowledgeBaseProxyFactory() {
    this(System.getProperty("cz.cuni.mff.xrg.odalic.sti"));
  }

  @Override
  public Map<String, KBProxy> getKBProxies() {
    if (kbProxies == null) {
      try {
        initComponents();
      } catch (STIException | IOException e) {
        e.printStackTrace();
      }
    }
    return kbProxies;
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
      Collection<KBProxy> kbProxyInstances = initKBProxies();

      for (KBProxy kbProxy : kbProxyInstances){
        initKBCache(kbProxy);
      }

      kbProxies = kbProxyInstances.stream().collect(Collectors.toMap(KBProxy::getName, item -> item));

      isInitialized = true;
    } finally {
      initLock.unlock();
    }
  }

  private Collection<KBProxy> initKBProxies() throws STIException {
    logger.info("Initializing KBProxy ...");
    try {
      KBProxyFactory fbf = new KBProxyFactory();
      return fbf.createInstances(
          properties.getProperty(PROPERTY_PROXY_PROP_FILE),
          properties.getProperty(PROPERTY_CACHE_FOLDER),
          properties.getProperty(PROPERTY_HOME));
    } catch (Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException(
          "Failed initializing KBProxy: " + getAbsolutePath(PROPERTY_PROXY_PROP_FILE), e);
    }
  }

  private void initKBCache(KBProxy kbProxy) throws STIException {
    logger.info("Initializing KB cache ...");
    try {
      kbProxy.initializeCaches();
    }
    catch (KBProxyException e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing KBProxy cache.", e);
    }
  }

  private String getAbsolutePath(String propertyName) {
    return combinePaths(properties.getProperty(PROPERTY_HOME), properties.getProperty(propertyName));
  }
}
