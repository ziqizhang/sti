package uk.ac.shef.oak.gazetteers;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Logger;
import uk.ac.shef.oak.xpathExperiment.XpathCandidateExtractor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author annalisa
 * 
 */
public abstract class GenerateGazeteer {
	protected static Logger l4j = Logger.getLogger(XpathCandidateExtractor.class);


	// static String PREFIX = "PREFIX dbpprop: <http://dbpedia.org/property/> "
	// + '\n' + "PREFIX dbpedia: <http://dbpedia.org/resource/> " + '\n'
	// + "PREFIX dbpedia-owl: <http://dbpedia.org/> " + '\n'
	// + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";

	static String PREFIX = "PREFIX dbpprop: <http://dbpedia.org/property/> \n"
			+ "PREFIX dbpedia: <http://dbpedia.org/resource/> \n"
			+ "PREFIX dbpedia-owl: <http://dbpedia.org/> \n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";

//	 static String SERVICE = "http://dbpedia.org/sparql";
	 static String SERVICE = "http://sparql.sindice.com/sparql";
//	static String SERVICE = "http://lod.openlinksw.com/sparql";
	// static String SERVICE = "http://sparql.sindice.com/sparql";
	//static String SERVICE = "http://lod.openlinksw.com/sparql";


	public static String getSERVICE() {
		return SERVICE;
	}

	public static void setSERVICE(String sERVICE) {
		SERVICE = sERVICE;
	}

	public static String getPREFIX() {
		return PREFIX;
	}

	public static void setPREFIX(String pREFIX) {
		PREFIX = pREFIX;
	}

	/**
	 * 
	 * @param service
	 * @param sparqlQueryString
	 * @param solutionConcept
	 *            the target concept to extract from the query, tye name should
	 *            be the same as the name of the target variable in
	 *            sparqlQueryString
	 * @return the set of string from literal results
	 */
	public Set<String> queryService(String service, String sparqlQueryString,
			String solutionConcept) {
		Set<String> resulStrings = new HashSet<String>();

		Query query = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(service,
				query);

		try {
			ResultSet results = qexec.execSelect();

			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();

				Literal l;
				try {
					if (soln.get(solutionConcept).isLiteral()){
					l = soln.getLiteral(solutionConcept);
					resulStrings.add(l.getString());}else{
						RDFNode u = soln.get(solutionConcept);
						resulStrings.add(u.toString());
					}
				} catch (Exception e) {
					l4j.debug("Escaped result. Can't parse "+soln);
					e.printStackTrace();
				}
			}

		} finally {
			qexec.close();
		}
		return resulStrings;
	}

	private static String matchesPattern(Pattern p, String sentence) {
		Matcher m = p.matcher(sentence);

		if (m.find()) {
			return m.group();
		}

		return null;
	}

	public static String alphNumString(String name, int minLength, boolean moreThanOneWord) {

		String cleanName = "";
		Pattern alphaNum = Pattern.compile("[a-zA-Z0-9]");


		if(name.length()>=minLength){
		if (matchesPattern(alphaNum, name) != null) {
			name.trim();
			cleanName = name;
		}}
		if (moreThanOneWord){
			if (cleanName.split(" ").length>1){
				cleanName = "";
			}
		}
		return cleanName;

	}

	public static Set<String> cleanStringGazeteer(Set<String> gaz, int minLength, boolean moreThanOneWord ){
		Set<String> cleanGazt = new HashSet<String> ();
		
		for (String g:gaz){
			String cg =  TextOperations.normalizeString(g);
			cg = alphNumString(g, minLength, moreThanOneWord);
			if (!cg.equals("")){
				cleanGazt.add(cg);
			}
		}
		return cleanGazt;
		
	}
	
	public static Set<String> cleanIsbnGazeteer(Set<String> gaz ){
		Set<String> cleanGazt = new HashSet<String> ();
		
		for (String g:gaz){
					g=TextOperations.normalizeString(g);
					String cg =  "";
					if (g.length()>=13)
						cg=g;
			if (!cg.equals("")){
				cleanGazt.add(cg);
			}
		}
		
		return cleanGazt;
		
	}
	
	
	public static Set<String> cleanUrlGazeteer(Set<String> gaz){
		Set<String> cleanGazt = new HashSet<String> ();
		
		for (String g:gaz){
			String cg =  g.trim().replaceAll("\\s+", " ");

			
			try {
				URL url = new URL( cg );
				if (url!=null){
					cleanGazt.add(url.toString());
				}
			} catch (MalformedURLException e) {
				if(!cg.startsWith("http://")){
					cg = "http://"+cg;
					try {
						URL url = new URL( cg );
						if (url!=null){
							cleanGazt.add(url.toString());
						}
					} catch (MalformedURLException ex) {
						l4j.debug( g+ " is not URL");

					}
				}

			}


		}
		
		return cleanGazt;
		
	}
	
	
	public static void logStart(String className){
		l4j.info(className+ " started");

	}
	
	public static void checkService(String service) {
		String query = "ASK { }";
		QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
		try {
			if (qe.execAsk()) {
				l4j.info(service + " is UP");
			}
		} catch (QueryExceptionHTTP e) {
			l4j.info(service + " is DOWN");
		} finally {
			qe.close();
		} // end try/catch/finally
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
