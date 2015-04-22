package uk.ac.shef.oak.gazetteers;

// Querying DBpedia by using Jena-ARQ 
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author annalisa
 */

public class GenerateMovieGazetteers extends GenerateGazeteer {

	
		public static void main(String[] args) {
			GenerateMovieGazetteers.generateGazeteer("./cacheCloudGazeteer"+ File.separator+"movie");

		}
		/**
		 * @param args
		 */
		public static void generateGazeteer(String resFolder) {

		String service = getSERVICE();
		GenerateMovieGazetteers queryDBPedia = new GenerateMovieGazetteers();
		logStart(queryDBPedia.getClass().getName());

		checkService(service);

		// director: http://dbpedia.org/property/director or
		// http://dbpedia.org/ontology/director or
		// http://dbpedia.org/property/directors
		String sparqlQueryStringDirector1 = getPREFIX()
				+ '\n'
				+ "select distinct ?name where { "
				+ "?movie <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Movie>."
				+ "?movie <http://dbpedia.org/property/director> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		String sparqlQueryStringDirector2 = getPREFIX()
				+ '\n'
				+ "select distinct ?name where { "
				+ "?movie <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Movie>."
				+ "?movie <http://dbpedia.org/ontology/director> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		String sparqlQueryStringDirector3 = getPREFIX()
				+ '\n'
				+ "select distinct ?name where { "
				+ "?movie <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Movie>."
				+ "?movie <http://dbpedia.org/property/directors> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		// genre: http://dbpedia.org/property/genre
		String sparqlQueryStringGenre = getPREFIX()
				+ '\n'
				+ "select distinct ?name where { "
				+ "?movie <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Movie>."
				+ "?movie <http://dbpedia.org/property/genre> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		// mpaa_rating: http://dbpedia.org/property/mpaa
		String sparqlQueryStringMPAA = getPREFIX()
				+ '\n'
				+ "select distinct ?mpaa where { "
				+ "?movie <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Movie> ."
				+ "?movie <http://dbpedia.org/property/mpaa> ?mpaa . }";

		// query title: http://dbpedia.org/property/title
		String sparqlQueryStringTitle1 = getPREFIX()
				+ '\n'
				+ "select distinct ?name where { "
				+ "?movie <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Movie>."
				+ "?movie <http://dbpedia.org/property/title> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?name. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		
		String sparqlQueryStringTitle2 = getPREFIX()
				+ '\n'
				+ "select distinct ?name where { "
				+ "?movie <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Movie>."
				+ "?movie <http://dbpedia.org/property/name> ?name."
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";
		
		
		// title
		Set<String> fullGazTitle = new HashSet<String>();
		Set<String> gazTitle1 = queryDBPedia.queryService(getSERVICE(),
				sparqlQueryStringTitle1, "name");
		Set<String> gazTitle2 = queryDBPedia.queryService(getSERVICE(),
				sparqlQueryStringTitle2, "name");
		
		fullGazTitle.addAll(cleanStringGazeteer(gazTitle1, 3, false));
		fullGazTitle.addAll(cleanStringGazeteer(gazTitle2, 3, false));

		// mpaa
		Set<String> fullGazMpaa = new HashSet<String>();
		Set<String> gazMpaa = queryDBPedia.queryService(getSERVICE(),
				sparqlQueryStringMPAA, "mpaa");
		fullGazMpaa.addAll(gazMpaa);

		// genre
		Set<String> fullGazGenre = new HashSet<String>();
		Set<String> gazGenre = queryDBPedia.queryService(getSERVICE(),
				sparqlQueryStringGenre, "name");
		fullGazGenre.addAll(cleanStringGazeteer(gazGenre, 2, false));

		// director
		Set<String> fullGazDirector = new HashSet<String>();
		Set<String> gazDirector = queryDBPedia.queryService(getSERVICE(),
				sparqlQueryStringDirector1, "name");
//		Set<String> gazDirector2 = queryDBPedia.queryService(getSERVICE(),
//				sparqlQueryStringDirector2, "name");
		Set<String> gazDirector3 = queryDBPedia.queryService(getSERVICE(),
				sparqlQueryStringDirector3, "name");

		fullGazDirector.addAll(cleanStringGazeteer(gazDirector, 2, false));

//		fullGazDirector.addAll(gazDirector2);
		fullGazDirector.addAll(cleanStringGazeteer(gazDirector3, 2, false));

		
		File auto = new File(resFolder);
		if (!auto.exists())
			auto.mkdirs();

		
		System.out.println("mpaa gazeteer size:"+fullGazMpaa.size());
		System.out.println("title gazeteer size:"+fullGazTitle.size());
		System.out.println("genre gazeteer size:"+fullGazGenre.size());
		System.out.println("director gazeteer size:"+fullGazDirector.size());
		
		GenerateGazeteer.printGazeteerOnFile(fullGazMpaa,
				auto.getAbsolutePath() + File.separator + "mpaa_rating.txt");
		GenerateGazeteer.printGazeteerOnFile(fullGazTitle,
				auto.getAbsolutePath() + File.separator + "title.txt");
		GenerateGazeteer.printGazeteerOnFile(fullGazGenre,
				auto.getAbsolutePath() + File.separator + "genre.txt");
		GenerateGazeteer.printGazeteerOnFile(fullGazDirector,
				auto.getAbsolutePath() + File.separator + "director.txt");
	}
}