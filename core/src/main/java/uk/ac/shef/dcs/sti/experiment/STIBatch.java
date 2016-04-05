package uk.ac.shef.dcs.sti.experiment;

import com.google.api.client.http.HttpResponseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.TMPInterpreter;
import uk.ac.shef.dcs.sti.util.TripleGenerator;
import uk.ac.shef.dcs.sti.io.TAnnotationWriter;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.FileUtils;
import uk.ac.shef.dcs.sti.xtractor.table.TableXtractor;

import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 */
public abstract class STIBatch {


    private static Logger LOG = Logger.getLogger(STIBatch.class.getName());

    protected KBSearch kbSearch;

    protected TableXtractor tableXtractor;

    protected static final String PROPERTY_HOME = "sti.home";

    protected static final String PROPERTY_WEBSEARCH_PROP_FILE = "sti.websearch.properties";
    protected static final String PROPERTY_WEBSEARCH_CLASS = "sti.websearch.class";

    protected static final String PROPERTY_NLP_RESOURCES = "sti.nlp";

    protected static final String PROPERTY_PERFORM_RELATION_LEARNING = "sti.learning.relation";

    protected static final String PROPERTY_CACHE_FOLDER = "sti.cache.main.dir";

    private static final String PROPERTY_ENTITY_CACHE_CORENAME = "entity";
    private static final String PROPERTY_CLAZZ_CACHE_CORENAME = "class";
    private static final String PROPERTY_RELATION_CACHE_CORENAME = "relation";
    private static final String PROPERTY_WEBSEARCH_CACHE_CORENAME = "websearch";

    protected static final String PROPERTY_START_INDEX = "sti.start";
    protected static final String PROPERTY_SELECT_LIST = "sti.list.select";
    protected static final String PROPERTY_FAILED_LIST = "sti.list.failure";

    protected static final String PROPERTY_KBSEARCH_PROP_FILE = "sti.kbsearch.propertyfile";
    protected static final String PROPERTY_KBSEARCH_CLASS = "sti.kbsearch.class";
    protected static final String PROPERTY_KBSEARCH_TRY_FUZZY_KEYWORD = "sti.kbsearch.tryfuzzykeyword";

    protected static final String PROPERTY_IGNORE_COLUMNS = "sti.columns.ignore";
    protected static final String PROPERTY_MUSTDO_COLUMNS = "sti.columns.mustdo";

    protected static final String PROPERTY_OUTPUT_TRIPLE_KB_NAMESPACE = "sti.output.triple.namespace.kb";
    protected static final String PROPERTY_OUTPUT_TRIPLE_DEFAULT_NAMESPACE = "sti.output.triple.namespace.default";

    protected static final String PROPERTY_TABLEXTRACTOR_CLASS = "sti.input.tablextractor.class";


    protected Properties properties;

    protected TAnnotationWriter writer;

    private CoreContainer cores;
    private EmbeddedSolrServer entityCache;
    private EmbeddedSolrServer conceptCache;
    private EmbeddedSolrServer relationCache;
    private EmbeddedSolrServer websearchCache;

    public STIBatch(String propertyFile) throws IOException, STIException {
        properties = new Properties();
        properties.load(new FileInputStream(propertyFile));
        initComponents();
        writer = new TAnnotationWriter(new TripleGenerator(
                properties.getProperty(PROPERTY_OUTPUT_TRIPLE_KB_NAMESPACE), properties.getProperty(PROPERTY_OUTPUT_TRIPLE_DEFAULT_NAMESPACE)
        ));
    }

    /**
     * Initialize kbsearchers, websearcher. implementing class to decide which are compulsory and how to
     * handle exceptions
     */
    protected abstract void initComponents() throws STIException;

    protected abstract List<Table> loadTable(String file);


    protected int getStartIndex() {
        String s = properties.get(PROPERTY_START_INDEX).toString();
        if (s == null)
            return 0;
        return Integer.valueOf(s);
    }

    protected TableXtractor getTableXtractor() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(tableXtractor==null) {
            String clazz = properties.get(PROPERTY_TABLEXTRACTOR_CLASS).toString();
            tableXtractor= (TableXtractor) Class.forName(clazz).newInstance();
        }
        return tableXtractor;
    }

    protected EmbeddedSolrServer getSolrServerCacheEntity() throws STIException {
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

    protected EmbeddedSolrServer getSolrServerCacheClazz() throws STIException {
        if (conceptCache == null) {
            String solrHomePath = properties.getProperty(PROPERTY_CACHE_FOLDER);
            if (solrHomePath == null || !new File(solrHomePath).exists() || PROPERTY_CLAZZ_CACHE_CORENAME == null) {
                String error = "Cannot proceed: the cache dir is not set or does not exist. " +
                        PROPERTY_CACHE_FOLDER + "=" + solrHomePath;
                LOG.error(error);
                throw new STIException(error);
            }
            if (cores == null) {
                conceptCache = new EmbeddedSolrServer(Paths.get(solrHomePath), PROPERTY_CLAZZ_CACHE_CORENAME);
                cores = conceptCache.getCoreContainer();
            } else
                conceptCache = new EmbeddedSolrServer(cores.getCore(PROPERTY_CLAZZ_CACHE_CORENAME));
        }
        return conceptCache;
    }

    protected EmbeddedSolrServer getSolrServerCacheRelation() throws STIException {
        if (relationCache == null) {
            String solrHomePath = properties.getProperty(PROPERTY_CACHE_FOLDER);
            if (solrHomePath == null || !new File(solrHomePath).exists() || PROPERTY_RELATION_CACHE_CORENAME == null) {
                String error = "Cannot proceed: the cache dir is not set or does not exist. " +
                        PROPERTY_CACHE_FOLDER + "=" + solrHomePath;
                LOG.error(error);
                throw new STIException(error);
            }
            if (cores == null) {
                relationCache = new EmbeddedSolrServer(Paths.get(solrHomePath), PROPERTY_RELATION_CACHE_CORENAME);
                cores = relationCache.getCoreContainer();
            } else
                relationCache = new EmbeddedSolrServer(cores.getCore(PROPERTY_RELATION_CACHE_CORENAME));
        }
        return relationCache;
    }

    protected EmbeddedSolrServer getSolrServerCacheWebsearch() throws STIException {
        if (websearchCache == null) {
            String solrHomePath = properties.getProperty(PROPERTY_CACHE_FOLDER);
            if (solrHomePath == null || !new File(solrHomePath).exists() || PROPERTY_RELATION_CACHE_CORENAME == null) {
                String error = "Cannot proceed: the cache dir is not set or does not exist. " +
                        PROPERTY_CACHE_FOLDER + "=" + solrHomePath;
                LOG.error(error);
                throw new STIException(error);
            }
            if(cores==null) {
                websearchCache = new EmbeddedSolrServer(Paths.get(solrHomePath), PROPERTY_WEBSEARCH_CACHE_CORENAME);
                cores=websearchCache.getCoreContainer();
            }
            else
                websearchCache=new EmbeddedSolrServer(cores.getCore(PROPERTY_WEBSEARCH_CACHE_CORENAME));
        }
        return websearchCache;
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
            writer = new TAnnotationWriter(
                    new TripleGenerator(properties.getProperty(PROPERTY_OUTPUT_TRIPLE_KB_NAMESPACE),
                            properties.getProperty(PROPERTY_OUTPUT_TRIPLE_DEFAULT_NAMESPACE)));
        }
        return writer;
    }

    protected void commitAll() {
        if (entityCache != null)
            try {
                entityCache.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        if (conceptCache != null)
            try {
                conceptCache.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        if (relationCache != null)
            try {
                relationCache.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        if (websearchCache != null)
            try {
                websearchCache.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    protected void closeAll() {
        if (entityCache != null)
            try {
                entityCache.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        if (conceptCache != null)
            try {
                conceptCache.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        if (relationCache != null)
            try {
                relationCache.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        if (websearchCache != null)
            try {
                websearchCache.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }


    public static boolean process(TMPInterpreter interpreter,
                                  Table table, String sourceTableFile, TAnnotationWriter writer,
                                  String outFolder,
                                  boolean relationLearning) throws Exception {
        File outDir = new File(outFolder);
        if(!outDir.exists())
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
        return properties.getProperty(PROPERTY_HOME)
                + File.separator + properties.getProperty(propertyName);
    }
}
