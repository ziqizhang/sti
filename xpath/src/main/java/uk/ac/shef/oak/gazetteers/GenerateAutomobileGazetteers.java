package uk.ac.shef.oak.gazetteers;

// Querying DBpedia by using Jena-ARQ 
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author annalisa
 */



public class GenerateAutomobileGazetteers extends GenerateGazeteer {

	
	public static void main(String [] args) {

		generateGazeteer("./LODgazeteers/auto");
		
	}
	
	/**
	 * @param args
	 */
	public static void generateGazeteer(String resFolder) {
		

		GenerateAutomobileGazetteers queryDBPedia = new GenerateAutomobileGazetteers();
		logStart(queryDBPedia.getClass().getName());
		checkService(GenerateGazeteer.getSERVICE());

		// fuel_economy: http://dbpedia.org/property/fuelEconomy
		String fuel_economy = GenerateGazeteer.getPREFIX()
				+ '\n'
				+ "select distinct ?fuel_economy where { "
				+ " ?auto rdf:type <http://dbpedia.org/ontology/Automobile>."
				+ "?auto <http://dbpedia.org/property/fuelEconomy> ?fuel_economy . "
				+ "} ";

		// engine: http://dbpedia.org/property/engine or
		// http://dbpedia.org/property/engines
		String engine = GenerateGazeteer.getPREFIX()
				+ '\n'
				+ "select distinct ?engine where { "
				+ "?auto <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Automobile>."
				+ "?auto <http://dbpedia.org/property/engine> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?engine. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		String engine2 = GenerateGazeteer.getPREFIX()
				+ '\n'
				+ "select distinct ?engine where { "
				+ "?auto <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Automobile>."
				+ "?auto <http://dbpedia.org/property/engines> ?o. "
				+ "?o <http://dbpedia.org/property/name> ?engine. "
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		// model: http://dbpedia.org/property/name
		String model = GenerateGazeteer.getPREFIX()
				+ '\n'
				+ "select distinct ?model where { "
				+ "?auto <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Automobile> ."
				+ "?auto <http://dbpedia.org/property/name> ?model."
				+ "FILTER ( langMatches(lang(?name), 'EN')) ." + "} ";

		// TODO write proper query price
		// String price = "";

		Set<String> fullazFuel_economy = new HashSet<String>();
		Set<String> gazFuel_economy = queryDBPedia.queryService(getSERVICE(),
				fuel_economy, "fuel_economy");
		fullazFuel_economy.addAll(gazFuel_economy);

		// if you have multiple queries for the same property generate one set
		// for each different query e.g. engine2
		Set<String> fullGazEngine = new HashSet<String>();

		Set<String> gazEngine = queryDBPedia.queryService(getSERVICE(), engine,
				"engine");
		
		Set<String> gazEngine2 = queryDBPedia.queryService(getSERVICE(),
				engine2, "engine");
		fullGazEngine.addAll(gazEngine);
		fullGazEngine.addAll(gazEngine2);

		Set<String> fullGazModel = new HashSet<String>();
		Set<String> gazModel = queryDBPedia.queryService(getSERVICE(), model,
				"model");
		fullGazModel.addAll(gazModel);

		// Set<String> fullGazPrice= new HashSet<String>();
		// Set<String> gazPrice = queryDBPedia.queryService(getSERVICE(), price,
		// "price");
		// fullGazPrice.addAll(gazPrice);

		// for (String a : gazEngine){
		// System.out.print(a + "\n");
		// }

		File auto = new File(resFolder);
		if (!auto.exists())
			auto.mkdirs();

		System.out.println("Fuel_economy gazeteer size:"+fullazFuel_economy.size());
		System.out.println("engine gazeteer size:"+gazEngine.size());
		System.out.println("model gazeteer size:"+fullGazModel.size());

		
		GenerateGazeteer.printGazeteerOnFile(fullazFuel_economy,
				auto.getAbsolutePath() + File.separator + "fuel_economy.txt");

		GenerateGazeteer.printGazeteerOnFile(gazEngine, auto.getAbsolutePath()
				+ File.separator + "engine.txt");
		GenerateGazeteer.printGazeteerOnFile(fullGazModel,
				auto.getAbsolutePath() + File.separator + "model.txt");
		// GenerateGazeteer.printGazeteerOnFile(fullGazPrice,
		// auto.getAbsolutePath()+File.separator+"price.txt");

	}

}