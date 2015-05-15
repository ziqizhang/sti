package uk.ac.shef.dcs.oak.sti.experiment;

import com.google.api.client.http.HttpResponseException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.oak.sti.algorithm.ji.*;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.TripleGenerator;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.maincol.MainColumnFinder;
import uk.ac.shef.dcs.oak.sti.io.LTableAnnotationWriter;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher_Freebase;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.util.FileUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by zqz on 06/05/2015.
 */
public class TestTableInterpretation_LimayeDataset_JI {
    private static Logger log = Logger.getLogger(TestTableInterpretation_LimayeDataset_JI.class.getName());
    public static int[] IGNORE_COLUMNS = new int[]{};

    public static void main(String[] args) throws IOException {
        String inFolder = args[0];
        String outFolder = args[1];
        String propertyFile = args[2]; //"D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties"
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertyFile));
        String cacheFolderEntity = args[3];  //String cacheFolder = "D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr";
        String cacheFolderConcept = args[4];
        String cacheFolderProperty = args[5];
        String cacheFolderSimilarity=args[6];
        String nlpResources = args[7]; //"D:\\Work\\lodie\\resources\\nlp_resources";
        int start = Integer.valueOf(args[8]);
        boolean relationLearning = Boolean.valueOf(args[9]);
        int multiThreads=Integer.valueOf(properties.getProperty("JI_NUM_OF_THREADS"));
        //cache target location

        List<Integer> missed_files = new ArrayList<Integer>();
        if (args.length == 11) {
            String in_missed = args[10];
            for (String line : FileUtils.readList(in_missed, false)) {
                missed_files.add(Integer.valueOf(line.split(",")[0].trim()));
            }
        }

        File configFile = new File(cacheFolderEntity + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(cacheFolderEntity,
                configFile);
        SolrServer serverEntity = new EmbeddedSolrServer(container, "collection1");

        File configFile2 = new File(cacheFolderConcept + File.separator + "solr.xml");
        CoreContainer container2 = new CoreContainer(cacheFolderConcept,
                configFile2);
        SolrServer serverConcept = new EmbeddedSolrServer(container2, "collection1");

        File configFile3 = new File(cacheFolderProperty + File.separator + "solr.xml");
        CoreContainer container3 = new CoreContainer(cacheFolderProperty,
                configFile3);
        SolrServer serverProperty =new EmbeddedSolrServer(container3, "collection1");

        File configFile4 = new File(cacheFolderSimilarity + File.separator + "solr.xml");
        CoreContainer container4 = new CoreContainer(cacheFolderSimilarity,
                configFile4);
        SolrServer serverSimilarity =new EmbeddedSolrServer(container4, "collection1");

        //object to fetch things from KB
        KnowledgeBaseSearcher_Freebase freebaseSearcherGeneral = new KnowledgeBaseSearcher_Freebase(propertyFile, true, serverEntity,
                serverConcept, serverProperty);
        freebaseSearcherGeneral.registerOtherCache(KnowledgeBaseSearcher_Freebase.NAME_SIMILARITY_CACHE,
                serverSimilarity);

        List<String> stopWords = uk.ac.shef.dcs.oak.util.FileUtils.readList(nlpResources + "/stoplist.txt", true);
        MainColumnFinder main_col_finder = new MainColumnFinder(
                cacheFolderEntity,
                nlpResources,
                false,
                stopWords
        );//   dobs
        //object to find main subject column
        boolean useSubjectColumn = Boolean.valueOf(properties.getProperty("JI_USE_SUBJECT_COLUMN"));

        Integer maxIteration=0;

        //DisambiguationScorer disambiguator = new DisambiguationScorer_SMP_adapted(stopWords, nlpResources);
        TI_JointInference interpreter = new TI_JointInference(
                main_col_finder,
                new CandidateEntityGenerator(freebaseSearcherGeneral,
                        new DisambiguationScorer_JI_adapted()),
                new CandidateConceptGenerator(freebaseSearcherGeneral,
                        new ClassificationScorer_JI_adapted(),
                        new EntityAndConceptScorer_Freebase(stopWords, nlpResources),
                        multiThreads),
                new CandidateRelationGenerator(new RelationTextMatcher_Scorer_JI_adapted(stopWords,
                        new Levenshtein(), 0.5),
                        freebaseSearcherGeneral, true),
                new FactorGraphBuilder(),
                useSubjectColumn,
                IGNORE_COLUMNS,
                new int[0],maxIteration
        );

        LTableAnnotationWriter writer = new LTableAnnotationWriter(
                new TripleGenerator("http://www.freebase.com", "http://lodie.dcs.shef.ac.uk"));

        int count = 0;
        List<File> all = Arrays.asList(new File(inFolder).listFiles());
        Collections.sort(all);
        System.out.println(all.size());

        for (File f : all) {
            count++;

            if (missed_files.size() != 0 && !missed_files.contains(count))
                continue;

            if (count - 1 < start)
                continue;
            boolean complete = false;
            String inFile = f.toString();

            try {
                LTable table = LimayeDatasetLoader.readTable(inFile, null, null);

                String sourceTableFile = inFile;
                if (sourceTableFile.startsWith("\"") && sourceTableFile.endsWith("\""))
                    sourceTableFile = sourceTableFile.substring(1, sourceTableFile.length() - 1).trim();
                System.out.println(count + "_" + sourceTableFile + " " + new Date());
                log.info(">>>" + count + "_" + sourceTableFile);

                complete = process(interpreter, table, sourceTableFile, writer, outFolder, relationLearning);

                if (TableMinerConstants.COMMIT_SOLR_PER_FILE) {
                    serverEntity.commit();
                    serverConcept.commit();
                }

                if (!complete) {
                    System.out.println("\t\t\t missed: " + count + "_" + sourceTableFile);
                    PrintWriter missedWriter = null;
                    try {
                        missedWriter = new PrintWriter(new FileWriter("limaye_missed.csv", true));
                        missedWriter.println(count + "," + inFile);
                        missedWriter.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                //gs annotator

            } catch (Exception e) {
                e.printStackTrace();
                PrintWriter missedWriter = null;
                try {
                    missedWriter = new PrintWriter(new FileWriter("limaye_missed.csv", true));
                    missedWriter.println(count + "," + inFile);
                    missedWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                e.printStackTrace();
                serverEntity.shutdown();
                serverConcept.shutdown();
                System.exit(1);
            }

        }
        serverEntity.shutdown();
        serverConcept.shutdown();
        serverProperty.shutdown();
        serverSimilarity.shutdown();
        System.out.println(new Date());
        System.exit(0);
    }


    public static boolean process(TI_JointInference interpreter, LTable table, String sourceTableFile,
                                  LTableAnnotationWriter writer,
                                  String outFolder, boolean relationLearning) throws Exception {
        String outFilename = sourceTableFile.replaceAll("\\\\", "/");
        try {
            LTableAnnotation annotations = interpreter.start(table, relationLearning);

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
}
