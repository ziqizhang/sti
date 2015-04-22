package uk.ac.shef.oak.gazetteers;

import java.io.File;

public class GenerateAllGazeteers {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		String folder = "./dbpediaGazeteer";
//		String folder = "./sindiceGazeteer";
		String folder = "./cacheCLOUDgazeteer";

//		GenerateAutomobileGazetteers.generateGazeteer(folder+ File.separator+"auto");
		GenerateBookGazetteers.generateGazeteer(folder+ File.separator+"book");
//		GenerateCameraGazetteers.generateGazeteer(folder+ File.separator+"camera");
//		GenerateJobGazetteers.generateGazeteer(folder+ File.separator+"job");
		GenerateMovieGazetteers.generateGazeteer(folder+ File.separator+"movie");
		GenerateNbaplayerGazetteers.generateGazeteer(folder+ File.separator+"nbaplayer");
		GenerateRestaurantGazetteers.generateGazeteer(folder+ File.separator+"restaurant");
		GenerateUniversityGazetteers.generateGazeteer(folder+ File.separator+"university");
	}

}
