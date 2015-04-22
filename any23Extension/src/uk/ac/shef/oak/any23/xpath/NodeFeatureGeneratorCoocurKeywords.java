package uk.ac.shef.oak.any23.xpath;
//package uk.ac.shef.oak.any23.extension.xpath;
//
//
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
//import org.apache.lucene.util.Version;
//import org.xml.sax.SAXException;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//public class NodeFeatureGeneratorCoocurKeywords implements NodeFeatureGenerator {
//
//    private String fieldNameInSolrSchema;
//    private Analyzer analyzer;
//    private Map<String, Integer> keys = new HashMap<String, Integer>();
//    private String tag;
//
//    public NodeFeatureGeneratorCoocurKeywords(String fieldNameInSolrSchema, String tag) throws IOException {
//        //TODO DOUBLE CHECK THE USAGE OF TokenizeAndStopwordAnalyzer
//    	analyzer = new TokenizeAndStopwordAnalyzer(Version.LUCENE_36);
//        this.fieldNameInSolrSchema=fieldNameInSolrSchema;
//        this.tag = tag;
//    }
//    
//    
//    public void extratFeatures(HtmlDocument doc, String xpath) {
//
//            	// TODO check if this is ok, 
//            	// then decide cleaninig procedure for keyword and use TokenizeAndStopwordAnalyzer.tokenizeString everywhere
//    			//TODO limit the text to a context window of xpath
////                List<String> text = TokenizeAndStopwordAnalyzer.tokenizeString(string.toString());
//                List<String> text;
//				try {
//					text = this.tokenizeString(HtmlUtil.extractText(doc.getPageHtml()));
//
//                for (String t : text) {
//                    if (keys.get(t) == null) {
//                        keys.put(t, 1);
//                    } else {
//                        int f = keys.get(t);
//                        keys.put(t, f + 1);
//                    }
//                }
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (SAXException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//    }
//
//    public void output(String destination) throws IOException {
//        destination=destination+ File.separator+this.getClass().getSimpleName();
//        new File(destination).mkdirs();
//        String filename= NodeFeaturesGeneratorThread.createFileNameForTag(tag);
//
//        PrintWriter p = new PrintWriter(new FileWriter(destination + File.separator + filename, true));
//        for (Map.Entry<String, Integer> k : keys.entrySet()) {
//            p.println(k.getKey() + "," + k.getValue());
//        }
//        p.close();
//    }
//
//    private List<String> tokenizeString(String string) throws IOException {
//        List<String> result = new ArrayList<String>();
//        TokenStream stream = analyzer.tokenStream(null, new StringReader(string));
//        // get the CharTermAttribute from the TokenStream
//        CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
//        try {
//            stream.reset();
//            // print all tokens until stream is exhausted
//            while (stream.incrementToken()) {
//                result.add(stream.getAttribute(CharTermAttribute.class).toString());
//
////            System.out.println(termAtt.toString());
//            }
//            stream.end();
//        } finally {
//            stream.close();
//        }
//
//        return result;
//    }
//}
