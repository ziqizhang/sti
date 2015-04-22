package uk.ac.shef.dcs.oak.triplesearch.queryextension;

import java.io.IOException;

/**
 * Author: Isabelle Augenstein (i.augenstein@sheffield.ac.uk)
 * Date: 30/01/13
 * Time: 16:13
 */
public class FirstApproach {
		
	public static KeywordExtension ke = new KeywordExtension();
	public static ResultRanking rr = new ResultRanking();
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		// add again later
		/*Set<String> firstURIs = new HashSet<String>();
		firstURIs = ke.getInstancesWithLabel("pistol"); */
		
		// Get more labels, URIs for labels and those labels, but might not be a good idea
		/*
		Set<String> firstLabels = new HashSet<String>();
		
		firstLabels = ke.getMoreLabels(firstURIs);
		
		Set<String> secondURIs = new HashSet<String>();
		Set<String> secondLabels = new HashSet<String>();
		
		for (String label : firstLabels) {
			secondURIs.addAll(ke.getInstancesWithLabel(label));	
		}
		secondLabels = ke.getMoreLabels(secondURIs);
		Set<String> finalURIs = new HashSet<String>();
		finalURIs.addAll(firstURIs);
		finalURIs.addAll(secondURIs);
		System.out.println(finalURIs);
		
		ke.getAllKeywords(finalURIs); */
		
		// add again later
		/*System.out.println(firstURIs);
		ke.getAllKeywords(firstURIs); */
		
		//ke.runTraining();
		//ke.evaluateTrainingResults();
		ke.runTest();
		//rr.runRanking();
	}

}
