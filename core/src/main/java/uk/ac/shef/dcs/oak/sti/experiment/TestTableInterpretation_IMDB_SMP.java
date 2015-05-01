package uk.ac.shef.dcs.oak.sti.experiment;

import com.google.api.client.http.HttpResponseException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.oak.sti.kb.KBSearcher_Freebase;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.DisambiguationScorer;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.DisambiguationScorer_Overlap;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.TripleGenerator;
import uk.ac.shef.dcs.oak.sti.io.LTableAnnotationWriter;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.maincol.MainColumnFinder;
import uk.ac.shef.dcs.oak.sti.algorithm.smp.*;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.sti.xtractor.validator.TabValGeneric;
import uk.ac.shef.dcs.oak.sti.xtractor.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.oak.sti.xtractor.TableNormalizerFrequentRowLength;
import uk.ac.shef.dcs.oak.sti.xtractor.TableObjCreatorIMDB;
import uk.ac.shef.dcs.oak.sti.xtractor.TableXtractorIMDB;
import uk.ac.shef.dcs.oak.util.FileUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by zqz on 30/04/2015.
 */
public class TestTableInterpretation_IMDB_SMP {
    private static Logger log = Logger.getLogger(TestTableInterpretation_IMDB_SMP.class.getName());

    public static int[] IGNORE_COLUMNS = new int[]{0, 2, 3};

    public static void main(String[] args) throws IOException {
        String inFolder = args[0];
        String outFolder = args[1];
        String propertyFile = args[2]; //"D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties"
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertyFile));
        String cacheFolderGeneral = args[3];  //String cacheFolder = "D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr";
        String cacheFolderConceptGranularity = args[4];
        String nlpResources = args[5]; //"D:\\Work\\lodie\\resources\\nlp_resources";
        int start = Integer.valueOf(args[6]);
        boolean relationLearning = Boolean.valueOf(args[7]);
        //cache target location
        List<Integer> missed_files = new ArrayList<Integer>();
        if (args.length == 9) {
            String in_missed = args[8];
            for (String line : FileUtils.readList(in_missed, false)) {
                missed_files.add(Integer.valueOf(line.split(",")[0].trim()));
            }
        }

        File configFile = new File(cacheFolderGeneral + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(cacheFolderGeneral,
                configFile);
        SolrServer serverGeneral = new EmbeddedSolrServer(container, "collection1");

        File configFile2 = new File(cacheFolderConceptGranularity + File.separator + "solr.xml");
        CoreContainer container2 = new CoreContainer(cacheFolderConceptGranularity,
                configFile2);
        SolrServer serverConceptGranularity = new EmbeddedSolrServer(container2, "collection1");

        //object to fetch things from KB
        //object to fetch things from KB
        KBSearcher_Freebase freebaseSearcherGeneral = new KBSearcher_Freebase(propertyFile, serverGeneral, true);
        KBSearcher_Freebase freebaseSearcherConceptGranularity = new KBSearcher_Freebase(propertyFile, serverConceptGranularity, true);
/*        freebaseMatcher.find_typesForEntityId("/m/02hrh1q");
        server.shutdown();
        System.exit(0);*/
        List<String> stopWords = uk.ac.shef.dcs.oak.util.FileUtils.readList(nlpResources + "/stoplist.txt", true);

        MainColumnFinder main_col_finder = new MainColumnFinder(
                cacheFolderGeneral,
                nlpResources,
                false,
                stopWords
        );//   dobs
        //object to find main subject column
        boolean useSubjectColumn = Boolean.valueOf(properties.getProperty("SMP_USE_SUBJECT_COLUMN"));
        String neRankerChoice = properties.getProperty("SMP_NAMED_ENTITY_RANKER");
        DisambiguationScorer disambiguator;
        if (neRankerChoice != null && neRankerChoice.equalsIgnoreCase("tableminer")) {
            disambiguator = new DisambiguationScorer_Overlap(
                    stopWords,
                    new double[]{1.0, 0.5, 0.5, 1.0, 1.0}, //row,column, tablecontext other,refent, tablecontext pagetitle (unused)
                    nlpResources);
        } else
            disambiguator = new DisambiguationScorer_SMP_adapted(stopWords, nlpResources);

        //DisambiguationScorer disambiguator = new DisambiguationScorer_SMP_adapted(stopWords, nlpResources);
        TI_SemanticMessagePassing interpreter = new TI_SemanticMessagePassing(
                main_col_finder,
                useSubjectColumn,
                new NamedEntityRanker(freebaseSearcherGeneral, disambiguator),
                new ColumnClassifier(freebaseSearcherConceptGranularity),
                new RelationLearner(new RelationTextMatch_Scorer(stopWords, new Levenshtein(), 0.5)),
                IGNORE_COLUMNS,
                new int[0]
        );


        LTableAnnotationWriter writer = new LTableAnnotationWriter_SMP(
                new TripleGenerator("http://www.freebase.com", "http://lodie.dcs.shef.ac.uk"));

        TableXtractorIMDB xtractor = new TableXtractorIMDB(new TableNormalizerFrequentRowLength(true),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorIMDB(),
                new TabValGeneric());
        int count = 0;
        List<File> all = Arrays.asList(new File(inFolder).listFiles());
        Collections.sort(all);
        System.out.println(all.size());
        int[] onlyDo = new int[]{52, 56};

        for (File f : all) {
            count++;

            /* boolean found=false;
        for(int od: onlyDo){
            if(od==count)
                found=true;
        }
        if(!found)
            continue;*/

            if (missed_files.size() != 0 && !missed_files.contains(count))
                continue;

            if (count - 1 < start)
                continue;
            boolean complete = false;
            String inFile = f.toString();
            /*if (!inFile.contains("994e"))
                continue;*///System.out.println("start debugging");;
            /*if(inFile.contains("Daytona")){
                TableMinerConstants.enable_forceQuery();
            }*/

            try {
                String fileContent = org.apache.any23.util.FileUtils.readFileContent(new File(inFile));
                List<LTable> tables = xtractor.extract(fileContent, inFile);
                if (tables.size() == 0)
                    continue;

                LTable table = tables.get(0);

                String sourceTableFile = inFile;
                if (sourceTableFile.startsWith("\"") && sourceTableFile.endsWith("\""))
                    sourceTableFile = sourceTableFile.substring(1, sourceTableFile.length() - 1).trim();
                System.out.println(count + "_" + sourceTableFile + " " + new Date());
                log.info(">>>" + count + "_" + sourceTableFile);

                complete = process(interpreter, table, sourceTableFile, writer, outFolder, relationLearning);

                if (TableMinerConstants.COMMIT_SOLR_PER_FILE) {
                    serverGeneral.commit();
                    serverConceptGranularity.commit();
                }
                /**************check bugged cache/load for "Deep Space 9" in Seinfeld document*****************/
                /*   if (inFile.contains("Seinfeld")) {
               List<String[]> facts = freebaseMatcher.find_triplesForEntity(new EntityCandidate("/m/06qw_", ""));
               String mid = "";
               for (String[] ft : facts) {
                   if (ft[0].equals("/type/object/type") && ft[3].equals("n")) {
                       mid = mid + "," + ft[2];
                   }
               }
               System.out.println("---------" + facts.size() + ", " + mid);
                }*/

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
                serverGeneral.shutdown();
                serverConceptGranularity.shutdown();
                System.exit(1);
            }

        }
        serverGeneral.shutdown();
        serverConceptGranularity.shutdown();
        System.out.println(new Date());
        System.exit(0);
    }


    public static boolean process(TI_SemanticMessagePassing interpreter, LTable table, String sourceTableFile, LTableAnnotationWriter writer,
                                  String outFolder, boolean relationLearning) throws FileNotFoundException {
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
                ste.printStackTrace();

        }
        return true;
    }
}
