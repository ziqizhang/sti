package uk.ac.shef.oak.xpathExperiment;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.shef.oak.gazetteers.TextOperations;
import uk.ac.shef.oak.xpath.collectiveExperiment.SetOperations;

public class CheckResultsAgainstGS {

	// private static String annotationFolder =
	// "/Users/annalisa/Desktop/latestResults/resWithXpathMaps26Jan/annotation_results/";
	// private static String annotationFolder =
	// "/Users/annalisa/Desktop/latestResults/rerunOfFailingOnesWithDifferentStrategies/annotation_results_strategyMultipleXpath";
	// private static String annotationFolder =
	// "/Users/annalisa/Desktop/latestResults/dbpediaExperiment/annotation_results/";
	// private static String annotationFolder =
	// "/Users/annalisa/Desktop/latestResults/cacheCloudExperiment/annotation_results/";
	// private static String annotationFolder =
	// "/Users/annalisa/Desktop/latestResults/checkBOOK/annotation_results/";
//	private static String annotationFolder = "/Users/annalisa/Desktop/latestResults/cacheCLOUDrepeatedForAborted/annotation_results/";
//	private static String annotationFolder = "/Users/annalisa/Desktop/latestResults/dbpediaExperiment/annotation_results/";
//	private static String annotationFolder = "/Users/annalisa/Desktop/latestResults/cacheCloudExperiment/annotation_results/";
//	private static String annotationFolder = "/Users/annalisa/Desktop/latestResults/cacheCLOUDrepeatedExperimentUsingCache/annotation_results/";
	
	
	
	private static String annotationFolder = "/Users/annalisa/Desktop/latestResults/toplineExperiment/annotation_results";
//	private static String annotationFolder = "/Users/annalisa/Desktop/latestResults/SINGLEPATHONLY/dbpediaExpResults/annotation_results/";
//	private static String annotationFolder = "/Users/annalisa/Desktop/latestResults/SINGLEPATHONLY/sindiceExpResults/annotation_results/";
//	private static String annotationFolder = "/Users/annalisa/Desktop/latestResults/SINGLEPATHONLY/cacheCLOUDExpResults/annotation_results/";


	private static Logger l4j = Logger.getLogger(CheckResultsAgainstGS.class);

	// static
	// {
	// Logger rootLogger = Logger.getRootLogger();
	// rootLogger.setLevel(Step.INFO);
	// rootLogger.addAppender(new ConsoleAppender(
	// new PatternLayout("%-6r [%p] %c - %m%n")));
	// }
	public double compare(String gsFile, String resultFile) {
		LoadGS gs = new LoadGS(gsFile);
		HashMap<String, Set<String>> gsData = gs.getValues();

		double accuracy = 0;
		double accuracyPartial = 0;

		LoadGS res = new LoadGS(resultFile);
		HashMap<String, Set<String>> resData = res.getValues();

		int gsCountPages = 0; // number of pages, 2000 or less
		int gsCountNumberOfRes = 0; // number of results, can be multiple on
									// single page
		int resCountfullMatches = 0; // number of full matches with the gold
										// standard
		int resCountMatchesSIGIRlike = 0; // number of matches with the gold
										// standard, following SIGIR method for multiple values

		int resCountPartialMatch = 0; // number of actual matching, counting
										// each result as indipendent from the
										// page
		int resFalseNegative = 0; // number of actual matching, counting each
									// result as indipendent from the page

		int resCountTotal = 0; // total number of results from the method


		
		for (String g : gsData.keySet()) {
			gsCountPages++;
			Set<String> gsResSet = new HashSet<String>(); // set of result for
															// each page
			for (String r : gsData.get(g)) {
				r = TextOperations.normalizeString(r);
				gsResSet.add(r);
			}
			gsCountNumberOfRes = gsCountNumberOfRes + gsResSet.size();
			// System.out.println(gsCountPages+" loaded GS for page "+g+"\t"+gsResSet+"\t size: "+gsResSet.size());

			if (resData.get(g) != null) {
				Set<String> resResSet = new HashSet<String>();
				for (String re : resData.get(g)) {
					resResSet.add(TextOperations.normalizeString(re));
				}
				resCountTotal = resCountTotal + resResSet.size();

//				 System.out.print(gsCountPages+" loaded RE for page "+g+"\t"+resResSet+"\t size: "+resResSet.size()+
//				 " " +resResSet.equals(gsResSet));

				if (resResSet.equals(gsResSet)) {
					resCountfullMatches++;
					resCountMatchesSIGIRlike++;
					resCountPartialMatch = resCountPartialMatch
							+ gsResSet.size();
				} else {
					
					if (resResSet.size()==1){
						String r = resResSet.iterator().next();
						if(gsResSet.contains(r))
						resCountMatchesSIGIRlike++;
					}
					Set<String> match = SetOperations.intersection(resResSet,
							gsResSet);
					resCountPartialMatch = resCountPartialMatch + match.size();
					resFalseNegative = resFalseNegative + resResSet.size()
							- match.size();
//					 System.out.print(" CORRECT anwsers: "+match.size()+" WRONG anwsers: "+(resResSet.size()-match.size()));
					if(gsFile.contains("book-")&resultFile.contains("-title")&resultFile.contains("annotation_results_strategySingleXpath")) 
					System.out.println(" GS: "+gsResSet+" RESULTS: "+resResSet);

				}
			}
			// System.out.println();

		}

		accuracyPartial = (double) resCountPartialMatch / gsCountNumberOfRes;
//		accuracy = (double) resCountfullMatches / gsCountPages;
		accuracy = (double) resCountMatchesSIGIRlike / gsCountPages;

		
		

		// TODO save on file
		// System.out.print(gsFile.substring(gsFile.lastIndexOf(File.separator)));
		// System.out.println("gs: "+
		// gsCountPages+" gs res: "+gsCountNumberOfRes+" res: "+resCountTotal+" full matches: "+resCountfullMatches+" partial matches: "+resCountPartialMatch);

		// System.out.println("accuracy "+
		// resCountfullMatches+"/"+gsCountPages+" = "+ accuracy);
		//
		// System.out.println("partial accuracy "+
		// resCountPartialMatch+"/"+gsCountNumberOfRes+" = "+ accuracyPartial);
		//
		// System.out.println("resCountTotal "+ resCountTotal);

		if (accuracy < 1) {
			// System.out.println(resultFile.substring(resultFile.lastIndexOf(File.separator))+"\t accuracy "+
			// resCountfullMatches+"/"+gsCountPages+" = "+ accuracy);
			l4j.warn(resultFile.substring(resultFile
					.lastIndexOf(File.separator) + 1) + "\t" + accuracy);

			// String qsub = resultFile.replace(annotationFolder+"", "qsub ");
			// qsub = qsub.substring(0, qsub.lastIndexOf(".txt"));
			// String s [] = qsub.split("/");
			// qsub = s [0]+ File.separator+s[1].split("-")[1];
			// qsub = qsub + File.separator+s[1];
			// System.out.println(qsub);
		}
		return accuracy;
	}

	public static void main(String[] args) {

		l4j.info("started");
		CheckResultsAgainstGS cr = new CheckResultsAgainstGS();


		File gs = new File(
				"/Users/annalisa/Documents/CORPORAandDATASETS/swde-17477/groundtruth/");

		File res = new File(annotationFolder);

		System.out.print("file Name");
		for (File strategy : res.listFiles()) {
			if (strategy.isDirectory())
				System.out.print("\t" + strategy.getName());

		}
		System.out.println();
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter("./accuracy.csv"));
		for (File att : gs.listFiles()) {
			if (att.isDirectory()) {
				System.out.println(att);

				for (File gsXatt : att.listFiles()) {
					if (gsXatt.getName().endsWith(".txt")) {
						System.out.print(gsXatt.getName());

						// for each strategy

						
						for (File strategy : res.listFiles()) {
							if (strategy.isDirectory()) {
								// load domain folder
								File resultFromStrategy = new File(strategy
										+ File.separator + att.getName());
								// System.out.print(" "+strategy.getName());
								// search for correct file
								if (resultFromStrategy.isDirectory()) {
									Double acc = null;

									for (File g : resultFromStrategy
											.listFiles()) {
										if (g.getName().endsWith(".txt")
												&& g.getName().equals(
														gsXatt.getName())) {
//											acc = cr.compare(
//													g.getAbsolutePath(),
//													gsXatt.getAbsolutePath());
											acc = cr.compare(
													gsXatt.getAbsolutePath(),
													g.getAbsolutePath());
											continue;
										}
									}
									if (acc != null) {
										System.out.print("\t" + acc);
										out.print(acc+"\t");

									} else {
										System.out.print("\t" + "<null>");
										out.print("<null>"+"\t");


									}
								}
							}
						}
						out.println(gsXatt.getName());

					}

					System.out.println();
					


				}			out.close();	
			}

		}}catch (Exception e) {
			// TODO: handle exception
		}

	}
}
