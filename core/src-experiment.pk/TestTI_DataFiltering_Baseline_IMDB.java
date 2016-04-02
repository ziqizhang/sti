package uk.ac.shef.dcs.sti.experiment;

import com.google.api.client.http.HttpResponseException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.sti.algorithm.tm.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearch;
import uk.ac.shef.dcs.sti.algorithm.tm.*;
import uk.ac.shef.dcs.sti.algorithm.tm.sampler.Random;
import uk.ac.shef.dcs.sti.io.TAnnotationWriter;
import uk.ac.shef.dcs.sti.algorithm.tm.sampler.*;
import uk.ac.shef.dcs.sti.algorithm.tm.stopping.IInf;
import uk.ac.shef.dcs.sti.rep.TAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.sti.xtractor.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.xtractor.TableNormalizerFrequentRowLength;
import uk.ac.shef.dcs.sti.xtractor.TableObjCreatorIMDB;
import uk.ac.shef.dcs.sti.xtractor.TableXtractorIMDB;
import uk.ac.shef.dcs.sti.xtractor.validator.TabValGeneric;
import uk.ac.shef.dcs.util.FileUtils;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Logger;

/**
 */
public class TestTI_DataFiltering_Baseline_IMDB {
    private static Logger log = Logger.getLogger(TestTI_DataFiltering_Baseline_IMDB.class.getName());
    public static int[] IGNORE_COLUMNS = new int[]{};

    public static void main(String[] args) throws IOException {
        String inFolder = args[0];
        String outFolder = args[1];
        String propertyFile = args[2]; //"D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties"
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertyFile));
        String cacheFolder = args[3];  //String cacheFolder = "D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr";
        String nlpResources = args[4]; //"D:\\Work\\lodie\\resources\\nlp_resources";
        int start = Integer.valueOf(args[5]);
        boolean relationLearning = Boolean.valueOf(args[6]);
        int cellSelector = Integer.valueOf(args[7]);
        //cache target location

        List<Integer> missed_files = new ArrayList<Integer>();
        if (args.length == 9) {
            String in_missed = args[8];
            for (String line : FileUtils.readList(in_missed, false)) {
                missed_files.add(Integer.valueOf(line.split(",")[0].trim()));
            }
        }


        File configFile = new File(cacheFolder + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(cacheFolder,
                configFile);
        SolrServer server = new EmbeddedSolrServer(container, "collection1");


        //object to fetch things from KB
        FreebaseSearch freebaseMatcher = new FreebaseSearch(propertyFile,true, server, null,null);
/*        freebaseMatcher.find_typesForEntityId("/m/02hrh1q");
        server.closeConnection();
        System.exit(0);*/

        List<String> stopWords = FileUtils.readList(nlpResources + "/stoplist.txt", true);
        //object to find main subject column
        SubjectColumnDetector main_col_finder = new SubjectColumnDetector(
                new TContentTContentRowRankerImpl(),
                IInf.class.getName(),
                new String[]{"0.0", "1", "0.01"},
                server,
                nlpResources,
                TableMinerConstants.MAIN_COL_DETECT_USE_WEBSEARCH,
                //"/BlhLSReljQ3Koh+vDSOaYMji9/Ccwe/7/b9mGJLwDQ=");  //zqz.work
                //"fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0="); //ziqizhang
                stopWords,
                MultiKeyStringSplitter.split(properties.getProperty("BING_API_KEYS"))
                //"7ql9acl+fXXfdjBGIIAH+N2WHk/dIZxdSkl4Uur68Hg"
        );//   dobs


        //stop words and stop properties (freebase) are used for disambiguation
        //List<String> stopProperties = FileUtils.readList("D:\\Work\\lodie\\resources\\nlp_resources/stopproperties_freebase.txt", true);

        //object to computeElementScores columns, and disambiguate entities

        TCellDisambiguator disambiguator = new TCellDisambiguator(
                freebaseMatcher,
                new BaselineEntityScorer(
                        stopWords,
                        new double[]{1.0, 0.5, 0.5, 1.0, 1.0}, //row,column, tablecontext other,refent, tablecontext pagetitle (unused)
                        nlpResources));                         //1.0, 0.5, 0.25, 1.0, 1.0
        ClazzScorer class_scorer = new BaselineTColumnClassifier(
                nlpResources,
                //new FreebaseConceptBoWCreator(),
                stopWords
                //new double[]{1.0, 1.0, 1.0, 1.0}         //all 1.0
        );                                              //header,column,tablecontext other, page title+caption

        TContentCellRanker selector = null;
        if (cellSelector == 0) {
            selector = new Random();
        } else if (cellSelector == 1) {
            selector=new OSPD_nonEmpty();
        } else if (cellSelector == 2) {
            selector=new OSPD_contextWords(stopWords);
        }else if (cellSelector == 3) {
            selector=new OSPD_nameLength();
        }else if(cellSelector==4){
            selector=new OSPD_combined(stopWords);
        } else{
            selector=new OSPD_random();
        }

        LEARNINGPreliminaryColumnClassifier column_learnerSeeding = new LEARNINGPreliminaryColumnClassifier(
                selector,
                IInf.class.getName(),
                new String[]{"0.0", "2", "0.01"},
                freebaseMatcher,
                disambiguator,
                class_scorer
        );
        /*LEARNINGPreliminaryColumnClassifier column_learnerSeeding = new LEARNINGPreliminaryColumnClassifier(
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
                column_learnerSeeding, column_updater, TableMinerConstants.TCELLDISAMBIGUATOR_MAX_REFERENCE_ENTITIES);

        //object to computeElementScores relations between columns
        RelationScorer relation_scorer = new RelationScorer_Vote(nlpResources,
                new FreebaseRelationBoWCreator(),
                stopWords,
                new double[]{1.0, 1.0, 0.0, 0.0, 1.0}    //entity, header text, column, title&caption, other
                // new double[]{1.0, 1.0, 0.0, 0.0, 1.0}
        );
        TColumnColumnRelationEnumerator interpreter_relation = new TColumnColumnRelationEnumerator(
                new TMPAttributeValueMatcher(0.0, stopWords),
                relation_scorer
        );

        //object to consolidate previous output, further computeElementScores columns and disamgiuate entities
        DataLiteralColumnClassifier interpreter_with_knownRelations = new DataLiteralColumnClassifier_exclude_entity_col(
                IGNORE_COLUMNS
        );

        UPDATE updater = null;
        //new UPDATE(sampler, freebaseMatcher, disambiguator, class_scorer, stopWords, nlpResources);
        TMPInterpreter interpreter = new TMPInterpreter(
                main_col_finder,
                columnInterpreter,
                interpreter_with_knownRelations,
                interpreter_relation,
                IGNORE_COLUMNS, new int[0],
                updater, relation_scorer);

        TAnnotationWriter writer = new TAnnotationWriter(
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

                if (TableMinerConstants.COMMIT_SOLR_PER_FILE)
                    server.commit();
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
                        missedWriter = new PrintWriter(new FileWriter("imdb_missed.csv", true));
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
                    missedWriter = new PrintWriter(new FileWriter("imdb_missed.csv", true));
                    missedWriter.println(count + "," + inFile);
                    missedWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                e.printStackTrace();
                server.shutdown();
                System.exit(1);
            }

        }
        server.shutdown();
        System.out.println(new Date());
    }


    public static boolean process(TMPInterpreter interpreter, Table table, String sourceTableFile, TAnnotationWriter writer,
                                  String outFolder, boolean relationLearning) throws FileNotFoundException {
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
                ste.printStackTrace();

        }
        return true;
    }
}
