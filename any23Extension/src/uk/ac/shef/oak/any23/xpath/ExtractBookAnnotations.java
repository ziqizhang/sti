package uk.ac.shef.oak.any23.xpath;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sindice.result.SearchResult;
import com.sindice.result.SearchResults;

import uk.ac.shef.dcs.oak.triplesearch.Triple;
import uk.ac.shef.dcs.oak.triplesearch.sindiceapi.SindiceAPIProxy;
import uk.ac.shef.dcs.oak.triplesearch.sindiceapi.TripleExtractor;
import uk.ac.shef.dcs.oak.triplesearch.sindiceapi.TripleExtractorSindiceCacheAPI;
import uk.ac.shef.dcs.oak.util.FileUtils;
import uk.ac.shef.oak.any23.extension.extractor.LTriple;


/**
 * @author Anna Lisa Gentile (a.l.gentile@dcs.shef.ac.uk)
 * this class is to build a Gazeteer using Sindice
 *
 *
 */


public class ExtractBookAnnotations {



	public ExtractBookAnnotations(String queryFile, String resultFile) {
		super();
		this.queryFile = queryFile;
		this.resultFile = resultFile;
		this.maxNumberOfSindiceResultPages=100;

	}
	public ExtractBookAnnotations(String queryFile, String resultFile, int maxNumberOfSindiceResultPages) {
		super();
		this.queryFile = queryFile;
		this.resultFile = resultFile;
		this.maxNumberOfSindiceResultPages=maxNumberOfSindiceResultPages;

	}
	
	
	ExtractXpath exp = new ExtractXpath();
    private String queryFile;
    private String resultFile;
    private int maxNumberOfSindiceResultPages;

	public Set<String> getHtmlBYGuruQuery(String queryFile, int maxNumberOfSindiceResultPages) throws IOException{  	
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
        System.out.println("Benchmarking started at " + begin + ", total=" + lines.size());
        System.out.println("----------");
        int count = 0;
        int totalToProcess=-1;
        for (String l : lines) {
            count++;
            String[] queryParams = FileUtils.splitCSV(l);

            try {
                Date perQueryStart = new Date();
                System.out.println(count + " - New query @ " + perQueryStart);
                SearchResults rs = sindice.search(queryParams[0],
                        queryParams[1],
                        queryParams[2]);
                System.out.println("\t- query ends @" + new Date() + " total: " + rs.getTotalResults());
                totalToProcess=rs.getTotalResults();

                System.out.println("\t- process each page for " + pagesToProcess + " total pages max. @ " + new Date());
                
                int currentPage = 1;
                while (currentPage <= pagesToProcess && rs != null && rs.size() != 0) {
                    Iterator<SearchResult> it = rs.iterator();
                    while (it.hasNext()) {
                        SearchResult r = it.next();
                        try {
//                            sindice.retrieveDocument(r);
                            htmlPages.add(r.getLink());
                        } catch (Exception e) {
                            System.err.println("Cannot open url, skipped:" + r.getLink());
                        }
                        //TODO substitute with log
                        System.out.print("+");

                    }
//                    System.out.println();
                    currentPage++;
                    rs = rs.nextPage();
                }
//                Date perQueryEnd = new Date();
//                System.out.println("\t- process ends @ " + perQueryEnd);
//                p.println("\"" + l + "\"," + (perQueryStart.getTime() - perQueryEnd.getTime())+","+totalToProcess);
            } catch (Exception e) {
                System.err.println("Exception, skipped");
                e.printStackTrace();
            }


        }

//        Date end = new Date();
//        System.out.println("----------");
//        System.out.println("Completed at " + end);
//        System.out.println("Total time taken: " + (end.getTime() - begin.getTime()) + " msec");
//        System.out.println("Average per query: " + (end.getTime() - begin.getTime()) / lines.size() + " msec");
		return htmlPages;
    }
	
public Set<String>  collectTitles() throws IOException{
	
	Set<String> html = new HashSet<String>();
	// `TODO get results from SINDICE instead
//	html.add("http://www.goodreads.com/book/show/24583.The_Adventures_of_Tom_Sawyer");
//	html.add("http://www.goodreads.com/book/show/8296.The_First_Four_Years");
//	html.add("http://www.goodreads.com/book/show/157993.The_Little_Prince");
//	html.add("http://www.goodreads.com/book/show/23919.Complete_Stories_and_Poems");
	
	
	html = this.getHtmlBYGuruQuery(this.queryFile, this.maxNumberOfSindiceResultPages);
//	html.add("http://www.goodreads.com/book/show/24583.The_Adventures_of_Tom_Sawyer");
//	html.add("http://www.goodreads.com/book/show/8296.The_First_Four_Years");
//	html.add("http://www.goodreads.com/book/show/157993.The_Little_Prince");
//	html.add("http://www.goodreads.com/book/show/23919.Complete_Stories_and_Poems");
//	System.out.println(html);
	
	
//	html.add("");
//	html.add("");
//	html.add("");
//	html.add("");
//	html.add("");
//	html.add("");

	Set<String>  titles = new HashSet<String>();

    for (String h : html){
    	try{
    List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage(h);
	for(LTriple s :tripleXp){

		try {

			if (s.getpXPath()!=null/*&(s.getpXPath().contains("@itemprop"))*/){
			
				String p =s.getTriple().getPredicate().toString();
					
//				if ((s.getpXPath().contains("@itemprop"))){
				if (isTitle(p)){
				titles.add(s.getTriple().getObject().stringValue());

				}
			}


		} catch (Exception e) {
			System.out.println("trouble with triple "+s);
		}

}
    
    } catch (Exception e) {
      System.err.println("Cannot open url, skipped:" + h);
  }    	}
    return titles;

}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws  
	 */
	public static void main(String[] args) throws IOException {
		ExtractBookAnnotations eba;
		//args[2] if present is the max number of results
		if (args[2]!=null){
			int max = Integer.valueOf(args[2]);
			 eba = new ExtractBookAnnotations(args[0], args[1], max);
		}else{
			//args[0] String queryFile
			//args[1] String resultFile
			eba = new ExtractBookAnnotations(args[0], args[1]);
		}
		
		Set<String> t = eba.collectTitles();
		
        PrintWriter p = new PrintWriter(eba.resultFile);

		for (String ti:t){
			System.out.println(ti);
		p.println(ti);}
	    p.close();
//		
		
		
//		Set<String> links = eba. getHtmlBYGuruQuery(args[0], 1, ".\temp\res.txt");
//		System.out.println(links);
		
		/*
			      ExtractXpath exp = new ExtractXpath();

			      System.out.println("******* example of usage for extracting xpath *******");
//			      List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage("http://www.goodreads.com/book/show/397454.Cinnamon");
//			      List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage("http://www.goodreads.com/book/show/8252.Farmer_Boy");
			      
			      
			      List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage("http://www.goodreads.com/book/show/24583.The_Adventures_of_Tom_Sawyer");

			      
//			      List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage("http://www.kirkusreviews.com/book-reviews/nancy-k-wallace/christmas-cats/");
			      
//			      List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage("http://www.ecampus.com/half-life-darin-strauss/bk/9781934781708");
//			      List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage("http://www.goodreads.com/book/show/660523.Secrets_of_the_Morning");
//			      List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage("http://etd.ohiolink.edu/view.cgi?acc_num=osu1310663396");

			      
			      Set<String> properties = new HashSet<String>();
			      Set<String> objects = new HashSet<String>();

		
	
			      Set<String> xp = new HashSet<String>();

					for(LTriple s :tripleXp){

						try {

							if (s.getpXPath()!=null/*&(s.getpXPath().contains("@itemprop"))*//*){
							
								String p =s.getTriple().getPredicate().toString();
									
//								if ((s.getpXPath().contains("@itemprop"))){
								if (isSimliar(p)){
								properties.add(p);
								objects.add(s.getTriple().getObject().stringValue());

								}
							}

							System.out.println("*********************************************");
							System.out.println(s.getTriple().getSubject()+ " "+s.getTriple().getPredicate()+ " "+s.getTriple().getObject());

//							System.out.println(s.getTriple().getSubject()+ " "+s.getsXPath());
//							System.out.println(s.getTriple().getPredicate()+ " "+s.getpXPath());
//							System.out.println(s.getTriple().getObject()+ " "+s.getoXPath());
//					          ExtractXpath.printX(doc, seedNodes);

						} catch (Exception e) {
							System.out.println("trouble with xpath in "+s);
						}

		}
					System.out.println("***************************************************");
					for (String p:properties){
					System.out.println(p);}
					System.out.println("***************************************************");
					for (String p:objects){
					System.out.println(" ***"+p);}
          
	*/}

	private static boolean isSimliar(String p) {
		/*
			http://schema.org/Book/inLanguage
			http://schema.org/Book/bookFormatType
			http://schema.org/Book/image
			http://www.w3.org/1999/xhtml/microdata#item
			http://schema.org/Review/publishDate
			http://schema.org/Book/url
			http://schema.org/Book/reviews
			http://schema.org/Review/url
			http://schema.org/Book/numberOfPages
			http://www.w3.org/1999/02/22-rdf-syntax-ns#type
			http://schema.org/Book/author
			http://schema.org/Book/name
			http://schema.org/Book/isbn
			*/
//return true;
//if (p.equalsIgnoreCase("http://schema.org/Book/name"))		return true;
if (p.equalsIgnoreCase("http://opengraphprotocol.org/schema/title"))		return true;

return false;
	}
	
	private static boolean isTitle(String p) {

if (p.equalsIgnoreCase("http://opengraphprotocol.org/schema/title"))		return true;

return false;
	}
	
}
