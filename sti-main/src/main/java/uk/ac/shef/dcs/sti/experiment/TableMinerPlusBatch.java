package uk.ac.shef.dcs.sti.experiment;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.simmetrics.metrics.StringMetrics;
import uk.ac.shef.dcs.kbsearch.KBSearchFactory;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.*;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPClazzScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPEntityScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPRelationScorer;
import uk.ac.shef.dcs.sti.core.feature.FreebaseConceptBoWCreator;
import uk.ac.shef.dcs.sti.core.feature.FreebaseRelationBoWCreator;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.OSPD_nonEmpty;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.io.*;
import java.util.*;

/**
 */
public class TableMinerPlusBatch extends STIBatch {

    protected static final String PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS = "sti.tmp.iinf.learning.stopping.class";
    protected static final String PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS_CONSTR_PARAM
            = "sti.tmp.iinf.learning.stopping.class.constructor.params";


    private static final Logger LOG = Logger.getLogger(TableMinerPlusBatch.class.getName());


    public TableMinerPlusBatch(String propertyFile) throws IOException, STIException {
        super(propertyFile);
    }

    //initialise kbsearcher, websearcher
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

    public static void main(String[] args) throws IOException, STIException {
        String inFolder = args[0];
        String outFolder = args[1];
        TableMinerPlusBatch tmp = new TableMinerPlusBatch(args[2]);

        int count = 0;
        List<File> all = Arrays.asList(new File(inFolder).listFiles());
        Collections.sort(all);
        LOG.info("Initialization complete. Begin STI. Total input files=" + all.size() + "\n");

        List<Integer> previouslyFailed = tmp.loadPreviouslyFailed();
        int start = tmp.getStartIndex();
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
                List<Table> tables = tmp.loadTable(inFile);
                if (tables.size() == 0)
                    tmp.recordFailure(count, inFile, inFile);

                for (Table table : tables) {
                    complete = tmp.process(
                            table,
                            sourceTableFile,
                            tmp.getTAnnotationWriter(), outFolder,
                            Boolean.valueOf(tmp.properties.getProperty(PROPERTY_PERFORM_RELATION_LEARNING)));

                    if (STIConstantProperty.SOLR_COMMIT_PER_FILE)
                        tmp.kbSearch.commitChanges();
                    if (!complete) {
                        tmp.recordFailure(count, sourceTableFile, inFile);
                    }
                }
                //gs annotator

            } catch (Exception e) {
                e.printStackTrace();
                tmp.recordFailure(count, inFile, inFile);
            }

        }
        try {
            tmp.kbSearch.closeConnection();
        }
        catch (Exception e){
        }
        LOG.info(new Date());
    }


}
