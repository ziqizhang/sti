//package uk.ac.shef.oak.xpath.collectiveExperiment;
//
//import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author annalisa
// * this class generates a Lucene index containing a document for each web page
// */
//public class AnnotateWebPages {
//
//	
//	/**
//	 * @param htmlFolder
//	 * @return
//	 */
//	public Map<String, Map<String, Integer>> getXpathUsage(String htmlFolder) {
//
//		Map<String, Map<String, Integer>> usage = new HashMap<String, Map<String, Integer>>();
//
//		File dir = new File(htmlFolder);
//		if (dir.isDirectory()) {
//			File[] ht = dir.listFiles();
//			for (File f : ht) {
//
//				Map<String, String> xp = this.getAnnotationsFromPage(f);
//				// l4j.info(xp.size());
//				for (String p : xp.keySet()) {
//					// String s = this.relaxXpath(p);
//					// store in the global map
//					this.xpathOnPage.put(f.getName(), xp);
//
//					// store in thge usage map
//					if (usage.get(p) == null) {
//						Map<String, Integer> val = new HashMap<String, Integer>();
//						val.put(xp.get(p), 1);
//						usage.put(p, val);
//					} else {
//						Map<String, Integer> val = usage.get(p);
//						if (val.get(xp.get(p)) != null) {
//							int freq = val.get(xp.get(p)) + 1;
//							val.put(xp.get(p), freq);
//						} else {
//							val.put(xp.get(p), 1);
//						}
//						usage.put(p, val);
//					}
//				}
//				// Set<String> rxp = xce.relaxXpath(xp);
//				//
//				// l4j.info(rxp.size());
//				// for (String s:rxp){
//				// l4j.info(s);
//				// }
//			}
//		}
//
//		return usage;
//	}
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//	}
//
//	
//	
//}
