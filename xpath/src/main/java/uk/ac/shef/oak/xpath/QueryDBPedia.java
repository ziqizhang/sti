package uk.ac.shef.oak.xpath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.google.gson.Gson;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

// TODO: Auto-generated Javadoc
/**
 * The Class QueryDBPedia.
 */
public class QueryDBPedia {

	/** The query dictionary. */
	private HashMap queryDictionary;

	/**
	 * Instantiates a new query db pedia.
	 */
	public QueryDBPedia() {
		// Load hashmap from JSON file
		String jsonString = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					"queriesHashmap.txt"));
			String readLine;
			while ((readLine = br.readLine()) != null) {
				jsonString = jsonString + readLine;
			}
			br.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		Gson gson = new Gson();
		queryDictionary = gson.fromJson(jsonString, HashMap.class);
	}

	/**
	 * Enrich individual.
	 * 
	 * @param indy
	 *            the indy
	 */
	public void enrichIndividual(Individual indy) {
		// Get class, e.g. SingerOrGroup
		OntClass ontClass = indy.getOntClass();
		// As an example, the hashmap loaded from the JSON file returns the
		// string for the key "SingerOrGroup" as:
		String queryString = (String) queryDictionary.get("SingerOrGroup");
		// This is an example URI:
		String exampleURI = "<http://dbpedia.org/resource/Black_Sabbath>";
		// Put the URI into the query
		queryString = queryString.replaceAll("######", exampleURI);
		String endPoint = "http://dbpedia.org/sparql";

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endPoint,
				query);

		// block of 7 lines from http://jena.sourceforge.net/tutorial/RDF_API/
		try {
			ResultSet results = qexec.execSelect();
			ResultSetFormatter.out(System.out, results, query); // To see it
																// working, for
																// testing and
																// devlopment
		} finally {
			qexec.close();
		}
	}
	
	public static void main(String[] args){
		
	}
}
