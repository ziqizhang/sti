package uk.ac.shef.oak.gazetteers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.shef.oak.xpathExperiment.LoadGS;

public class GenerateBaselineGazeteers {

	public Set<String> queryService(String service, String sparqlQueryString,
			String solutionConcept) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void printGazeteerOnFile(Set<String> gaz, String filename) {
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(filename));
			for (String a : gaz) {
				out.print(a + "\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, HashMap<String, Set<String>>> gazeteers = new HashMap<String, HashMap<String, Set<String>>>();

		File gsFolder = new File(args[0]);
		if (gsFolder.isDirectory()) {
			File verticals[] = gsFolder.listFiles();
			for (File domain : verticals) {
				if (domain.isDirectory()) {
					String domainName = domain.getName();
					HashMap<String, Set<String>> attForDomain = new HashMap<String, Set<String>>();

					if (!gazeteers.containsKey(domainName)) {
						gazeteers.put(domainName, attForDomain);
					}

					File attributes[] = domain.listFiles();

					for (File a : attributes) {
						System.out.println("processing " + a);

						String aname = a.getName();
						if (aname.endsWith(".txt")) {
							String att = aname.substring(
									aname.lastIndexOf("-") + 1,
									aname.lastIndexOf(".txt"));
							// System.out.println(att);
							if (!attForDomain.containsKey(att)) {
								attForDomain.put(att, new HashSet<String>());
							}
							LoadGS lgs = new LoadGS(a.getAbsolutePath());
							attForDomain.get(att).addAll(
									lgs.getAllDifferentAnnotations());
						}
					}
				}
			}
		}

		for (Entry<String, HashMap<String, Set<String>>> e : gazeteers
				.entrySet()) {
			String resultFolder = "./optimalGaz/";
			resultFolder = resultFolder + e.getKey();
			if (!new File(resultFolder).exists())
				new File(resultFolder).mkdirs();
			System.out
			.print(e.getKey());
			for (Entry<String, Set<String>> a : e.getValue().entrySet()) {


				System.out
						.println("\t" +a.getKey() + "\t" + a.getValue().size());
				printGazeteerOnFile(a.getValue(), resultFolder + File.separator
						+ a.getKey() + ".txt");
			}
		}
	}

}
