package uk.ac.shef.oak.gazetteers;

	// Querying DBpedia by using Jena-ARQ 
	import com.hp.hpl.jena.query.Query;
	import com.hp.hpl.jena.query.QueryExecution;
	import com.hp.hpl.jena.query.QueryExecutionFactory;
	import com.hp.hpl.jena.query.QueryFactory;
	import com.hp.hpl.jena.query.QuerySolution;
	import com.hp.hpl.jena.query.ResultSet;
	import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
	/**
	 * @author annalisa
	 * This class is only for internal usage to test dbpedia queries
	 * Gazetteer genaration is done by implementing the interface GenerateGazeteer
	 */
	public class QueryRDF {
	public void queryService(String service, String sparqlQueryString,
	String solutionConcept) {
	Query query = QueryFactory.create(sparqlQueryString);
	QueryExecution qexec = QueryExecutionFactory.sparqlService(service,
	query);
	try {
	ResultSet results = qexec.execSelect();
	for (; results.hasNext();) {
	QuerySolution soln = results.nextSolution();
	String x = soln.get(solutionConcept).toString();
	System.out.print(x + "\n");
	}
	} finally {
	qexec.close();
	}
	}
	public void checkService(String service) {
	String query = "ASK { }";
	QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
	try {
	if (qe.execAsk()) {
	System.out.println(service + " is UP");
	}
	} catch (QueryExceptionHTTP e) {
	System.out.println(service + " is DOWN");
	} finally {
	qe.close();
	} // end try/catch/finally
	}
	
	   public static void now()
	    {

	        String sparqlQueryString = "select distinct ?Concept where {[] a ?Concept } LIMIT 10";

	        Query query = QueryFactory.create(sparqlQueryString);

	        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);


	        try {
	            ResultSet results = qexec.execSelect();
	            for ( ; results.hasNext() ; )
	        {
	            QuerySolution soln = results.nextSolution() ;
	            String x = soln.get("Concept").toString();
	            System.out.print(x +"\n");
	        }

	        }
	        finally { qexec.close() ; }

	        }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	// Configure the proxy settings
//	System.setProperty("socksProxyHost", "11.222.33.44");
//	System.setProperty("socksProxyPort", "80");
//	System.setProperty("http.proxyHost", "11.222.33.44");
//	System.setProperty("http.proxyPort", "80");
	String service = "http://dbpedia.org/sparql";
	QueryRDF queryDBPedia = new QueryRDF();
	queryDBPedia.checkService(service);
	String prefix = "PREFIX dbpprop: <http://dbpedia.org/property/> "
	+ '\n' + "PREFIX dbpedia: <http://dbpedia.org/resource/> "
	+ '\n' + "PREFIX dbpedia-owl: <http://dbpedia.org/> "
	+ '\n' + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
	
	/**
	 * unrelated example
	 */
//	String sparqlQueryString = prefix + '\n'
//	+ "select distinct ?mosque where { "
//	+ "?mosque dbpprop:architectureType  \"Mosque\"@en. "
//	+ "?mosque dbpprop:location dbpedia:Istanbul. " + "} "
//	+ "LIMIT 100";
//	queryDBPedia.queryService(service, sparqlQueryString, "mosque");
	
	/**
	 * get all properties for books
	 */
	String sparqlQueryString = prefix + '\n'
	+ "select DISTINCT ?p  where { "
//	+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>." //+ "} "
	+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Book>." //+ "} "

+ "?book ?p ?o. " + "} "
//	+ "?o <http://dbpedia.org/property/name> ?name. " 
//	+ "FILTER ( langMatches(lang(?name), 'EN')) ."+ "} "
//
	+ "LIMIT 100";
	queryDBPedia.queryService(service, sparqlQueryString, "p");
	
	
	
	/**
	 * get all properties for books
	 */
	String sparqlQueryStringRest = prefix + '\n'
	+ "select DISTINCT ?p  where { "
//	+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>." //+ "} "
	+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Restaurant>." //+ "} "

+ "?book ?p ?o. " + "} "
//	+ "?o <http://dbpedia.org/property/name> ?name. " 
//	+ "FILTER ( langMatches(lang(?name), 'EN')) ."+ "} "
//
	+ "LIMIT 100";
	queryDBPedia.queryService(service, sparqlQueryStringRest, "p");
	
		
	
//	String sparqlQueryString = prefix + '\n'
//	+ "select distinct ?concept where { "
//	+ "?s a ?concept ." + "} "
//	+ "LIMIT 100";
//	queryDBPedia.queryService(service, sparqlQueryString, "concept");
	
	
	/**
	 * get all books
	 */
//	String sparqlQueryString = prefix + '\n'
//	+ "select ?book  where { "
//	+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>." + "} "
//	+ "LIMIT 100";
//	queryDBPedia.queryService(service, sparqlQueryString, "book");

	
	/**
	 * get all authors of books
	 */
//	String sparqlQueryString = prefix + '\n'
//	+ "select distinct ?name where { "
//	+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
//	+ "?book <http://dbpedia.org/property/author> ?o. " 
//	+ "?o <http://dbpedia.org/property/name> ?name. " 
//	+ "FILTER ( langMatches(lang(?name), 'EN')) ."+ "} "
//
//	+ "LIMIT 100";
//	queryDBPedia.queryService(service, sparqlQueryString, "name");	

	/**
	 * get all release dates of books
	 */
//	String sparqlQueryString = prefix + '\n'
//	+ "select distinct ?date where { "
//	+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
//	+ "?book <http://dbpedia.org/property/releaseDate> ?date. "+ "} "
//
//	+ "LIMIT 100";
//	queryDBPedia.queryService(service, sparqlQueryString, "date");	
	
	
	
//	
	/**
	 * get all publisher of books
	 */
//	String sparqlQueryString = prefix + '\n'
//	+ "select distinct ?name where { "
//	+ "?book <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Book>."
//	+ "?book <http://dbpedia.org/property/publisher> ?o. " 
//	+ "?o <http://dbpedia.org/property/name> ?name. " 
//	+ "FILTER ( langMatches(lang(?name), 'EN')) ."+ "} "
//
//	+ "LIMIT 100";
//	queryDBPedia.queryService(service, sparqlQueryString, "name");	
	
	
//	    http://www.w3.org/1999/02/22-rdf-syntax-ns#type
//		http://www.w3.org/2002/07/owl#sameAs
//		http://www.w3.org/2000/01/rdf-schema#label
//		http://www.w3.org/2000/01/rdf-schema#comment
//		http://dbpedia.org/ontology/wikiPageExternalLink
//		http://xmlns.com/foaf/0.1/depiction
//		http://xmlns.com/foaf/0.1/name
//		http://purl.org/dc/terms/subject
//		http://dbpedia.org/property/name
//		http://dbpedia.org/property/country
//		http://dbpedia.org/ontology/abstract
//		http://dbpedia.org/property/author
//		http://dbpedia.org/property/language
//		http://dbpedia.org/property/wikiPageUsesTemplate
//		http://dbpedia.org/property/releaseDate
//		http://dbpedia.org/ontology/thumbnail
//		http://dbpedia.org/property/imageCaption
//		http://dbpedia.org/property/genre
//		http://dbpedia.org/property/publisher
//		http://dbpedia.org/property/mediaType
//		http://dbpedia.org/property/no
//		http://dbpedia.org/ontology/publisher
//		http://dbpedia.org/ontology/literaryGenre
//		http://dbpedia.org/ontology/mediaType
//		http://dbpedia.org/ontology/author
//		http://www.w3.org/ns/prov#wasDerivedFrom
//		http://dbpedia.org/property/hasPhotoCollection
//		http://xmlns.com/foaf/0.1/isPrimaryTopicOf
//		http://dbpedia.org/property/isbn
//		http://dbpedia.org/property/pages
//		http://dbpedia.org/property/precededBy
//		http://dbpedia.org/property/followedBy
//		http://dbpedia.org/ontology/isbn
//		http://dbpedia.org/ontology/subsequentWork
//		http://dbpedia.org/ontology/numberOfPages
//		http://dbpedia.org/ontology/previousWork
//		http://dbpedia.org/ontology/country
//		http://dbpedia.org/ontology/language
//		http://dbpedia.org/property/subject
//		http://dbpedia.org/property/pubDate
//		http://dbpedia.org/property/titleOrig
//		http://dbpedia.org/ontology/nonFictionSubject
//		http://www.w3.org/2000/01/rdf-schema#seeAlso
//		http://xmlns.com/foaf/0.1/homepage
//		http://dbpedia.org/property/illustrator
//		http://dbpedia.org/property/coverArtist
//		http://dbpedia.org/ontology/coverArtist
//		http://dbpedia.org/ontology/illustrator
//		http://dbpedia.org/property/englishPubDate
//		http://dbpedia.org/property/series
//		http://dbpedia.org/ontology/series
//		http://dbpedia.org/property/caption
//		http://dbpedia.org/property/congress
//		http://dbpedia.org/property/type
//		http://dbpedia.org/property/oclc
//		http://dbpedia.org/property/dewey
//		http://dbpedia.org/property/width
//		http://dbpedia.org/property/align
//		http://dbpedia.org/property/direction
//		http://dbpedia.org/property/image
//		http://dbpedia.org/property/id
//		http://dbpedia.org/ontology/oclc
//		http://dbpedia.org/ontology/publicationDate
//		http://dbpedia.org/ontology/dcc
//		http://dbpedia.org/ontology/lcc
//		http://dbpedia.org/property/wordnet_type
//		http://dbpedia.org/property/unreferenced
//		http://dbpedia.org/property/plot
//		http://dbpedia.org/property/translator
//		http://dbpedia.org/ontology/translator
//		http://dbpedia.org/property/colwidth
//		http://dbpedia.org/property/group
//		http://dbpedia.org/property/italicTitle
//		http://dbpedia.org/property/englishReleaseDate
//		http://dbpedia.org/property/source
//		http://dbpedia.org/property/salign
//		http://dbpedia.org/property/quote
//		http://dbpedia.org/property/date
//		http://dbpedia.org/property/data
//		http://dbpedia.org/property/title
//		http://dbpedia.org/property/pointOfView
//		http://dbpedia.org/property/bot
//		http://dbpedia.org/property/originalResearch
//		http://dbpedia.org/property/refimprove
//		http://dbpedia.org/property/c
//		http://dbpedia.org/property/p
//		http://dbpedia.org/property/s
//		http://dbpedia.org/property/t
//		http://dbpedia.org/property/w
//		http://dbpedia.org/property/l
//		http://dbpedia.org/property/j
//		http://dbpedia.org/property/poj
//		http://dbpedia.org/property/wuu
//		http://dbpedia.org/property/showflag
//		http://dbpedia.org/property/before
//		http://dbpedia.org/property/years
//		http://dbpedia.org/property/after
//		http://dbpedia.org/property/translators
//		http://dbpedia.org/property/coverArtists
//		http://dbpedia.org/property/moreFootnotes

}
}