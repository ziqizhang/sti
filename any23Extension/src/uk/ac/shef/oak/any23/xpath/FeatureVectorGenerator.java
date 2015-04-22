package uk.ac.shef.oak.any23.xpath;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;

import org.xml.sax.SAXException;

/**
 * README:
//TODO chanhe this for xpath
 */
/**
 * @author annalisa
 *
 */
//public class FeatureVectorGenerator {
//    //todo
//    //how long to wait for between re-initiating new threads
//
//    private SysProp sp;
//
//    //the file sys path to the tweet data index
//    private String solrPath;
//
//    private static Logger logger = Logger.getLogger(FeatureVectorGenerator.class.getName());
//
//
//    private String dataDir;
//
//    private int optimizePoint;
//
//    private Set<String> allTags = new HashSet<String>();
////    private Map featureVectors = new HashMap<String, List<String>>;
//
//    /*
//    dataDir - folder containing concatenated text files for each tag
//     */
//    public FeatureVectorGenerator(String dataDir) {
//        this.dataDir = dataDir;
//    }
//
//    
//    public void generateTagProfileIndex(String startTagFile) {
//        EmbeddedSolrServer tagIndexServer = null;
//        File data = new File(dataDir);
//        List<File> files = Arrays.asList(data.listFiles());
//        Collections.sort(files);
//        int start = 0;
//        start = startTagFile == null || files.indexOf(startTagFile) == -1 ? start : files.indexOf(startTagFile);
//        int total = files.size() - start;
//
//        try {
//            logger.info("\t + Initialising server on the tag-profile-index ");
//            CoreContainer.Initializer tweetIndexInit = new CoreContainer.Initializer();
//            CoreContainer tweetIndexContainer = tweetIndexInit.initialize();
//            tagIndexServer = new EmbeddedSolrServer(tweetIndexContainer, "");
//        } catch (Exception e) {
//            logger.warn("\t ! SEVERE! Initialisation of required servers failed. Program cannot start. Quit.");
//            e.printStackTrace();
//            System.exit(1);
//        }
//
//        logger.info("\t Total tags to process=" + total);
//
//        int counter = 0;
//        Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
//        for (int i = start; i < files.size(); i++) {
//            logger.info("\t processing tag, index=" + i + ", tagFile=" + files.get(i));
//            counter++;
//            try {
//                SolrInputDocument doc = createSolrDocumentForTag(files.get(i));
//                docs.add(doc);
//                tagIndexServer.add(docs);
//                docs.clear();
//                logger.info("commiting");
//                tagIndexServer.commit();
//
//                if (counter >= optimizePoint) {
//                    logger.info("optimizing index");
//                    tagIndexServer.optimize();
//                    counter = 0;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
///*        tagIndexServer.add(docs);
//        logger.info("commiting size of " + optimizePoint);
//        tagIndexServer.commit();*/
//        logger.info("optimizing index");
//        try {
//            tagIndexServer.optimize();
//        } catch (SolrServerException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//
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
//        logger.info("+++ COMPLETE");
//    }
//    
//    public HashMap<String, HashMap<String, HashMap<String,String>>> generateArffFeaturVector(String startTagFile) {
//
//        HashMap<String, HashMap<String, HashMap<String,String>>> featureVs = new HashMap<String, HashMap<String, HashMap<String,String>>>();
//
//        
//    	//TODO Iterate around different folders, one per feature
//        File data = new File(dataDir);
//
//        	//name of directory is same as solr field
//            List<File> featuresDirectories = Arrays.asList(data.listFiles());
//            for (File feature : featuresDirectories)
//            if (feature.isDirectory()){
//            	String currentFeature = feature.getName();
//            	//name of directory is same as solr field
//
//        
////        List<File> files = Arrays.asList(data.listFiles());
//        List<File> files = Arrays.asList(feature.listFiles());
//
//        Collections.sort(files);
//        int start = 0;
//        start = startTagFile == null || files.indexOf(startTagFile) == -1 ? start : files.indexOf(startTagFile);
//        int total = files.size() - start;
//
//
//
//        logger.info("\t Total tags to process=" + total);
//        HashMap<String, HashMap<String,String>> currentFeatureMap = new HashMap<String, HashMap<String,String>>();
//
//        int counter = 0;
//        for (int i = start; i < files.size(); i++) {
//        	String currentTag = files.get(i).getName();
//        	this.allTags.add(currentTag);
//            logger.info("\t processing tag, index=" + i + ", tagFile=" + files.get(i));
//            counter++;
//
////            	currentFeatureMap.put(files.get(i).getName(), new HashMap<String,String>());
//            	//get feature for the tag
//                HashMap<String, String> doc;
//				try {
//					doc = getFeaturesForTag(files.get(i));
//				
//                
//                for (String t:doc.keySet()){
//                	if (currentFeatureMap.get(t)==null){
//                		currentFeatureMap.put(t, new HashMap<String,String>());
//                	}            
//            		currentFeatureMap.get(t).put(currentTag, doc.get(t));
//
//
//
//            } 
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//        }
//        featureVs.put(currentFeature, currentFeatureMap);
//
//    }
//
//        logger.info("+++ COMPLETE");
//        
//        System.out.println(featureVs.keySet());
//
//        for (String k :featureVs.keySet()){
//        	System.out.println(featureVs.get(k).size());
//        }
//        for (String k :featureVs.keySet()){
//        	if (!k.contains("Keyword"))
//        	System.out.println(featureVs.get(k).keySet());
//
//        }
//    	
//        return featureVs;
//    }
//    
//    private void printARFF(Set<String> tags, HashMap<String, HashMap<String, HashMap<String,String>>> fv) throws IOException{
//    	
//      String tmpDataDir = "./tempWeka";
//    	File f = new File(tmpDataDir);
//        if (!f.exists()) {
//            f.mkdirs();
//        }
//
//
//        PrintWriter p = new PrintWriter(new FileWriter(tmpDataDir + File.separator + "tagsArff", true));
//
//        p.println(" % 1. Title: representation of tags");
//        p.println("% ");
//        p.println("% 2. Sources:");
//        p.println("%      (a) Creator: A. L. Gentile");
//        p.println("%      (b) Date: October, 2012");
//        p.println("% ");
//        p.println("@RELATION tags");
//
// 	   //TODO getAllAtt
//        HashMap<String, Integer> attributes = new HashMap<String, Integer>();
//        
//        int attNumber = 0;
//        for (String portion : fv.keySet()){
//        	HashMap<String, HashMap<String, String>> e = fv.get(portion);
//        	for (String att:e.keySet()){
//        		if (!attributes.containsKey(att)){
//        			attributes.put(att, attNumber);
//        		attNumber++;
//        		}else{
//        			System.out.println("something went wrong "+att);
//        		}
//        }
//
//        }
//            for (String a : attributes.keySet()){
//    	p.println("@ATTRIBUTE "+a+" NUMERIC");}
//
//
// 	   //@ATTRIBUTE <name>  NUMERIC
//
//// 	   @ATTRIBUTE class        {Iris-setosa,Iris-versicolor,Iris-virginica}
//        
//        
//        // data
//        	p.println("@data");
//
//        String record = "{";
//    	for (String t:tags){
//
//        for (String portion : fv.keySet()){
//        	HashMap<String, HashMap<String, String>> e = fv.get(portion);
//        		for (String feat : e.keySet()){
////        			if (e.get(feat).keySet().contains(t)){
////        				record = record +", "+e.get(feat).get(t);
////        			}else{
////        				record = record +", ?";
////
////        			}
//        			//for sparse files
//        			if (e.get(feat).keySet().contains(t)){
//        				System.out.println(e.get(feat));
//        				record = record +attributes.get(feat) + " "+e.get(feat).get(t)+", ";
//        			}
//        		}
//        		
//        }
//        record= record.substring(0, record.length()-2)+"}";
//
//    	p.println(record);
//        record = "{";
//
//        }
//        p.close();
//    }
//
////    private SolrInputDocument createSolrDocumentForTag(File file) throws IOException {
////        SolrInputDocument doc = new SolrInputDocument();
////
////        StringBuilder content = new StringBuilder();
////
////        final BufferedReader reader = new BufferedReader(new FileReader(file));
////        String line;
////
////        int count = 0;
////        while ((line = reader.readLine()) != null) {
////            line = line.trim();
////            if (line.equals("")) continue;
////
////            count++;
////            if (count == 1) {
////                doc.addField("id_s", line);
////                continue;
////            }
////            content.append(line).append(" ");
////
////        }
////
////        reader.close();
////
////        doc.addField("text_content_t", content.toString().trim());
////
////
////        return doc;
////    }
//
//    private HashMap<String,String> getFeaturesForTag(File file) throws IOException {
//    	HashMap<String,String> doc = new HashMap<String,String>();
//
//        StringBuilder content = new StringBuilder();
//
//        final BufferedReader reader = new BufferedReader(new FileReader(file));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            String [] value = line.split(",");
//            doc.put(value[0].trim(), value[1].trim());
//        }
//
//        reader.close();
//
//        return doc;
//    }
//    /**
//     * @param args
//     * @throws SAXException
//     * @throws ParserConfigurationException
//     * @throws IOException
//     */
//    public static void main(String[] args) throws IOException,
//            ParserConfigurationException, SAXException {
//
//        FeatureVectorGenerator ss = new FeatureVectorGenerator(args[0]);
//        String startTagFile=null;
//        HashMap<String, HashMap<String, HashMap<String, String>>> fv = ss.generateArffFeaturVector(startTagFile);
//        
//        ss.printARFF(ss.allTags, fv);
//        
//        System.exit(0);
//    }
//
//
//}
