package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.simmetrics.metrics.StringMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.KBSearchFactory;
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
 * Implementation of {@link SemanticTableInterpreterFactory} that provides {@link TMPInterpreter}
 * instances.
 * 
 * @author Josef Janoušek
 *
 */
public final class TableMinerPlusFactory implements SemanticTableInterpreterFactory {

  private static final String PROPERTY_HOME = "sti.home";
  private static final String PROPERTY_WEBSEARCH_PROP_FILE = "sti.websearch.properties";
  private static final String PROPERTY_NLP_RESOURCES = "sti.nlp";
  private static final String PROPERTY_KBSEARCH_PROP_FILE = "sti.kbsearch.propertyfile";
  private static final String PROPERTY_CACHE_FOLDER = "sti.cache.main.dir";

  private static final String PROPERTY_WEBSEARCH_CACHE_CORENAME = "websearch";

  private static final String PROPERTY_IGNORE_COLUMNS = "sti.columns.ignore";
  private static final String PROPERTY_MUSTDO_COLUMNS = "sti.columns.mustdo";

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


  private Map<String, SemanticTableInterpreter> interpreters;
  private LiteralColumnTagger literalColumnTagger;

  private Properties properties;

  // private EmbeddedSolrServer websearchCache;

  public TableMinerPlusFactory(String propertyFilePath) {
    Preconditions.checkNotNull(propertyFilePath);

    this.propertyFilePath = propertyFilePath;
  }

  public TableMinerPlusFactory() {
    this(System.getProperty("cz.cuni.mff.xrg.odalic.sti"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.executions.InterpreterFactory#getInterpreter()
   */
  @Override
  public Map<String, SemanticTableInterpreter> getInterpreters() {
    if (interpreters == null) {
      try {
        initComponents();
      } catch (STIException | IOException e) {
        e.printStackTrace();
      }
    }
    return interpreters;
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.executions.SemanticTableInterpreterFactory#
   * setColumnIgnoresForInterpreter(java.util.Set)
   */
  @Override
  public void setColumnIgnoresForInterpreter(Set<? extends ColumnIgnore> ignoreColumnsPositions) {
    final Set<Integer> indexSet = ignoreColumnsPositions.stream()
        .map(e -> e.getPosition().getIndex()).collect(Collectors.toSet());

    for(SemanticTableInterpreter interpreter : interpreters.values()) {
      interpreter.setIgnoreColumns(indexSet);
    }
    literalColumnTagger
        .setIgnoreColumns(indexSet.stream().mapToInt(e -> e.intValue()).sorted().toArray());
  }

  // Initialize kbsearcher, websearcher
  private void initComponents() throws STIException, IOException {
    properties = new Properties();
    properties.load(new FileInputStream(propertyFilePath));

    // object to fetch things from KB

    logger.info("Initializing KBSearch...");
    KBSearchFactory fbf = new KBSearchFactory();
    Collection<KBSearch> kbSearchInstances;
    try {
      kbSearchInstances = fbf.createInstances(
          properties.getProperty(PROPERTY_KBSEARCH_PROP_FILE),
          properties.getProperty(PROPERTY_CACHE_FOLDER),
          properties.getProperty(PROPERTY_HOME));

    } catch (Exception e) {
      e.printStackTrace();
      logger.error(ExceptionUtils.getFullStackTrace(e));
      throw new STIException(
          "Failed initialising KBSearch:" + getAbsolutePath(PROPERTY_KBSEARCH_PROP_FILE), e);
    }

    interpreters = new HashMap<>();
    for (KBSearch kbSearch : kbSearchInstances) {
      logger.info("Initializing KB cache ...");
      try {
        kbSearch.initializeCaches();
      }
      catch (KBSearchException e) {
        e.printStackTrace();
        logger.error(ExceptionUtils.getFullStackTrace(e));
        throw new STIException("Failed initialising KBSearch cache.", e);
      }

      logger.info("Initializing SUBJECT COLUMN DETECTION components ...");
      SubjectColumnDetector subcolDetector;
      try {
        subcolDetector = new SubjectColumnDetector(new TContentTContentRowRankerImpl(),
                properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS),
                StringUtils.split(
                        properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS_CONSTR_PARAM), ','),
                // new String[]{"0.0", "1", "0.01"},
                kbSearch.getSolrServer(PROPERTY_WEBSEARCH_CACHE_CORENAME), getNLPResourcesDir(),
                Boolean
                        .valueOf(properties.getProperty(PROPERTY_TMP_SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH)),
                // "/BlhLSReljQ3Koh+vDSOaYMji9/Ccwe/7/b9mGJLwDQ="); //zqz.work
                // "fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0="); //ziqizhang
                getStopwords(), getAbsolutePath(PROPERTY_WEBSEARCH_PROP_FILE)
                // , lodie
                // "7ql9acl+fXXfdjBGIIAH+N2WHk/dIZxdSkl4Uur68Hg"
        );// dobs
      } catch (Exception e) {
        e.printStackTrace();
        logger.error(ExceptionUtils.getFullStackTrace(e));
        throw new STIException("Failed initialising SUBJECT COLUMN DETECTION components:"
                + properties.getProperty(PROPERTY_WEBSEARCH_PROP_FILE), e);
      }


      logger.info("Initializing LEARNING components ...");
      LEARNINGPreliminaryColumnClassifier preliminaryClassify;
      TCellDisambiguator disambiguator;
      TColumnClassifier classifier;
      TContentCellRanker selector;
      LEARNING learning;
      try {
        disambiguator = new TCellDisambiguator(kbSearch,
                new TMPEntityScorer(getStopwords(), STIConstantProperty.SCORER_ENTITY_CONTEXT_WEIGHT, // row,
                        // column,
                        // column
                        // header,
                        // tablecontext
                        // all
                        getNLPResourcesDir()));
        classifier = new TColumnClassifier(
                new TMPClazzScorer(getNLPResourcesDir(), new FreebaseConceptBoWCreator(), getStopwords(),
                        STIConstantProperty.SCORER_CLAZZ_CONTEXT_WEIGHT) // all 1.0
        ); // header, column, out trivial, out important
        selector = new OSPD_nonEmpty();
        preliminaryClassify = new LEARNINGPreliminaryColumnClassifier(selector,
                properties.getProperty(PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS),
                StringUtils.split(
                        properties.getProperty(PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS_CONSTR_PARAM), ','),
                kbSearch, disambiguator, classifier);
        LEARNINGPreliminaryDisamb preliminaryDisamb =
                new LEARNINGPreliminaryDisamb(kbSearch, disambiguator, classifier);

        learning = new LEARNING(preliminaryClassify, preliminaryDisamb);
      } catch (Exception e) {
        e.printStackTrace();
        logger.error(ExceptionUtils.getFullStackTrace(e));
        throw new STIException("Failed initialising LEARNING components:", e);
      }


      logger.info("Initializing UPDATE components ...");
      UPDATE update;
      try {
        update = new UPDATE(selector, kbSearch, disambiguator, classifier, getStopwords(),
                getNLPResourcesDir());
      } catch (Exception e) {
        e.printStackTrace();
        logger.error(ExceptionUtils.getFullStackTrace(e));
        throw new STIException("Failed initialising LEARNING components:", e);
      }


      logger.info("Initializing RELATIONLEARNING components ...");
      RelationScorer relationScorer = null;
      TColumnColumnRelationEnumerator relationEnumerator = null;

      try {
        // object to computeElementScores relations between columns
        relationScorer = new TMPRelationScorer(getNLPResourcesDir(), new FreebaseRelationBoWCreator(),
                getStopwords(), STIConstantProperty.SCORER_RELATION_CONTEXT_WEIGHT
                // new double[]{1.0, 1.0, 0.0, 0.0, 1.0}
        );
        relationEnumerator = new TColumnColumnRelationEnumerator(
                new AttributeValueMatcher(STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE, getStopwords(),
                        StringMetrics.levenshtein()),
                relationScorer);

        // object to consolidate previous output, further computeElementScores columns and
        // disambiguate entities
        literalColumnTagger = new LiteralColumnTaggerImpl(getIgnoreColumns());
      } catch (Exception e) {
          logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      }

      SemanticTableInterpreter interpreter = new TMPInterpreter(subcolDetector, learning, update, relationEnumerator,
              literalColumnTagger, getIgnoreColumns(), getMustdoColumns());

      interpreters.put(kbSearch.getName(), interpreter);
    }

  }

  private String getNLPResourcesDir() throws STIException {
    String prop = getAbsolutePath(PROPERTY_NLP_RESOURCES);
    if (prop == null || !new File(prop).exists()) {
      String error = "Cannot proceed: nlp resources folder is not set or does not exist. "
          + PROPERTY_KBSEARCH_PROP_FILE + "=" + prop;
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

  private int[] getIgnoreColumns() {
    String ignore = properties.getProperty(PROPERTY_IGNORE_COLUMNS);
    String[] splits = StringUtils.split(ignore, ',');
    int[] res = new int[splits.length];
    for (int i = 0; i < splits.length; i++) {
      res[i] = Integer.valueOf(splits[i].trim());
    }
    return res;
  }

  private int[] getMustdoColumns() {
    String ignore = properties.getProperty(PROPERTY_MUSTDO_COLUMNS);
    String[] splits = StringUtils.split(ignore, ',');
    int[] res = new int[0];
    for (int i = 0; i < splits.length; i++) {
      res[i] = Integer.valueOf(splits[i].trim());
    }
    return res;
  }
}
