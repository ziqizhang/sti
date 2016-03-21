package uk.ac.shef.dcs.oak.sti.experiment;

import com.google.api.client.http.HttpResponseException;
import org.apache.any23.util.FileUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher_Freebase;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.*;
import uk.ac.shef.dcs.oak.sti.io.LTableAnnotationWriter;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.maincol.MainColumnFinder;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.selector.CellSelector;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.selector.Sampler_LTableContentCell_OSPD_nonEmpty;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.selector.LTableContentRow_Sampler_nonEmpty;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.stopping.EntropyConvergence;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.websearch.bing.v2.MultiKeyStringSplitter;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import uk.ac.shef.dcs.oak.sti.xtractor.csv.TableXtractorCSV;

/**
 */
public class TestTableInterpretation_CSV {
    private static Logger log = Logger.getLogger(TestTableInterpretation_CSV.class.getName());

    //  no columns ignored
    public static int[] IGNORE_COLUMNS = new int[] {};

    public static void main(String[] args) throws IOException {
        String inFolder = args[0];
        String outFolder = args[1];
        String propertyFile = args[2]; //"D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties"
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertyFile));
        String cacheFolder = args[3]; //String cacheFolder = "D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr";
        String nlpResources = args[4]; //"D:\\Work\\lodie\\resources\\nlp_resources";
        int offset = Integer.valueOf(args[5]);
        boolean relationLearning = Boolean.valueOf(args[6]);
        //cache target location

        File configFile = new File(cacheFolder + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(cacheFolder,
                configFile);
        SolrServer server = new EmbeddedSolrServer(container, "collection1");

        //object to fetch things from KB
        KnowledgeBaseSearcher_Freebase freebaseMatcher = new KnowledgeBaseSearcher_Freebase(propertyFile, true, server, null, null);
        List<String> stopWords = uk.ac.shef.dcs.oak.util.FileUtils.readList(nlpResources + "/stoplist.txt", true);
        //object to find main subject column
        MainColumnFinder main_col_finder = new MainColumnFinder(
                new LTableContentRow_Sampler_nonEmpty(),
                EntropyConvergence.class.getName(),
                new String[] { "0.0", "1", "0.01" }, /*Stopping criteria params - double minimum_state_score_sum, int minimum_iterations, double convergence_threshold*/
                server, /* Solr server */
                nlpResources, false, stopWords,
                MultiKeyStringSplitter.split(properties.getProperty("BING_API_KEYS")));

        //stop words and stop properties (freebase) are used for disambiguation
        //List<String> stopProperties = FileUtils.readList("D:\\Work\\lodie\\resources\\nlp_resources/stopproperties_freebase.txt", true);

        //object to score columns, and disambiguate entities
        Disambiguator disambiguator = new Disambiguator(freebaseMatcher, new DisambiguationScorer_Overlap(
                stopWords,
                new double[] { 1.0, 0.5, 0.5, 1.0, 1.0 }, //row, column, tablecontext other, refent, tablecontext pagetitle (unused)
                nlpResources));
        ClassificationScorer class_scorer = new ClassificationScorer_Vote(nlpResources,
                new Creator_ConceptHierarchicalBOW_Freebase(),
                stopWords,
                new double[] { 1.0, 1.0, 1.0, 1.0 } //all 1.0
        );

        CellSelector selector = new Sampler_LTableContentCell_OSPD_nonEmpty();
        ColumnLearner_LEARN_Seeding column_learnerSeeding = new ColumnLearner_LEARN_Seeding(
                selector,
                EntropyConvergence.class.getName(),
                new String[] { "0.0", "2", "0.01" }, /*Stopping criteria params - double minimum_state_score_sum, int minimum_iterations, double convergence_threshold*/
                freebaseMatcher,
                disambiguator,
                class_scorer
                );
        /*ColumnLearner_LEARN_Seeding column_learnerSeeding = new ColumnLearner_LEARN_Seeding(
                selector,
                FixedNumberOfRows.class.getName(),
                new String[]{TableMinerConstants.SAMPLE_SIZE},
                freebaseMatcher,
                disambiguator,
                class_scorer
        );*/

        ColumnLearner_LEARN_Update column_updater = new ColumnLearner_LEARN_Update(
                freebaseMatcher, disambiguator, class_scorer
                );

        ColumnInterpreter columnInterpreter = new ColumnInterpreter(column_learnerSeeding, column_updater, TableMinerConstants.MAX_REFERENCE_ENTITY_FOR_DISAMBIGUATION);

        //object to interpret relations between columns
        HeaderBinaryRelationScorer relation_scorer = new HeaderBinaryRelationScorer_Vote(nlpResources,
                new Creator_RelationHierarchicalBOW_Freebase(),
                stopWords,
                new double[] { 1.0, 1.0, 0.0, 0.0, 1.0 } //entity, header text, column, title&caption, other
        );
        BinaryRelationInterpreter interpreter_relation = new BinaryRelationInterpreter(
                new RelationTextMatch_Scorer(0.0, stopWords),
                relation_scorer
                );

        //object to consolidate previous output, further score columns and disamgiuate entities
        ColumnInterpreter_relDepend interpreter_with_knownRelations = new ColumnInterpreter_relDepend_exclude_entity_col(
                IGNORE_COLUMNS
                );

        Backward_updater updater = null;//new Backward_updater(selector,freebaseMatcher,disambiguator,class_scorer,stopWords,nlpResources);
        MainInterpreter_and_Forward_learner interpreter = new MainInterpreter_and_Forward_learner(
                main_col_finder, /* object responsible for finding main column */
                columnInterpreter,
                interpreter_with_knownRelations,
                interpreter_relation,
                IGNORE_COLUMNS, new int[0],
                updater, relation_scorer);

        LTableAnnotationWriter writer = new LTableAnnotationWriter(
                new TripleGenerator("http://www.freebase.com", "http://lodie.dcs.shef.ac.uk"));

        // Table extractor
        TableXtractorCSV xtractor = new TableXtractorCSV();
        //        TableXtractorIMDB xtractor = new TableXtractorIMDB(new TableNormalizerFrequentRowLength(true),
        //                new TableHODetectorByHTMLTag(),
        //                new TableObjCreatorIMDB(),
        //                new TabValGeneric());
        int count = 0;
        List<File> all = Arrays.asList(new File(inFolder).listFiles());
        Collections.sort(all);
        log.info("Number of files to be processed: " + all.size());
        for (File f : all) {
            count++;
            if (count - 1 < offset)
                continue;

            boolean complete = false;
            String inFile = f.toString();
            log.info("\n\n*************** Processing file: " + f.getName());
            TimeWatch watch = TimeWatch.start();
            try {

                List<LTable> tables = xtractor.extract(f, inFile);
                log.info("Extraction of tables finished");
                log.info("Extracted: " + tables.size() + " tables");

                if (tables.size() == 0) {
                    log.warning("No tables");
                    continue;
                }

                LTable table = tables.get(0);

                String sourceTableFile = inFile;
                if (sourceTableFile.startsWith("\"") && sourceTableFile.endsWith("\""))
                    sourceTableFile = sourceTableFile.substring(1, sourceTableFile.length() - 1).trim();
                System.out.println(count + "_" + sourceTableFile + " " + new Date());
                log.info(">>>" + count + "_" + sourceTableFile);

                complete = process(f, interpreter, table, sourceTableFile, writer, outFolder, relationLearning);
                //server.commit();
                if (TableMinerConstants.COMMIT_SOLR_PER_FILE)
                    server.commit();
                if (!complete) {
                    System.out.println("\t\t\t missed: " + count + "_" + sourceTableFile);
                    PrintWriter missedWriter = null;
                    try {
                        missedWriter = new PrintWriter(new FileWriter("ti_imdb_missed.csv", true));
                        missedWriter.println(count + "," + inFile);
                        missedWriter.close();
                    } catch (IOException e1) {
                        e1.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
                log.info("Processed in seconds: " + passedTimeInSeconds);
                //gs annotator

            } catch (Exception e) {
                e.printStackTrace();
                PrintWriter missedWriter = null;
                try {
                    missedWriter = new PrintWriter(new FileWriter("ti_imdb_missed.csv", true));
                    missedWriter.println(count + "," + inFile);
                    missedWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
                }
                e.printStackTrace();
            }

        }
        server.shutdown();
        System.out.println(new Date());
    }

    public static boolean process(File f, MainInterpreter_and_Forward_learner interpreter, LTable table, String sourceTableFile, LTableAnnotationWriter writer, String outFolder, boolean relationLearning) throws Exception {
        String outFilename = sourceTableFile.replaceAll("\\\\", "/");
        try {
            LTableAnnotation annotations = interpreter.start(table, relationLearning);

            int startIndex = outFilename.lastIndexOf("/");
            if (startIndex != -1) {
                outFilename = outFilename.substring(startIndex + 1).trim();
            }
            writer.writeHTML(table, annotations, outFolder + "/" + outFilename + ".html");

            String basicStatsFileName = outFolder + "/" + "_basicStats";
            log.info("Writing basic stats to " + basicStatsFileName);
            writer.writeBasicSubjectColumnStatisticsCSV(f, table, annotations, basicStatsFileName);

            String classificationFileName = outFolder + "/" + "_classifyStats";
            log.info("Writing classification stats to " + classificationFileName);
            writer.writeClassificationStatisticsCSV(f, table, annotations, classificationFileName);

            String classificationAllFileName = outFolder + "/" + "_classifyStatsAll";
            log.info("Writing all classification stats to " + classificationAllFileName);
            writer.writeClassificationStatisticsAllConceptsCSV(f, table, annotations, classificationAllFileName);

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
}
