package uk.ac.shef.dcs.sti.experiment;

import com.google.api.client.http.HttpResponseException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.kbsearch.KBSearchFactory;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.io.TAnnotationWriterJSON;
import uk.ac.shef.dcs.sti.util.TripleGenerator;
import uk.ac.shef.dcs.sti.io.TAnnotationWriter;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.FileUtils;
import uk.ac.shef.dcs.sti.parser.table.TableParser;

import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

/**
 *
 */
public abstract class STIBatch {


  private static Logger LOG = Logger.getLogger(STIBatch.class.getName());

  protected KBSearch kbSearch;

  protected TableParser tableParser;

  protected SemanticTableInterpreter interpreter;

  protected static final String PROPERTY_HOME = "sti.home";

  protected static final String PROPERTY_WEBSEARCH_PROP_FILE = "sti.websearch.properties";

  protected static final String PROPERTY_NLP_RESOURCES = "sti.nlp";

  protected static final String PROPERTY_PERFORM_RELATION_LEARNING = "sti.learning.relation";

  protected static final String PROPERTY_CACHE_FOLDER = "sti.cache.main.dir";

  protected static final String PROPERTY_WEBSEARCH_CACHE_CORENAME = "websearch";

  protected static final String PROPERTY_START_INDEX = "sti.start";
  protected static final String PROPERTY_SELECT_LIST = "sti.list.select";
  protected static final String PROPERTY_FAILED_LIST = "sti.list.failure";

  protected static final String PROPERTY_KBSEARCH_PROP_FILE = "sti.kbsearch.propertyfile";

  protected static final String PROPERTY_IGNORE_COLUMNS = "sti.columns.ignore";
  protected static final String PROPERTY_MUSTDO_COLUMNS = "sti.columns.mustdo";

  protected static final String PROPERTY_OUTPUT_TRIPLE_KB_NAMESPACE = "sti.output.triple.namespace.kb";
  protected static final String PROPERTY_OUTPUT_TRIPLE_DEFAULT_NAMESPACE = "sti.output.triple.namespace.default";

  protected static final String PROPERTY_TABLEXTRACTOR_CLASS = "sti.input.parser.class";

  protected static final String PROPERTY_TMP_SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH =
      "sti.subjectcolumndetection.ws";
  protected static final String PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS = "sti.iinf.websearch.stopping.class";
  protected static final String PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS_CONSTR_PARAM
      = "sti.iinf.websearch.stopping.class.constructor.params";

  protected Properties properties;

  protected TAnnotationWriter writer;

  public STIBatch(String propertyFile) throws IOException, STIException {
    properties = new Properties();
    properties.load(new FileInputStream(propertyFile));
    initComponents();
        /*writer = new TAnnotationWriter(new TripleGenerator(
                properties.getProperty(PROPERTY_OUTPUT_TRIPLE_KB_NAMESPACE), properties.getProperty(PROPERTY_OUTPUT_TRIPLE_DEFAULT_NAMESPACE)
        ));*/
  }

  /**
   * Initialize kbsearchers, websearcher. implementing class to decide which are compulsory and how to
   * handle exceptions
   */
  protected abstract void initComponents() throws STIException;

  protected List<Table> loadTable(String file) {
    try {
      return getTableParser().extract(file, file);
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  protected int getStartIndex() {
    String s = properties.get(PROPERTY_START_INDEX).toString();
    if (s == null)
      return 0;
    return Integer.valueOf(s);
  }

  protected TableParser getTableParser() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    if (tableParser == null) {
      String clazz = properties.get(PROPERTY_TABLEXTRACTOR_CLASS).toString();
      tableParser = (TableParser) Class.forName(clazz).newInstance();
    }
    return tableParser;
  }

  protected String getKBSearchPropFile() throws STIException {
    String prop = properties.getProperty(PROPERTY_KBSEARCH_PROP_FILE);
    if (prop == null || !new File(prop).exists()) {
      String error = "Cannot proceed: the property file for your kbsearch module is not set or does not exist. " +
          PROPERTY_KBSEARCH_PROP_FILE + "=" + prop;
      LOG.error(error);
      throw new STIException(error);
    }
    return prop;
  }


  protected String getNLPResourcesDir() throws STIException {
    String prop = getAbsolutePath(PROPERTY_NLP_RESOURCES);
    if (prop == null || !new File(prop).exists()) {
      String error = "Cannot proceed: nlp resources folder is not set or does not exist. " +
          PROPERTY_KBSEARCH_PROP_FILE + "=" + prop;
      LOG.error(error);
      throw new STIException(error);
    }
    return prop;
  }

  protected List<String> getStopwords() throws STIException, IOException {
    return FileUtils.readList(getNLPResourcesDir() + File.separator + "stoplist.txt", true);
  }

  protected int[] getIgnoreColumns() {
    String ignore = properties.getProperty(PROPERTY_IGNORE_COLUMNS);
    String[] splits = StringUtils.split(ignore, ',');
    int[] res = new int[splits.length];
    for (int i = 0; i < splits.length; i++) {
      res[i] = Integer.valueOf(splits[i].trim());
    }
    return res;
  }

  protected int[] getMustdoColumns() {
    String ignore = properties.getProperty(PROPERTY_MUSTDO_COLUMNS);
    String[] splits = StringUtils.split(ignore, ',');
    int[] res = new int[0];
    for (int i = 0; i < splits.length; i++) {
      res[i] = Integer.valueOf(splits[i].trim());
    }
    return res;
  }

  protected TAnnotationWriter getTAnnotationWriter() {
    if (writer == null) {
            /*writer = new TAnnotationWriter(
                    new TripleGenerator(properties.getProperty(PROPERTY_OUTPUT_TRIPLE_KB_NAMESPACE),
                            properties.getProperty(PROPERTY_OUTPUT_TRIPLE_DEFAULT_NAMESPACE)));*/
      writer = new TAnnotationWriterJSON(
          new TripleGenerator(properties.getProperty(PROPERTY_OUTPUT_TRIPLE_KB_NAMESPACE),
              properties.getProperty(PROPERTY_OUTPUT_TRIPLE_DEFAULT_NAMESPACE)));
    }
    return writer;
  }

  protected boolean process(Table table, String sourceTableFile, TAnnotationWriter writer,
                            String outFolder,
                            boolean relationLearning) throws Exception {
    File outDir = new File(outFolder);
    if (!outDir.exists())
      outDir.mkdirs();
    String outFilename = sourceTableFile.replaceAll("\\\\", "/");
    try {
      TAnnotation annotations = interpreter.start(table, relationLearning);

      int startIndex = outFilename.lastIndexOf("/");
      if (startIndex != -1) {
        outFilename = outFilename.substring(startIndex + 1).trim();
      }
      writer.writeHTML(table, annotations, outFolder + "/" + outFilename + ".html");

    } catch (Exception ste) {
      if (ste instanceof SocketTimeoutException || ste instanceof HttpResponseException) {
        ste.printStackTrace();
        System.out.println("Remote server timed out, continue 10 seconds. Missed." + outFilename);
        try {
          Thread.sleep(10000);
        } catch (Exception e) {
        }
        return false;
      } else
        throw ste;

    }
    return true;
  }

  protected void recordFailure(int count, String sourceTableFile, String inFile) {
    System.out.println("\t\t\t missed: " + count + "_" + sourceTableFile);
    PrintWriter missedWriter = null;
    try {
      missedWriter = new PrintWriter(new FileWriter(properties.getProperty(PROPERTY_FAILED_LIST), true));
      missedWriter.println(count + "," + inFile);
      missedWriter.close();
    } catch (IOException e1) {
      e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  protected List<Integer> loadPreviouslyFailed() {
    List l =
        null;
    File f = new File(getAbsolutePath(PROPERTY_SELECT_LIST));
    if (properties.getProperty(PROPERTY_SELECT_LIST) == null
        || properties.getProperty(PROPERTY_SELECT_LIST).length() == 0
        || !f.exists()) {
      LOG.info("No sub-list of input files provided. All files will be processed. ");
      return new ArrayList<>();
    }
    try {
      l = org.apache.commons.io.FileUtils.readLines(
          f);
    } catch (IOException e) {
      e.printStackTrace();
    }
    List<Integer> selected = new ArrayList<>();
    for (Object o : l) {
      String line = o.toString();
      String index = line.split(",")[0].trim();
      if (index.length() > 0)
        selected.add(Integer.valueOf(index));
    }
    return selected;
  }

  protected String getAbsolutePath(String propertyName) {
    return combinePaths(properties.getProperty(PROPERTY_HOME), properties.getProperty(propertyName));
  }

  void initKB() throws STIException {
    KBSearchFactory factory = new KBSearchFactory();
    try {
      kbSearch = factory.createInstances(
          properties.getProperty(PROPERTY_KBSEARCH_PROP_FILE),
          properties.getProperty(PROPERTY_CACHE_FOLDER),
          properties.getProperty(PROPERTY_HOME)).iterator().next();
      kbSearch.initializeCaches();
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(ExceptionUtils.getFullStackTrace(e));
      throw new STIException("Failed initialising KBSearch:" +
          getAbsolutePath(PROPERTY_KBSEARCH_PROP_FILE)
          , e);
    }
  }
}
