package uk.ac.shef.oak.xpathExperiment;

import java.io.File;
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
import uk.ac.shef.oak.gazetteers.TextOperations;
import uk.ac.shef.oak.xpath.collectiveExperiment.ValueComparator;
import uk.ac.shef.wit.ie.wrapper.html.xpath.DOMUtil;


public class XpathCandidateExtractor {


	//logging managment
	private static Logger l4j = Logger.getLogger(XpathCandidateExtractor.class);
	//    static{
	//	PropertyConfigurator.configure("./properties/log4j.properties");
	//    }	

	private String stringExampleFile;
	private boolean relax;
	private double relaxThreashold;
	private boolean singleResult;
	
	
	Map<String, Map<String,String>> xpathOnPage = new HashMap<String, Map<String,String>>();

	
	
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



	public XpathCandidateExtractor(String examples) {
		l4j.trace("initializing XpathCandidateExtractor");
		this.stringExampleFile= examples;
		this.relax=true;
		this.relaxThreashold=0.20;
		this.singleResult=true;
		populateAnnotationsSimple();
	}

	public XpathCandidateExtractor(String examples, boolean singleResult, boolean relax, double relaxThreashold) {
		l4j.info("initializing XpathCandidateExtractor");
		this.stringExampleFile= examples;
		this.relax=relax;
		this.relaxThreashold=relaxThreashold;
		this.singleResult = singleResult;
		populateAnnotationsSimple();
	}

	private Set<String> annotations;


	private void populateAnnotationsSimple(){

		BookTitles bt = new BookTitles(this.stringExampleFile);
		Set<String> tokens = new HashSet<String>();
		// TODO clean the set of annotations
		// remove single word annotatons
		for (String t: bt.getBookTitles()){
				tokens.add(t.trim());
		}

		this.annotations= tokens;

	}

	//TODO incorporate cleaning logic in writing the gazeteers
	@Deprecated
	private void populateAnnotations(){
		Pattern alphaNum = Pattern.compile("[a-zA-Z0-9]");
		//		Pattern notAlphaNum = Pattern.compile("[^a-z&&^A-Z&&^0-9]");
		//		Pattern punct = Pattern.compile("\\p{Punct}");
		//		Pattern range = Pattern.compile("\\-");
		//		Pattern re =  Pattern.compile("[;\\\\/:*?\"<>|&']");

		//		Pattern notAlphaNum = Pattern.compile("[^a-z^A-Z^0-9]");
		//		Pattern alphaNum = Pattern.compile("^[\\u0000-\\u007F]*$");
		BookTitles bt = new BookTitles(this.stringExampleFile);
		Set<String> tokens = new HashSet<String>();
		// TODO clean the set of annotations
		// remove single word annotatons
		for (String t: bt.getBookTitles()){

			if (this.matchesPattern(alphaNum, t)!=null&&(t.split(" ").length>1)){
				tokens.add(t.trim());
			}

			//		l4j.info(tokens); 
			//		for (String s: tokens){
			//			l4j.info(s);
			//
			//		}
			//		String patt = StringUtils.join(tokens, "|");


			//		l4j.info(patt);



			//		this.annotations= Pattern.compile("^embracing persephone$|^no other life but this$|^on the crofter's trail$|^the sword of armageddon (the new kid, #3)$|^Твояэа иьэоъия - 52 ьедмиџи оэ �?ачалоэо �?а xxi век$");
			//		l4j.info(this.annotations);
			//	     
		}

		this.annotations= tokens;

	}


	public Set<XPath> getAnnotationsFromPage(URL page){
		Set<XPath> xp = new HashSet<XPath>();
		HtmlDocument h = new HtmlDocument(page.toString());
		//		l4j.info(h.getPageDom());
		return xp;

	}

	private String matchesPattern(Pattern p, String sentence) {
		Matcher m = p.matcher(sentence);

		if (m.find()) {
			return m.group();
		}

		return null;
	}

	/**
	 * This function extract all nodes in a cached html file which match with the class gazetter
	 * @param f the cached html page 
	 * @return a map where the key is the xpath and the value is the value of the node identified from the xpath
	 */
	public Map<String,String> getAnnotationsFromPage(File f){
		Map<String,String> xp = new HashMap<String,String>();
		HtmlDocument h = new HtmlDocument(f);

		//			String s = FileUtils.readFileContent(f);
		//			l4j.info(s);
		//			h.setPageHtml(s); 
		Document doc = h.getPageDom();
		ExtractXpath exp = new ExtractXpath();
		l4j.trace("**getAnnotationsFromPage**fetching candidate Xpaths from page "+f.getAbsolutePath()); 

		try {
			//				 NodeList nodes = exp.findXpathNodeOnHtmlPage(doc, "//node()");
			NodeList nodes = exp.findXpathNodeOnHtmlPage(doc, "//text()");

			for (int i = 0; i < nodes.getLength(); i++) {
				//					  if ((nodes.item(i).getNodeType()== Node.TEXT_NODE))  {
				String v = nodes.item(i).getNodeValue();
				//I added this line to normalize the text, remove if causes issues
				v = TextOperations.normalizeString(v);
				if (v!=null){
					//							  l4j.info("node "+v); 
					if (this.matchesTitle(v)){
						//						  String xpn = DomUtils.getXPathForNode(nodes.item(i));
						String xpn = DOMUtil.getXPath(nodes.item(i));
						l4j.trace("**getAnnotationsFromPage***matching node "+v.trim()+" "+xpn); 
						xp.put(xpn, v.trim());
					}
				}
				//					  }
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}

		return xp;

	}


	public Map<String,String> getMarchingXpathFromPage(File f, String xpath){
		Map<String,String> xp = new HashMap<String,String>();
		HtmlDocument h = new HtmlDocument(f);

		//			String s = FileUtils.readFileContent(f);
		//			l4j.info(s);
		//			h.setPageHtml(s); 
		Document doc = h.getPageDom();
		ExtractXpath exp = new ExtractXpath();
		try {
			NodeList nodes = exp.findXpathNodeOnHtmlPage(doc, xpath);

			for (int i = 0; i < nodes.getLength(); i++) {
				String v = nodes.item(i).getNodeValue();
				if (v!=null){
					//						  String xpn = DomUtils.getXPathForNode(nodes.item(i));
					String xpn = DOMUtil.getXPath(nodes.item(i));


					//						  l4j.info("matching node "+v+" "+xpn); 
					xp.put(xpn, v);

				}

			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}

		return xp;

	}
	
	public Map<String,String> getMatchingXpathFromCacheUsageMap(String page, Set<String> xpaths){
		Map<String,String> matchingXp = new HashMap<String,String>();
		Map<String,String> xp = this.xpathOnPage.get(page);

		for (String xpath : xpaths) {
			if (xp.get(xpath)!=null)
			matchingXp.put(xpath, xp.get(xpath));			
		}
		return matchingXp;

	}
	
	
	public Map<String,String> getMatchingXpathFromPage(File f, Set<String> xpaths){
		Map<String,String> xp = new HashMap<String,String>();
		HtmlDocument h = new HtmlDocument(f);

		//			String s = FileUtils.readFileContent(f);
		//			l4j.info(s);
		//			h.setPageHtml(s); 
		Document doc = h.getPageDom();
		ExtractXpath exp = new ExtractXpath();
		
		for (String xpath : xpaths) {
			try {
				NodeList nodes = exp.findXpathNodeOnHtmlPage(doc, xpath);

				for (int i = 0; i < nodes.getLength(); i++) {
					String v = nodes.item(i).getNodeValue();
					if (v != null) {
						//						  String xpn = DomUtils.getXPathForNode(nodes.item(i));
						String xpn = DOMUtil.getXPath(nodes.item(i));

						//						  l4j.info("matching node "+v+" "+xpn); 
						xp.put(xpn, v);

					}

				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			
			
		}
		

		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}

		return xp;

	}

	private boolean matchesTitle(String nodeValue) {

		nodeValue=nodeValue.trim();
		for (String s:this.annotations){
			if (nodeValue.equalsIgnoreCase(s)){
				return true;
			}
		}

		return false;
	}

	public String relaxXpath(String xp){


		String relaxedXp = DOMUtil.removeXPathPositionFilters(xp);


		return relaxedXp;

	}


	public Set<String>relaxXpath(Set<String> xpaths){
		Set<String>relaxXpath = new HashSet<String>();

		for (String xp:xpaths){

			String relaxedXp = DOMUtil.removeXPathPositionFilters(xp);
			relaxXpath.add(relaxedXp);
			//		    l4j.info(xp +" "+ relaxedXp); 

		}

		return relaxXpath;

	}


	public Map<String, Map<String,Integer>> getXpathUsage(String htmlFolder){

		Map<String, Map<String,Integer>> usage = new HashMap<String, Map<String,Integer>>();


		
		File dir = new File(htmlFolder);
		if (dir.isDirectory()){
			File[] ht = dir.listFiles();
			for (File f:ht){
				
				Map<String,String> xp = this.getAnnotationsFromPage(f);
				//    	    	l4j.info(xp.size());
				for (String p:xp.keySet()){
					//    	    		String s = this.relaxXpath(p);
					//store in the global map
					this.xpathOnPage.put(f.getName(), xp);
					
					//store in thge usage map
					if (usage.get(p)==null){
						Map<String, Integer> val = new HashMap<String, Integer>();
						val.put(xp.get(p),1);
						usage.put(p, val);}else{
							Map<String, Integer> val = usage.get(p);
							if (val.get(xp.get(p))!=null){
								int freq = val.get(xp.get(p))+1;
								val.put(xp.get(p), freq);
							}else{
								val.put(xp.get(p), 1);
							}
							usage.put(p, val);
						}
				}
				//    	    	Set<String> rxp = xce.relaxXpath(xp);
				//
				//    	    	l4j.info(rxp.size());
				//    	    	for (String s:rxp){
				//    	        	l4j.info(s);
				//    	    	}
			}
		}

		return usage;
	}

	
	public void printXpatMap(Map<String, Double> ranking, String filename) {
		PrintWriter out;

		double totalCoverage=0;
		
		for (String k : ranking.keySet()) {
			totalCoverage=totalCoverage+ranking.get(k);
		}
		
		try {
			out = new PrintWriter(new FileWriter(filename));
			out.println("Number of Xpath \t"+ ranking.size());
			out.println("Coverage \t"+ totalCoverage);
			out.println("Min coverage \t"+ calculateMin(ranking.values()));
			out.println("Max coverage \t"+calculateMax(ranking.values()));
			out.println("Avg coverage per Xpath \t"+ calculateAvg(ranking.values()));
			//TODO check if correct
			out.println("StDev \t"+ calculateStdev(ranking.values()));

			
			for (Entry<String, Double> a : ranking.entrySet()){

				out.println(a.getKey() + "\t"+ a.getValue());
			}
			out.close();	
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}


	public void printResutls(Map<String,Set<String>> gsPath, String filename) {
		PrintWriter out;

		try {
			out = new PrintWriter(new FileWriter(filename));
			//TODO write header lines
			out.println();
			out.println();

			for (Entry<String,Set<String>> a : gsPath.entrySet()){

				out.print(a.getKey() + "\t"+ a.getValue().size());
				if (a.getValue().size()==0){
					out.print("\t <NULL>\n");

				}else{
					for (String s: a.getValue()){
						out.print("\t"+ s );

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

		
//		String experimentFolder = "dbpediaExperiment";

		//folder to process
		//e.g. of folder /fastdata/ac1ag/restaurant/restaurant-fodors-2000
		String htmlFolder= args[0];
		//this point to the gazeteer
		String examples = args[1];
		//this indicates the name of the folder where the results will be stored; the folder willl be placed inside htmlFolder
		String experimentFolder = args[2];
				
		l4j.info(" Experiment folder name: "+experimentFolder);

		
		String propertyName = "";
		if (examples.endsWith(".txt")) {
			String p [] = examples.split(File.separator);
			propertyName = p[p.length-1].substring(0, p[p.length-1].lastIndexOf(".txt"));
		}

		l4j.info("Initializing XpathCandidateExtractor");
		XpathCandidateExtractor xpce = new XpathCandidateExtractor(examples);


		Map<String, Set<String>> xpStrategies = new HashMap<String, Set<String>>();
		
		l4j.info("Computing coveverage of " +examples+ " over "+htmlFolder);

		Map<String, Map<String, Integer>> usage = xpce.getXpathUsage(htmlFolder);
		l4j.info("Xpath usage map completed");

		Map<String, Double> ranking = new HashMap<String, Double>();

		String winningXpath ="";
		int totalCoverage=0;
		for (String k : usage.keySet()) {

			//			l4j.info(k+"\t"+usage.get(k));

			ranking.put(k, (double)usage.get(k).size());
//			l4j.info(k+" "+usage.get(k).size());

			//			l4j.info(k+"\t"+ranking.get(k));
			totalCoverage=totalCoverage+usage.get(k).size();
		}
		
		l4j.info("Total coverage \t"+totalCoverage);

		ValueComparator bvc =  new ValueComparator(ranking);
		SortedMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
		sorted_map.putAll(ranking);

		Map<String, Double> relaxedRanking = new HashMap<String, Double>();

		for (Entry <String,Double> e :sorted_map.entrySet()){
			
			String r = DOMUtil.removeXPathPositionFilters(e.getKey());
			if (relaxedRanking.get(r)!=null){
				double count = relaxedRanking.get(r) + e.getValue();
				relaxedRanking.put(r, count);}else{
					relaxedRanking.put(r, e.getValue());
				}
		}
		
		l4j.info("Ranking \t"+sorted_map.size()+ sorted_map);
		

		
		// STRATEGY: strategySingleXpath
		Set<String> strategyOne = new HashSet<String>();
		winningXpath = sorted_map.firstKey();
		strategyOne.add(winningXpath);  
		
///*		
		// STRATEGY: strategyRelaxingSingleXpath
		ValueComparator bvcsorted =  new ValueComparator(relaxedRanking);
		SortedMap<String,Double> sortedRelaxedMap = new TreeMap<String,Double>(bvcsorted);
		sortedRelaxedMap.putAll(relaxedRanking);
		l4j.info("RelaxedXpath \t"+relaxedRanking.size()+sortedRelaxedMap);
		
		String relaxedXp = DOMUtil.removeXPathPositionFilters(winningXpath);
		double coveragePercentage = (double )ranking.get(winningXpath)/totalCoverage;
		double coveragePercentageWithRelaxing = (double )relaxedRanking.get(relaxedXp)/totalCoverage;
		l4j.info("Coverage with original Xpath \t"+coveragePercentage);
		l4j.info("Coverage with relaxed Xpath \t"+coveragePercentageWithRelaxing);
		l4j.info("Increase ratio \t"+(coveragePercentageWithRelaxing-coveragePercentage));
		//TODO for now if the expected result is a unique value, then the first xpath is chosen, without any further checking
		l4j.info("strategyRelaxingSingleXpath - relaxing = "+(coveragePercentageWithRelaxing-coveragePercentage>xpce.getRelaxThreashold()));
		Set<String> strategyRelaxingSingleXpath = new HashSet<String>();
		//TODO change arbitrary value with Stdev function
		if (coveragePercentageWithRelaxing-coveragePercentage>xpce.getRelaxThreashold()){
//		if (ranking.get(winningXpath)< relaxedRanking.get(relaxedXp)){
//		if (relaxedRanking.size()<sorted_map.size()){
			l4j.info("strategyRelaxingSingleXpath \t"+winningXpath +" --> "+relaxedXp);
			//TODO for now only one xpath is chosen, possibly appropriate to choose multiple rather than relaxing
			strategyRelaxingSingleXpath.add(relaxedXp);
		}else{
			strategyRelaxingSingleXpath.add(winningXpath);
		}

//	*/
//		/*
		
		// STRATEGY: strategyMultipleXpath
	
		Set<String> strategyMultipleXpath = new HashSet<String>();
		double currentCoverage=0;
        for (Entry<String, Double> s : sorted_map.entrySet()){
        	strategyMultipleXpath.add(s.getKey());

        	currentCoverage= currentCoverage+s.getValue();
    		double coverage = (double )currentCoverage/totalCoverage;
            l4j.trace("strategyMultipleXpath - adding xpath "+s.getKey()+ " coverage: "+s.getValue()+"/"+totalCoverage +" total coverage "+coverage);

        	if (coverage>=0.80){
        		break;
        }

        }
//		*/
//		/*
		// STRATEGY: strategyMultipleXpathFromRelaxed
    	
		Set<String> strategyMultipleXpathFromRelaxed = new HashSet<String>();
		currentCoverage=0;
        for (String s : strategyMultipleXpath){
    		String r = DOMUtil.removeXPathPositionFilters(s);

        	if (r.equals(relaxedXp)){
            	strategyMultipleXpathFromRelaxed.add(s);

        	}

            l4j.trace("strategyMultipleXpathFromRelaxed - adding xpath "+s);


        }
//        */
		
		//TODO other strategies
		xpStrategies.put("strategySingleXpath", strategyOne);
		xpStrategies.put("strategyRelaxingSingleXpath", strategyRelaxingSingleXpath);
		xpStrategies.put("strategyMultipleXpath", strategyMultipleXpath);
		xpStrategies.put("strategyMultipleXpathFromRelaxed", strategyMultipleXpathFromRelaxed);

		
		//TODO add other strategies


//		for (Entry<String, Set<String>> x_entry:xp.entrySet()) {
//			int i = 0;
			for (Entry<String, Set<String>> x_entry:xpStrategies.entrySet()) {
//			for (String s : x_entry.getValue()) {
//				i++;

				Map<String, Set<String>> res = new HashMap<String, Set<String>>();

				File folder = new File(htmlFolder);
				if (folder.isDirectory()) {

					String resFolder = "";
					String xpFolder = "";

					//create directory for results
					String[] dirs = htmlFolder.split(File.separator);
					String resName = dirs[dirs.length - 1];
					resName = resName.substring(0, resName.lastIndexOf("-"));
					for (int j = 0; j < dirs.length - 2; j++) {
						resFolder = resFolder + File.separator + dirs[j];
						xpFolder = xpFolder + File.separator + dirs[j];
					}
					resFolder = resFolder + File.separator
							+ experimentFolder+ File.separator
							+ "annotation_results_"+x_entry.getKey() + File.separator
							+ dirs[dirs.length - 2];
					xpFolder = xpFolder + File.separator + experimentFolder+ File.separator
							+"xpath_results"
							+ File.separator + dirs[dirs.length - 2];

					File resultsFolder = new File(resFolder);
					File xpathFolder = new File(xpFolder);

					if (!resultsFolder.exists())
						resultsFolder.mkdirs();
					if (!xpathFolder.exists())
						xpathFolder.mkdirs();

					l4j.debug("Xpath set size: " + x_entry.getValue().size() + "\t" + x_entry.getValue());

					
					//printing the xpath usage map
					xpce.printXpatMap(ranking, xpathFolder + File.separator
							+ resName + "-" + propertyName + ".txt");

					
					for (File f : folder.listFiles()) {
						if(f.getAbsolutePath().endsWith(".htm")){
						//using cache, remove if causing issues
						Map<String, String> n = xpce.getMatchingXpathFromPage(
								f, x_entry.getValue());
//						Map<String, String> n = xpce.getMatchingXpathFromCacheUsageMap(
//								f.getName(), x_entry.getValue());
						l4j.debug("******* page " + f.getAbsolutePath());

						Set<String> r = new HashSet<String>();

						if (n.isEmpty()) {
							l4j.warn("no results for page " + f.getName());
						}

						//just printouts
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
					}
					if (res.isEmpty()) {
						// TODO scan all sorted_map to find the winning xpath
						l4j.warn("no results for " + folder);
					} else {
						//					System.out.println(res);
						xpce.printResutls(res, resultsFolder + File.separator
								+ resName + "-" + propertyName + ".txt");
					}

				} else {
					l4j.debug("******* NOT a DIR " + folder.getAbsolutePath());
				}

			}
//		}


	}
	
	
private static double calculateStdev(Collection<Double>data){
		double n    = 0;
	    double sum1 = 0;
	    double sum2 = 0;
	 
	    for (double x : data){
	        n    = n + 1;
	        sum1 = sum1 + x;}
	 
	    double mean = (double) sum1/n;
	 
	    for (double x : data){
	        sum2 =  sum2 + (x - mean)*(x - mean);}
	 
	    double variance = sum2/(n - 1);
	    
	    return Math.sqrt(variance);
}


private static double calculateAvg(Collection<Double>data){
    double sum1 = 0;
 
    for (double x : data){
        sum1 = sum1 + x;}
 
    double mean = (double) sum1/data.size();
     
    return mean;
}

private static double calculateMin(Collection<Double>data){
    double min = data.iterator().next();
 
    for (double x : data){
    	if (x<min)
       min = x;}
      
    return min;
}

private static double calculateMax(Collection<Double>data){
    double max = 0;
 
    for (double x : data){
    	if (x>max)
       max = x;}
      
    return max;
}

}
