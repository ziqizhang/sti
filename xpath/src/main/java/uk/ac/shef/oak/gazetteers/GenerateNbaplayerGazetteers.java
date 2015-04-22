package uk.ac.shef.oak.gazetteers;

// Querying DBpedia by using Jena-ARQ 
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

/**
 * @author annalisa
 */

public class GenerateNbaplayerGazetteers extends GenerateGazeteer{

		
		public static void main(String[] args) {
			GenerateNbaplayerGazetteers.generateGazeteer("./cacheCloudGazeteer"+ File.separator+"nbaplayer");

		}
		/**
		 * @param args
		 */
		public static void generateGazeteer(String resFolder) {

		GenerateNbaplayerGazetteers queryDBPedia = new GenerateNbaplayerGazetteers();
		logStart(queryDBPedia.getClass().getName());

		checkService(getSERVICE());


		// name: http://dbpedia.org/property/name
		String sparqlQueryStringName = getPREFIX()
				+ '\n'
				+ "select distinct ?name where { "
				+ "?nba <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/BasketballPlayer> ."
				+ "?nba <http://dbpedia.org/property/name> ?name."
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		// team: http://dbpedia.org/property/team or
		// http://dbpedia.org/ontology/team or http://dbpedia.org/property/teams
		String sparqlQueryStringTeam1 = getPREFIX()
				+ '\n'
				+ "select distinct ?name where { "
				+ "?nba <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/BasketballPlayer>."
				+ "?nba <http://dbpedia.org/property/team> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		String sparqlQueryStringTeam2 = getPREFIX()
				+ '\n'
				+ "select distinct ?name where { "
				+ "?nba <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/BasketballPlayer>."
				+ "?nba <http://dbpedia.org/ontology/team> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		String sparqlQueryStringTeam3 = getPREFIX()
				+ '\n'
				+ "select distinct ?name where { "
				+ "?nba <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/BasketballPlayer>."
				+ "?nba <http://dbpedia.org/property/teams> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		// TODO write proper query weight
		// no weight available in DBpedia or schema.org

		
		// name
				Set<String> fullGazName = new HashSet<String>();
				Set<String> gazName1 = queryDBPedia.queryService(getSERVICE(),
						sparqlQueryStringName, "name");
				fullGazName.addAll(gazName1);
				
				// name
				Set<String> fullGazTeam = new HashSet<String>();
				Set<String> gazTeam1 = queryDBPedia.queryService(getSERVICE(),
						sparqlQueryStringTeam1, "name");
				Set<String> gazTeam2 = queryDBPedia.queryService(getSERVICE(),
						sparqlQueryStringTeam2, "name");
				Set<String> gazTeam3 = queryDBPedia.queryService(getSERVICE(),
						sparqlQueryStringTeam3, "name");
				
				fullGazTeam.addAll(gazTeam1);
				fullGazTeam.addAll(gazTeam2);
				fullGazTeam.addAll(gazTeam3);

				
				File auto = new File(resFolder);
				if (!auto.exists())
					auto.mkdirs();

				
				System.out.println("name gazeteer size:"+fullGazName.size());
				System.out.println("team gazeteer size:"+fullGazTeam.size());

				GenerateGazeteer.printGazeteerOnFile(fullGazName,
						auto.getAbsolutePath() + File.separator + "name.txt");
				GenerateGazeteer.printGazeteerOnFile(fullGazTeam,
						auto.getAbsolutePath() + File.separator + "team.txt");


	}
}