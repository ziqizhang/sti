package uk.ac.shef.oak.gazetteers;

// Querying DBpedia by using Jena-ARQ 
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author annalisa
 */

public class GenerateBookGazetteers extends GenerateGazeteer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GenerateBookGazetteers.generateGazeteer("./cacheCloudGazeteer"+ File.separator+"book");

	}
	/**
	 * @param args
	 */
	public static void generateGazeteer(String resFolder) {
		GenerateBookGazetteers queryDBPedia = new GenerateBookGazetteers();
		queryDBPedia.checkService(getSERVICE());
		logStart(queryDBPedia.getClass().getName());

		File book = new File(resFolder);
		if (!book.exists())
			book.mkdirs();

		String prefix = GenerateGazeteer.getPREFIX();

		String sparqlQueryString1 = prefix
				+ '\n'
				+ "select distinct ?name where { "
				+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
				// + "?book <http://dbpedia.org/property/author> ?o. "
				+ "?book <http://dbpedia.org/ontology/author> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		String sparqlQueryString2 = prefix
				+ '\n'
				+ "select distinct ?name where { "
				+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
				+ "?book <http://dbpedia.org/property/author> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		// + "LIMIT 100";
		Set<String> gaz1 = queryDBPedia.queryService(
				GenerateGazeteer.getSERVICE(), sparqlQueryString1, "name");
		Set<String> gaz2 = queryDBPedia.queryService(
				GenerateGazeteer.getSERVICE(), sparqlQueryString2, "name");

		Set<String> fullGaz = new HashSet<String>();
		fullGaz.addAll(cleanStringGazeteer(gaz1, 2, false));
		fullGaz.addAll(cleanStringGazeteer(gaz2, 2, false));
		System.out.println("author gazeteer size: " + fullGaz.size());

		GenerateGazeteer.printGazeteerOnFile(fullGaz, book.getAbsolutePath()
				+ File.separator + "author.txt");

		gaz1.clear();
		gaz2.clear();
		fullGaz.clear();

		// sparqlQueryString1 = prefix
		// + '\n'
		// + "select distinct ?isbn where { "
		// +
		// "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
		// + "?book <http://dbpedia.org/property/isbn> ?isbn. " + "} ";

		sparqlQueryString2 = prefix
				+ '\n'
				+ "select distinct ?isbn where { "
				+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
				+ "?book <http://dbpedia.org/ontology/isbn> ?isbn. " + "} ";

		// + "LIMIT 100";
		// gaz1 = queryDBPedia.queryService(getSERVICE(), sparqlQueryString1,
		// "isbn");
		gaz2 = queryDBPedia.queryService(getSERVICE(), sparqlQueryString2,
				"isbn");

		// fullGaz.addAll(gaz1);
		fullGaz.addAll(cleanIsbnGazeteer(gaz2));
		System.out.println("isbn gazeteer size: " + fullGaz.size());

		GenerateGazeteer.printGazeteerOnFile(fullGaz, book.getAbsolutePath()
				+ File.separator + "isbn.txt");

		gaz1.clear();
		gaz2.clear();
		fullGaz.clear();

		// TODO write proper query
		/**
		 * get all titles of books
		 */
		sparqlQueryString1 = prefix
				+ '\n'
				+ "select distinct ?name where { "
				+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
				+ "?book <http://dbpedia.org/property/title> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		sparqlQueryString2 = prefix
				+ '\n'
				+ "select distinct ?name where { "
				+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
				+ "?book <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		gaz1 = queryDBPedia.queryService(getSERVICE(), sparqlQueryString1,
				"name");
		gaz2 = queryDBPedia.queryService(getSERVICE(), sparqlQueryString2,
				"name");
		fullGaz = new HashSet<String>();
		fullGaz.addAll(cleanStringGazeteer(gaz1, 2, false));
		fullGaz.addAll(cleanStringGazeteer(gaz2, 2, false));

		System.out.println("title gazeteer size: " + fullGaz.size());

		GenerateGazeteer.printGazeteerOnFile(fullGaz, book.getAbsolutePath()
				+ File.separator + "title.txt");

		gaz1.clear();
		gaz2.clear();
		fullGaz.clear();

		/**
		 * get all publisher of books
		 */
		sparqlQueryString1 = prefix
				+ '\n'
				+ "select distinct ?name where { "
				+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
				+ "?book <http://dbpedia.org/property/publisher> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		sparqlQueryString2 = prefix
				+ '\n'
				+ "select distinct ?name where { "
				+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
				+ "?book <http://dbpedia.org/ontology/publisher> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		// + "LIMIT 100";
		gaz1 = queryDBPedia.queryService(getSERVICE(), sparqlQueryString1,
				"name");
		gaz2 = queryDBPedia.queryService(getSERVICE(), sparqlQueryString2,
				"name");
		fullGaz = new HashSet<String>();
		fullGaz.addAll(cleanStringGazeteer(gaz1, 2, false));
		fullGaz.addAll(cleanStringGazeteer(gaz2, 2, false));

		System.out.println("publisher gazeteer size: " + fullGaz.size());
		GenerateGazeteer.printGazeteerOnFile(fullGaz, book.getAbsolutePath()
				+ File.separator + "publisher.txt");

		
		
		
		/**
		 * get all release date
		 */
		
		gaz1.clear();
		gaz2.clear();
		fullGaz.clear();

		// sparqlQueryString1 = prefix
		// + '\n'
		// + "select distinct ?isbn where { "
		// +
		// "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
		// + "?book <http://dbpedia.org/property/isbn> ?isbn. " + "} ";

		sparqlQueryString2 = prefix
				+ '\n'
				+ "select distinct ?date where { "
				+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
				+ "?book <http://dbpedia.org/property/releaseDate> ?date. " + "} ";

		// + "LIMIT 100";
		// gaz1 = queryDBPedia.queryService(getSERVICE(), sparqlQueryString1,
		// "isbn");
		gaz2 = queryDBPedia.queryService(getSERVICE(), sparqlQueryString2,
				"date");

		fullGaz = new HashSet<String>();
		// fullGaz.addAll(gaz1);
		fullGaz.addAll(gaz2);
		System.out.println("publication_date gazeteer size: " + fullGaz.size());

		GenerateGazeteer.printGazeteerOnFile(fullGaz, book.getAbsolutePath()
				+ File.separator + "publication_date.txt");

		
		
		
	}
}