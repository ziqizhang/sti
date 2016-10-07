package uk.ac.shef.dcs.sti.experiment;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
import uk.ac.shef.dcs.kbsearch.KBSearchFactory;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.baseline.*;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTagger;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTaggerImpl;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.TColumnColumnRelationEnumerator;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by - on 12/04/2016.
 */
public class BaselineBatch extends STIBatch {

    private static final String BASELINE_METHOD="sti.baseline.method"; //nm=name match; sim=similarity
    private static final String BASELINE_SIMILARITY_STRING_METRIC="sti.baseline.similarity.stringmetrics.method";

    private static final Logger LOG = Logger.getLogger(BaselineBatch.class.getName());
    private StringMetric stringMetric;
    public BaselineBatch(String propertyFile) throws IOException, STIException {
        super(propertyFile);
    }

    private TCellDisambiguator getCellDisambiguator() throws STIException, IOException {
        String method = properties.getProperty(BASELINE_METHOD,"nm");
        if(method.equals("nm"))
            return new TCellDisambiguatorNameMatch(kbSearch);
        else if(method.equals("sim")){
            if(stringMetric==null)
                stringMetric=getStringMetric();
            return new TCellDisambiguatorSimilarity(kbSearch,
                    new BaselineSimilarityEntityScorer(getStopwords(), getNLPResourcesDir(),
                            stringMetric));
        }else
            throw new STIException("Not supported");
    }

    private TColumnClassifier getColumnClassifier() throws STIException, IOException {
        String method = properties.getProperty(BASELINE_METHOD,"nm");
        if(method.equals("nm"))
            return new TColumnClassifierNameMatch();
        else if(method.equals("sim")){
            if(stringMetric==null)
                stringMetric=getStringMetric();
            return new TColumnClassifierSimilarity(
                    new BaselineSimilarityClazzScorer(getNLPResourcesDir(), getStopwords(),
                            stringMetric));
        }else
            throw new STIException("Not supported");
    }

    private StringMetric getStringMetric() throws STIException{
        try {
            Method m=StringMetrics.class.getDeclaredMethod(
                    properties.getProperty(BASELINE_SIMILARITY_STRING_METRIC)
            );
            if(m!=null){
                return (StringMetric)m.invoke(StringMetrics.class);
            }
            else
                throw new STIException("Not supported string similarity method");
        }catch (Exception e){
            throw new STIException(e);
        }

    }

    @Override
    protected void initComponents() throws STIException {
        //object to fetch things from KB

        LOG.info("Initializing KBSearch...");
        initKB();

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
                    kbSearch.getSolrServer(PROPERTY_WEBSEARCH_CACHE_CORENAME),
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

            //object to computeElementScores relations between columns
            TColumnColumnRelationEnumerator interpreter_relation = new TColumnColumnRelationEnumerator(
                    new AttributeValueMatcher(STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE,
                            getStopwords(),
                            StringMetrics.levenshtein()),
                    new BaselineRelationScorer()
            );


            LiteralColumnTagger literalColumnTagger = new LiteralColumnTaggerImpl(
                    getIgnoreColumns()
            );
            interpreter = new BaselineInterpreter(
                    subcolDetector,
                    getCellDisambiguator(),
                    getColumnClassifier(),
                    interpreter_relation, literalColumnTagger,
                    getIgnoreColumns(), getMustdoColumns());

        }catch (Exception e){
            throw new STIException(e);
        }

    }

    public static void main(String[] args) throws IOException, STIException {
        String inFolder = args[0];
        String outFolder = args[1];
        BaselineBatch baseline = new BaselineBatch(args[2]);

        int count = 0;
        List<File> all = Arrays.asList(new File(inFolder).listFiles());
        Collections.sort(all);
        LOG.info("Initialization complete. Begin STI. Total input files=" + all.size() + "\n");

        List<Integer> previouslyFailed = baseline.loadPreviouslyFailed();
        int start = baseline.getStartIndex();
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
                List<Table> tables = baseline.loadTable(inFile);
                if (tables.size() == 0)
                    baseline.recordFailure(count, inFile, inFile);

                for (Table table : tables) {
                    complete = baseline.process(
                            table,
                            sourceTableFile,
                            baseline.getTAnnotationWriter(), outFolder,
                            Boolean.valueOf(baseline.properties.getProperty(PROPERTY_PERFORM_RELATION_LEARNING)));

                    if (STIConstantProperty.SOLR_COMMIT_PER_FILE)
                        baseline.kbSearch.commitChanges();
                    if (!complete) {
                        baseline.recordFailure(count, sourceTableFile, inFile);
                    }
                }
                //gs annotator

            } catch (Exception e) {
                e.printStackTrace();
                baseline.recordFailure(count, inFile, inFile);
            }

        }
        try {
            baseline.kbSearch.closeConnection();
        }
        catch (Exception e){
        }
        LOG.info(new Date());
    }
}
