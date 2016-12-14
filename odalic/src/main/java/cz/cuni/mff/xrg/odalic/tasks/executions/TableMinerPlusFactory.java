package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.simmetrics.metrics.StringMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.*;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPClazzScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPEntityScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPRelationScorer;
import uk.ac.shef.dcs.sti.core.feature.FreebaseConceptBoWCreator;
import uk.ac.shef.dcs.sti.core.feature.FreebaseRelationBoWCreator;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.OSPD_nonEmpty;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.FileUtils;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

/**
 * Implementation of {@link SemanticTableInterpreterFactory} that provides {@link TMPOdalicInterpreter}
 * instances.
 * 
 * @author Josef Janoušek
 *
 */
public final class TableMinerPlusFactory implements SemanticTableInterpreterFactory {

  private static final String PROPERTY_HOME = "sti.home";
  private static final String PROPERTY_WEBSEARCH_PROP_FILE = "sti.websearch.properties";
  private static final String PROPERTY_NLP_RESOURCES = "sti.nlp";

  private static final String PROPERTY_WEBSEARCH_CACHE_CORENAME = "websearch";

  private static final String PROPERTY_TMP_SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH =
      "sti.subjectcolumndetection.ws";
  private static final String PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS =
      "sti.iinf.websearch.stopping.class";
  private static final String PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS_CONSTR_PARAM =
      "sti.iinf.websearch.stopping.class.constructor.params";

  private static final String PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS =
      "sti.tmp.iinf.learning.stopping.class";
  private static final String PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS_CONSTR_PARAM =
      "sti.tmp.iinf.learning.stopping.class.constructor.params";

  private static final Logger logger = LoggerFactory.getLogger(TableMinerPlusFactory.class);

  private final String propertyFilePath;

  private final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory;

  private Map<String, SemanticTableInterpreter> interpreters;
  private Properties properties;
  private final Lock initLock = new ReentrantLock();
  private boolean isInitialized = false;

  public TableMinerPlusFactory(KnowledgeBaseProxyFactory knowledgeBaseProxyFactory, String propertyFilePath) {
    Preconditions.checkNotNull(knowledgeBaseProxyFactory);
    Preconditions.checkNotNull(propertyFilePath);

    this.knowledgeBaseProxyFactory = knowledgeBaseProxyFactory;
    this.propertyFilePath = propertyFilePath;
  }

  @Autowired
  public TableMinerPlusFactory(KnowledgeBaseProxyFactory knowledgeBaseProxyFactory) {
    this(knowledgeBaseProxyFactory, System.getProperty("cz.cuni.mff.xrg.odalic.sti"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.executions.SemanticTableInterpreterFactory#getInterpreters()
   */
  @Override
  public Map<String, SemanticTableInterpreter> getInterpreters() throws STIException, IOException {
    if (interpreters == null) {
        initComponents();
    }
    return interpreters;
  }

  // Initialize kbsearcher, websearcher
  private void initComponents() throws STIException, IOException {
    initLock.lock();
    try {
      if (isInitialized) {
        return;
      }

      properties = new Properties();
      properties.load(new FileInputStream(propertyFilePath));

      // object to fetch things from KB
      Map<String, KBProxy> kbProxyInstances = knowledgeBaseProxyFactory.getKBProxies();

      interpreters = new HashMap<>();
      for (KBProxy kbProxy : kbProxyInstances.values()) {
        SubjectColumnDetector subcolDetector = initSubColDetector(kbProxy);

        TCellDisambiguator disambiguator = initDisambiguator(kbProxy);
        TColumnClassifier classifier = initClassifier();
        TContentCellRanker selector = new OSPD_nonEmpty();

        LEARNING learning = initLearning(kbProxy, selector, disambiguator, classifier);

        UPDATE update = initUpdate(kbProxy, selector, disambiguator, classifier);

        TColumnColumnRelationEnumerator relationEnumerator = initRelationEnumerator();

        // object to consolidate previous output, further computeElementScores columns
        // and disambiguate entities
        LiteralColumnTagger literalColumnTagger = new LiteralColumnTaggerImpl();

        SemanticTableInterpreter interpreter = new TMPOdalicInterpreter(subcolDetector, learning, update,
                relationEnumerator, literalColumnTagger);

        interpreters.put(kbProxy.getName(), interpreter);

        isInitialized = true;
      }
    } finally {
      initLock.unlock();
    }
  }

  private String getNLPResourcesDir() throws STIException {
    String prop = getAbsolutePath(PROPERTY_NLP_RESOURCES);
    if (prop == null || !new File(prop).exists()) {
      String error = "Cannot proceed: nlp resources folder is not set or does not exist. "
          + PROPERTY_NLP_RESOURCES + "=" + prop;
      logger.error(error);
      throw new STIException(error);
    }
    return prop;
  }

  private List<String> getStopwords() throws STIException, IOException {
    return FileUtils.readList(getNLPResourcesDir() + File.separator + "stoplist.txt", true);
  }

  private String getAbsolutePath(String propertyName) {
    return combinePaths(properties.getProperty(PROPERTY_HOME), properties.getProperty(propertyName));
  }

  private SubjectColumnDetector initSubColDetector(KBProxy kbProxy) throws STIException {
    logger.info("Initializing SUBJECT COLUMN DETECTION components ...");
    try {
      return new SubjectColumnDetector(new TContentTContentRowRankerImpl(),
          properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS),
          StringUtils.split(
              properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS_CONSTR_PARAM), ','),
              // new String[]{"0.0", "1", "0.01"},
          kbProxy.getSolrServer(PROPERTY_WEBSEARCH_CACHE_CORENAME), getNLPResourcesDir(),
          Boolean.valueOf(properties.getProperty(PROPERTY_TMP_SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH)),
          getStopwords(), getAbsolutePath(PROPERTY_WEBSEARCH_PROP_FILE));
    } catch (Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing SUBJECT COLUMN DETECTION components: "
              + properties.getProperty(PROPERTY_WEBSEARCH_PROP_FILE), e);
    }
  }

  private TCellDisambiguator initDisambiguator(KBProxy kbProxy) throws STIException {
    try {
      return new TCellDisambiguator(kbProxy,
          new TMPEntityScorer(getStopwords(), STIConstantProperty.SCORER_ENTITY_CONTEXT_WEIGHT,
              // row, column, column header, table context all
              getNLPResourcesDir()));
    } catch (Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing LEARNING components.", e);
    }
  }

  private TColumnClassifier initClassifier() throws STIException {
    try {
      return new TColumnClassifier(
          new TMPClazzScorer(getNLPResourcesDir(), new FreebaseConceptBoWCreator(), getStopwords(),
              STIConstantProperty.SCORER_CLAZZ_CONTEXT_WEIGHT) // all 1.0
      ); // header, column, out trivial, out important
    } catch (Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing LEARNING components.", e);
    }
  }

  private LEARNING initLearning(KBProxy kbProxy, TContentCellRanker selector, TCellDisambiguator disambiguator,
                                TColumnClassifier classifier) throws STIException {
    logger.info("Initializing LEARNING components ...");
    try {
      LEARNINGPreliminaryColumnClassifier preliminaryClassify = new LEARNINGPreliminaryColumnClassifier(selector,
          properties.getProperty(PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS),
          StringUtils.split(
              properties.getProperty(PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS_CONSTR_PARAM), ','),
              kbProxy, disambiguator, classifier);
      LEARNINGPreliminaryDisamb preliminaryDisamb =
          new LEARNINGPreliminaryDisamb(kbProxy, disambiguator, classifier);

      return new LEARNING(preliminaryClassify, preliminaryDisamb);
    } catch (Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing LEARNING components.", e);
    }
  }

  private UPDATE initUpdate(KBProxy kbProxy, TContentCellRanker selector, TCellDisambiguator disambiguator,
                            TColumnClassifier classifier) throws STIException {
    logger.info("Initializing UPDATE components ...");
    try {
      return new UPDATE(selector, kbProxy, disambiguator, classifier, getStopwords(), getNLPResourcesDir());
    } catch (Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing UPDATE components.", e);
    }
  }

  private TColumnColumnRelationEnumerator initRelationEnumerator() throws STIException {
    logger.info("Initializing RELATIONLEARNING components ...");
    try {
      // object to computeElementScores relations between columns
      RelationScorer relationScorer = new TMPRelationScorer(getNLPResourcesDir(), new FreebaseRelationBoWCreator(),
          getStopwords(), STIConstantProperty.SCORER_RELATION_CONTEXT_WEIGHT
          // new double[]{1.0, 1.0, 0.0, 0.0, 1.0}
      );
      return new TColumnColumnRelationEnumerator(
          new AttributeValueMatcher(STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE, getStopwords(),
              StringMetrics.levenshtein()),
          relationScorer);
    } catch (Exception e) {
        logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
        throw new STIException("Failed initializing RELATIONLEARNING components.", e);
    }
  }
}
