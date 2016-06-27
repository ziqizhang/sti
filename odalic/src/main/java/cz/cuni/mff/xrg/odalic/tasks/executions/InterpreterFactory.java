package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.simmetrics.metrics.StringMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbsearch.KBSearch;
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

public class InterpreterFactory {

  private static final Logger LOG = LoggerFactory.getLogger(InterpreterFactory.class);

  //TODO: Write your own path to the property file.
  protected static final String PROPERTYFILE = "C:\\Users\\Acer\\git\\sti\\sti.properties";

  protected static KBSearch kbSearch;

  protected static SemanticTableInterpreter interpreter;

  protected static final String PROPERTY_HOME = "sti.home";
  protected static final String PROPERTY_WEBSEARCH_PROP_FILE = "sti.websearch.properties";
  protected static final String PROPERTY_NLP_RESOURCES = "sti.nlp";
  protected static final String PROPERTY_KBSEARCH_PROP_FILE = "sti.kbsearch.propertyfile";
  protected static final String PROPERTY_CACHE_FOLDER = "sti.cache.main.dir";

  private static final String PROPERTY_ENTITY_CACHE_CORENAME = "entity";
  private static final String PROPERTY_RELATION_CACHE_CORENAME = "relation";
  private static final String PROPERTY_WEBSEARCH_CACHE_CORENAME = "websearch";

  protected static final String PROPERTY_IGNORE_COLUMNS = "sti.columns.ignore";
  protected static final String PROPERTY_MUSTDO_COLUMNS = "sti.columns.mustdo";

  protected static final String PROPERTY_TMP_SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH =
      "sti.subjectcolumndetection.ws";
  protected static final String PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS = "sti.iinf.websearch.stopping.class";
  protected static final String PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS_CONSTR_PARAM =
      "sti.iinf.websearch.stopping.class.constructor.params";

  protected static final String PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS = "sti.tmp.iinf.learning.stopping.class";
  protected static final String PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS_CONSTR_PARAM =
      "sti.tmp.iinf.learning.stopping.class.constructor.params";

  protected static Properties properties;

  protected static CoreContainer cores;
  private static EmbeddedSolrServer entityCache;
  private static EmbeddedSolrServer websearchCache;

  public static SemanticTableInterpreter getInterpreter() {
    if (interpreter == null) {
      try {
        initComponents();
      } catch (STIException | IOException e) {
        e.printStackTrace();
      }
    }
    return interpreter;
  }

  //Initialize kbsearcher, websearcher
  protected static void initComponents() throws STIException, IOException {
    properties = new Properties();
    properties.load(new FileInputStream(PROPERTYFILE));

    LOG.info("Initializing entity cache...");
    EmbeddedSolrServer kbEntityServer = getSolrServerCacheEntity();
    //object to fetch things from KB

    LOG.info("Initializing KBSearch...");
    KBSearchFactory fbf = new KBSearchFactory();
    try {
      kbSearch = fbf.createInstance(
          getAbsolutePath(PROPERTY_KBSEARCH_PROP_FILE),
          kbEntityServer, null, null,null);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(ExceptionUtils.getFullStackTrace(e));
      throw new STIException("Failed initialising KBSearch:" +
          getAbsolutePath(PROPERTY_KBSEARCH_PROP_FILE)
      , e);
    }

    //LOG.info("Initializing WebSearcher...");


    LOG.info("Initializing SUBJECT COLUMN DETECTION components ...");
    SubjectColumnDetector subcolDetector;
    try {
      subcolDetector = new SubjectColumnDetector(
          new TContentTContentRowRankerImpl(),
          properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS),
          StringUtils.split(properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS_CONSTR_PARAM),
              ','),
          //new String[]{"0.0", "1", "0.01"},
          getSolrServerCacheWebsearch(),
          getNLPResourcesDir(),
          Boolean.valueOf(properties.getProperty(PROPERTY_TMP_SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH)),
          //"/BlhLSReljQ3Koh+vDSOaYMji9/Ccwe/7/b9mGJLwDQ=");  //zqz.work
          //"fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0="); //ziqizhang
          getStopwords(),
          getAbsolutePath(PROPERTY_WEBSEARCH_PROP_FILE)
          //, lodie
          //"7ql9acl+fXXfdjBGIIAH+N2WHk/dIZxdSkl4Uur68Hg"
          );//   dobs
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(ExceptionUtils.getFullStackTrace(e));
      throw new STIException("Failed initialising SUBJECT COLUMN DETECTION components:" + properties.getProperty(PROPERTY_WEBSEARCH_PROP_FILE)
      , e);
    }


    LOG.info("Initializing LEARNING components ...");
    LEARNINGPreliminaryColumnClassifier preliminaryClassify;
    TCellDisambiguator disambiguator;
    TColumnClassifier classifier;
    TContentCellRanker selector;
    LEARNING learning;
    try {
      disambiguator = new TCellDisambiguator(kbSearch,
          new TMPEntityScorer(
              getStopwords(),
              STIConstantProperty.SCORER_ENTITY_CONTEXT_WEIGHT, //row,column, column header, tablecontext all
              getNLPResourcesDir()));
      classifier = new TColumnClassifier(new TMPClazzScorer(getNLPResourcesDir(),
          new FreebaseConceptBoWCreator(),
          getStopwords(),
          STIConstantProperty.SCORER_CLAZZ_CONTEXT_WEIGHT)        //all 1.0
          );                                              //header,column,out trivial, out important
      selector = new OSPD_nonEmpty();
      preliminaryClassify = new LEARNINGPreliminaryColumnClassifier(
          selector,
          properties.getProperty(PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS),
          StringUtils.split(
              properties.getProperty(PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS_CONSTR_PARAM),
              ','),
          kbSearch,
          disambiguator,
          classifier
          );
      LEARNINGPreliminaryDisamb preliminaryDisamb = new LEARNINGPreliminaryDisamb(
          kbSearch, disambiguator, classifier
          );

      learning = new LEARNING(
          preliminaryClassify, preliminaryDisamb);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(ExceptionUtils.getFullStackTrace(e));
      throw new STIException("Failed initialising LEARNING components:"
          , e);
    }


    LOG.info("Initializing UPDATE components ...");
    UPDATE update;
    try {
      update =
          new UPDATE(selector, kbSearch, disambiguator, classifier, getStopwords(), getNLPResourcesDir());
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(ExceptionUtils.getFullStackTrace(e));
      throw new STIException("Failed initialising LEARNING components:"
          , e);
    }


    LOG.info("Initializing RELATIONLEARNING components ...");
    RelationScorer relationScorer = null;
    TColumnColumnRelationEnumerator relationEnumerator = null;
    LiteralColumnTagger literalColumnTagger = null;
    try {
      //object to computeElementScores relations between columns
      relationScorer = new TMPRelationScorer(
          getNLPResourcesDir(),
          new FreebaseRelationBoWCreator(),
          getStopwords(),
          STIConstantProperty.SCORER_RELATION_CONTEXT_WEIGHT
          // new double[]{1.0, 1.0, 0.0, 0.0, 1.0}
          );
      relationEnumerator = new TColumnColumnRelationEnumerator(
          new AttributeValueMatcher(
              STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE, getStopwords(),
              StringMetrics.levenshtein()),
          relationScorer
          );

      //object to consolidate previous output, further computeElementScores columns and disamgiuate entities
      literalColumnTagger =
          new LiteralColumnTaggerImpl(
              getIgnoreColumns()
              );
    } catch (Exception e) {

    }

    interpreter = new TMPInterpreter(
        subcolDetector,
        learning,
        update,
        relationEnumerator,
        literalColumnTagger,
        getIgnoreColumns(), getMustdoColumns()
        );

  }

  protected static EmbeddedSolrServer getSolrServerCacheEntity() throws STIException {
    if (entityCache == null) {
      String solrHomePath = properties.getProperty(PROPERTY_CACHE_FOLDER);
      if (solrHomePath == null || !new File(solrHomePath).exists()) {
        String error = "Cannot proceed: the cache dir is not set or does not exist. " +
            PROPERTY_CACHE_FOLDER + "=" + solrHomePath;
        LOG.error(error);
        throw new STIException(error);
      }

      if (cores == null) {
        entityCache = new EmbeddedSolrServer(Paths.get(solrHomePath), PROPERTY_ENTITY_CACHE_CORENAME);
        cores = entityCache.getCoreContainer();
      } else
        entityCache = new EmbeddedSolrServer(cores.getCore(PROPERTY_ENTITY_CACHE_CORENAME));
    }
    return entityCache;
  }

  protected static EmbeddedSolrServer getSolrServerCacheWebsearch() throws STIException {
    if (websearchCache == null) {
      String solrHomePath = properties.getProperty(PROPERTY_CACHE_FOLDER);
      if (solrHomePath == null || !new File(solrHomePath).exists() || PROPERTY_RELATION_CACHE_CORENAME == null) {
        String error = "Cannot proceed: the cache dir is not set or does not exist. " +
            PROPERTY_CACHE_FOLDER + "=" + solrHomePath;
        LOG.error(error);
        throw new STIException(error);
      }
      if (cores == null) {
        websearchCache = new EmbeddedSolrServer(Paths.get(solrHomePath), PROPERTY_WEBSEARCH_CACHE_CORENAME);
        cores = websearchCache.getCoreContainer();
      } else
        websearchCache = new EmbeddedSolrServer(cores.getCore(PROPERTY_WEBSEARCH_CACHE_CORENAME));
    }
    return websearchCache;
  }

  protected static String getNLPResourcesDir() throws STIException {
    String prop = getAbsolutePath(PROPERTY_NLP_RESOURCES);
    if (prop == null || !new File(prop).exists()) {
      String error = "Cannot proceed: nlp resources folder is not set or does not exist. " +
          PROPERTY_KBSEARCH_PROP_FILE + "=" + prop;
      LOG.error(error);
      throw new STIException(error);
    }
    return prop;
  }

  protected static List<String> getStopwords() throws STIException, IOException {
    return FileUtils.readList(getNLPResourcesDir() + File.separator + "stoplist.txt", true);
  }

  protected static String getAbsolutePath(String propertyName) {
    return properties.getProperty(PROPERTY_HOME)
        + File.separator + properties.getProperty(propertyName);
  }

  protected static int[] getIgnoreColumns() {
    String ignore = properties.getProperty(PROPERTY_IGNORE_COLUMNS);
    String[] splits = StringUtils.split(ignore, ',');
    int[] res = new int[splits.length];
    for (int i = 0; i < splits.length; i++) {
      res[i] = Integer.valueOf(splits[i].trim());
    }
    return res;
  }

  protected static int[] getMustdoColumns() {
    String ignore = properties.getProperty(PROPERTY_MUSTDO_COLUMNS);
    String[] splits = StringUtils.split(ignore, ',');
    int[] res = new int[0];
    for (int i = 0; i < splits.length; i++) {
      res[i] = Integer.valueOf(splits[i].trim());
    }
    return res;
  }

}
