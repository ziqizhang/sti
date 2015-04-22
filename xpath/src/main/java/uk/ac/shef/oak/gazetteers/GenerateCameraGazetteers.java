package uk.ac.shef.oak.gazetteers;

// Querying DBpedia by using Jena-ARQ 
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author annalisa
 */

public class GenerateCameraGazetteers extends GenerateGazeteer {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GenerateCameraGazetteers.generateGazeteer("./cacheCloudGazeteer"+ File.separator+"camera");

	}
	/**
	 * @param args
	 */
	public static void generateGazeteer(String resFolder) {

		GenerateCameraGazetteers queryDBPedia = new GenerateCameraGazetteers();
		logStart(queryDBPedia.getClass().getName());
		checkService(getSERVICE());

		// TODO write proper query manufacturer
		String manufacturer = GenerateGazeteer.getPREFIX() + '\n'
				+ "select distinct ?manufacturer where { "
				// write query here

				+ "} ";

		// TODO write proper query model
		String model = "";

		// TODO write proper query price
		String price = "";

		Set<String> fullGazManufacturer = new HashSet<String>();
		Set<String> gazManufacturer = queryDBPedia.queryService(getSERVICE(),
				manufacturer, "manufacturer");
		fullGazManufacturer.addAll(gazManufacturer);

		Set<String> fullGazModel = new HashSet<String>();
		Set<String> gazModel = queryDBPedia.queryService(getSERVICE(), model,
				"model");
		fullGazModel.addAll(gazModel);

		Set<String> fullGazPrice = new HashSet<String>();
		Set<String> gazPrice = queryDBPedia.queryService(getSERVICE(), price,
				"price");
		fullGazPrice.addAll(gazPrice);

		// for (String a : gazEngine){
		// System.out.print(a + "\n");
		// }

		File auto = new File(resFolder);
		if (!auto.exists())
			auto.mkdirs();

		GenerateGazeteer.printGazeteerOnFile(fullGazManufacturer,
				auto.getAbsolutePath() + File.separator + "fuel_economy.txt");
		GenerateGazeteer.printGazeteerOnFile(fullGazModel,
				auto.getAbsolutePath() + File.separator + "model.txt");
		GenerateGazeteer.printGazeteerOnFile(fullGazPrice,
				auto.getAbsolutePath() + File.separator + "price.txt");

	}
}