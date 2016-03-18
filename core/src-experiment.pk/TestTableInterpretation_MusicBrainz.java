package uk.ac.shef.dcs.sti.experiment;

import com.google.api.client.http.HttpResponseException;
import org.apache.any23.util.FileUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.sti.algorithm.tm.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearch;
import uk.ac.shef.dcs.sti.algorithm.tm.*;
import uk.ac.shef.dcs.sti.algorithm.tm.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.io.TAnnotationWriter;
import uk.ac.shef.dcs.sti.algorithm.tm.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.algorithm.tm.sampler.OSPD_nonEmpty;
import uk.ac.shef.dcs.sti.algorithm.tm.stopping.EntropyConvergence;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.sti.rep.LTableAnnotation;
import uk.ac.shef.dcs.sti.xtractor.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.xtractor.TableNormalizerDummy;
import uk.ac.shef.dcs.sti.xtractor.TableObjCreatorMusicBrainz;
import uk.ac.shef.dcs.sti.xtractor.TableXtractorMusicBrainz;
import uk.ac.shef.dcs.sti.xtractor.validator.TabValGeneric;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Logger;

/**
 */
public class TestTableInterpretation_MusicBrainz {
    private static Logger log = Logger.getLogger(TestTableInterpretation_MusicBrainz.class.getName());
    public static int[] IGNORE_COLUMNS = new int[]{7
    };

    public static void main(String[] args) throws IOException {
        String inFolder = args[0];
        String outFolder = args[1];
        String propertyFile = args[2]; //"D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties"
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertyFile));
        String cacheFolder = args[3];  //String cacheFolder = "D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr";
        String nlpResources = args[4]; //"D:\\Work\\lodie\\resources\\nlp_resources";
        int start = Integer.valueOf(args[5]);
        boolean relationLearning=Boolean.valueOf(args[6]);
        //cache target location

        File configFile = new File(cacheFolder + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(cacheFolder,
                configFile);
        SolrServer server = new EmbeddedSolrServer(container, "collection1");

        //object to fetch things from KB
        FreebaseSearch freebaseMatcher = new FreebaseSearch(propertyFile, true,server, null,null);
        List<String> stopWords = uk.ac.shef.dcs.util.FileUtils.readList(nlpResources + "/stoplist.txt", true);
        //object to find main subject column
        SubjectColumnDetector main_col_finder = new SubjectColumnDetector(
                new TContentTContentRowRankerImpl(),
                EntropyConvergence.class.getName(),
                new String[]{"0.0", "1", "0.01"},
                server,
                nlpResources, true, stopWords,
                //"tHSVoL7Xxn+sZ7N+gP081zev4wLvzeD5SWhyCpfFtbI"); //ziqi.zhang
                MultiKeyStringSplitter.split(properties.getProperty("BING_API_KEYS"))
                ); //annalisa


        //stop words and stop properties (freebase) are used for disambiguation
        //List<String> stopProperties = FileUtils.readList("D:\\Work\\lodie\\resources\\nlp_resources/stopproperties_freebase.txt", true);

        //object to score columns, and disambiguate entities
        TCellDisambiguator disambiguator = new TCellDisambiguator(freebaseMatcher, new TMPEntityScorer(
                stopWords,
                new double[]{1.0, 0.5, 0.5, 1.0, 1.0}, //row,column, tablecontext other,refent, tablecontext pagetitle (unused)
                nlpResources));
        TColumnClassifier class_scorer = new TMPTColumnClassifier(nlpResources,
                new Creator_ConceptHierarchicalBOW_Freebase(),
                stopWords,
                new double[]{1.0, 1.0,1.0, 1.0}         //all 1.0
        );

        TContentCellRanker selector=new OSPD_nonEmpty();
        LEARNINGPreliminaryClassify column_learnerSeeding = new LEARNINGPreliminaryClassify(
                selector,
                EntropyConvergence.class.getName(),
                new String[]{"0.0", "2", "0.01"},
                freebaseMatcher,
                disambiguator,
                class_scorer
        );
        /*LEARNINGPreliminaryClassify column_learnerSeeding = new LEARNINGPreliminaryClassify(
                sampler,
                FixedNumberOfRows.class.getName(),
                new String[]{TableMinerConstants.SAMPLE_SIZE},
                freebaseMatcher,
                disambiguator,
                class_scorer
        );*/

        LEARNINGPreliminaryDisamb column_updater = new LEARNINGPreliminaryDisamb(
                freebaseMatcher, disambiguator, class_scorer
        );


        LEARNING columnInterpreter = new LEARNING(
                column_learnerSeeding, column_updater,
                TableMinerConstants.TCELLDISAMBIGUATOR_MAX_REFERENCE_ENTITIES);

        //object to interpret relations between columns
        HeaderBinaryRelationScorer relation_scorer = new HeaderBinaryRelationScorer_Vote(nlpResources,
                new Creator_RelationHierarchicalBOW_Freebase(),
                stopWords,
                new double[]{1.0, 1.0, 0.0, 0.0, 1.0}    //entity, header text, column, title&caption, other
        );

        BinaryRelationInterpreter interpreter_relation = new BinaryRelationInterpreter(
                new RelationTextMatch_Scorer(0.0, stopWords),
                relation_scorer
        );

        //object to consolidate previous output, further score columns and disamgiuate entities
        DataLiteralColumnClassifier interpreter_with_knownRelations = new DataLiteralColumnClassifier_exclude_entity_col(
                IGNORE_COLUMNS
        );


        UPDATE updater = new UPDATE(selector,freebaseMatcher,disambiguator,class_scorer,stopWords,nlpResources);

        TMPInterpreter interpreter = new TMPInterpreter(
                main_col_finder,
                columnInterpreter,
                interpreter_with_knownRelations,
                interpreter_relation,
                IGNORE_COLUMNS,
                new int[]{1},
                updater,relation_scorer);

        TAnnotationWriter writer = new TAnnotationWriter(
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
            if (count - 1 < start)
                continue;
            boolean complete = false;
            String inFile = f.toString();
            /*if(!inFile.contains("1136361096"))
                continue;
*/
            try {
                String fileContent = FileUtils.readFileContent(new File(inFile));
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
                //server.commit();
                if(TableMinerConstants.COMMIT_SOLR_PER_FILE)
                    server.commit();
                if (!complete) {
                    System.out.println("\t\t\t missed: " + count + "_" + sourceTableFile);
                    PrintWriter missedWriter = null;
                    try {
                        missedWriter = new PrintWriter(new FileWriter("ti_musicbrainz_missed.csv", true));
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
                    missedWriter = new PrintWriter(new FileWriter("ti_musicbrainz_missed.csv", true));
                    missedWriter.println(count + "," + inFile);
                    missedWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                e.printStackTrace();
            }

        }
        server.shutdown();
        System.out.println(new Date());
    }


    public static boolean process(TMPInterpreter interpreter, Table table, String sourceTableFile, TAnnotationWriter writer,
                                  String outFolder,boolean relationLearning) throws Exception {
        String outFilename = sourceTableFile.replaceAll("\\\\", "/");
        try {
            LTableAnnotation annotations = interpreter.start(table,relationLearning);

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
