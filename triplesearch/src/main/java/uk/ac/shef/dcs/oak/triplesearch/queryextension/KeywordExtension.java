package uk.ac.shef.dcs.oak.triplesearch.queryextension;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import uk.ac.shef.dcs.oak.triplesearch.triple.SPARQLQueryAgent;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Author: Isabelle Augenstein (i.augenstein@sheffield.ac.uk)
 * Date: 30/01/13
 * Time: 16:13
 */
public class KeywordExtension {
	
	/**
	 * Contains cnt of all useful relations (from training set)
	 */
	HashMap<String, Integer> useful_cnt = new HashMap<String, Integer>();
	
	/**
	 * cnt of all relations (from training set)
	 */
	HashMap<String, Integer> all_cnt = new HashMap<String, Integer>();
	
	/**
	 * Set containing all useful relations for test
	 */
	HashMap<String, Float> relmap = new HashMap<String, Float>();
	
	String dbpediaEndpoint = "http://dbpedia.org/sparql";
	String sindiceEndpoint = "http://sparql.sindice.com/sparql";
	// String endpoint = "http://lod.openlinksw.com/sparql";
	// String endpoint = "http://dbpedia-live.openlinksw.com/sparql";
	
	static String PREFIX = "PREFIX dbpprop: <http://dbpedia.org/property/> \n"
			+ "PREFIX dbpedia: <http://dbpedia.org/resource/> \n"
			+ "PREFIX dbpedia-owl: <http://dbpedia.org/> \n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
			+ "PREFIX fb: <http://rdf.freebase.com/ns/>\n"
			+ "PREFIX basekb: <http://rdf.basekb.com/ns/>\n";
		
	//protected static Logger l4j = Logger.getLogger(KeywordExtension.class);
	SPARQLQueryAgent spql = new SPARQLQueryAgent();
	
	/**
	 * Returns all instances for a certain label
	 * @param keyword Keyword user entered
	 * @return List of instances that have keyword as a label
	 */
	public static ArrayList<String> getInstancesWithLabel(String keyword, String endpoint) {
		
		String keyword_upper = "";
		if (keyword.contains(" ")){
			String[] keysplit = keyword.split(" ");
			for (int i = 0; i<keysplit.length; i++) {
				if(i>0) {
					keyword_upper = keyword_upper + " ";
				}
				char[] stringArray = keysplit[i].toCharArray();
				stringArray[0] = Character.toUpperCase(stringArray[0]);
				keyword_upper = keyword_upper + new String(stringArray);
			}
		}
		else if(keyword.length() == 1) {
			keyword_upper = keyword.toUpperCase();
		}
		else {
			char[] stringArray = keyword.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			keyword_upper = new String(stringArray);
		}
		
		ArrayList<String> results = new ArrayList<String>();
		String q = PREFIX +
				   "SELECT DISTINCT ?s " +
				   "WHERE {" +
				   "{" + 
				   "?s rdfs:label \"" + keyword + "\"@en ." +
				   "} " +
				   "UNION {" +
				   "?s rdfs:label \"" + keyword + "\" . } " +
				   "UNION {" +
				   "?s rdfs:label \"" + keyword_upper + "\" . } " +
				   "UNION {" +
				   "?s rdfs:label \"" + keyword_upper + "\"@en . }" +
				   
				   "UNION {" +
				   "?s foaf:name \"" + keyword + "\"@en . } " +
				   "UNION {" +
				   "?s foaf:name \"" + keyword + "\" . } " +
				   "UNION {" +
				   "?s foaf:name \"" + keyword_upper + "\" . } " +
				   "UNION {" +
				   "?s foaf:name \"" + keyword_upper + "\"@en . } " +
				   
				   "UNION {" +
				   "?s dc:title \"" + keyword + "\"@en . } " +
				   "UNION {" +
				   "?s dc:title \"" + keyword + "\" . } " +
				   "UNION {" +
				   "?s dc:title \"" + keyword_upper + "\" . } " +
				   "UNION {" +
				   "?s dc:title \"" + keyword_upper + "\"@en . } " +
				   
				   "UNION {" +
				   "?s skos:prefLabel \"" + keyword + "\"@en . } " +
				   "UNION {" +
				   "?s skos:prefLabel \"" + keyword + "\" . } " +
				   "UNION {" +
				   "?s skos:prefLabel \"" + keyword_upper + "\" . } " +
				   "UNION {" +
				   "?s skos:prefLabel \"" + keyword_upper + "\"@en . } " +
				   
				   "UNION {" +
				   "?s skos:altLabel \"" + keyword + "\"@en . } " +
				   "UNION {" +
				   "?s skos:altLabel \"" + keyword + "\" . } " +
				   "UNION {" +
				   "?s skos:altLabel \"" + keyword_upper + "\" . } " +
				   "UNION {" +
				   "?s skos:altLabel \"" + keyword_upper + "\"@en . } " +
				   
				   "UNION {" +
				   "?s fb:type.object.name \"" + keyword + "\"@en . } " +
				   "UNION {" +
				   "?s fb:type.object.name \"" + keyword + "\" . } " +
				   "UNION {" +
				   "?s fb:type.object.name \"" + keyword_upper + "\" . } " +
				   "UNION {" +
				   "?s fb:type.object.name \"" + keyword_upper + "\"@en . } " +
				   
				   "UNION {" +
				   "?s basekb:m.06b \"" + keyword + "\"@en . } " +
				   "UNION {" +
				   "?s basekb:m.06b \"" + keyword + "\" . } " +
				   "UNION {" +
				   "?s basekb:m.06b \"" + keyword_upper + "\" . } " +
				   "UNION {" +
				   "?s basekb:m.06b \"" + keyword_upper + "\"@en . }" +
				   
				   // add other label properties
				   "}";
		
		//spql.searchSPARQL(q, endpoint);
		results = cleanSPARQL(endpoint, q);
		
		/*for (String s : results) {
			System.out.println(s);
		} */	
		
		return results;
	}
	
	
	
	/**
	 * Returns all instances for a certain label
	 * @param keyword Keyword user entered
	 * @return List of instances that have keyword as a label
	 */
	public ArrayList<String> getLabels(String uri, String endpoint) {

		String newURI = "";
		
		if (!uri.startsWith("<")) {
			newURI = "<" + uri + ">"; 
		}
		
		ArrayList<String> results = new ArrayList<String>();
		String q = PREFIX +
				   "SELECT DISTINCT ?s " +
				   "WHERE  {" +
				   "{" + 
				   newURI + " rdfs:label ?s ." +
				   "} " +
				   
				   "UNION {" +
				   newURI + " foaf:name ?s . } " +				   
				   
				   "UNION {" +
				   newURI + " dc:title ?s . } " +	
				   
				   "UNION {" +
				   newURI + " skos:prefLabel ?s . } " +	
				   
				   "UNION {" +
				   newURI + " skos:altLabel ?s . } " +	
				   
				   "UNION {" +
				   newURI + " fb:type.object.name ?s . } " +
				   
				   "UNION {" +
				   newURI + " basekb:m.06b ?s . } " +
				   
				   // add other label properties
				   "}";
		
		//spql.searchSPARQL(q, endpoint);
		results = cleanSPARQL(endpoint, q);
		
		/*for (String s : results) {
			System.out.println(s);
		} */	
		
		return results;
	}
	
	
	/**
	 * Returns all labels for a certain instance
	 * @param instance Instance
	 * @return List of labels for a certain instance
	 */
	public ArrayList<String> getMoreLabels(ArrayList<String> firstResults, String endpoint) {
		
		ArrayList<String> results = new ArrayList<String>();
		
		for (String s1 : firstResults) {
			if (s1.startsWith("http://")) {
				//System.out.println(s1);

				String q = "";

				if (s1.startsWith("<")) {
					q = PREFIX +
							"SELECT DISTINCT ?s " +
							"WHERE  {" +
							s1 + " rdfs:label ?s ." +
							"}"; 
				}
				else {
					q = PREFIX +
							"SELECT DISTINCT ?s " +
							"WHERE  {" +
							"<" + s1 + "> rdfs:label ?s ." +
							"}"; 
				}
		
				results.addAll(cleanSPARQL(endpoint, q));

			}
			else {
				results.add(s1);
			}
		}
		return results;
	}
	
	/**
	 * Runs algorithm for test set (uses relations learned from test set)
	 * @throws IOException
	 */
	public void runTest() throws IOException {

		ReadWrite rw = new ReadWrite();
		relmap = rw.readTrainingFile("/Users/Isabelle/Documents/Freitas-Synonyms/relations.txt");
		
		HashMap<String, Set<String>> keywordsExt;
		
		//Set<String> testset = rw.readSetFromFile("/Users/Isabelle/Documents/Freitas-Synonyms/Freitas-Test_Set.txt");
		Set<String> testset = rw.readSetFromFile("/Users/Isabelle/Documents/EKP-Expansion/input-test.txt");

		
		for (String test : testset) {
			/**
			 * counts frequency of URIs for each test instance
			 */
			HashMap<String, Integer> uriCnt = new HashMap<String, Integer>();
			
			/**
			 * counts frequency of URIs for each test instance for split keywords
			 */
			HashMap<String, Integer> uriCntSingle = new HashMap<String, Integer>();
			
			/**
			 * counts frequency of URIs for each test instance for both keywords
			 */
			HashMap<String, Integer> uriCntBoth = new HashMap<String, Integer>();

			/**
			 * contains entries for both whole and split keywords
			 */
			HashMap<String, Set<String>> dbpediaURIs = new HashMap<String, Set<String>>();
			
			/**
			 * contains all entries (DBpedia URI, predicate) for split keywords
			 */
			HashMap<String, Set<String>> dbpediaURIsSingle = new HashMap<String, Set<String>>();
			
			/**
			 * contains all entries (DBpedia URI, predicate) for whole keywords
			 */
			HashMap<String, Set<String>> dbpediaURIsBoth = new HashMap<String, Set<String>>();
			
			
			//dbpediaURIs = new HashMap<String, Set<String>>();
			ArrayList printlist = new ArrayList<String>();
			
			ArrayList<String> testURIs = new ArrayList<String>();
			keywordsExt = new HashMap<String, Set<String>>();
			testURIs = getInstancesWithLabel(test, sindiceEndpoint);
			
			// run queries
			keywordsExt = getAllKeywordsTrained(testURIs, sindiceEndpoint);  //replace that with tested relations
			
			// get DBpedia URIs		
			Iterator<Entry<String, Set<String>>> entries = keywordsExt.entrySet().iterator();
			while(entries.hasNext()) {
				try {
					Entry<String, Set<String>> entry = entries.next();
					String key = entry.getKey();
					Set<String> value = entry.getValue();
					ArrayList<String> dbURI = new ArrayList<String>();
					for (String v : value) {
						v = v.replace("\"", "");
						v = v.replace("\n", "");
						v = v.replace("/", "");
						if (v != "" && !v.contains("\"")) {
							dbURI = getInstancesWithLabel(v, dbpediaEndpoint);
							for (String uri : dbURI) {
								if(uri.startsWith("http://dbpedia.org/ontology/")) {  // exclude non dbpedia-ontology
									if (dbpediaURIs.containsKey(uri)) {
										Set<String> preds = new HashSet<String>();
										preds = dbpediaURIs.get(uri);
										preds.add(key);
										dbpediaURIs.put(uri, preds);
										dbpediaURIsBoth.put(uri, preds);
										Integer cnt;
										if (uriCntBoth.containsKey(uri)) {
											cnt = uriCntBoth.get(uri);
											cnt ++;
										}
										else {
											cnt = 1;
										}
										uriCntBoth.put(uri, cnt);
										//System.out.println("DBpediaURI = " + uri + ", Predicate = " + preds);
									}
									else {
										Set<String> preds = new HashSet<String>();
										preds.add(key);
										dbpediaURIs.put(uri, preds);
										dbpediaURIsBoth.put(uri, preds);
										Integer cnt;
										if (uriCntBoth.containsKey(uri)) {
											cnt = uriCntBoth.get(uri);
											cnt ++;
										}
										else {
											cnt = 1;
										}
										uriCntBoth.put(uri, cnt);
										//System.out.println("DBpediaURI = " + uri + ", Predicate = " + preds);
										//printlist.add("DBpediaURI = " + uri + ", Predicate = " + value);
									}
								}

							}
						}
						if (v.contains(" ")) {  // check for parts of the labels
							// System.out.println("Key to split: " + key);
							String[] keysplit = v.split(" ");
							for (String splitkey : keysplit) {
								if (splitkey.length()>0 && splitkey != "" && !splitkey.contains("\"")) {
									dbURI = getInstancesWithLabel(splitkey, dbpediaEndpoint);
									for (String uri : dbURI) {
										if(uri.startsWith("http://dbpedia.org/ontology/")) {  // exclude non dbpedia-ontology
											if (dbpediaURIs.containsKey(uri)) {
												Set<String> preds = new HashSet<String>();
												preds = dbpediaURIs.get(uri);
												preds.add(key);
												dbpediaURIs.put(uri, preds);
												dbpediaURIsSingle.put(uri, preds);
												Integer cnt;
												if (uriCntSingle.containsKey(uri)) {
													cnt = uriCntSingle.get(uri);
													cnt ++;
												}
												else {
													cnt = 1;
												}
												uriCntSingle.put(uri, cnt);
												//System.out.println("DBpediaURI = " + uri + ", Predicate = " + preds);
											}
											else {
												Set<String> preds = new HashSet<String>();
												preds.add(key);
												dbpediaURIs.put(uri, preds);
												dbpediaURIsSingle.put(uri, preds);
												Integer cnt;
												if (uriCntSingle.containsKey(uri)) {
													cnt = uriCntSingle.get(uri);
													cnt ++;
												}
												else {
													cnt = 1;
												}
												uriCntSingle.put(uri, cnt);
												//System.out.println("DBpediaURI = " + uri + ", Predicate = " + preds);
												//printlist.add("DBpediaURI = " + uri + ", Predicate = " + value);
											}
										}
									}
								}
							}
						}
					}
					System.out.println("\n");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			dbpediaURIs.putAll(dbpediaURIsBoth);
			dbpediaURIs.putAll(dbpediaURIsSingle);
			uriCnt.putAll(uriCntBoth);
			uriCnt.putAll(uriCntSingle);
			System.out.println("List whole");
			System.out.println(dbpediaURIsBoth);
			System.out.println(uriCntBoth);
			System.out.println("List split");
			System.out.println(dbpediaURIsSingle);
			System.out.println(uriCntSingle);

			/*rw.printHashMapOnFile(dbpediaURIsSingle, "/Users/Isabelle/Documents/Freitas-Synonyms/test-out/" + test + "_single");
			rw.printHashMapOnFile(dbpediaURIsBoth, "/Users/Isabelle/Documents/Freitas-Synonyms/test-out/" + test + "_both");
			rw.printHashMapOnFile(dbpediaURIs, "/Users/Isabelle/Documents/Freitas-Synonyms/test-out/" + test); */
			
			rw.printHashMapOnFile(dbpediaURIsSingle, "/Users/Isabelle/Documents/EKP-Expansion/test-out/" + test + "_single");
			rw.printHashMapOnFile(dbpediaURIsBoth, "/Users/Isabelle/Documents/EKP-Expansion/test-out/" + test + "_both");
			rw.printHashMapOnFile(dbpediaURIs, "/Users/Isabelle/Documents/EKP-Expansion/test-out/" + test);
			
			ResultRanking rr = new ResultRanking();
			
			// rank URIs in reverse order
			Map<Double, String> rankmapSingle = new TreeMap<Double, String>(Collections.reverseOrder());
			
			Iterator<Entry<String, Set<String>>> urientriesSingle = dbpediaURIsSingle.entrySet().iterator();
			while(urientriesSingle.hasNext()) {
				Double rank = new Double(0);
				Entry<String, Set<String>> urientry = urientriesSingle.next();
				String key = urientry.getKey();
				Set<String> value = urientry.getValue();
				ArrayList<String> dbURI = new ArrayList<String>();
				rank = rr.runRanking(key, value, uriCntSingle.get(key));
				System.out.println(rank + "\t" + key);
				rankmapSingle.put(rank, key);
			}
			
			System.out.println("rankmapSingle: " + rankmapSingle);
			//rw.printRankedResultsOnFile(rankmapSingle, "/Users/Isabelle/Documents/Freitas-Synonyms/test-out-ranked/" + test + "_single");
			rw.printRankedResultsOnFile(rankmapSingle, "/Users/Isabelle/Documents/EKP-Expansion/test-out-ranked/" + test + "_single");

			
			// rank URIs in reverse order
			Map<Double, String> rankmapBoth = new TreeMap<Double, String>(Collections.reverseOrder());
			
			Iterator<Entry<String, Set<String>>> urientriesBoth = dbpediaURIsBoth.entrySet().iterator();
			while(urientriesBoth.hasNext()) {
				Double rank = new Double(0);
				Entry<String, Set<String>> urientry = urientriesBoth.next();
				String key = urientry.getKey();
				Set<String> value = urientry.getValue();
				ArrayList<String> dbURI = new ArrayList<String>();
				rank = rr.runRanking(key, value, uriCntBoth.get(key));
				System.out.println(rank + "\t" + key);
				rankmapBoth.put(rank, key);
			}
			
			System.out.println("rankmapBoth: " + rankmapBoth);
			//rw.printRankedResultsOnFile(rankmapBoth, "/Users/Isabelle/Documents/Freitas-Synonyms/test-out-ranked/" + test + "_both");
			rw.printRankedResultsOnFile(rankmapBoth, "/Users/Isabelle/Documents/EKP-Expansion/test-out-ranked/" + test + "_both");

		
			Map<Double, String> rankmap = new TreeMap<Double, String>(Collections.reverseOrder());
			rankmap.putAll(rankmapSingle);
			rankmap.putAll(rankmapBoth);
			
			System.out.println("rankmap: " + rankmap);
			//rw.printRankedResultsOnFile(rankmap, "/Users/Isabelle/Documents/Freitas-Synonyms/test-out-ranked/" + test);
			rw.printRankedResultsOnFile(rankmap, "/Users/Isabelle/Documents/EKP-Expansion/test-out-ranked/" + test);

		}
	}
	
	
	/**
	 * Computes precision for training examples
	 * @throws IOException
	 */
	public void runTraining() throws IOException {
		// read file here instead
		//ArrayList<String> testKeywords = new ArrayList<String>();
		HashMap<String, Set<String>> keywordsExt = new HashMap<String, Set<String>>();
		HashMap<String, Set<String>> dbpediaURIs = new HashMap<String, Set<String>>();

		//testKeywords.add("plane");
		
		ReadWrite rw = new ReadWrite();
		//Set<String> trainset = rw.readSetFromFile("/Users/Isabelle/Documents/Freitas-Synonyms/Freitas-Training_Set.txt");
		Set<String> trainset = rw.readSetFromFile("/Users/Isabelle/Documents/EKP-Expansion/Freitas-Training_Set.txt");

		
		for (String train : trainset) {
			dbpediaURIs = new HashMap<String, Set<String>>();
			ArrayList printlist = new ArrayList<String>();
			
			ArrayList<String> testURIs = new ArrayList<String>();
			keywordsExt = new HashMap<String, Set<String>>();
			testURIs = getInstancesWithLabel(train, sindiceEndpoint);
			
			// run queries
			keywordsExt = getAllKeywords(testURIs, sindiceEndpoint);
			
			// get DBpedia URIs
			
			
			Iterator<Entry<String, Set<String>>> entries = keywordsExt.entrySet().iterator();
			while(entries.hasNext()) {
				try {
					Entry<String, Set<String>> entry = entries.next();
					String key = entry.getKey();
					Set<String> value = entry.getValue();
					ArrayList<String> dbURI = new ArrayList<String>();
					key = key.replace("\"", "");
					key = key.replace("\n", "");
					key = key.replace("/", "");
					if (key != "" && !key.contains("\"")) {
						dbURI = getInstancesWithLabel(key, dbpediaEndpoint);
						for (String uri : dbURI) {
							if(uri.startsWith("http://dbpedia.org/ontology/")) {  // exclude non dbpedia-ontology
								if (dbpediaURIs.containsKey(uri)) {
									Set<String> val = new HashSet<String>();
									val = dbpediaURIs.get(uri);
									value.addAll(val);
									dbpediaURIs.put(uri, value);
									System.out.println("DBpediaURI = " + uri + ", Predicate = " + value);
								}
								else {
									dbpediaURIs.put(uri, value);
									System.out.println("DBpediaURI = " + uri + ", Predicate = " + value);
									//printlist.add("DBpediaURI = " + uri + ", Predicate = " + value);
								}
							}

						}
					}
					if (key.contains(" ")) {  // check for parts of the labels
						// System.out.println("Key to split: " + key);
						String[] keysplit = key.split(" ");
						for (String splitkey : keysplit) {
							if (splitkey.length()>0 && splitkey != "" && !splitkey.contains("\"")) {
								dbURI = getInstancesWithLabel(splitkey, dbpediaEndpoint);
								for (String uri : dbURI) {
									if(uri.startsWith("http://dbpedia.org/ontology/")) {  // exclude non dbpedia-ontology
										if (dbpediaURIs.containsKey(uri)) {
											Set<String> val = new HashSet<String>();
											val = dbpediaURIs.get(uri);
											value.addAll(val);
											dbpediaURIs.put(uri, value);
											System.out.println("DBpediaURI = " + uri + ", Predicate = " + value);
										}
										else {
											dbpediaURIs.put(uri, value);
											System.out.println("DBpediaURI = " + uri + ", Predicate = " + value);
											//printlist.add("DBpediaURI = " + uri + ", Predicate = " + value);
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			//rw.printHashMapOnFile(dbpediaURIs, "/Users/Isabelle/Documents/Freitas-Synonyms/training-out/" + train);
			rw.printHashMapOnFile(dbpediaURIs, "/Users/Isabelle/Documents/EKP-Expansion/training-out/" + train);

			//rw.printListOnFile(printlist, "/Users/Isabelle/Documents/Freitas-Synonyms/training-out/" + train);
			
			// also print dbpedia-ontology concepts
			// if there is a dbpedia-ontology concept match, print it with
			// the properties that lead to it
			// check manually which of these concepts are correct
			// save them in a file
		}
		//rw.printCntOnFile(all_cnt, "/Users/Isabelle/Documents/Freitas-Synonyms/training-out/_all_cnt");

	}
	
	
	
	public HashMap<String, Integer> evaluateTrainingResults(){
		ReadWrite rw = new ReadWrite();
		
		// read exact match files
		// for each of the exact match files
				// get all predicates
				// put them in HashMap with their count
		//HashMap<String, HashMap<String,Integer>> exactMatchMap = rw.readEvalData("/Users/Isabelle/Documents/Freitas-Synonyms/training-out-useful-exact/");
		HashMap<String, HashMap<String,Integer>> exactMatchMap = rw.readEvalData("/Users/Isabelle/Documents/EKP-Expansion/training-out-useful-exact/");

		
		HashMap<String, Integer> exactCnt = new HashMap<String, Integer>();

		// read all files
				// get all predicates
				// put them in HashMap with their count
		//HashMap<String, HashMap<String,Integer>> allMatchMap = rw.readEvalData("/Users/Isabelle/Documents/Freitas-Synonyms/training-out-without-kb");
		HashMap<String, HashMap<String,Integer>> allMatchMap = rw.readEvalData("/Users/Isabelle/Documents/EKP-Expansion/training-out-without-kb");

		
		HashMap<String, Integer> allCnt = new HashMap<String, Integer>();
	    
	    
		// write both HashMaps to file (pred, count)
		
		String key = "";
		HashMap<String, Integer> exactMatchValue = new HashMap<String, Integer>();
		HashMap<String, Integer> allMatchValue = new HashMap<String, Integer>();
		
		Iterator<Entry<String, HashMap<String, Integer>>> entries = exactMatchMap.entrySet().iterator();
		while(entries.hasNext()) {
			Entry<String, HashMap<String, Integer>> entry = entries.next();
			key = entry.getKey();
			exactMatchValue = entry.getValue();
			allMatchValue = allMatchMap.get(key);
			for (String pred : exactMatchValue.keySet()) {
				Integer exactMatchCnt = exactMatchValue.get(pred);
				Integer allMatchCnt = allMatchValue.get(pred);
				if (exactCnt.containsKey(pred)) {
					exactCnt.put(pred, exactCnt.get(pred) + exactMatchCnt);
					allCnt.put(pred, allCnt.get(pred) + allMatchCnt);
				}
				else {
					exactCnt.put(pred, exactMatchCnt);
					allCnt.put(pred, allMatchCnt);
				}
				//Float precision = new Float (new Float(exactMatchValue.get(pred)) / new Float(allMatchValue.get(pred)));
				//System.out.println(precision);
			}
			//exactMatchMap.put(key, exactCnt);
			//allMatchMap.put(key, allCnt);
		}


		Iterator<Entry<String, Integer>> exactentries = exactCnt.entrySet().iterator();
		while(exactentries.hasNext()) {
			Entry<String, Integer> exactentry = exactentries.next();
			String key2 = exactentry.getKey();
			Float precision = new Float (new Float(exactCnt.get(key2)) / new Float(allCnt.get(key2)));
			System.out.println(precision + "\t" + exactCnt.get(key2) + "\t" + allCnt.get(key2) + "\t" + key2);
		}

		return exactCnt;
		
		
		// for each entity in the ExactMatchHashMap
			// get count
			// get count of same entity in AllHashMap
			// precision <- countExact/countSame
		
		// Why am I doing that for each entity? Doesn't make any sense. You want precision values for the predicates
		/*
		Iterator<Entry<String, HashMap<String, Integer>>> entries = exactMatchMap.entrySet().iterator();
		while(entries.hasNext()) {
			Entry<String, HashMap<String, Integer>> entry = entries.next();
			String key = entry.getKey();
			HashMap<String, Integer> exactMatchValue = entry.getValue();
			HashMap<String, Integer> allMatchValue = allMatchMap.get(key);
			for (String pred : exactMatchValue.keySet()) {
				Float precision = new Float (new Float(exactMatchValue.get(pred)) / new Float(allMatchValue.get(pred)));
				System.out.println(precision);
			}
		} */

	}
	

	public HashMap<String, Set<String>> getAllKeywordsTrained(ArrayList<String> testURIs, String endpoint) {
		// get all objects
		// if object is already a literal, put it in HashMap
		// if object is a long literal (description) --> maybe first ignore that? (tokenise it, remove stop words, but what to do with MWE?)
		// if object is a URI, look up label (not just rdf:label, but also other labels?)
		// compute TF/IDF for literal
		
		HashMap<String, Set<String>> preobj = new HashMap<String, Set<String>>();
		ArrayList<String> labels = new ArrayList<String>();
		ArrayList<String> moreLabels = new ArrayList<String>();
		ArrayList<String> evenMoreLabels = new ArrayList<String>();
		HashMap<String, Set<String>> finalPreObj = new HashMap<String, Set<String>>();  // values are literals, not uris
		
		for (String uri : testURIs) {

			try {
				System.out.println(uri + ": ");
				
				preobj = getPredObjTrained(uri, endpoint);
				Iterator<Entry<String, Set<String>>> entries = preobj.entrySet().iterator();

				while(entries.hasNext()) {
					Entry<String, Set<String>> entry = entries.next();
					String key = entry.getKey();
					Set<String> value = entry.getValue();
					Set<String> valueset = new HashSet<String>();
					for (String v : value) {
						if (v.startsWith("\"")) {
							v = v.substring(1, v.length()-2);
						}
						
						if (v.startsWith("http://")) { // if object is a uri, we want the label(s)
							moreLabels = getLabels(v, endpoint);
							Iterator<Entry<String, Set<String>>> moreEntries = preobj.entrySet().iterator();
							for (String lbl : moreLabels) {
								//finalPreObj.put(lbl, value);
								if (lbl.startsWith("\"")) {   // sometimes there's a URI with ""
									lbl = lbl.substring(1, lbl.length()-1);
									evenMoreLabels = getLabels(v, endpoint);
									Iterator<Entry<String, Set<String>>> evenMoreEntries = preobj.entrySet().iterator();
									for (String moreLbl : evenMoreLabels) {
										if (finalPreObj.containsKey(lbl)) {
											valueset = finalPreObj.get(lbl);
											valueset.addAll(value);
											finalPreObj.put(lbl, valueset);
										}
										else {
											valueset = new HashSet<String>();
											valueset.addAll(value);
											finalPreObj.put(lbl, valueset); // key (object) must be identifier
										}
									}
								}
								
								if (finalPreObj.containsKey(key)) {
									valueset = finalPreObj.get(key);
									valueset.add(lbl);
									finalPreObj.put(key, valueset);
								}
								else {
									valueset = new HashSet<String>();
									valueset.add(lbl);
									finalPreObj.put(key, valueset); // key (object) must be identifier
								}
								System.out.println("Key = " + key + ", Value = " + lbl);
							}
						}
						else {
							//finalPreObj.put(key, value);
							if (finalPreObj.containsKey(key)) {
								valueset = finalPreObj.get(key);
								valueset.add(v);
								finalPreObj.put(key, valueset);
							}
							else {
								valueset = new HashSet<String>();
								valueset.add(v);
								finalPreObj.put(key, valueset); // key (object) must be identifier
							}
							System.out.println("Key = " + key + ", Value = " + v);
						}
					
						for (String s : valueset) {
							if (all_cnt.containsKey(s)) {
								Integer cnt = all_cnt.get(s);
								cnt ++;
								all_cnt.put(s, cnt);
							}
							else {
								all_cnt.put(s, 1);
								useful_cnt.put(s, 0);
							}
						}
					}

					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			continue;
		}
		return finalPreObj;
	}
	
	
	public HashMap<String, Set<String>> getAllKeywords(ArrayList<String> testURIs, String endpoint) {
		// get all objects
		// if object is already a literal, put it in HashMap
		// if object is a long literal (description) --> maybe first ignore that? (tokenise it, remove stop words, but what to do with MWE?)
		// if object is a URI, look up label (not just rdf:label, but also other labels?)
		// compute TF/IDF for literal
		
		HashMap<String, Set<String>> preobj = new HashMap<String, Set<String>>();
		ArrayList<String> labels = new ArrayList<String>();
		ArrayList<String> moreLabels = new ArrayList<String>();
		ArrayList<String> evenMoreLabels = new ArrayList<String>();
		HashMap<String, Set<String>> finalPreObj = new HashMap<String, Set<String>>();  // values are literals, not uris
		
		for (String uri : testURIs) {

			try {
				System.out.println(uri + ": ");
				
				preobj = getPredObj(uri, endpoint);
				Iterator<Entry<String, Set<String>>> entries = preobj.entrySet().iterator();

				while(entries.hasNext()) {
					Entry<String, Set<String>> entry = entries.next();
					String key = entry.getKey();
					Set<String> value = entry.getValue();
					Set<String> valueset = new HashSet<String>();
					
					if (key.startsWith("\"")) {
						key = key.substring(1, key.length()-2);
					}
					
					if (key.startsWith("http://")) { // if object is a uri, we want the label(s)
						moreLabels = getLabels(key, endpoint);
						Iterator<Entry<String, Set<String>>> moreEntries = preobj.entrySet().iterator();
						for (String lbl : moreLabels) {
							//finalPreObj.put(lbl, value);
							if (lbl.startsWith("\"")) {   // sometimes there's a URI with ""
								lbl = lbl.substring(1, lbl.length()-1);
								evenMoreLabels = getLabels(key, endpoint);
								Iterator<Entry<String, Set<String>>> evenMoreEntries = preobj.entrySet().iterator();
								for (String moreLbl : evenMoreLabels) {
									if (finalPreObj.containsKey(lbl)) {
										valueset = finalPreObj.get(lbl);
										valueset.addAll(value);
										finalPreObj.put(lbl, valueset);
									}
									else {
										valueset = new HashSet<String>();
										valueset.addAll(value);
										finalPreObj.put(lbl, valueset); // key (object) must be identifier
									}
								}
							}
							
							if (finalPreObj.containsKey(lbl)) {
								valueset = finalPreObj.get(lbl);
								valueset.addAll(value);
								finalPreObj.put(lbl, valueset);
							}
							else {
								valueset = new HashSet<String>();
								valueset.addAll(value);
								finalPreObj.put(lbl, valueset); // key (object) must be identifier
							}
							System.out.println("Key = " + lbl + ", Value = " + value);
						}
					}
					else {
						//finalPreObj.put(key, value);
						
						if (finalPreObj.containsKey(key)) {
							valueset = finalPreObj.get(key);
							valueset.addAll(value);
							finalPreObj.put(key, valueset);
						}
						else {
							valueset = new HashSet<String>();
							valueset.addAll(value);
							finalPreObj.put(key, valueset); // key (object) must be identifier
						}
						System.out.println("Key = " + key + ", Value = " + value);
					}
				
					for (String s : valueset) {
						if (all_cnt.containsKey(s)) {
							Integer cnt = all_cnt.get(s);
							cnt ++;
							all_cnt.put(s, cnt);
						}
						else {
							all_cnt.put(s, 1);
							useful_cnt.put(s, 0);
						}
					}
					
				}
				System.out.print("\n\n");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			continue;
		}
		return finalPreObj;
		
	}
	
	
	/**
	 * gets all objects
	 * @param service
	 * @param sparqlQueryString
	 * @return
	 */
	public HashMap<String, Set<String>> getPredObjTrained(String s, String endpoint) {
		
		HashMap<String, Set<String>> results = new HashMap<String, Set<String>>();
        
		Iterator<Entry<String, Float>> entries = relmap.entrySet().iterator();
        
		if (s.startsWith("http://")) {
			while(entries.hasNext()) {
				ArrayList<String> partreslist = new ArrayList<String>();
				Entry<String, Float> entry = entries.next();
				String key = entry.getKey();
				Float value = entry.getValue();
				//System.out.println(key);
				String q = PREFIX +
						   "SELECT DISTINCT ?s " +
						   "WHERE {" +
						   "{" + 
			        		"<" + s + "> <" + key + ">" + " ?s .}" +
						   "} ";	
				partreslist = cleanSPARQL(endpoint, q);
				HashSet<String> partres = new HashSet<String>(partreslist);
				results.put(key, partres);
			}
		}
		else {
			while(entries.hasNext()) {
				ArrayList<String> partreslist = new ArrayList<String>();
				Entry<String, Float> entry = entries.next();
				String key = entry.getKey();
				Float value = entry.getValue();
				//System.out.println(key);
			
				//System.out.println(key);
				String q = PREFIX +
						   "SELECT DISTINCT ?s " +
						   "WHERE {" +
						   "{" + 
			        		s + " <" + key + ">" + " ?s .}" +
						   "} ";
				partreslist = cleanSPARQL(endpoint, q);
				HashSet<String> partres = new HashSet<String>(partreslist);
				results.put(key, partres);
			}
		}
		
		//results = cleanSPARQL_PO(endpoint, q);
				//System.out.println(results + "\n");
		
		return results;
	}
	
	
	/**
	 * gets all objects
	 * @param service
	 * @param sparqlQueryString
	 * @return
	 */
	public HashMap<String, Set<String>> getPredObj(String s, String endpoint) {
		HashMap<String, Set<String>> results = new HashMap<String, Set<String>>();
		
			if (s.startsWith("http://")) {
				String q = "";
				//System.out.println(s);
				if (s.startsWith("<")) {
					q = PREFIX +
							"SELECT DISTINCT ?p ?o " +
							"WHERE  {" +
							s + " ?p ?o ." +
							"}"; 
				}
				else {
					q = PREFIX +
							"SELECT DISTINCT ?p ?o " +
							"WHERE  {" +
							"<" + s + "> ?p ?o ." +
							"}"; 
				}
			
				results = cleanSPARQL_PO(endpoint, q);
				//System.out.println(results + "\n");
			}
		
		return results;
	}
	
	/**
	 * gets all objects
	 * @param service
	 * @param sparqlQueryString
	 * @return
	 */
	public ArrayList<String> getObjects(String s, String endpoint) {
		ArrayList<String> results = new ArrayList<String>();
		
			if (s.startsWith("http://")) {
				String q = "";
				//System.out.println(s);
				if (s.startsWith("<")) {
					q = PREFIX +
							"SELECT DISTINCT ?s " +
							"WHERE  {" +
							s + " ?p ?s ." +
							"}"; 
				}
				else {
					q = PREFIX +
							"SELECT DISTINCT ?s " +
							"WHERE  {" +
							"<" + s + "> ?p ?s ." +
							"}"; 
				}
			
				results.addAll(cleanSPARQL(endpoint, q));
				//System.out.println(results + "\n");
			}
		
		return results;
	}
	
	
	/**
	 * gets all objects
	 * @param service
	 * @param sparqlQueryString
	 * @return
	 */
	public ArrayList<String> getObjects(ArrayList<String> subjects, String endpoint) {
		ArrayList<String> results = new ArrayList<String>();
		
		for (String s : subjects) {
			if (s.startsWith("http://")) {
				String q = "";
				//System.out.println(s);
				if (s.startsWith("<")) {
					q = PREFIX +
							"SELECT ?s " +
							"WHERE  {" +
							s + " ?p ?s ." +
							"}"; 
				}
				else {
					q = PREFIX +
							"SELECT ?s " +
							"WHERE  {" +
							"<" + s + "> ?p ?s ." +
							"}"; 
				}
			
				results.addAll(cleanSPARQL(endpoint, q));
				//System.out.println(results);
			}
		}
		
		return results;
	}
	
	
	/**
	 * gets all objects
	 * @param service
	 * @param sparqlQueryString
	 * @return
	 */
	public ArrayList<String> getPredicates(String s, String endpoint) {
		ArrayList<String> results = new ArrayList<String>();
		
			if (s.startsWith("http://")) {
				String q = "";
				//System.out.println(s);
				if (s.startsWith("<")) {
					q = PREFIX +
							"SELECT ?s " +
							"WHERE  {" +
							s + " ?s ?o ." +
							"}"; 
				}
				else {
					q = PREFIX +
							"SELECT ?s " +
							"WHERE  {" +
							"<" + s + "> ?s ?o ." +
							"}"; 
				}
			
				results.addAll(cleanSPARQL(endpoint, q));
				//System.out.println(results + "\n");
			}
		
		return results;
	}
	
	/**
	 * Executes SPARQL query and cleans it, returns results as Set
	 * @return results as Set
	 */
	public HashMap<String, Set<String>> cleanSPARQL_PO(String service, String sparqlQueryString) {
		HashMap<String, Set<String>> resulStrings = new HashMap<String, Set<String>>();

		String solutionConcept1 = "p";
		String solutionConcept2 = "o";
		
		Query query = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(service,
				query);

		try {
			ResultSet results = qexec.execSelect();

			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();

				Literal l;
				try {
					String tmp1 = "";
					String tmp2 = "";
					
					if (soln.get(solutionConcept1).isLiteral()) {
						//System.out.println("soln.get(solutionConcept:  )" + soln.get(solutionConcept).toString());
						l = soln.getLiteral(solutionConcept1);   // l still has language tag
						if (l.toString().contains("@")) {
							if (l.toString().contains("@en")) {
								tmp1 = l.getString();  // resulsString doesn't
							}
							else {}
						}
						else {
							tmp1 = l.getString();}   // resulsString doesn't
						}				
					else {
						RDFNode u = soln.get(solutionConcept1);
						tmp1 = u.toString();
					}
					
					if (soln.get(solutionConcept2).isLiteral()) {
						//System.out.println("soln.get(solutionConcept:  )" + soln.get(solutionConcept).toString());
						l = soln.getLiteral(solutionConcept2);   // l still has language tag
						if (l.toString().contains("@")) {
							if (l.toString().contains("@en")) {
								tmp2 = l.getString();  // resulsString doesn't
							}
							else {}
						}
						else {
							tmp2 = l.getString();}   // resulsString doesn't
						}				
					else {
						RDFNode u = soln.get(solutionConcept2);
						tmp2 = u.toString();
					}
					if (resulStrings.containsKey(tmp2)) {
						Set<String> tmp1set = resulStrings.get(tmp2);
						tmp1set.add(tmp1);
						resulStrings.put(tmp2, tmp1set);
					}
					else {
						Set<String> tmp1set = new HashSet<String>();
						tmp1set.add(tmp1);
						resulStrings.put(tmp2, tmp1set); // key (object) must be identifier
					}
				} catch (Exception e) {
					//l4j.debug("Escaped result. Can't parse "+soln);
					e.printStackTrace();
				}
				continue;
			}		

		} finally {
			qexec.close();
		}
		return resulStrings;
	}
	
	/**
	 * Executes SPARQL query and cleans it, returns results as Set
	 * @return results as Set
	 */
	public static ArrayList<String> cleanSPARQL(String service, String sparqlQueryString) {
		ArrayList<String> resulStrings = new ArrayList<String>();

		String solutionConcept = "s";
		
		Query query = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(service,
				query);

		try {
			ResultSet results = qexec.execSelect();

			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();

				Literal l;
				try {
					if (soln.get(solutionConcept).isLiteral()) {
						//System.out.println("soln.get(solutionConcept:  )" + soln.get(solutionConcept).toString());
						l = soln.getLiteral(solutionConcept);   // l still has language tag
						if (l.toString().contains("@")) {
							if (l.toString().contains("@en")) {
								resulStrings.add(l.getString());  // resulsString doesn't
							}
							else {}
						}
						else {
							resulStrings.add(l.getString());}   // resulsString doesn't
						}				
					else {
						RDFNode u = soln.get(solutionConcept);
						resulStrings.add(u.toString());
					}
				} catch (Exception e) {
					//l4j.debug("Escaped result. Can't parse "+soln);
					e.printStackTrace();
				}
				continue;
			}
			
		} finally {
			qexec.close();
		}
		return resulStrings;
	}
}
