package uk.ac.shef.dcs.sti.experiment;

import com.google.api.client.http.HttpResponseException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearch;
import uk.ac.shef.dcs.sti.core.scorer.EntityScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.TMPEntityScorer;
import uk.ac.shef.dcs.sti.util.TripleGenerator;
import uk.ac.shef.dcs.sti.io.TAnnotationWriter;
import uk.ac.shef.dcs.sti.core.algorithm.smp.*;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.xtractor.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.xtractor.TableNormalizerDummy;
import uk.ac.shef.dcs.sti.xtractor.TableObjCreatorMusicBrainz;
import uk.ac.shef.dcs.sti.xtractor.TableXtractorMusicBrainz;
import uk.ac.shef.dcs.sti.xtractor.validator.TabValGeneric;
import uk.ac.shef.dcs.util.FileUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by zqz on 30/04/2015.
 */
public class TestTableInterpretation_MusicBrainz_SMP {
    private static Logger log = Logger.getLogger(TestTableInterpretation_MusicBrainz_SMP.class.getName());

    public static int[] IGNORE_COLUMNS=new int[]{7
    };

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
        SolrServer serverEntity = new EmbeddedSolrServer(container, "collection1");

        File configFile2 = new File(cacheFolderConceptGranularity + File.separator + "solr.xml");
        CoreContainer container2 = new CoreContainer(cacheFolderConceptGranularity,
                configFile2);
        SolrServer serverConcept = new EmbeddedSolrServer(container2, "collection1");

        //object to fetch things from KB
        //object to fetch things from KB
        FreebaseSearch freebaseSearch = new FreebaseSearch(propertyFile, true, serverEntity,serverConcept,null);

        List<String> stopWords = FileUtils.readList(nlpResources + "/stoplist.txt", true);

        SubjectColumnDetector main_col_finder = new SubjectColumnDetector(
                cacheFolderGeneral,
                nlpResources,
                false,
                stopWords
        );//   dobs
        //object to find main subject column
        boolean useSubjectColumn = Boolean.valueOf(properties.getProperty("SMP_USE_SUBJECT_COLUMN"));
        String neRankerChoice = properties.getProperty("SMP_NAMED_ENTITY_RANKER");
        EntityScorer disambiguator;
        if (neRankerChoice != null && neRankerChoice.equalsIgnoreCase("tableminer")) {
            disambiguator = new TMPEntityScorer(
                    stopWords,
                    new double[]{1.0, 0.5, 0.5, 1.0, 1.0}, //row,column, tablecontext other,refent, tablecontext pagetitle (unused)
                    nlpResources);
        } else
            disambiguator = new SMPAdaptedEntityScorer(stopWords, nlpResources);

        //EntityScorer disambiguator = new SMPAdaptedEntityScorer(stopWords, nlpResources);
        TI_SemanticMessagePassing interpreter = new TI_SemanticMessagePassing(
                main_col_finder,
                useSubjectColumn,
                new NamedEntityRanker(freebaseSearch, disambiguator),
                new ColumnClassifier(freebaseSearch),
                new RelationLearner(new RelationTextMatch_Scorer(stopWords, new Levenshtein(), 0.5)),
                IGNORE_COLUMNS,
                new int[0]
        );


        TAnnotationWriter writer = new TAnnotationWriter_SMP(
                new TripleGenerator("http://www.freebase.com", "http://lodie.dcs.shef.ac.uk"));

        TableXtractorMusicBrainz xtractor = new TableXtractorMusicBrainz(new TableNormalizerDummy(),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorMusicBrainz(),
                new TabValGeneric());
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
                String fileContent = org.apache.any23.util.FileUtils.readFileContent(new File(inFile));
                List<Table> tables = xtractor.extract(fileContent, inFile);

                if (tables.size() == 0)
                    continue;

                Table table = tables.get(0);

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
                        missedWriter = new PrintWriter(new FileWriter("mb_missed.csv", true));
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
                    missedWriter = new PrintWriter(new FileWriter("mb_missed.csv", true));
                    missedWriter.println(count + "," + inFile);
                    missedWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                e.printStackTrace();
            }

        }
        serverEntity.shutdown();
        serverConcept.shutdown();
        System.out.println(new Date());
        System.exit(0);
    }


    public static boolean process(TI_SemanticMessagePassing interpreter, Table table, String sourceTableFile, TAnnotationWriter writer,
                                  String outFolder, boolean relationLearning) throws Exception {
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
}
