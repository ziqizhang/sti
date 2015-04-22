package uk.ac.shef.oak.any23.xpath;


import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;

import org.xml.sax.SAXException;

import uk.ac.shef.dcs.oak.util.SysProp;

public class NodeFeaturesGenerator {
    //todo
    //how long to wait for between re-initiating new threads
    private static final long INTERVAL = 1000;
    private SysProp sp;


    private static Logger logger = Logger.getLogger(NodeFeaturesGenerator.class.getName());

    //# of tags to fetch at a time from the tag-index
    private int maxNumberOfPageToProcess;

    private String tmpDataDir;




    public NodeFeaturesGenerator(String propertyFile) {
        this.sp = new SysProp(propertyFile);
        this.maxNumberOfPageToProcess = Integer.valueOf(this.sp.getProperties().getProperty("maxNumberOfPageToProcess"));
        this.tmpDataDir = this.sp.getProperties().getProperty("tmpDataDir");
        clearTmpDir();
    }




    public void generateTagFeaturesFor(List<String> allHtmlPages) throws IOException {
        Collections.sort(allHtmlPages);
        logger.info("\t Total web pages: " + allHtmlPages.size());
        //TODO iterate over html documents
        startProcessorThreads(allHtmlPages, this.maxNumberOfPageToProcess);
        logger.info("+++ COMPLETE");
    }


   //TODO create method for processing pages from the ClueWeb index
//    public void generateTagFeatures() throws IOException {
//
//        List<String> allTagStrings = null;
//        try {
//            allTagStrings = new ArrayList<String>(tagIndex.getAllTags(luceneIndexPath));
//        } catch (IOException e) {
//            logger.warn("\t ! SEVERE! Initialisation of the required luncene index failed. Program cannot start. Quit.");
//            e.printStackTrace();
//            System.exit(1);
//        }
//        Collections.sort(allTagStrings);
//
//        //todo
//        /*List<String> testing = new ArrayList<String>(allTagStrings);
//        allTagStrings.clear();
//        allTagStrings.add("manchester");
//        allTagStrings.add("cpc11");
//        for(int i=0; i<5000; i++)
//            allTagStrings.add(testing.get(i));*/
//        //
//
//        logger.info("\t Total tags: " + allTagStrings.size());
//        logger.info("\t Warm up server...");
//        ModifiableSolrParams params = new ModifiableSolrParams();
//        params.set("q", "user_tag_s:" + "manchester");
//        params.set("start", 0);
//        params.set("rows", 1000);
//        //iteratively query the server until all documents containing the tag are retrieved
//        try {
//            QueryResponse response = tweetIndexServer.query(params);
//        } catch (SolrServerException e) {
//            e.printStackTrace();
//        }
//        logger.info("\t Warm-up complete. Beginning processors");
//        startProcessorThreads(allTagStrings, tweetIndexServer, getFetchSizeTagIndex());
//        logger.info("+++ COMPLETE");
//    }


    private void startProcessorThreads(List<String> allHtmlPages, int maxNumberOfPageToProcess) throws IOException {
        int nextPage = 0;
        //firstly, create matching number of threads to process
        List<NodeFeaturesGeneratorThread> processors = new ArrayList<NodeFeaturesGeneratorThread>();

        int fetch = allHtmlPages.size();
        if (maxNumberOfPageToProcess < fetch)
            fetch = maxNumberOfPageToProcess;

        for (int i = 0; i < fetch; i++) {
            if (allHtmlPages.get(i).trim().length() == 0)
                continue;

            NodeFeaturesGeneratorThread thread =
                    new NodeFeaturesGeneratorThread(allHtmlPages, allHtmlPages.get(i), getTmpDataDir(), this.getMaxNumberOfPageToProcess());
            processors.add(thread);
            nextPage = i + 1;
        }
        //then, start all processors
        for (NodeFeaturesGeneratorThread t : processors) {
            logger.info("\t + Started the processor for the " + allHtmlPages.indexOf(t.getHtmlPage()) + "page: " + t.getHtmlPage());
            t.start();
        }

        //keep listenning to the processors, as soon as any one finishes, start another thread.
        while (nextPage <= allHtmlPages.size()) {
            int countFinished = 0;
            Iterator<NodeFeaturesGeneratorThread> it = processors.iterator();
            while (it.hasNext()) {
                NodeFeaturesGeneratorThread thread = it.next();
                if (thread.isFinished()) {
                    if (thread.isStatusOK()) {
                        logger.info("\t + Completed one processor, for page:" + thread.getHtmlPage());
                    } else {
                        logger.warn("\t ! Processor interrupted for page:" + thread.getHtmlPage());
                    }
                    thread = null;
                    it.remove();
                    countFinished++;
                }
            }

            if (countFinished > 0) {
                if (nextPage < allHtmlPages.size()) { //if there are more data to be processed, initiate new threads and start them
                    for (int j = 0; j < countFinished && nextPage < allHtmlPages.size(); j++) {
                        String currentPage = allHtmlPages.get(nextPage);
                        NodeFeaturesGeneratorThread thread =
                                new NodeFeaturesGeneratorThread(allHtmlPages, currentPage, getTmpDataDir(), this.getMaxNumberOfPageToProcess());
                        thread.start();
                        logger.info("\t + Started the processor for the " + allHtmlPages.indexOf(thread.getHtmlPage()) + "page: " + thread.getHtmlPage());
                        processors.add(thread);
                        nextPage = nextPage + 1;
                        try {
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    countFinished = 0;
                } else { //otherwise, break out of the loop and just wait for the remaining threads to complete (line 235)
                    break;
                }
            }

            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
            }
        }
        //lastly check if remaining threads have completed
        int remainder = processors.size(), finished = 0;
        while (finished < remainder) {
            for (NodeFeaturesGeneratorThread t : processors) {
                if (t.isFinished()) {
                    if (t.isStatusOK()) {
                        logger.info("\t + Completed one processor, for tag:" + t.getHtmlPage());
                    } else {
                        logger.warn("\t ! Processor interrupted for tag:" + t.getHtmlPage());
                    }
                    finished++;
                }
            }
            if(finished!=remainder)
                finished=0;
        }
    }


    public String getTmpDataDir() {
        return tmpDataDir;
    }

    public void setTmpDataDir(String tmpDataDir) {
        this.tmpDataDir = tmpDataDir;
    }

    public int getMaxNumberOfPageToProcess() {
        return maxNumberOfPageToProcess;
    }

    public void setMaxNumberOfPageToProcess(int fetchSizeTagIndex) {
        this.maxNumberOfPageToProcess = fetchSizeTagIndex;
    }



    void clearTmpDir() {
        File f = new File(tmpDataDir);
        if (f.exists()) {
            for (File af : f.listFiles())
                af.delete();
        }
    }


    /**
     * @param args
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException,
            ParserConfigurationException, SAXException {


//    	Set<String> sw = (Set<String>) org.apache.lucene.analysis.StopAnalyzer.ENGLISH_STOP_WORDS_SET;
//    	System.out.println(sw);

        NodeFeaturesGenerator ss = new NodeFeaturesGenerator(args[0]);
        
        //TODO for all html pages belonging to a cluster
        List<String> allHtmlPages = new ArrayList<String>();
//        allHtmlPages.add("class");
//        allHtmlPages.add("peak");
//        allHtmlPages.add("sheffield");
//        allHtmlPages.add("http://www.rottentomatoes.com/m/godfather/");
        allHtmlPages.add("http://www.goodreads.com/book/show/660523.Secrets_of_the_Morning");

        //thread version
        ss.generateTagFeaturesFor(allHtmlPages);

        //no thread version
//        ss.ge(allTagStrings);


        System.exit(0);
    }


}
