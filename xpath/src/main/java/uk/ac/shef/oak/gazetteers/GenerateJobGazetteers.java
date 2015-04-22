package uk.ac.shef.oak.gazetteers;

import java.io.File;

	// Querying DBpedia by using Jena-ARQ 
		/**
	 * @author annalisa
	 */

	public class GenerateJobGazetteers extends GenerateGazeteer{
		
		public static void main(String[] args) {
			GenerateJobGazetteers.generateGazeteer("./cacheCloudGazeteer"+ File.separator+"job");

		}
		/**
		 * @param args
		 */
		public static void generateGazeteer(String resFolder) {

	GenerateJobGazetteers queryDBPedia = new GenerateJobGazetteers();
	logStart(queryDBPedia.getClass().getName());

	checkService(getSERVICE());


	String sparqlQueryString ="";
	//TODO write proper query company
	
	//TODO write proper query date_posted
	
	//TODO write proper query location
	
	//TODO write proper query title
	
	queryDBPedia.queryService(getSERVICE(), sparqlQueryString, "name");	

}
}