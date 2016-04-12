package uk.ac.shef.dcs.sti.todo;

import com.google.api.client.http.HttpResponseException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.core.algorithm.baseline.*;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTagger;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTaggerImpl;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping.IInf;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearch;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.io.TAnnotationWriter;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.TripleGenerator;
import uk.ac.shef.dcs.sti.parser.table.TableParserMusicBrainz;
import uk.ac.shef.dcs.sti.parser.table.validator.TableValidatorGeneric;
import uk.ac.shef.dcs.sti.parser.table.hodetector.TableHODetectorByHTMLTag;
import uk.ac.shef.dcs.sti.parser.table.normalizer.TableNormalizerSimple;
import uk.ac.shef.dcs.sti.parser.table.creator.TableObjCreatorMusicBrainz;
import uk.ac.shef.dcs.sti.util.FileUtils;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 25/02/14
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public class TestTableInterpretation_MusicBrainz_Baseline {
    private static Logger log = Logger.getLogger(TestTableInterpretation_MusicBrainz_Baseline.class.getName());

       public static int[] IGNORE_COLUMNS=new int[]{7
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
        boolean relationLearning = Boolean.valueOf(args[6]);

        List<Integer> missed_files = new ArrayList<Integer>();
        if(args.length==8){
            String in_missed = args[7];
            for(String line: FileUtils.readList(in_missed,false)){
                missed_files.add(Integer.valueOf(line.split(",")[0].trim()));
            }
        }
        //cache target location

        File configFile = new File(cacheFolder + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(cacheFolder,
                configFile);
        SolrServer server = new EmbeddedSolrServer(container, "collection1");

        //object to fetch things from KB
        FreebaseSearch freebaseMatcher = new FreebaseSearch(propertyFile, true, server, null,null);
        List<String> stopWords = FileUtils.readList(nlpResources + "/stoplist.txt", true);
        //object to find main subject column
        SubjectColumnDetector main_col_finder = new SubjectColumnDetector(
                new TContentTContentRowRankerImpl(),
                IInf.class.getName(),
                new String[]{"0.0", "1", "0.01"},
                server,
                nlpResources,            true, stopWords,
                //"tHSVoL7Xxn+sZ7N+gP081zev4wLvzeD5SWhyCpfFtbI"); //ziqi.zhang
                MultiKeyStringSplitter.split(properties.getProperty("BING_API_KEYS"))
                );


        //stop words and stop properties (freebase) are used for disambiguation
        //List<String> stopProperties = FileUtils.readList("D:\\Work\\lodie\\resources\\nlp_resources/stopproperties_freebase.txt", true);

        //object to computeElementScores columns, and disambiguate entities
        TCellNameMatchDisambiguator disambiguator = new TCellNameMatchDisambiguator();


        TCellDisambiguatorNameMatch column_learner = new TCellDisambiguatorNameMatch(
                freebaseMatcher,
                disambiguator
        );

        //object to computeElementScores relations between columns
        TColumnColumnRelationEnumerator interpreter_relation = new TColumnColumnRelationEnumerator(
                new TMPAttributeValueMatcher(0.0, stopWords)
        );


        LiteralColumnTagger interpreter_with_knownRelations = new LiteralColumnTaggerImpl(
                IGNORE_COLUMNS
        );
        BaselineInterpreter interpreter = new BaselineInterpreter(
                main_col_finder,
                column_learner,
                interpreter_relation, interpreter_with_knownRelations,
                IGNORE_COLUMNS,
                new int[]{1});

        TAnnotationWriter writer = new TAnnotationWriter(
                new TripleGenerator("http://www.freebase.com", "http://lodie.dcs.shef.ac.uk"));


        TableParserMusicBrainz xtractor = new TableParserMusicBrainz(new TableNormalizerSimple(),
                new TableHODetectorByHTMLTag(),
                new TableObjCreatorMusicBrainz(),
                new TableValidatorGeneric());
        int count = 0;
        List<File> all = Arrays.asList(new File(inFolder).listFiles());
        Collections.sort(all);
        System.out.println(all.size());
        for (File f : all) {
            count++;
            if(count-1<start)
                continue;
            if(missed_files.size()!=0 && !missed_files.contains(count))
                continue;

            boolean  complete=false;
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

                complete = process(interpreter, table, sourceTableFile, writer, outFolder,relationLearning);
                //server.commit();
                if(STIConstantProperty.SOLR_COMMIT_PER_FILE)
                    server.commit();
                if (!complete){
                    System.out.println("\t\t\t missed: " + count + "_" + sourceTableFile);
                    PrintWriter missedWriter = null;
                    try {
                        missedWriter = new PrintWriter(new FileWriter("ti_musicbrainz_missed_base.csv", true));
                        missedWriter.println(count+","+inFile);
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
                    missedWriter = new PrintWriter(new FileWriter("ti_musicbrainz_missed_base.csv", true));
                    missedWriter.println(count+","+inFile);
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
    }


    public static boolean process(BaselineInterpreter interpreter, Table table, String sourceTableFile, TAnnotationWriter writer,
                                  String outFolder, boolean relationLearning) throws FileNotFoundException {
        String outFilename = sourceTableFile.replaceAll("\\\\", "/");
        try {
            TAnnotation annotations = interpreter.start(table,relationLearning);

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
            } else{
                ste.printStackTrace();
                System.out.println("EXCEPTION! "+sourceTableFile);
            }

        }
        return true;
    }
}
