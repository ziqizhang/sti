package uk.ac.shef.oak.any23.xpath;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.xpath.XPathFactory;

import org.apache.xerces.impl.xpath.XPath;

import uk.ac.shef.oak.any23.extension.extractor.LTriple;
import uk.ac.shef.wit.ie.wrapper.html.xpath.DOMUtil;


/**
 * @author annalisa
 *
 */
public class NodeFeatureGeneratorCoocurTags implements NodeFeatureGenerator {
    private Map<String, Integer> keys = new HashMap<String, Integer>();
    private HtmlDocument doc;

    public NodeFeatureGeneratorCoocurTags(HtmlDocument doc){
        this.doc = doc;
    }

    private void splitXpath(String targetXpath){
    	String nodeToTheRoot[]= targetXpath.split("/");
  
    }
    
    
//    public void extratFeatures(LTriple targetTriple) {
//
//    	//TODO extract all xpath from the target node to the root of the document 
//    	// the map keys will contain an Xpath entry, called starXpath, for each of those tags built as
//    	// ""//tag/*/.../*/targetXpathLeafNode 
//    	// the distance of each tag fro the targe node will define the number of * in the path
//    	
//    	//TODO repeat for subject, predicate, object
//    	String targetXpath = targetTriple.getoXPath();
//    	
//    	targetXpath=DOMUtil.removeXPathPositionFilters(targetXpath);
//    	String nodeToTheRoot[]= targetXpath.split("/");
//    	
//    	for (int n=0; n<nodeToTheRoot.length; n++){
//
//    	String starXpath = nodeToTheRoot[n];
//    	for (int i=n+1; i<nodeToTheRoot.length; i++){
//    		starXpath=starXpath+"/*";
//    		
//    	}
//    	starXpath = starXpath+"/"+nodeToTheRoot[nodeToTheRoot.length];
//
//        try {
//            if (keys.get(starXpath.toString()) == null) {
//                keys.put(starXpath.toString(), 1);
//            } else {
//                int f = keys.get(starXpath.toString());
//                keys.put(starXpath.toString(), f + 1);
//            }
//        } catch (Exception e) {
//            System.err.println("Tag skipped because of exception:" + starXpath);
//            e.printStackTrace();
////            	keys.put(string.toString(), "");
//
//        }
//    
//    	
//    	}
//    }

    /* TODO dummy method, the right one is the one above which is to test
     * @see uk.ac.shef.oak.any23.extension.xpath.NodeFeatureGenerator#extratFeatures(uk.ac.shef.oak.any23.extension.extractor.LTriple)
     */
    public void extratFeatures(LTriple targetTriple) {

    	//TODO extract all xpath from the target node to the root of the document 
    	// the map keys will contain an Xpath entry, called starXpath, for each of those tags built as
    	// ""//tag/*/.../*/targetXpathLeafNode 
    	// the distance of each tag fro the targe node will define the number of * in the path
    	
    	//TODO repeat for subject, predicate, object
    	String targetXpath = targetTriple.getoXPath();
    	
    	targetXpath=DOMUtil.removeXPathPositionFilters(targetXpath);

                keys.put(targetXpath.toString(), 1);
            
    }
    
    public void output(String destination) throws IOException {
        destination=destination+ File.separator+this.getClass().getSimpleName();
        new File(destination).mkdirs();
        String filename= NodeFeaturesGeneratorThread.createFileNameForTrainingInstanceOnWebPage(this.doc.getPageUrl());

        PrintWriter p = new PrintWriter(new FileWriter(destination + File.separator + filename, true));
        for (Map.Entry<String, Integer> k : keys.entrySet()) {
            p.println(k.getKey() + "," + k.getValue());
        }

        p.close();
    }

}
