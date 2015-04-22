package uk.ac.shef.dcs.oak.triplesearch.queryextension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Author: Isabelle Augenstein (i.augenstein@sheffield.ac.uk)
 * Date: 30/01/13
 * Time: 16:13
 */
public class ResultRanking {

	HashMap<String, Integer> tfdenom = new HashMap<String, Integer>();
	HashMap<String, Integer> idfdenom = new HashMap<String, Integer>();
	
	/**
	 * compute TFIDF for URIs
	 * @param syncand URI which is possible a synonym
	 * @param freq frequency of URI in test set
	 * @throws IOException 
	 */
	public double computeTFIDF(String syncand, Integer freq) throws IOException {
		
		ReadWrite rw = new ReadWrite();
		
		// input: syn URI candidate + count in test set
		
		// TF-num <- input from evaluation

		
		// TF-denom
		// for each of the training files
		// get all DBpedia URIs
		// is it syn URI candidate?
			// count
			
		double tf = 0;
		double idf = 0;
		if (tfdenom.containsKey(syncand)) {
			tf = new Float(freq)/new Float(tfdenom.get(syncand)+1);
		}
		else {
			tf = new Float(freq)/new Float(1);
		}
		if (idfdenom.containsKey(syncand)) {
			idf = Math.log10(new Float(63)/new Float(idfdenom.get(syncand))+1);
		}
		else {
			idf = Math.log10(new Float(63)/new Float(1));
		}
		
		double tfidf = tf*idf;
			
		System.out.println("tf: " + freq + "/" + tfdenom.get(syncand));
		System.out.println("idf: 63/" + idfdenom.get(syncand));
		System.out.println("tfidf: " + tfidf);
	
		
		// IDF log (num/denom)
		
		// IDF-num
		// number of training sets (no counting necessary)
		
		// IDF-denom
		// for each of the training files
		// get all DBpedia URIs
		// Is one of the DBpedia URIs the syn URI candidate?
			// count
			// break
		
		return tfidf;
	}
	
	public double runRanking(String syncand, Set<String> rels, Integer freq) throws IOException {
		KeywordExtension ke = new KeywordExtension();
		ReadWrite rw = new ReadWrite();
		
		if (ke.relmap.size() == 0) {
			ke.relmap = rw.readTrainingFile("/Users/Isabelle/Documents/Freitas-Synonyms/relations.txt");
		}
		if (tfdenom.size() == 0) {
			tfdenom = rw.readFromFilesTFDenom("/Users/Isabelle/Documents/Freitas-Synonyms/training-out-without-kb");
			System.out.println(tfdenom);

		}
		if (idfdenom.size() == 0) {
			idfdenom = rw.readFromFilesIDFDenom("/Users/Isabelle/Documents/Freitas-Synonyms/training-out-without-kb");
			System.out.println(idfdenom);	
		}
		
		double tfidf = computeTFIDF(syncand, freq);
		
		Float precision = new Float(0);
		for (String s : rels) {
			Float tmpprecision = new Float(0);
			if (ke.relmap.containsKey(s)) {
				tmpprecision = ke.relmap.get(s);
			}
			if (tmpprecision > precision) {
				precision = tmpprecision;
			}
		}
		
		System.out.println("precision: " + precision);
		double rank = tfidf*precision;
		System.out.println("tfidf*precision: " + rank);
		return rank;
	}

}
