 package uk.ac.shef.oak.gazetteers;

// Querying DBpedia by using Jena-ARQ

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author annalisa
 */

public class GenerateUniversityGazetteers extends GenerateGazeteer {

	
	public static void main(String[] args) {
		GenerateUniversityGazetteers.generateGazeteer("./cacheCloudGazeteer"+ File.separator+"university");

	}
	/**
	 * @param args
	 */
	public static void generateGazeteer(String resFolder) {
		GenerateUniversityGazetteers queryDBPedia = new GenerateUniversityGazetteers();
		logStart(queryDBPedia.getClass().getName());

		queryDBPedia.checkService(getSERVICE());
		File university = new File(resFolder);
		if (!university.exists())
			university.mkdirs();
		
		Set<String> universities = new HashSet<String>();

		// Step 1, query to get the class "university"
		String sparqlQueryString = getPREFIX() + "\n"
				+ "select distinct ?Concept where {\n"
				+ "        ?Concept a owl:Class.\n"
				+ "        ?Concept rdfs:label ?conceptlabel.\n"
				+ "        filter regex(?conceptlabel,\"university\",\"i\")\n"
				+ "        }";
		/*
		 * universities = queryDBPedia.queryService(service, sparqlQueryString,
		 * "Concept"); sparqlQueryString =
		 * prefix+"\n"+"select distinct ?Concept where {\n" +
		 * "       ?Concept a owl:Class.\n" +
		 * "       filter regex(?Concept,\"university\",\"i\")\n" + "       }";
		 * universities.addAll(queryDBPedia.queryService(service,
		 * sparqlQueryString, "Concept"));
		 */

		// todo: filter classes, many can be noisy. (the following code is
		// arbitrary, change!)
		universities.clear();
		universities.add("http://dbpedia.org/ontology/University");
		universities.add("http://dbpedia.org/ontology/College");

		// step 2, for each "university" class found above, query to get the
		// property "name"
		Set<String> universityGaz = new HashSet<String>();
		/*
		 * for (String uni : universities) { sparqlQueryString = prefix + "\n" +
		 * "select DISTINCT ?p  where {\n" + "        ?uni a <" + uni + ">.\n" +
		 * "        ?uni ?p ?o.\n" + "        ?o dbpprop:name ?name.\n" +
		 * "        FILTER ( langMatches(lang(?name), 'EN')) .}"; Set<String>
		 * properties = queryDBPedia.queryService(service, sparqlQueryString,
		 * "name"); Iterator<String> it = properties.iterator(); while
		 * (it.hasNext()) { //now select the right property that means "name" if
		 * (!it.next().endsWith("name")) //todo: use better strategies
		 * it.remove();
		 * 
		 * //todo: create sparql query here for each class, each possible
		 * property, then fetch gazetteers } universityGaz.addAll(properties); }
		 */

		// FOR NOW LETS STICK TO THE SIMPLE SOLUTION
		sparqlQueryString =  getPREFIX() + "\n"
				+ "select distinct ?name where {\n"
				+ "?uni a <http://schema.org/CollegeOrUniversity>.\n"
				+ "?uni dbpprop:name ?name.\n"
				+ "FILTER ( langMatches(lang(?name), 'EN')) .}\n";
		universityGaz.addAll(queryDBPedia.queryService(getSERVICE(),
				sparqlQueryString, "name"));

		sparqlQueryString = getPREFIX() + "\n"
				+ "select distinct ?name where {\n"
				+ "?uni a <http://schema.org/CollegeOrUniversity>.\n"
//				+ "?uni a <http://dbpedia.org/ontology/College>.\n"
				+ "?uni dbpprop:name ?name.\n"
				+ "FILTER ( langMatches(lang(?name), 'EN')) .}\n";
		universityGaz.addAll(queryDBPedia.queryService(getSERVICE(),
				sparqlQueryString, "name"));



		System.out.println("name gazeteer size:"+universityGaz.size());

		GenerateGazeteer.printGazeteerOnFile(universityGaz,
				university.getAbsolutePath() + File.separator + "name.txt");
		
		
		// TODO write proper query phone: not possible
		sparqlQueryString = getPREFIX() + "\n"
				+ "select distinct ?name where {\n"
				+ "?uni a <http://schema.org/CollegeOrUniversity>.\n"
				+ "?uni <http://dbpedia.org/property/telephone> ?name.\n"
				+ "FILTER ( langMatches(lang(?name), 'EN')) .}\n";
		
		universityGaz.clear();
		universityGaz.addAll(cleanStringGazeteer(queryDBPedia.queryService(getSERVICE(),
				sparqlQueryString, "name"),2,false));
		
		System.out.println("phone gazeteer size:"+universityGaz.size());

		GenerateGazeteer.printGazeteerOnFile(universityGaz,
				university.getAbsolutePath() + File.separator + "phone.txt");
		
		// TODO write proper query type: no such property

		// TODO write proper query website: no such property
		sparqlQueryString = getPREFIX() + "\n"
				+ "select distinct ?name where {\n"
				+ "?uni a <http://schema.org/CollegeOrUniversity>.\n"
				+ "?uni <http://dbpedia.org/property/website> ?name.}\n";

		
		universityGaz.clear();
		universityGaz.addAll(cleanStringGazeteer(queryDBPedia.queryService(getSERVICE(),
				sparqlQueryString, "name"),2,false));
		
		System.out.println("website gazeteer size:"+universityGaz.size());

		GenerateGazeteer.printGazeteerOnFile(cleanUrlGazeteer(universityGaz),
				university.getAbsolutePath() + File.separator + "website.txt");
		
		
	}
}