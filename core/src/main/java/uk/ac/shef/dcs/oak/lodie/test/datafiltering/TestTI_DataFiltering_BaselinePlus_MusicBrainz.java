package uk.ac.shef.dcs.oak.lodie.test.datafiltering;

import com.google.api.client.http.HttpResponseException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.content.KBSearcher_Freebase;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.interpret.*;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.io.LTableAnnotationWriter;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.maincol.MainColumnFinder;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.selector.*;
import uk.ac.shef.dcs.oak.lodie.table.interpreter.stopping.EntropyConvergence;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.validator.TabValGeneric;
import uk.ac.shef.dcs.oak.lodie.table.xtractor.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.oak.lodie.table.xtractor.TableNormalizerDummy;
import uk.ac.shef.dcs.oak.lodie.table.xtractor.TableObjCreatorMusicBrainz;
import uk.ac.shef.dcs.oak.lodie.table.xtractor.TableXtractorMusicBrainz;
import uk.ac.shef.dcs.oak.lodie.test.TableMinerConstants;
import uk.ac.shef.dcs.oak.util.FileUtils;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 06/06/14
 * Time: 12:30
 * To change this template use File | Settings | File Templates.
 */
public class TestTI_DataFiltering_BaselinePlus_MusicBrainz {

    private static Logger log = Logger.getLogger(TestTI_DataFiltering_BaselinePlus_MusicBrainz.class.getName());
    public static int[] IGNORE_COLUMNS = new int[]{};

    public static void main(String[] args) throws IOException {
        String inFolder = args[0];
        String outFolder = args[1];
        String freebaseProperties = args[2]; //"D:\\Work\\lodiecrawler\\src\\main\\java/freebase.properties"
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
        KBSearcher_Freebase freebaseMatcher = new KBSearcher_Freebase(freebaseProperties, server, true);
/*        freebaseMatcher.find_typesForEntityId("/m/02hrh1q");
        server.shutdown();
        System.exit(0);*/

        List<String> stopWords = uk.ac.shef.dcs.oak.util.FileUtils.readList(nlpResources + "/stoplist.txt", true);
        //object to find main subject column
        MainColumnFinder main_col_finder = new MainColumnFinder(
                new LTableContentRow_Sampler_nonEmpty(),
                EntropyConvergence.class.getName(),
                new String[]{"0.0", "1", "0.01"},
                server,
                nlpResources,
                TableMinerConstants.MAIN_COL_DETECT_USE_WEBSEARCH,
                //"/BlhLSReljQ3Koh+vDSOaYMji9/Ccwe/7/b9mGJLwDQ=");  //zqz.work
                //"fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0="); //ziqizhang
                stopWords,
                "fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0=",
                "/BlhLSReljQ3Koh+vDSOaYMji9/Ccwe/7/b9mGJLwDQ=",//, lodie
                "7ql9acl+fXXfdjBGIIAH+N2WHk/dIZxdSkl4Uur68Hg"
        );//   dobs


        //stop words and stop properties (freebase) are used for disambiguation
        //List<String> stopProperties = FileUtils.readList("D:\\Work\\lodie\\resources\\nlp_resources/stopproperties_freebase.txt", true);

        //object to score columns, and disambiguate entities

        Disambiguator disambiguator = new Disambiguator(
                freebaseMatcher,
                new DisambiguationScorer_BaselinePlus(
                        stopWords,
                        new double[]{1.0, 0.5, 0.5, 1.0, 1.0}, //row,column, tablecontext other,refent, tablecontext pagetitle (unused)
                        nlpResources));                         //1.0, 0.5, 0.25, 1.0, 1.0
        ClassificationScorer class_scorer = new ClassificationScorer_ISWC_version(
                nlpResources,
                new Creator_ConceptHierarchicalBOW_Freebase(),
                stopWords,
                new double[]{1.0, 1.0, 1.0, 1.0}         //all 1.0
        );                                              //header,column,tablecontext other, page title+caption

        CellSelector selector = null;
        if (cellSelector == 0) {
            selector = new Sampler_LTableContentCell_random();
        } else if (cellSelector == 1) {
            selector=new Sampler_LTableContentCell_OSPD_nonEmpty();
        } else if (cellSelector == 2) {
            selector=new Sampler_LTableContentCell_OSPD_contextWords(stopWords);
        }else if (cellSelector == 3) {
            selector=new Sampler_LTableContentCell_OSPD_nameLength();
        }else{
            selector=new Sampler_LTableContentCell_OSPD_combined(stopWords);
        }

        ColumnLearner_LEARN_Seeding column_learnerSeeding = new ColumnLearner_LEARN_Seeding(
                selector,
                EntropyConvergence.class.getName(),
                new String[]{"0.0", "2", "0.01"},
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


        ColumnInterpreter columnInterpreter = new ColumnInterpreter(
                column_learnerSeeding, column_updater, TableMinerConstants.MAX_REFERENCE_ENTITY_FOR_DISAMBIGUATION);

        //object to interpret relations between columns
        HeaderBinaryRelationScorer relation_scorer = new HeaderBinaryRelationScorer_Vote(nlpResources,
                new Creator_RelationHierarchicalBOW_Freebase(),
                stopWords,
                new double[]{1.0, 1.0, 0.0, 0.0, 1.0}    //entity, header text, column, title&caption, other
                // new double[]{1.0, 1.0, 0.0, 0.0, 1.0}
        );
        BinaryRelationInterpreter interpreter_relation = new BinaryRelationInterpreter(
                new RelationTextMatch_Scorer(0.0, stopWords),
                relation_scorer
        );

        //object to consolidate previous output, further score columns and disamgiuate entities
        ColumnInterpreter_relDepend interpreter_with_knownRelations = new ColumnInterpreter_relDepend_exclude_entity_col(
                IGNORE_COLUMNS
        );

        Backward_updater updater = null;
        //new Backward_updater(selector, freebaseMatcher, disambiguator, class_scorer, stopWords, nlpResources);
        MainInterpreter_and_Forward_learner interpreter = new MainInterpreter_and_Forward_learner(
                main_col_finder,
                columnInterpreter,
                interpreter_with_knownRelations,
                interpreter_relation,
                IGNORE_COLUMNS, new int[0],
                updater, relation_scorer);

        LTableAnnotationWriter writer = new LTableAnnotationWriter(
                new TripleGenerator("http://www.freebase.com", "http://lodie.dcs.shef.ac.uk"));


        TableXtractorMusicBrainz xtractor = new TableXtractorMusicBrainz(new TableNormalizerDummy(),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorMusicBrainz(),
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
                server.shutdown();
                System.exit(1);
            }

        }
        server.shutdown();
        System.out.println(new Date());
    }


    public static boolean process(MainInterpreter_and_Forward_learner interpreter, LTable table, String sourceTableFile, LTableAnnotationWriter writer,
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
