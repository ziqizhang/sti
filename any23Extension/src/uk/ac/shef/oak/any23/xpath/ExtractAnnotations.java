package uk.ac.shef.oak.any23.xpath;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.ac.shef.dcs.oak.triplesearch.Triple;
import uk.ac.shef.dcs.oak.triplesearch.sindiceapi.SindiceAPIProxy;
import uk.ac.shef.dcs.oak.util.FileUtils;
import uk.ac.shef.oak.any23.extension.extractor.LTriple;

import com.sindice.result.SearchResult;
import com.sindice.result.SearchResults;

/**
 * @author Anna Lisa Gentile (a.l.gentile@dcs.shef.ac.uk) this class is to build
 *         a Gazeteer using Sindice
 * 
 * 
 */

public class ExtractAnnotations {

	private String targetProperty;

	public ExtractAnnotations(String queryFile, String resultFile, String targetProperty) {
		super();
		this.queryFile = queryFile;
		this.resultFile = resultFile;
		this.targetProperty = targetProperty;
		this.maxNumberOfSindiceResultPages = 100;
		

	}

	public ExtractAnnotations(String queryFile, String resultFile, String targetProperty,
			int maxNumberOfSindiceResultPages) {
		super();
		this.queryFile = queryFile;
		this.resultFile = resultFile;
		this.targetProperty = targetProperty;

		this.maxNumberOfSindiceResultPages = maxNumberOfSindiceResultPages;

	}

	ExtractXpath exp = new ExtractXpath();
	private String queryFile;
	private String resultFile;
	private int maxNumberOfSindiceResultPages;

	public Set<String> getHtmlBYGuruQuery(String queryFile,
			int maxNumberOfSindiceResultPages) throws IOException {
		Set<String> htmlPages = new HashSet<String>();

		List<String> lines = FileUtils.readList(queryFile, false);
		Iterator<String> lit = lines.iterator();
		while (lit.hasNext()) {
			String l = lit.next();
			if (l.startsWith("#"))
				lit.remove();
		}

		int pagesToProcess = maxNumberOfSindiceResultPages;
		SindiceAPIProxy sindice = new SindiceAPIProxy();

		Date begin = new Date();
		System.out.println("Benchmarking started at " + begin + ", total="
				+ lines.size());
		System.out.println("----------");
		int count = 0;
		int totalToProcess = -1;
		for (String l : lines) {
			count++;
			String[] queryParams = FileUtils.splitCSV(l);

			try {
				Date perQueryStart = new Date();
				System.out.println(count + " - New query @ " + perQueryStart);
				SearchResults rs = sindice.search(queryParams[0],
						queryParams[1], queryParams[2]);
				System.out.println("\t- query ends @" + new Date() + " total: "
						+ rs.getTotalResults());
				totalToProcess = rs.getTotalResults();

				System.out.println("\t- process each page for "
						+ pagesToProcess + " total pages max. @ " + new Date());

				int currentPage = 1;
				while (currentPage <= pagesToProcess && rs != null
						&& rs.size() != 0) {
					Iterator<SearchResult> it = rs.iterator();
					while (it.hasNext()) {
						SearchResult r = it.next();
						try {
							// sindice.retrieveDocument(r);
							htmlPages.add(r.getLink());
						} catch (Exception e) {
							System.err.println("Cannot open url, skipped:"
									+ r.getLink());
						}
						// TODO substitute with log
						System.out.print("+");

					}
					// System.out.println();
					currentPage++;
					rs = rs.nextPage();
				}
				// Date perQueryEnd = new Date();
				// System.out.println("\t- process ends @ " + perQueryEnd);
				// p.println("\"" + l + "\"," + (perQueryStart.getTime() -
				// perQueryEnd.getTime())+","+totalToProcess);
			} catch (Exception e) {
				System.err.println("Exception, skipped");
				e.printStackTrace();
			}

		}

		// Date end = new Date();
		// System.out.println("----------");
		// System.out.println("Completed at " + end);
		// System.out.println("Total time taken: " + (end.getTime() -
		// begin.getTime()) + " msec");
		// System.out.println("Average per query: " + (end.getTime() -
		// begin.getTime()) / lines.size() + " msec");
		return htmlPages;
	}

	public Set<String> collectTitles() throws IOException {

		Set<String> html = new HashSet<String>();

		html = this.getHtmlBYGuruQuery(this.queryFile,
				this.maxNumberOfSindiceResultPages);

		Set<String> titles = new HashSet<String>();

		for (String h : html) {
			List<Triple> tripleXp=null;
//			List<LTriple> tripleXp=null;

			try {
				tripleXp = exp.getTriplesFromWebPage(h);
//				tripleXp = exp.getTriplesAndXpathFromWebPage(h);
//				exp.getTriplesFromWebPage(h);
			} catch (Exception e) {
				System.err.println("Cannot open url, skipped:" + h);
			}
			
			if (tripleXp!=null){
				for (Triple s : tripleXp) {
//				for (LTriple s : tripleXp) {

					try {

//							String p = s.getTriple().getPredicate().toString();
//
//							System.out.println(s.getTriple().getSubject()
//									.stringValue()+"\t"+p+"\t"+s.getTriple().getObject()
//										.stringValue());
//							// if ((s.getpXPath().contains("@itemprop"))){
//							if (isTargetProperty(p)) {
//								titles.add(s.getTriple().getObject()
//										.stringValue());
//							}	

						String p = s.getPredicate().toString();

//						System.out.println(s.getSubject()
//								+"\t"+p+"\t"+s.getObject()
//									);
						// if ((s.getpXPath().contains("@itemprop"))){
						if (isTargetProperty(p)) {
							titles.add(s.getObject());
						}

					} catch (Exception e) {
						System.out.println("trouble with triple " + s);
					}

				}
			}
			
		}
		return titles;

	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws
	 */
	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		
		ExtractAnnotations eba;
		if (args[3] != null) {
			int max = Integer.valueOf(args[3]);
			eba = new ExtractAnnotations(args[0], args[1], args[2], max);
		} else {
			eba = new ExtractAnnotations(args[0], args[1], args[2]);
		}

		Set<String> t = eba.collectTitles();
		
		
		for (String ti : t) {
			System.out.println(ti);
		}
		
		

		PrintWriter p = new PrintWriter(eba.resultFile);

		for (String ti : t) {
			p.println(ti);
		}
		p.close();
		//

		// Set<String> links = eba. getHtmlBYGuruQuery(args[0], 1,
		// ".\temp\res.txt");
		// System.out.println(links);

		/*
		 * ExtractXpath exp = new ExtractXpath();
		 * 
		 * System.out.println(
		 * "******* example of usage for extracting xpath *******"); //
		 * List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage(
		 * "http://www.goodreads.com/book/show/397454.Cinnamon"); //
		 * List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage(
		 * "http://www.goodreads.com/book/show/8252.Farmer_Boy");
		 * 
		 * 
		 * List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage(
		 * "http://www.goodreads.com/book/show/24583.The_Adventures_of_Tom_Sawyer"
		 * );
		 * 
		 * 
		 * // List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage(
		 * "http://www.kirkusreviews.com/book-reviews/nancy-k-wallace/christmas-cats/"
		 * );
		 * 
		 * // List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage(
		 * "http://www.ecampus.com/half-life-darin-strauss/bk/9781934781708");
		 * // List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage(
		 * "http://www.goodreads.com/book/show/660523.Secrets_of_the_Morning");
		 * // List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage(
		 * "http://etd.ohiolink.edu/view.cgi?acc_num=osu1310663396");
		 * 
		 * 
		 * Set<String> properties = new HashSet<String>(); Set<String> objects =
		 * new HashSet<String>();
		 * 
		 * 
		 * 
		 * Set<String> xp = new HashSet<String>();
		 * 
		 * for(LTriple s :tripleXp){
		 * 
		 * try {
		 * 
		 * if (s.getpXPath()!=null/*&(s.getpXPath().contains("@itemprop"))
		 *//*
			 * ){
			 * 
			 * String p =s.getTriple().getPredicate().toString();
			 * 
			 * // if ((s.getpXPath().contains("@itemprop"))){ if (isSimliar(p)){
			 * properties.add(p);
			 * objects.add(s.getTriple().getObject().stringValue());
			 * 
			 * } }
			 * 
			 * System.out.println("*********************************************"
			 * ); System.out.println(s.getTriple().getSubject()+
			 * " "+s.getTriple().getPredicate()+ " "+s.getTriple().getObject());
			 * 
			 * // System.out.println(s.getTriple().getSubject()+
			 * " "+s.getsXPath()); //
			 * System.out.println(s.getTriple().getPredicate()+
			 * " "+s.getpXPath()); //
			 * System.out.println(s.getTriple().getObject()+ " "+s.getoXPath());
			 * // ExtractXpath.printX(doc, seedNodes);
			 * 
			 * } catch (Exception e) {
			 * System.out.println("trouble with xpath in "+s); }
			 * 
			 * } System.out.println(
			 * "***************************************************"); for
			 * (String p:properties){ System.out.println(p);}
			 * System.out.println(
			 * "***************************************************"); for
			 * (String p:objects){ System.out.println(" ***"+p);}
			 */
		
	
		long end = System.currentTimeMillis();
		long processTime = (end-start);
		
		System.out.println("processTime"+processTime);

	}

	private static boolean isSimliar(String p) {
		/*
		 * http://schema.org/Restaurant/name
		 * http://schema.org/Restaurant/servesCuisine
		 * http://www.w3.org/1999/xhtml/microdata#item
		 * http://schema.org/Review/publishDate http://schema.org/Book/url
		 * http://schema.org/Book/reviews http://schema.org/Review/url
		 * http://schema.org/Book/numberOfPages
		 * http://www.w3.org/1999/02/22-rdf-syntax-ns#type
		 * http://schema.org/Book/author http://schema.org/Book/name
		 * http://schema.org/Book/isbn
		 */
		// return true;
		// if (p.equalsIgnoreCase("http://schema.org/Book/name")) return true;
		
		
		
		if (p.equalsIgnoreCase("http://opengraphprotocol.org/schema/title"))
			return true;
		return false;
	}

	private boolean isTargetProperty(String p) {
		
//		if (p.equalsIgnoreCase("http://opengraphprotocol.org/schema/title")|p.equalsIgnoreCase("http://ogp.me/ns#title"))
//		if (p.equalsIgnoreCase("http://schema.org/Movie/name"))
//			if (p.equalsIgnoreCase("http://ogp.me/ns/fb/good_reads#isbn"))
				if (p.equalsIgnoreCase(this.targetProperty))

//			if (p.equals("sorg:Movie/name"))

//		if (p.equalsIgnoreCase("http://ogp.me/ns#title"))

			return true;
		return false;
	}

}
