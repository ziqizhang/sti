

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
	public static String generateScript(String domainName) {
		String script = "#!/bin/bash \n" 
				+ "#$ -m bea -M a.gentile@shef.ac.uk -l h_rt=168:00:00 \n"
				+ "module add apps/java/1.6 \n"
				+ "java -Xmx2048m -Dlog4j.configuration=file:///fastdata/ac1ag/tables/log4j.properties"
//				+ "-jar /fastdata/ac1ag/annotations/lodie-xpathExperiment-1.0-jar-with-dependencies.jar  "
				+ " -jar /fastdata/ac1ag/tables/lodie-tables-1.0-jar-with-dependencies.jar"
				+ " /fastdata/ac1ag/tables/"
				+ domainName
				+ File.separator
				+ domainName
				+ "_query.txt " 
				+ "/fastdata/ac1ag/tables/"
				+ domainName
				+ File.separator
				+ domainName
				+ "_websites.txt"+ " 1000 \n";
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

		String fol = "./queries";

		File[] domains = new File(fol).listFiles();

		for (File d : domains) {

						String name[] = d.getName().split("_");
						String script = generateScript(name[0]);
						if (!new File("./script/"+name[0]).exists())
							new File("./script/"+name[0]).mkdirs();
						printScriptOnFile(script, "./script/"+name[0]+File.separator+name[0]+".txt");
					}


			System.out.println();
			System.out.println();
		}

	}


