package uk.ac.shef.oak.xpathExperiment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class GenerateIcebergScripts {
	
//	static String gazetteerFolderName = "baselineGaz";
//	static String resultFolderName = "toplineExpResults";
//	static String prop = "topline";
//	
//	static String gazetteerFolderName = "dbpediaGazeteer";
//	static String resultFolderName = "dbpediaExpResults";
//	static String prop = "dbpedia";

	static String gazetteerFolderName = "sindiceGazeteer";
	static String resultFolderName = "sindiceExpResults";
	static String prop = "sindice";
	
//	static String gazetteerFolderName = "cacheCLOUDgazeteer";
//	static String resultFolderName = "cacheCLOUDExpResults";
//	static String prop = "cacheCloud";

	// static String SCRIPT = "#!/bin/bash \n"+
	// "#$ -l mem=16G -l rmem=12G -m bea -M a.gentile@shef.ac.uk -l h_rt=168:00:00 \n"+
	// "module add apps/java/1.6 \n"+
	// "java -Xmx4096m -Dlog4j.configuration=file:///fastdata/ac1ag/annotations/properties/log4j.properties "+
	// "-jar /fastdata/ac1ag/annotations/lodie-xpathExperiment-1.0-jar-with-dependencies.jar  "+
	// "/fastdata/ac1ag/book/book-amazon-2000 /fastdata/ac1ag/titles/book_titles.txt \n";

	public static void printScriptOnFile(String script, String filename) {
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(filename));
			out.print(script);

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

/*
 * 
 * 
#!/bin/bash 
#$ -l mem=16G -l rmem=8G -m bea -M a.gentile@shef.ac.uk -l h_rt=7:59:00 
module add apps/java/1.6 
java -Xmx4096m -Dlog4j.configuration=file:///fastdata/ac1ag/annotations/properties/auto/aol/fuel_economy.log4j.properties -jar 
/fastdata/ac1ag/annotations/lodie-xpathExperiment-1.0-jar-with-dependencies.jar  
/fastdata/ac1ag/annotations/testSET/auto/auto-aol-2000 
/fastdata/ac1ag/annotations/optimalGaz/auto/fuel_economy.txt 
 *
 *
 */
	public static String generateScript(String domainName, String websitename,
			String size, String gazeteer) {
		String script = "#!/bin/bash \n"
				+ "#$ -m bea -M a.gentile@shef.ac.uk \n"
				+ "module add apps/java/1.6 \n"
				+ "java -Xmx2048m -Dlog4j.configuration=file:///fastdata/ac1ag/annotations/prop_"+prop+File.separator
				+ domainName
				+ File.separator
				+ websitename
				+ File.separator
				+ gazeteer
				+ ".log4j.properties "
//				+ "-jar /fastdata/ac1ag/annotations/lodie-xpathExperiment-1.0-jar-with-dependencies.jar  "
				+ "-jar /fastdata/ac1ag/annotations/lodie-xpathExperiment-single.jar  "
				+ "/fastdata/ac1ag/annotations/testSET/"
				+ domainName
				+ File.separator
				+ domainName
				+ "-"
				+ websitename
				+ "-"
				+ size
				+ " "
				+
				// "/fastdata/ac1ag/annotations/optimalGaz/"+domainName+File.separator+gazeteer+".txt "
				// +
				// "/fastdata/ac1ag/annotations/dbpediaGazeteer/"+domainName+File.separator+gazeteer+".txt "
				// +
				"/fastdata/ac1ag/annotations/"
				+ gazetteerFolderName + File.separator
				+ domainName
				+ File.separator + gazeteer + ".txt " +resultFolderName+ "\n";
		return script;
	}

	public static String generatePropertiesFileContent(String domainName,
			String websitename, String attribute) {
		String script = "log4j.rootLogger = TRACE, fileout \n"
				+ "log4j.appender.fileout = uk.ac.shef.oak.logging.LogLevelFilterFileAppender \n"
				+ "log4j.appender.fileout.layout.ConversionPattern = %d{ABSOLUTE} %5p %c - %m%n \n"
				+ "log4j.appender.fileout.layout = org.apache.log4j.PatternLayout \n"
				+ "log4j.appender.fileout.File = /fastdata/ac1ag/annotations/log_"+prop+File.separator
				+ domainName + File.separator + websitename + File.separator
				+ attribute + File.separator + websitename + "-" + attribute;

		return script;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String fol = "/Users/annalisa/Documents/CORPORAandDATASETS/swde-17477/testSET";
		String baseline = "./"+gazetteerFolderName+"/";
//		String script = "./icebergLODScript/";
		String script = "./script_"+prop+"/";
//		String script = "./icebergCacheCloudScript/";
//		String properties = "./dbpediaproperties/";
		String properties = "./prop_"+prop+"/";
//		String properties = "./cachecloudproperties/";

		
		File[] domains = new File(fol).listFiles();

		for (File d : domains) {
			if (d.isDirectory()) {

				File gaz = new File("./"+gazetteerFolderName + File.separator
						+ d.getName());
				Set<String> attri = new HashSet<String>();
				if (gaz.exists()) {
					for (File fi : gaz.listFiles()) {
						attri.add(fi.getName().substring(0,
								fi.getName().lastIndexOf(".txt")));
					}
				}

				// ws are all different websites
				File[] ws = d.listFiles();
				for (File w : ws) {
					if (w.isDirectory()) {
						String name[] = w.getName().split("-");
						// System.out.println(name[0]);
						// System.out.println(name[1]);
						// System.out.println(name[2]);

						if (new File(baseline + name[0]).isDirectory()) {

							for (File f : new File(baseline + name[0])
									.listFiles()) {
								if (f.getName().endsWith(".txt")) {
									String att = f.getName().substring(0,
											f.getName().lastIndexOf(".txt"));
									String scr = generateScript(name[0],
											name[1], name[2], att);
									String prop = generatePropertiesFileContent(
											name[0], name[1], att);

									File sc = new File(script + File.separator
											+ name[0] + File.separator
											+ name[1]);
									if (!sc.exists())
										sc.mkdirs();
									printScriptOnFile(scr, sc + File.separator
											+ name[0] + "-" + name[1] + "-"
											+ att);
									// printScriptOnFile(scr,
									// sc+File.separator+name[0]+File.separator+name[1]+File.separator+att);

									File pr = new File(properties
											+ File.separator + name[0]
											+ File.separator + name[1]);
									if (!pr.exists())
										pr.mkdirs();
									printScriptOnFile(prop, pr + File.separator
											+ att + ".log4j.properties");

									// TODO comment/uncomment if for printing
									// all qsub or only the ones for which a
									// gazetteer is available
									 if (attri.contains(att)){
									System.out.println("qsub " + name[0]
											+ File.separator + name[1]
											+ File.separator + name[0] + "-"
											+ name[1] + "-" + att);
								}
								 }

							}
						}

					}
				}
			}

			System.out.println();
			System.out.println();
		}

	}

}
