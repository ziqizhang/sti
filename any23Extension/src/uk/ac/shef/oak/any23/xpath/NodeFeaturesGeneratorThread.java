package uk.ac.shef.oak.any23.xpath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;


import uk.ac.shef.oak.any23.extension.extractor.LTriple;




public class NodeFeaturesGeneratorThread extends Thread {
    private static String FIELD_TO_CONCATENATE = "text_content_t";

    private String htmlPage;
    private int fetchSize;
    private Logger log;

    private boolean finished;
    private boolean statusOK;

    private String tmpDataDir;

//	Analyzer analyzer= new TokenizeAndStopwordAnalyzer(Version.LUCENE_36, new File("./resources/TwitterStopword.txt"));

    public NodeFeaturesGeneratorThread(List<String> allHtmlPages, String page, String tmpDataDir, int fetchSize) throws IOException {
        this.htmlPage = page;
        this.fetchSize = fetchSize;
        this.tmpDataDir = tmpDataDir;

//    	Set<String> sw = (Set<String>) StopAnalyzer.ENGLISH_STOP_WORDS_SET;

        log = Logger.getLogger(NodeFeaturesGeneratorThread.class.getName() + "_for_" + htmlPage);
    }

    @Override
    public void run() {
        log.info("*********************** process started " + htmlPage);
        try {
            process();
            statusOK = true;
//        } catch (SolrServerException e) {
//            e.printStackTrace();
//
//            log.warning("SEVERE: cannot begin this thread because:");
//            statusOK = false;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//
//            log.warning("SEVERE: cannot begin this thread because:");
//            statusOK = false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("***********************");
            log.warning("SEVERE: cannot begin this thread because:");
            statusOK = false;
        }
        System.out.println("finished tag *********************** " + htmlPage);
        setFinished(true);
    }

    
    //TODO change this to obtain a folder for each html page
    // each cointaining a file for each xpath
    private void process() throws Exception {

    	//list of target xpath on the html page
        List<LTriple> result = new ArrayList<LTriple>();
        HtmlDocument htmlDoc = new HtmlDocument(htmlPage);
        
        //TODO populate the list using sindice, now result is always null
        result = htmlDoc.getAnnotations();
        
        
        //create a temp folder if not exist 
        File f = new File(tmpDataDir);
        if (!f.exists()) {
            f.mkdirs();
        }

        //create folder for the html page
        File htmlPageFolder = new File(tmpDataDir + File.separator + createFileNameForHtmlPage(htmlPage));
        if (!htmlPageFolder.exists()) {
            htmlPageFolder.mkdirs();
        }


//        PrintWriter p = new PrintWriter(new FileWriter(tmpDataDir + File.separator + createFileNameForTrainingInstanceOnWebPage(htmlPage), true));
        //PrintWriter p_user = new PrintWriter(new FileWriter(user.getAbsolutePath() + File.separator + createFileNameForTag(tag), true));

//        p.println(htmlPage);

        log.info(toString() + ", total=" + result.size());


//      NodeFeatureGenerator coocurrKeywords = new NodeFeatureGeneratorCoocurKeywords(htmlDoc);

        NodeFeatureGenerator coocurTags = new NodeFeatureGeneratorCoocurTags(htmlDoc);

            for  (LTriple  d : result) {
//                coocurrKeywords.extratFeatures(d);
                coocurTags.extratFeatures(d);
            }



//        usageByTime.output(tmpDataDir);
//        coocurrKeywords.output(tmpDataDir);
//        tagUsedByUser.output(tmpDataDir);

        coocurTags.output(tmpDataDir);



    }



//    TestTagNormalizer ss = new TestTagNormalizer(args[0]);
//	Map<String, List<String>> rm = new HashMap<String, List<String>>();
//    List<String> tags = ss.getAllTagFromIndex(1000);
//    
//    for (String t : tags){
////    	String tag= gs.correctSingleTag(t);
//    	
//    	//list of features for a tag
//    	List< String> v = new ArrayList<String>();
//
//    	//is multiword feature






    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

//    protected static String createFileNameForTag(String tag) {
//        return tag.replaceAll("[^\\p{L}\\p{N}]", "_") + "-" + tag.hashCode();
//    }
    
    // TODO make sure what's the model:
    // a training instance on a web page should be a triple
    // stiil can't get my head round if it should be an xpath or a triple
    // in a more general setting is a triple and the various xpaths to reach the triple in the web page are features in the vector space
    protected static String createFileNameForTrainingInstanceOnWebPage(String tag) {
        //TODO I might want to have an index number for each of them
    	return tag.replaceAll("[^\\p{L}\\p{N}]", "_");
    }
    
    protected static String createFileNameForHtmlPage(String htmlUrl) {
        return htmlUrl.replaceAll("[^\\p{L}\\p{N}]", "_");
    }
    
    
    public boolean isStatusOK() {
        return statusOK;
    }

    public String getHtmlPage() {
        return htmlPage;
    }

    public String toString() {
        return htmlPage + "," + fetchSize;
    }


//	CsvExporter.writeMapOnCsv(rm, "./out");
//	
//    System.exit(0);

}
