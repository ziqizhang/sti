package uk.ac.shef.dcs.oak.tables;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.shef.dcs.oak.triplesearch.TripleSearchException;
import uk.ac.shef.dcs.oak.triplesearch.sindiceapi.SindiceAPIProxy;
import uk.ac.shef.dcs.oak.util.FileUtils;
import uk.ac.shef.oak.any23.extension.extractor.LAny23;
import uk.ac.shef.oak.any23.extension.extractor.LTriple;
import uk.ac.shef.oak.any23.xpath.ExtractXpath;

import com.sindice.SindiceException;
import com.sindice.result.SearchResult;
import com.sindice.result.SearchResults;

/**
 * @author Anna Lisa Gentile (a.l.gentile@dcs.shef.ac.uk) this class is to build
 *         a Gazeteer using Sindice
 * 
 */

public class ExtractTableAnnotations {

	private static Logger l4j = Logger.getLogger(ExtractTableAnnotations.class);

	public Set<String> tables = new HashSet<String>();

	public ExtractTableAnnotations(String queryFile, String resultFile) {
		super();
		this.queryFile = queryFile;
		this.resultFile = resultFile;
		this.maxNumberOfSindiceResultPages = Integer.MAX_VALUE;

	}

	public ExtractTableAnnotations(String queryFile, String resultFile,
			int maxNumberOfSindiceResultPages) {
		super();
		this.queryFile = queryFile;
		this.resultFile = resultFile;
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
		l4j.info("Processing results started at " + begin );
		// System.out.println("----------");
		int count = 0;
		int totalToProcess = -1;
		for (String l : lines) {
			count++;
			String[] queryParams = FileUtils.splitCSV(l);

			try {
				Date perQueryStart = new Date();
				l4j.info(count + " - New query @ " + perQueryStart);
				SearchResults rs = sindice.search(queryParams[0],
						queryParams[1], queryParams[2]);
				totalToProcess = rs.getTotalResults();

				l4j.info("\t- query ends @" + new Date() + " total: "
						+ totalToProcess);
				if (totalToProcess < pagesToProcess)
					pagesToProcess = totalToProcess;

				l4j.info("\t- process each page for " + pagesToProcess
						+ " total pages max. @ " + new Date());

				int currentPage = 1;
				while (currentPage <= pagesToProcess && rs != null
						&& rs.size() != 0) {
					Iterator<SearchResult> it = rs.iterator();
					int res = 0;
					while (it.hasNext()) {
						SearchResult r = it.next();
						try {
							// sindice.retrieveDocument(r);
							htmlPages.add(r.getLink());
							res++;

						} catch (Exception e) {
							l4j.error("Cannot open url, skipped:" + r.getLink());
						}

					}
					l4j.trace("added " + res + " results from page "
							+ currentPage + " of " + pagesToProcess);
					currentPage++;
					rs = rs.nextPage();
				}
				// Date perQueryEnd = new Date();
				// System.out.println("\t- process ends @ " + perQueryEnd);
				// p.println("\"" + l + "\"," + (perQueryStart.getTime() -
				// perQueryEnd.getTime())+","+totalToProcess);
			} catch (Exception e) {
				l4j.error("Exception, skipped");
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
	private boolean triplesInPageContainTables(LAny23 runner, String h){
		boolean containT= false;

		
		try {
			List<LTriple> tripleXp = exp
					.getTriplesAndXpathFromPage(runner, h);
			for (LTriple s : tripleXp) {

				try {
					Set<String> xp = new HashSet<String>();

					if (s.getsXPath() != null) {
						xp.add(s.getsXPath().toLowerCase());
					}
					if (s.getpXPath() != null) {
						xp.add(s.getpXPath().toLowerCase());
					}
					if (s.getoXPath() != null) {
						xp.add(s.getoXPath().toLowerCase());
					}
					for (String x : xp) {
						if (x.contains("table")) {
							containT=true;
							break;
						}
						
					}


					if (containT)
						break;
				} catch (Exception e) {
					l4j.error("trouble with triple " + s);
					e.printStackTrace();
				}

			}

		} catch (Exception e) {
			l4j.error("Cannot open url, skipped:" + h);
		}
		
		
		return containT;
		
	}
	
	public Set<String> getPagesContaingTables(String queryFile,
			int maxNumberOfSindiceResultPages) throws IOException {


		LAny23 runner = new LAny23("lodie-html-rdfa11");
		runner.setHTTPUserAgent("test-user-agent");

		
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
		l4j.info("Checking for results conrtaing tables started at " + begin
				+ ", total=" + lines.size());
		int count = 0;
		int totalToProcess = -1;
		int totalPages = -1;

		for (String l : lines) {
			count++;
			String[] queryParams = FileUtils.splitCSV(l);

			try {
				Date perQueryStart = new Date();
				SearchResults rs = sindice.search(queryParams[0],
						queryParams[1], queryParams[2]);
				totalToProcess = rs.getTotalResults();
				totalPages = rs.getTotalResults()/rs.getResultsPerPage();

				if (totalPages < pagesToProcess)
					pagesToProcess = totalPages;

				l4j.info("Started processing: "+pagesToProcess+ " pages of " + totalPages
						+ " total pages max. @ " + new Date());

				int currentPage = rs.getCurrentPage();
				while (currentPage <= pagesToProcess && rs != null
						&& rs.size() != 0) {

					l4j.info("\t Processing page "+currentPage+" ** "+rs.getCurrentPage());
					rs.setCurrentPage(currentPage);
					
					Iterator<SearchResult> it = rs.iterator();
					int res = 0;
					while (it.hasNext()) {
						SearchResult r = it.next();
						try {
							String h = r.getLink();

							if (this.triplesInPageContainTables(runner, h)){

								tables.add(h);
								l4j.warn("contains table --> "
										+ h);
							}else{
								l4j.error("no table --> "
										+ h);
							}

							res++;

						} catch (Exception e) {
							l4j.error("Cannot open url, skipped:" + r.getLink());
						}

					}
					l4j.trace("processed " + res + " results from page "
							+ currentPage + " of " + pagesToProcess);
					
					try {
						rs = rs.nextPage();
						currentPage = rs.getCurrentPage();
					} catch (SindiceException e) {
						l4j.error("Exception occured at page number "+currentPage);
						currentPage++;
						e.printStackTrace();
					}
				}

			} catch (TripleSearchException e) {
				l4j.error("Exception processing Sindice search with param: "+queryParams[0]+" "+
						queryParams[1]+" "+ queryParams[2]);
				e.printStackTrace();
			}


		}

		return tables;

	}

	public Set<String> extractPagesWithTables() throws IOException {

		return (this.getPagesContaingTables(queryFile,
				maxNumberOfSindiceResultPages));
	}

	public Set<String> collectTables() throws IOException {

		Set<String> html = new HashSet<String>();
		// `TODO get results from SINDICE instead
		// html.add("http://www.goodreads.com/book/show/24583.The_Adventures_of_Tom_Sawyer");
		// html.add("http://www.goodreads.com/book/show/8296.The_First_Four_Years");
		// html.add("http://www.goodreads.com/book/show/157993.The_Little_Prince");
		// html.add("http://www.goodreads.com/book/show/23919.Complete_Stories_and_Poems");

		html = this.getHtmlBYGuruQuery(this.queryFile,
				this.maxNumberOfSindiceResultPages);
		// html.add("http://www.goodreads.com/book/show/24583.The_Adventures_of_Tom_Sawyer");
		// html.add("http://www.goodreads.com/book/show/8296.The_First_Four_Years");
		// html.add("http://www.goodreads.com/book/show/157993.The_Little_Prince");
		// html.add("http://www.goodreads.com/book/show/23919.Complete_Stories_and_Poems");
		// System.out.println(html);

		// html.add("");
		// html.add("");
		// html.add("");
		// html.add("");
		// html.add("");
		// html.add("");

		Set<String> tables = new HashSet<String>();

		for (String h : html) {
			try {
				List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage(h);
				for (LTriple s : tripleXp) {

					try {
						Set<String> xp = new HashSet<String>();

						if (s.getsXPath() != null) {
							xp.add(s.getsXPath().toLowerCase());
						}
						if (s.getpXPath() != null) {
							xp.add(s.getpXPath().toLowerCase());
						}
						if (s.getoXPath() != null) {
							xp.add(s.getoXPath().toLowerCase());
						}
						// if (xp.contains("table")){
						for (String x : xp) {

							if (x.contains("table")) {
								// if (x.contains("table")){

								// String p
								// =s.getTriple().getPredicate().toString();
								// tables.add(s.getTriple().getObject().stringValue());
								// tables.add(s.getsXPath()+" "+s.getTriple().getSubject());
								// tables.add(s.getpXPath()+" "+s.getTriple().getPredicate());
								// tables.add(s.getoXPath()+" "+s.getTriple().getObject());

								tables.add(h);
								l4j.warn("contains table --> " + h);
								break;
							}
						}

					} catch (Exception e) {
						l4j.error("trouble with triple " + s);
						e.printStackTrace();
					}

				}

			} catch (Exception e) {
				l4j.error("Cannot open url, skipped:" + h);
			}
		}
		return tables;

	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws
	 */
	public static void main(String[] args) throws IOException {
		ExtractTableAnnotations eba;
		// args[2] if present is the max number of results
		if (args[2] != null) {
			int max = Integer.valueOf(args[2]);
			eba = new ExtractTableAnnotations(args[0], args[1], max);
		} else {
			// args[0] String queryFile
			// args[1] String resultFile
			eba = new ExtractTableAnnotations(args[0], args[1]);
		}

		Set<String> t = eba.extractPagesWithTables();
		try {
			PrintWriter p = new PrintWriter(eba.resultFile);

			for (String ti : t) {
				l4j.info(ti);
				p.println(ti);
			}
			p.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean isSimliar(String p) {
		/*
		 * http://schema.org/Book/inLanguage
		 * http://schema.org/Book/bookFormatType http://schema.org/Book/image
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

	private static boolean isTitle(String p) {

		if (p.equalsIgnoreCase("http://opengraphprotocol.org/schema/title"))
			return true;

		return false;
	}

}
