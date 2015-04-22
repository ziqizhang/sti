package uk.ac.shef.oak.xpathExperiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import uk.ac.shef.oak.any23.xpath.ExtractXpath;
import uk.ac.shef.oak.any23.xpath.HtmlDocument;
import uk.ac.shef.oak.xpath.collectiveExperiment.ValueComparator;
import uk.ac.shef.wit.ie.wrapper.html.xpath.DOMUtil;

public class XpathCandidateExtractorLOCAL {

	// logging managment
	private static Logger l4j = Logger
			.getLogger(XpathCandidateExtractorLOCAL.class);
	// static{
	// PropertyConfigurator.configure("./properties/log4j.properties");
	// }

	private String stringExampleFile;
	private boolean relax;
	private double relaxThreashold;
	private boolean singleResult;

	public boolean isSingleResult() {
		return singleResult;
	}

	public void setSingleResult(boolean singleResult) {
		this.singleResult = singleResult;
	}

	public boolean isRelax() {
		return relax;
	}

	public void setRelax(boolean relax) {
		this.relax = relax;
	}

	public double getRelaxThreashold() {
		return relaxThreashold;
	}

	public void setRelaxThreashold(double relaxThreashold) {
		this.relaxThreashold = relaxThreashold;
	}

	public XpathCandidateExtractorLOCAL() {
		l4j.trace("initializing XpathCandidateExtractor");
		this.relax = true;
		this.relaxThreashold = 0.20;
		this.singleResult = true;
	}

	public XpathCandidateExtractorLOCAL( boolean singleResult,
			boolean relax, double relaxThreashold) {
		l4j.info("initializing XpathCandidateExtractor");
		this.relax = relax;
		this.relaxThreashold = relaxThreashold;
		this.singleResult = singleResult;
	}

	private Set<String> annotations;




	/**
	 * This function extract all nodes in a cached html file which match with
	 * the class gazetter
	 * 
	 * @param f
	 *            the cached html page
	 * @return
	 */
	public Map<String, String> getAnnotationsFromPage(File f) {
		Map<String, String> xp = new HashMap<String, String>();
		HtmlDocument h = new HtmlDocument(f);

		// String s = FileUtils.readFileContent(f);
		// l4j.info(s);
		// h.setPageHtml(s);
		Document doc = h.getPageDom();
		ExtractXpath exp = new ExtractXpath();
		l4j.trace("****fetching candidate Xpaths from page "
				+ f.getAbsolutePath());

		try {
			// NodeList nodes = exp.findXpathNodeOnHtmlPage(doc, "//node()");
			NodeList nodes = exp.findXpathNodeOnHtmlPage(doc, "//text()");

			for (int i = 0; i < nodes.getLength(); i++) {
				// if ((nodes.item(i).getNodeType()== Node.TEXT_NODE)) {
				String v = nodes.item(i).getNodeValue();
				if (v != null) {
					// l4j.info("node "+v);

					if (this.matchesTitle(v)) {
						// String xpn = DomUtils.getXPathForNode(nodes.item(i));
						String xpn = DOMUtil.getXPath(nodes.item(i));

						l4j.trace("*****matching node " + v.trim() + " " + xpn);

						xp.put(xpn, v.trim());

					}
				}
				// }
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		return xp;

	}

	public Map<String, String> getMarchingXpathFromPage(File f, String xpath) {
		Map<String, String> xp = new HashMap<String, String>();
		HtmlDocument h = new HtmlDocument(f);

		// String s = FileUtils.readFileContent(f);
		// l4j.info(s);
		// h.setPageHtml(s);
		Document doc = h.getPageDom();
		ExtractXpath exp = new ExtractXpath();
		try {
			NodeList nodes = exp.findXpathNodeOnHtmlPage(doc, xpath);

			for (int i = 0; i < nodes.getLength(); i++) {
				String v = nodes.item(i).getNodeValue();
				if (v != null) {
					// String xpn = DomUtils.getXPathForNode(nodes.item(i));
					String xpn = DOMUtil.getXPath(nodes.item(i));

					// l4j.info("matching node "+v+" "+xpn);
					xp.put(xpn, v);

				}

			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		return xp;

	}

	public Map<String, String> getMatchingXpathFromPage(File f,
			Set<String> xpaths) {
		Map<String, String> xp = new HashMap<String, String>();
		HtmlDocument h = new HtmlDocument(f);

		// String s = FileUtils.readFileContent(f);
		// l4j.info(s);
		// h.setPageHtml(s);
		Document doc = h.getPageDom();
		ExtractXpath exp = new ExtractXpath();

		for (String xpath : xpaths) {
			try {
				NodeList nodes = exp.findXpathNodeOnHtmlPage(doc, xpath);

				for (int i = 0; i < nodes.getLength(); i++) {
					String v = nodes.item(i).getNodeValue();
					if (v != null) {
						// String xpn = DomUtils.getXPathForNode(nodes.item(i));
						String xpn = DOMUtil.getXPath(nodes.item(i));

						// l4j.info("matching node "+v+" "+xpn);
						xp.put(xpn, v);

					}

				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}

		}

		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		return xp;

	}

	private boolean matchesTitle(String nodeValue) {

		nodeValue = nodeValue.trim();
		for (String s : this.annotations) {
			if (nodeValue.equalsIgnoreCase(s)) {
				return true;
			}
		}

		return false;
	}

	public String relaxXpath(String xp) {

		String relaxedXp = DOMUtil.removeXPathPositionFilters(xp);

		return relaxedXp;

	}

	public Set<String> relaxXpath(Set<String> xpaths) {
		Set<String> relaxXpath = new HashSet<String>();

		for (String xp : xpaths) {

			String relaxedXp = DOMUtil.removeXPathPositionFilters(xp);
			relaxXpath.add(relaxedXp);
			// l4j.info(xp +" "+ relaxedXp);

		}

		return relaxXpath;

	}

	public Map<String, Map<String, Integer>> getXpathUsage(String htmlFolder) {

		Map<String, Map<String, Integer>> usage = new HashMap<String, Map<String, Integer>>();

		File dir = new File(htmlFolder);
		if (dir.isDirectory()) {
			File[] ht = dir.listFiles();
			for (File f : ht) {
				Map<String, String> xp = this.getAnnotationsFromPage(f);
				// l4j.info(xp.size());
				for (String p : xp.keySet()) {
					// String s = this.relaxXpath(p);
					if (usage.get(p) == null) {
						Map<String, Integer> val = new HashMap<String, Integer>();
						val.put(xp.get(p), 1);
						usage.put(p, val);
					} else {
						Map<String, Integer> val = usage.get(p);
						if (val.get(xp.get(p)) != null) {
							int freq = val.get(xp.get(p)) + 1;
							val.put(xp.get(p), freq);
						} else {
							val.put(xp.get(p), 1);
						}
						usage.put(p, val);
					}
				}
				// Set<String> rxp = xce.relaxXpath(xp);
				//
				// l4j.info(rxp.size());
				// for (String s:rxp){
				// l4j.info(s);
				// }
			}
		}

		return usage;
	}

	public void printXpatMap(Map<String, Double> ranking, String filename) {
		PrintWriter out;

		double totalCoverage = 0;

		for (String k : ranking.keySet()) {
			totalCoverage = totalCoverage + ranking.get(k);
		}

		try {
			out = new PrintWriter(new FileWriter(filename));
			out.println("Number of Xpath \t" + ranking.size());
			out.println("Coverage \t" + totalCoverage);
			out.println("Min coverage \t" + calculateMin(ranking.values()));
			out.println("Max coverage \t" + calculateMax(ranking.values()));
			out.println("Avg coverage per Xpath \t"
					+ calculateAvg(ranking.values()));
			// TODO check if correct
			out.println("StDev \t" + calculateStdev(ranking.values()));

			for (Entry<String, Double> a : ranking.entrySet()) {

				out.println(a.getKey() + "\t" + a.getValue());
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void printResutls(Map<String, Set<String>> gsPath, String filename) {
		PrintWriter out;

		try {
			out = new PrintWriter(new FileWriter(filename));
			// TODO write header lines
			out.println();
			out.println();

			for (Entry<String, Set<String>> a : gsPath.entrySet()) {

				out.print(a.getKey() + "\t" + a.getValue().size());
				if (a.getValue().size() == 0) {
					out.print("\t <NULL>\n");

				} else {
					for (String s : a.getValue()) {
						out.print("\t" + s);

					}
					out.print("\n");
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException {

		String experimentFolder = "dbpediaExperiment";

		l4j.info(" Experiment folder name: " + experimentFolder);
		// folder to process
		// e.g. of folder /fastdata/ac1ag/restaurant/restaurant-fodors-2000
		String htmlFolder = args[0];
		String xpathMapCacheFile = args[1];

		String propertyName = "";
		if (xpathMapCacheFile.endsWith(".txt")) {
			String p[] = xpathMapCacheFile.split(File.separator);
			propertyName = p[p.length - 1].substring(0,
					p[p.length - 1].lastIndexOf(".txt"));
		}

		XpathCandidateExtractorLOCAL xpce = new XpathCandidateExtractorLOCAL();

		Map<String, Set<String>> xpStrategies = new HashMap<String, Set<String>>();

		// get xpath map from cache
		Map<String, Double> ranking = xpce
				.getXpathUsageFromCache(xpathMapCacheFile);
		double totalCoverage = 0;

		for (double v : ranking.values()) {
			totalCoverage = totalCoverage + v;
		}
		l4j.info("Ranking \t" + ranking );

		// sort the xpath map
		ValueComparator bvc = new ValueComparator(ranking);
		SortedMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
		sorted_map.putAll(ranking);

		l4j.info("Ranking \t" + sorted_map.size() + sorted_map);

		// * STRATEGY: strategySingleXpath
		String winningXpath = "";

		Set<String> strategyOne = new HashSet<String>();
		winningXpath = sorted_map.firstKey();
		strategyOne.add(winningXpath);
		l4j.info("strategySingleXpath = "+winningXpath);
		// * end STRATEGY: strategySingleXpath

		// * STRATEGY: strategyRelaxingSingleXpath
		// calculate map of relaxed xpath
		Map<String, Double> relaxedRanking = new HashMap<String, Double>();
		for (Entry<String, Double> e : sorted_map.entrySet()) {
			String r = DOMUtil.removeXPathPositionFilters(e.getKey());
			if (relaxedRanking.get(r) != null) {
				double count = relaxedRanking.get(r) + e.getValue();
				relaxedRanking.put(r, count);
			} else {
				relaxedRanking.put(r, e.getValue());
			}
		}
		// sort map of relaxed xpath
		ValueComparator bvcsorted = new ValueComparator(relaxedRanking);
		SortedMap<String, Double> sortedRelaxedMap = new TreeMap<String, Double>(
				bvcsorted);
		sortedRelaxedMap.putAll(relaxedRanking);
		l4j.trace("RelaxedXpath \t" + relaxedRanking.size() + sortedRelaxedMap);

		String relaxedXp = DOMUtil.removeXPathPositionFilters(winningXpath);
		double coveragePercentage = (double) ranking.get(winningXpath)
				/ totalCoverage;
		double coveragePercentageWithRelaxing = (double) relaxedRanking
				.get(relaxedXp) / totalCoverage;
		l4j.info("Coverage with original Xpath \t" + coveragePercentage);
		l4j.info("Coverage with relaxed Xpath \t"
				+ coveragePercentageWithRelaxing);
		l4j.info("Increase ratio \t"
				+ (coveragePercentageWithRelaxing - coveragePercentage));
		// TODO for now if the expected result is a unique value, then the first
		// xpath is chosen, without any further checking
		l4j.info("strategyRelaxingSingleXpath - relaxing = "
				+ (coveragePercentageWithRelaxing - coveragePercentage > xpce
						.getRelaxThreashold()));
		Set<String> strategyRelaxingSingleXpath = new HashSet<String>();
		// TODO change arbitrary value with Stdev function
		if (coveragePercentageWithRelaxing - coveragePercentage > xpce
				.getRelaxThreashold()) {
			// if (ranking.get(winningXpath)< relaxedRanking.get(relaxedXp)){
			// if (relaxedRanking.size()<sorted_map.size()){
			l4j.info("strategyRelaxingSingleXpath \t" + winningXpath + " --> "
					+ relaxedXp);
			// TODO for now only one xpath is chosen, possibly appropriate to
			// choose multiple rather than relaxing
			strategyRelaxingSingleXpath.add(relaxedXp);
		} else {
			strategyRelaxingSingleXpath.add(winningXpath);
		}
		// * end STRATEGY: strategyRelaxingSingleXpath

		// * STRATEGY: strategyMultipleXpath
		Set<String> strategyMultipleXpath = new HashSet<String>();
		double currentCoverage = 0;
		for (Entry<String, Double> s : sorted_map.entrySet()) {
			strategyMultipleXpath.add(s.getKey());
			currentCoverage = currentCoverage + s.getValue();
			double coverage = (double) currentCoverage / totalCoverage;
			l4j.trace("strategyMultipleXpath - adding xpath " + s.getKey()
					+ " coverage: " + s.getValue() + "/" + totalCoverage
					+ " total coverage " + coverage);
			if (coverage >= 0.80) {
				break;
			}
		}
		l4j.info("strategyMultipleXpath \t" + strategyMultipleXpath);
		// * end STRATEGY: strategyMultipleXpath

		// * STRATEGY: strategyMultipleXpathFromRelaxed
		Set<String> strategyMultipleXpathFromRelaxed = new HashSet<String>();
		currentCoverage = 0;
		for (String s : strategyMultipleXpath) {
			String r = DOMUtil.removeXPathPositionFilters(s);
			if (r.equals(relaxedXp)) {
				strategyMultipleXpathFromRelaxed.add(s);
			}
			l4j.trace("strategyMultipleXpathFromRelaxed - adding xpath " + s);
		}
		l4j.trace("strategyMultipleXpathFromRelaxed \t" + strategyMultipleXpathFromRelaxed);

		// * end STRATEGY: strategyMultipleXpathFromRelaxed

		// TODO design other strategies

		// * add all STRATEGIES results
		xpStrategies.put("strategyRelaxingSingleXpath",
				strategyRelaxingSingleXpath);
		xpStrategies.put("strategySingleXpath", strategyOne);
		xpStrategies.put("strategyMultipleXpath", strategyMultipleXpath);
		xpStrategies.put("strategyMultipleXpathFromRelaxed",
				strategyMultipleXpathFromRelaxed);
		// TODO add other strategies to results map

		for (Entry<String, Set<String>> x_entry : xpStrategies.entrySet()) {

			Map<String, Set<String>> res = new HashMap<String, Set<String>>();

			File folder = new File(htmlFolder);
			if (folder.isDirectory()) {

				String resFolder = "";

				// create directory for results
				String[] dirs = htmlFolder.split(File.separator);
				String resName = dirs[dirs.length - 1];
				resName = resName.substring(0, resName.lastIndexOf("-"));
				for (int j = 0; j < dirs.length - 2; j++) {
					resFolder = resFolder + File.separator + dirs[j];
				}
				resFolder = resFolder + File.separator + experimentFolder
						+ File.separator + "annotation_results_"
						+ x_entry.getKey() + File.separator
						+ dirs[dirs.length - 2];

				File resultsFolder = new File(resFolder);

				if (!resultsFolder.exists())
					resultsFolder.mkdirs();
				l4j.info("results in "+resultsFolder);
				// l4j.debug("Xpath " + i + " of " + x_entry.getValue().size() +
				// "\t" + s);
				l4j.debug("Xpath set size: " + x_entry.getValue().size() + "\t"
						+ x_entry.getValue());

				for (File f : folder.listFiles()) {
					Map<String, String> n = xpce.getMatchingXpathFromPage(f,
							x_entry.getValue());
					l4j.debug("******* page " + f.getAbsolutePath());

					Set<String> r = new HashSet<String>();

					if (n.isEmpty()) {
						l4j.warn("no results for page " + f.getName());
					}

					for (String nod : n.keySet()) {
						String value = n.get(nod).trim();
						l4j.debug("nodes from xpath (" + nod + ") " + value);
						String id = f.getName().substring(0,
								f.getName().lastIndexOf(".htm"));
						l4j.warn(id + "\t" + value);
						if (!value.equals("")) {
							r.add(value);
							res.put(id, r);
						}
					}
				}
				if (res.isEmpty()) {
					// TODO scan all sorted_map to find the winning xpath
					l4j.warn("no results for " + folder);
				} else {
					// System.out.println(res);
					xpce.printResutls(res, resultsFolder + File.separator
							+ resName + "-" + propertyName + ".txt");
				}

			} else {
				l4j.debug("******* NOT a DIR " + folder.getAbsolutePath());
			}

		}
		// }

	}

	private Map<String, Double> getXpathUsageFromCache(String xpathResultsFolder) {

		Map<String, Double> usage = new HashMap<String, Double>();

		File dir = new File(xpathResultsFolder);
		if (dir.isFile()) {
			if (dir.getName().endsWith(".txt")) {

				try {
					BufferedReader input = new BufferedReader(new FileReader(
							xpathResultsFolder));
					String line;
					l4j.trace("loading xpath usage from cache "
							+ xpathResultsFolder);

					// TODO skip headers
					line = input.readLine();
					line = input.readLine();
					line = input.readLine();
					line = input.readLine();
					line = input.readLine();
					line = input.readLine();

					while (line != null) {
						line = line.trim();
						if (!line.equals("")) {
							String[] v = line.split("\t");

							usage.put(v[0], Double.parseDouble(v[1]));
						}

						line = input.readLine();

					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		return usage;

	}

	private static double calculateStdev(Collection<Double> data) {
		double n = 0;
		double sum1 = 0;
		double sum2 = 0;

		for (double x : data) {
			n = n + 1;
			sum1 = sum1 + x;
		}

		double mean = (double) sum1 / n;

		for (double x : data) {
			sum2 = sum2 + (x - mean) * (x - mean);
		}

		double variance = sum2 / (n - 1);

		return Math.sqrt(variance);
	}

	private static double calculateAvg(Collection<Double> data) {
		double sum1 = 0;

		for (double x : data) {
			sum1 = sum1 + x;
		}

		double mean = (double) sum1 / data.size();

		return mean;
	}

	private static double calculateMin(Collection<Double> data) {
		double min = data.iterator().next();

		for (double x : data) {
			if (x < min)
				min = x;
		}

		return min;
	}

	private static double calculateMax(Collection<Double> data) {
		double max = 0;

		for (double x : data) {
			if (x > max)
				max = x;
		}

		return max;
	}

}
