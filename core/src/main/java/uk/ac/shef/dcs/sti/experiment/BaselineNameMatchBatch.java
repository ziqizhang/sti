package uk.ac.shef.dcs.sti.experiment;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.KBSearchFactory;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.baseline.Base_NameMatch_ColumnLearner;
import uk.ac.shef.dcs.sti.core.algorithm.baseline.Base_NameMatch_Disambiguator;
import uk.ac.shef.dcs.sti.core.algorithm.baseline.BaselineNameMatchInterpreter;
import uk.ac.shef.dcs.sti.core.algorithm.baseline.Baseline_BinaryRelationInterpreter;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTagger;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTaggerImpl;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by - on 12/04/2016.
 */
public class BaselineNameMatchBatch extends STIBatch {
    private static final Logger LOG = Logger.getLogger(BaselineNameMatchBatch.class.getName());
    public BaselineNameMatchBatch(String propertyFile) throws IOException, STIException {
        super(propertyFile);
    }

    @Override
    protected void initComponents() throws STIException {
        LOG.info("Initializing entity cache...");
        EmbeddedSolrServer kbEntityServer = this.getSolrServerCacheEntity();
        //object to fetch things from KB

        LOG.info("Initializing KBSearch...");
        KBSearchFactory fbf = new KBSearchFactory();
        try {
            kbSearch = fbf.createInstance(
                    getAbsolutePath(PROPERTY_KBSEARCH_PROP_FILE),
                    kbEntityServer, null, null);
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

        LOG.info("Initializing baseline STI components ...");
        try {
            Base_NameMatch_Disambiguator disambiguator = new Base_NameMatch_Disambiguator();

            Base_NameMatch_ColumnLearner column_learner = new Base_NameMatch_ColumnLearner(
                    kbSearch,
                    disambiguator
            );

            //object to computeElementScores relations between columns
            Baseline_BinaryRelationInterpreter interpreter_relation = new Baseline_BinaryRelationInterpreter(
                    new AttributeValueMatcher(STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE,
                            getStopwords(),
                            new Levenshtein())
            );


            LiteralColumnTagger interpreter_with_knownRelations = new LiteralColumnTaggerImpl(
                    getIgnoreColumns()
            );
            interpreter = new BaselineNameMatchInterpreter(
                    subcolDetector,
                    column_learner,
                    interpreter_relation, interpreter_with_knownRelations,
                    getIgnoreColumns(), getMustdoColumns());

        }catch (Exception e){
            throw new STIException(e);
        }

    }

    public static void main(String[] args) throws IOException, STIException {
        String inFolder = args[0];
        String outFolder = args[1];
        BaselineNameMatchBatch bnm = new BaselineNameMatchBatch(args[2]);

        int count = 0;
        List<File> all = Arrays.asList(new File(inFolder).listFiles());
        Collections.sort(all);
        LOG.info("Initialization complete. Begin STI. Total input files=" + all.size() + "\n");

        List<Integer> previouslyFailed = bnm.loadPreviouslyFailed();
        int start = bnm.getStartIndex();
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
                List<Table> tables = bnm.loadTable(inFile);
                if (tables.size() == 0)
                    bnm.recordFailure(count, inFile, inFile);

                for (Table table : tables) {
                    complete = bnm.process(
                            table,
                            sourceTableFile,
                            bnm.writer, outFolder,
                            Boolean.valueOf(bnm.properties.getProperty(PROPERTY_PERFORM_RELATION_LEARNING)));

                    if (STIConstantProperty.SOLR_COMMIT_PER_FILE)
                        bnm.commitAll();
                    if (!complete) {
                        bnm.recordFailure(count, sourceTableFile, inFile);
                    }
                }
                //gs annotator

            } catch (Exception e) {
                e.printStackTrace();
                bnm.recordFailure(count, inFile, inFile);
            }

        }
        bnm.closeAll();
        LOG.info(new Date());
    }
}
