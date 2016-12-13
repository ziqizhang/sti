package uk.ac.shef.dcs.sti.experiment;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.simmetrics.metrics.StringMetrics;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.smp.*;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPEntityScorer;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.TripleGenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by - on 06/04/2016.
 */
public class SemanticMessagePassingBatch extends STIBatch {

  private static final Logger LOG = LoggerFactory.getLogger(SemanticMessagePassingBatch.class.getName());

  private static final String PROPERTY_SMP_USE_SUBJECT_COLUMN = "sti.smp.usesubjectcolumn";
  private static final String PROPERTY_SMP_ENTITY_RANKER = "sti.smp.entityranker";
  private static final String PROPERTY_SMP_HALTING_CONFIDTION_MAX_ITERATION = "sti.smp.halting.maxiteration";
  private static final String PROPERTY_SMP_CLAZZ_SPECIFICITY_CALCULATOR = "sti.smp.clazzspecificitycalculator";
  private static final String PROPER_SMP_CHANGE_MESSAGE_SCORE_THRESHOLD = "sti.smp.changemessage.minscore";

  public SemanticMessagePassingBatch(String propertyFile) throws IOException, STIException {
    super(propertyFile);
    writer =
        new TAnnotationWriterSMP(
            new TripleGenerator(
                properties.getProperty(PROPERTY_OUTPUT_TRIPLE_KB_NAMESPACE), properties.getProperty(PROPERTY_OUTPUT_TRIPLE_DEFAULT_NAMESPACE)
            ));
  }

  private ClazzSpecificityCalculator getClazzSpecificityCalculator() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
    return (ClazzSpecificityCalculator)
        Class.forName(properties.getProperty(PROPERTY_SMP_CLAZZ_SPECIFICITY_CALCULATOR))
            .getDeclaredConstructor(KBProxy.class)
            .newInstance(kbSearch);
  }

  @Override
  protected void initComponents() throws STIException {
    //object to fetch things from KB

    LOG.info("Initializing KBProxy...");
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

    LOG.info("Initializing SMP components ...");
    try {
      String neRanker = properties.getProperty(PROPERTY_SMP_ENTITY_RANKER);
      EntityScorer entityScorer = null;
      if (neRanker != null && neRanker.equalsIgnoreCase("tmp")) {
        new TMPEntityScorer(
            getStopwords(),
            STIConstantProperty.SCORER_ENTITY_CONTEXT_WEIGHT,
            getNLPResourcesDir());
      } else if (neRanker != null && neRanker.equalsIgnoreCase("smpfreebase"))
        entityScorer = new SMPAdaptedEntityScorer(getStopwords(), getNLPResourcesDir(), kbSearch.getKbDefinition());
      else
        throw new STIException(neRanker + " is not a supported option for NE Ranker");

      Set<Integer> ignoreColumnSet = new HashSet<>();
      for (int i : getIgnoreColumns())
        ignoreColumnSet.add(i);
      SemanticMessagePassing smpAlgorithm = new SemanticMessagePassing(
          Integer.valueOf(properties.getProperty(PROPERTY_SMP_HALTING_CONFIDTION_MAX_ITERATION, "10")),
          Double.valueOf(properties.getProperty(PROPER_SMP_CHANGE_MESSAGE_SCORE_THRESHOLD, "0.5"))
      );
      interpreter = new SMPInterpreter(
          subcolDetector,
          new TCellEntityRanker(kbSearch, entityScorer),
          new TColumnClassifier(kbSearch, getClazzSpecificityCalculator()),
          new TColumnColumnRelationEnumerator(
              new AttributeValueMatcher(
                  STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE,
                  getStopwords(), StringMetrics.levenshtein()),
              ignoreColumnSet,
              Boolean.valueOf(properties.getProperty(PROPERTY_SMP_USE_SUBJECT_COLUMN, "false"))),
          smpAlgorithm,
          getIgnoreColumns(),
          getMustdoColumns()
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
    SemanticMessagePassingBatch smp = new SemanticMessagePassingBatch(args[2]);

    int count = 0;
    List<File> all = Arrays.asList(new File(inFolder).listFiles());
    Collections.sort(all);
    LOG.info("Initialization complete. Begin STI. Total input files=" + all.size() + "\n");

    List<Integer> previouslyFailed = smp.loadPreviouslyFailed();
    int start = smp.getStartIndex();
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
        List<Table> tables = smp.loadTable(inFile);
        if (tables.size() == 0)
          smp.recordFailure(count, inFile, inFile);

        for (Table table : tables) {
          complete = smp.process(
              table,
              sourceTableFile,
              smp.getTAnnotationWriter(), outFolder,
              Boolean.valueOf(smp.properties.getProperty(PROPERTY_PERFORM_RELATION_LEARNING)));

          if (STIConstantProperty.SOLR_COMMIT_PER_FILE)
            smp.kbSearch.commitChanges();
          if (!complete) {
            smp.recordFailure(count, sourceTableFile, inFile);
          }
        }
        //gs annotator

      } catch (Exception e) {
        e.printStackTrace();
        smp.recordFailure(count, inFile, inFile);
      }

    }
    try {
      smp.kbSearch.closeConnection();
    } catch (Exception ex) {
    }
    LOG.info(new Date().toString());
  }
}
