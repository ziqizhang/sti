package uk.ac.shef.dcs.sti.experiment;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;

import org.simmetrics.metrics.StringMetrics;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchFactory;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;

import uk.ac.shef.dcs.sti.core.algorithm.ji.*;
import uk.ac.shef.dcs.sti.core.algorithm.ji.similarity.EntityAndClazzSimilarityScorer;
import uk.ac.shef.dcs.sti.core.algorithm.smp.ClazzSpecificityCalculator;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 *
 */
public class JointInferenceBatch extends STIBatch {
  private static final Logger LOG = Logger.getLogger(JointInferenceBatch.class.getName());
  private static final String PROPERTY_JI_USE_SUBJECT_COLUMN = "sti.ji.usesubjectcolumn";
  private static final String PROPERTY_JI_MAX_ITERATIONS = "sti.ji.maxiterations";
  private static final String PROPERTY_JI_DEBUG_MODE = "sti.ji.debugmode";
  private static final String PROPERTY_JI_CLAZZ_SPECIFICITY_CALCULATOR = "sti.ji.clazzspecificitycalculator";

  private EmbeddedSolrServer simlarityServer;

  public JointInferenceBatch(String propertyFile) throws IOException, STIException {
    super(propertyFile);
  }

  private ClazzSpecificityCalculator getClazzSpecificityCalculator() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
    return (ClazzSpecificityCalculator)
        Class.forName(properties.getProperty(PROPERTY_JI_CLAZZ_SPECIFICITY_CALCULATOR))
            .getDeclaredConstructor(KBSearch.class)
            .newInstance(kbSearch);
  }

  @Override
  protected void initComponents() throws STIException {
    //object to fetch things from KB

    LOG.info("Initializing KBSearch...");
    initKB();

    LOG.info("Initializing SUBJECT COLUMN DETECTION components ...");
    SubjectColumnDetector subcolDetector;
    try {
      subcolDetector = new SubjectColumnDetector(
          new TContentTContentRowRankerImpl(),
          properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS),
          StringUtils.split(properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS_CONSTR_PARAM),
              ','),
          kbSearch.getSolrServer(PROPERTY_WEBSEARCH_CACHE_CORENAME),
          getNLPResourcesDir(),
          Boolean.valueOf(properties.getProperty(PROPERTY_TMP_SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH)),
          getStopwords(),
          getAbsolutePath(PROPERTY_WEBSEARCH_PROP_FILE)
      );//   dobs
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(ExceptionUtils.getFullStackTrace(e));
      throw new STIException("Failed initialising SUBJECT COLUMN DETECTION components:" + properties.getProperty(PROPERTY_WEBSEARCH_PROP_FILE)
          , e);
    }

    LOG.info("Initializing JI components ...");
    try {
      int cores = Runtime.getRuntime().availableProcessors();
      interpreter = new JIInterpreter(
          subcolDetector,
          new CandidateEntityGenerator(kbSearch,
              new JIAdaptedEntityScorer()),
          new CandidateConceptGenerator(kbSearch,
              new JIClazzScorer(),
              new EntityAndClazzSimilarityScorer(getStopwords(), getNLPResourcesDir()),
              getClazzSpecificityCalculator(),
              cores, true),
          new CandidateRelationGenerator(
              new JIAdaptedAttributeMatcher(STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE, getStopwords(),
                  StringMetrics.levenshtein()),
              kbSearch, true),
          Boolean.valueOf(properties.getProperty(PROPERTY_JI_USE_SUBJECT_COLUMN, "false")),
          getIgnoreColumns(),
          getMustdoColumns(),
          Integer.valueOf(properties.getProperty(PROPERTY_JI_MAX_ITERATIONS, "10")),
          Boolean.valueOf(properties.getProperty(PROPERTY_JI_DEBUG_MODE, "false"))
      );
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(ExceptionUtils.getFullStackTrace(e));
      throw new STIException("Failed initialising SMP components"
          , e);
    }
  }


  public static void main(String[] args) throws IOException, STIException {
    String inFolder = args[0];
    String outFolder = args[1];
    JointInferenceBatch ji = new JointInferenceBatch(args[2]);

    int count = 0;
    List<File> all = Arrays.asList(new File(inFolder).listFiles());
    Collections.sort(all);
    LOG.info("Initialization complete. Begin STI. Total input files=" + all.size() + "\n");

    List<Integer> previouslyFailed = ji.loadPreviouslyFailed();
    int start = ji.getStartIndex();
    for (File f : all) {
      if (f.toString().contains(".DS_Store")) continue;
      count++;

      //if a previously failed list of files is given, only learn these.
      if (previouslyFailed.size() != 0 && !previouslyFailed.contains(count))
        continue;

      if (count - 1 < start)
        continue;
      boolean complete;
      String inFile = f.toString();

      try {
        String sourceTableFile = inFile;
        if (sourceTableFile.startsWith("\"") && sourceTableFile.endsWith("\""))
          sourceTableFile = sourceTableFile.substring(1, sourceTableFile.length() - 1).trim();
        //System.out.println(count + "_" + sourceTableFile + " " + new Date());
        LOG.info("\n<< " + count + "_" + sourceTableFile);
        List<Table> tables = ji.loadTable(inFile);
        if (tables.size() == 0)
          ji.recordFailure(count, inFile, inFile);

        for (Table table : tables) {
          complete = ji.process(
              table,
              sourceTableFile,
              ji.getTAnnotationWriter(), outFolder,
              Boolean.valueOf(ji.properties.getProperty(PROPERTY_PERFORM_RELATION_LEARNING)));

          if (STIConstantProperty.SOLR_COMMIT_PER_FILE)
            ji.kbSearch.commitChanges();
          if (!complete) {
            ji.recordFailure(count, sourceTableFile, inFile);
          }
        }
        //gs annotator

      } catch (Exception e) {
        e.printStackTrace();
        ji.recordFailure(count, inFile, inFile);
      }

    }
    try {
      ji.kbSearch.closeConnection();
    } catch (Exception ex) {
    }
    LOG.info(new Date());
  }
}
