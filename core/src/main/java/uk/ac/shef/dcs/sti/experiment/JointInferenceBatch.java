package uk.ac.shef.dcs.sti.experiment;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;

import uk.ac.shef.dcs.kbsearch.KBSearchFactory;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;

import uk.ac.shef.dcs.sti.core.algorithm.ji.*;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by - on 13/04/2016.
 */
public class JointInferenceBatch extends STIBatch {
    private static final Logger LOG = Logger.getLogger(JointInferenceBatch.class.getName());
    private static final String PROPERTY_SIMILARITY_CACHE_CORENAME="similarity";
    private static final String PROPERTY_JI_USE_SUBJECT_COLUMN = "sti.ji.usesubjectcolumn";
    private static final String PROPERTY_JI_MAX_ITERATIONS = "sti.ji.maxiterations";

    private EmbeddedSolrServer simlarityServer;

    public JointInferenceBatch(String propertyFile) throws IOException, STIException {
        super(propertyFile);
    }

    private EmbeddedSolrServer getSolrServerCacheSimilarity() throws STIException {
        if (simlarityServer == null) {
            String solrHomePath = properties.getProperty(PROPERTY_CACHE_FOLDER);
            if (solrHomePath == null || !new File(solrHomePath).exists() || PROPERTY_SIMILARITY_CACHE_CORENAME == null) {
                String error = "Cannot proceed: the cache dir is not set or does not exist. " +
                        PROPERTY_CACHE_FOLDER + "=" + solrHomePath;
                LOG.error(error);
                throw new STIException(error);
            }
            if (cores == null) {
                simlarityServer = new EmbeddedSolrServer(Paths.get(solrHomePath), PROPERTY_SIMILARITY_CACHE_CORENAME);
                cores = simlarityServer.getCoreContainer();
            } else
                simlarityServer = new EmbeddedSolrServer(cores.getCore(PROPERTY_SIMILARITY_CACHE_CORENAME));
        }
        return simlarityServer;
    }

    @Override
    protected void initComponents() throws STIException {
        LOG.info("Initializing entity cache...");
        EmbeddedSolrServer kbEntityServer = this.getSolrServerCacheEntity();
        LOG.info("Initializing clazz cache...");
        EmbeddedSolrServer kbClazzServer = this.getSolrServerCacheClazz();
        LOG.info("Initializing property cache...");
        EmbeddedSolrServer kbPropertyServer = this.getSolrServerCacheRelation();
        LOG.info("Initializing similarity cache...");
        EmbeddedSolrServer simServer = this.getSolrServerCacheSimilarity();
        //object to fetch things from KB

        LOG.info("Initializing KBSearch...");
        KBSearchFactory fbf = new KBSearchFactory();
        try {
            kbSearch = fbf.createInstance(
                    getAbsolutePath(PROPERTY_KBSEARCH_PROP_FILE),
                    kbEntityServer, kbClazzServer, kbPropertyServer,simServer);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            throw new STIException("Failed initialising KBSearch:" +
                    getAbsolutePath(PROPERTY_KBSEARCH_PROP_FILE)
                    , e);
        }

        LOG.info("Initializing SUBJECT COLUMN DETECTION components ...");
        SubjectColumnDetector subcolDetector;
        try {
            subcolDetector = new SubjectColumnDetector(
                    new TContentTContentRowRankerImpl(),
                    properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS),
                    StringUtils.split(properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS_CONSTR_PARAM),
                            ','),
                    getSolrServerCacheWebsearch(),
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
                            new ClassificationScorer_JI_adapted(),
                            new EntityAndConceptScorer_Freebase(getStopwords(), getNLPResourcesDir()),
                            cores,true),
                    new CandidateRelationGenerator(
                            new JIAdaptedAttributeMatcher(STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE,getStopwords(), new Levenshtein()),
                            kbSearch,true),
                    Boolean.valueOf(properties.getProperty(PROPERTY_JI_USE_SUBJECT_COLUMN, "false")),
                    getIgnoreColumns(),
                    getMustdoColumns(),
                    Integer.valueOf(properties.getProperty(PROPERTY_JI_MAX_ITERATIONS,"10"))
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
                            ji.writer, outFolder,
                            Boolean.valueOf(ji.properties.getProperty(PROPERTY_PERFORM_RELATION_LEARNING)));

                    if (STIConstantProperty.SOLR_COMMIT_PER_FILE)
                        ji.commitAll();
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
        ji.closeAll();
        LOG.info(new Date());
    }
}
