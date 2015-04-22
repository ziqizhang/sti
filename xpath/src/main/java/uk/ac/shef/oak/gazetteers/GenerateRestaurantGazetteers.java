package uk.ac.shef.oak.gazetteers;

// Querying DBpedia by using Jena-ARQ

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author annalisa
 */

public class GenerateRestaurantGazetteers extends GenerateGazeteer {

	
	public static void main(String[] args) {
		GenerateRestaurantGazetteers.generateGazeteer("./cacheCloudGazeteer"+ File.separator+"restaurant");

	}
	/**
	 * @param args
	 */
	public static void generateGazeteer(String resFolder) {

		File restaurant = new File(resFolder);
		if (!restaurant.exists())
			restaurant.mkdirs();

		GenerateRestaurantGazetteers queryDBPedia = new GenerateRestaurantGazetteers();
		logStart(queryDBPedia.getClass().getName());

		queryDBPedia.checkService(getSERVICE());

		Set<String> restaurantClasses = new HashSet<String>();

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
		restaurantClasses.clear();
		restaurantClasses.add("http://dbpedia.org/ontology/Restaurant");
		restaurantClasses.add("http://schema.org/Restaurant");

		// step 2, for each "university" class found above, query to get the
		// property "name"
		Set<String> restaurantAttributeGaz = new HashSet<String>();
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
		// TODO write proper query name dbpprop:name
		sparqlQueryString = getPREFIX()
				+ '\n'
				+ "select distinct ?name where {\n"
				+ "{?restaurant a <http://schema.org/Restaurant>. } \n"
//				+ "UNION { ?restaurant a <http://dbpedia.org/ontology/Restaurant>. } \n"
				+ "?restaurant dbpprop:name ?name.\n"
//				+ "?restaurant <http://schema.org/Restaurant/name> ?name.\n"

				+ "}\n";

//				+ "FILTER ( langMatches(lang(?name), 'EN')) .}\n";
		restaurantAttributeGaz.addAll(queryDBPedia.queryService(getSERVICE(),
				sparqlQueryString, "name"));

		sparqlQueryString = getPREFIX()
				+ '\n'
				+ "select distinct ?name where {\n"
				+ "{?restaurant a <http://schema.org/Restaurant>. } \n"
//				+ "UNION { ?restaurant a <http://dbpedia.org/ontology/Restaurant>. } \n"
				+ "?restaurant <http://xmlns.com/foaf/0.1/name> ?name.\n"
				+ "}\n";

//				+ "FILTER ( langMatches(lang(?name), 'EN')) .}\n";
		restaurantAttributeGaz.addAll(queryDBPedia.queryService(getSERVICE(),
				sparqlQueryString, "name"));
		
		
		// Output to file
		// List<String> gazetteer = new
		// ArrayList<String>(restaurantAttributeGaz);
		// Collections.sort(gazetteer);
		//
		// try {
		// PrintWriter p = new PrintWriter(args[0] + File.separator +
		// "restaurant_name.txt");
		// for (String gaz : gazetteer) {
		// p.println(gaz);
		// }
		// p.close();
		// } catch (IOException ioe) {
		// ioe.printStackTrace();
		// }
		System.out.println("name gazeteer size:"+restaurantAttributeGaz.size());

		GenerateGazeteer.printGazeteerOnFile(restaurantAttributeGaz,
				restaurant.getAbsolutePath() + File.separator + "name.txt");

		// TODO write proper query adress
		// http://dbpedia.org/property/streetAddress
		restaurantAttributeGaz.clear();
		sparqlQueryString = getPREFIX()
				+ '\n'
				+ "select distinct ?addr where {\n"
				+ "{?restaurant a <http://schema.org/Restaurant>. } \n"
//				+ "UNION { ?restaurant a <http://dbpedia.org/ontology/Restaurant>. } \n"
				+ "?restaurant dbpprop:streetAddress ?addr.\n"
				+ "FILTER ( langMatches(lang(?addr), 'EN')) .}\n";
		restaurantAttributeGaz.addAll(queryDBPedia.queryService(getSERVICE(),
				sparqlQueryString, "addr"));
		// gazetteer = new ArrayList<String>(restaurantAttributeGaz);
		// Collections.sort(gazetteer);
		//
		// try {
		// PrintWriter p = new PrintWriter(args[0] + File.separator +
		// "restaurant_addr.txt");
		// for (String gaz : gazetteer) {
		// p.println(gaz);
		// }
		// p.close();
		// } catch (IOException ioe) {
		// ioe.printStackTrace();
		// }
		System.out.println("address gazeteer size:"+restaurantAttributeGaz.size());

		GenerateGazeteer.printGazeteerOnFile(restaurantAttributeGaz,
				restaurant.getAbsolutePath() + File.separator + "address.txt");

		// TODO write proper query cuisine http://dbpedia.org/property/foodType
		restaurantAttributeGaz.clear();
		sparqlQueryString = getPREFIX()
				+ '\n'
				+ "select distinct ?foodtype where {\n"
				+ "{?restaurant a <http://schema.org/Restaurant>. } \n"
//				+ "UNION { ?restaurant a <http://dbpedia.org/ontology/Restaurant>. } \n"
				+ "?restaurant dbpprop:foodType ?foodtype.\n"
				+ "FILTER ( langMatches(lang(?foodtype), 'EN')) .}\n";
		restaurantAttributeGaz.addAll(queryDBPedia.queryService(getSERVICE(),
				sparqlQueryString, "foodtype"));
		// gazetteer = new ArrayList<String>(restaurantAttributeGaz);
		// Collections.sort(gazetteer);
		//
		// try {
		// PrintWriter p = new PrintWriter(args[0] + File.separator +
		// "restaurant_cuisine.txt");
		// for (String gaz : gazetteer) {
		// p.println(gaz);
		// }
		// p.close();
		// } catch (IOException ioe) {
		// ioe.printStackTrace();
		// }

		System.out.println("cusine gazeteer size:"+restaurantAttributeGaz.size());

		GenerateGazeteer.printGazeteerOnFile(restaurantAttributeGaz,
				restaurant.getAbsolutePath() + File.separator + "cuisine.txt");

		// TODO write proper query phone

	}
}