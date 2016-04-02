package uk.ac.shef.dcs.sti.experiment;

import com.google.api.client.http.HttpResponseException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.sti.core.algorithm.baseline.*;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTagger;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTaggerImpl;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.TMPAttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.stopping.IInf;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearch;
import uk.ac.shef.dcs.sti.core.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.io.TAnnotationWriter;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.util.TripleGenerator;
import uk.ac.shef.dcs.sti.util.FileUtils;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 25/02/14
 * Time: 16:11
 * To change this template use File | Settings | File Templates.
 */
public class TestTableInterpretation_LimayeDataset_Baseline {
    private static Logger log = Logger.getLogger(TestTableInterpretation_LimayeDataset_Baseline.class.getName());
    public static int[] IGNORE_COLUMNS=new int[]{};

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
        int end=Integer.MAX_VALUE;
        String missed="";
        if(args.length==8){
            String arg=args[7];
            try{
                end = Integer.valueOf(arg);
            }catch (NumberFormatException e){
                missed=arg;
            }
        }
        List<Integer> missed_indexes = new ArrayList<Integer>();
        if(missed.length()>0){
            for(String l: FileUtils.readList(missed, false)){
                String[] parts = l.split(",",2);
                missed_indexes.add(Integer.valueOf(parts[0].trim()));
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
                nlpResources,
                TableMinerConstants.SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH,
                stopWords,
                MultiKeyStringSplitter.split(properties.getProperty("BING_API_KEYS"))
                //"8Yr8amTvrm5SM4XK3vM3KrLqOCT/ZhkwCfLEDtslE7o=","eT14G3TOr7NdItThWFCXFjDrRHNUxmPBmqgDvjoIc6Q"
                /*"fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0=",
                "/BlhLSReljQ3Koh+vDSOaYMji9/Ccwe/7/b9mGJLwDQ="*/);  //zqz.work
        //"fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0="); //ziqizhang
        //eT14G3TOr7NdItThWFCXFjDrRHNUxmPBmqgDvjoIc6Q   dobs
        LiteralColumnTagger interpreter_with_knownRelations = new LiteralColumnTaggerImpl(
                IGNORE_COLUMNS
        );

        //stop words and stop properties (freebase) are used for disambiguation
        //List<String> stopProperties = FileUtils.readList("D:\\Work\\lodie\\resources\\nlp_resources/stopproperties_freebase.txt", true);

        //object to computeElementScores columns, and disambiguate entities
        Base_NameMatch_Disambiguator disambiguator = new Base_NameMatch_Disambiguator();

        Base_NameMatch_ColumnLearner column_learner = new Base_NameMatch_ColumnLearner(
                freebaseMatcher,
                disambiguator
        );

        //object to computeElementScores relations between columns
        Baseline_BinaryRelationInterpreter interpreter_relation = new Baseline_BinaryRelationInterpreter(
                new TMPAttributeValueMatcher(0.0, stopWords)
        );

        //object to consolidate previous output, further computeElementScores columns and disamgiuate entities

        Base_NameMatch_MainInterpreter interpreter = new Base_NameMatch_MainInterpreter(
                main_col_finder,
                column_learner,
                interpreter_relation,interpreter_with_knownRelations,
                IGNORE_COLUMNS, new int[0]);

        TAnnotationWriter writer = new TAnnotationWriter(
                new TripleGenerator("http://www.freebase.com", "http://lodie.dcs.shef.ac.uk"));


        int count = 0;
        List<File> all = Arrays.asList(new File(inFolder).listFiles());
        Collections.sort(all);
        System.out.println(all.size());
        int[] onlyDo = new int[]{18,19,64,65,66,67};

        for (File f : all) {
            count++;
            if(count==end)  {
                System.out.println("End counter reached: "+end);
                break;
            }
            if(missed_indexes.size()>0){
                if(!missed_indexes.contains(count))
                    continue;            }
            /*boolean found=false;
            for(int od: onlyDo){
                if(od==count)
                    found=true;
            }
            if(!found)
                continue;*/

            if(count-1<start)
                continue;
            boolean  complete=false;
            String inFile = f.toString();
            /*if(!inFile.toLowerCase().contains("02b9"))
                continue;*/
            try {
                Table table = LimayeDatasetLoader.readTable(inFile, null, null);

                String sourceTableFile = inFile;
                if (sourceTableFile.startsWith("\"") && sourceTableFile.endsWith("\""))
                    sourceTableFile = sourceTableFile.substring(1, sourceTableFile.length() - 1).trim();
                System.out.println(count + "_" + sourceTableFile + " " + new Date());
                log.info(">>>" + count + "_" + sourceTableFile);

                complete = process(interpreter, table, sourceTableFile, writer, outFolder,relationLearning);
                //server.commit();
                if(TableMinerConstants.COMMIT_SOLR_PER_FILE)
                    server.commit();
                if (!complete){
                    System.out.println("\t\t\t missed: " + count + "_" + sourceTableFile);
                    PrintWriter missedWriter = null;
                    try {
                        missedWriter = new PrintWriter(new FileWriter("limaye_missed_baseline.csv", true));
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
                    missedWriter = new PrintWriter(new FileWriter("limaye_missed_baseline.csv", true));
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


    public static boolean process(Base_NameMatch_MainInterpreter interpreter, Table table, String sourceTableFile, TAnnotationWriter writer,
                                  String outFolder,boolean relationLearning) throws FileNotFoundException {
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
                System.err.println("Remote server timed out, continue 10 seconds. Missed." + outFilename);
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                }
                return false;
            } else{
                System.err.println("Other exception encounted="+outFilename);
                ste.printStackTrace();
            }

        }
        return true;
    }
}
