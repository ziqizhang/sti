package uk.ac.shef.dcs.sti.experiment;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.KBSearchFactory;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.smp.*;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.*;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.OSPD_nonEmpty;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPAttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPClazzScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPEntityScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPRelationScorer;
import uk.ac.shef.dcs.sti.core.feature.FreebaseConceptBoWCreator;
import uk.ac.shef.dcs.sti.core.feature.FreebaseRelationBoWCreator;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by - on 06/04/2016.
 */
public class SemanticMessagePassingBatch extends STIBatch {

    private static final Logger LOG = Logger.getLogger(SemanticMessagePassingBatch.class.getName());

    private static final String PROPERTY_SMP_USE_SUBJECT_COLUMN="sti.smp.usesubjectcolumn";
    private static final String PROPERTY_SMP_ENTITY_RANKER="sti.smp.entityranker";

    public SemanticMessagePassingBatch(String propertyFile) throws IOException, STIException {
        super(propertyFile);
    }

    @Override
    protected void initComponents() throws STIException {
        LOG.info("Initializing entity cache...");
        EmbeddedSolrServer kbEntityServer = this.getSolrServerCacheEntity();

        LOG.info("Initializing clazz cache...");
        EmbeddedSolrServer kbClazzServer = this.getSolrServerCacheClazz();
        //object to fetch things from KB

        LOG.info("Initializing KBSearch...");
        KBSearchFactory fbf = new KBSearchFactory();
        try {
            kbSearch = fbf.createInstance(properties.getProperty(PROPERTY_KBSEARCH_CLASS),
                    getAbsolutePath(PROPERTY_KBSEARCH_PROP_FILE),
                    Boolean.valueOf(properties.getProperty(PROPERTY_KBSEARCH_TRY_FUZZY_KEYWORD, "false")),
                    kbEntityServer, kbClazzServer, null);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            throw new STIException("Failed initialising KBSearch:" +
                    getAbsolutePath(PROPERTY_KBSEARCH_CLASS)
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
                    properties.getProperty(PROPERTY_WEBSEARCH_CLASS),
                    getAbsolutePath(PROPERTY_WEBSEARCH_PROP_FILE)
            );//   dobs
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            throw new STIException("Failed initialising SUBJECT COLUMN DETECTION components:" + properties.getProperty(PROPERTY_KBSEARCH_CLASS)
                    , e);
        }

        LOG.info("Initializing SMP components ...");
        try {
            String neRanker = properties.getProperty(PROPERTY_SMP_ENTITY_RANKER);
            EntityScorer entityScorer=null;
            if (neRanker != null && neRanker.equalsIgnoreCase("tmp")) {
                new TMPEntityScorer(
                        getStopwords(),
                        new double[]{1.0, 0.5, 1.0, 0.5}, //row,column, column header, tablecontext all
                        getNLPResourcesDir());
            } else
                entityScorer = new SMPAdaptedEntityScorer(getStopwords(),getNLPResourcesDir());

            interpreter = new SMPInterpreter(
                    subcolDetector,
                    Boolean.valueOf(properties.getProperty(PROPERTY_SMP_USE_SUBJECT_COLUMN,"false")),
                            new NamedEntityRanker(kbSearch, entityScorer),
                            new ColumnClassifier(kbSearch),
                            new RelationLearner(new RelationTextMatch_Scorer(0.5, getStopwords(), new Levenshtein())),
                            getIgnoreColumns(),
                            getMustdoColumns()
                    );
        }catch (Exception e){
            e.printStackTrace();
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            throw new STIException("Failed initialising SMP components:" + properties.getProperty(PROPERTY_KBSEARCH_CLASS)
                    , e);
        }

    }

    @Override
    protected List<Table> loadTable(String file) {
        return null;
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
                            smp.writer, outFolder,
                            Boolean.valueOf(smp.properties.getProperty(PROPERTY_PERFORM_RELATION_LEARNING)));

                    if (STIConstantProperty.COMMIT_SOLR_PER_FILE)
                        smp.commitAll();
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
        smp.closeAll();
        LOG.info(new Date());
    }
}
