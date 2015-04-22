package uk.ac.shef.oak.any23.xpath;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.any23.extractor.ExtractionException;
import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.apache.any23.http.HTTPClient;
import org.apache.any23.source.DocumentSource;
import org.apache.any23.source.HTTPDocumentSource;
import org.apache.any23.writer.TripleHandlerException;
import org.w3c.dom.Document;

import uk.ac.shef.oak.any23.extension.extractor.LAny23;
import uk.ac.shef.oak.any23.extension.extractor.LNTripleWriter;
import uk.ac.shef.oak.any23.extension.extractor.LTriple;


/**
 * @author Anna Lisa Gentile (a.l.gentile@dcs.shef.ac.uk)
 *
 */
public class WrapRottenTomatoes {

	

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		System.out.println("******* example of usage for extracting xpath *******");


			ExtractXpath exp = new ExtractXpath();
		      Set<String> xpTEST = new HashSet<String>();

		      xpTEST.add("/HTML[1]/BODY[1]/DIV[9]/DIV[2]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/DIV[7]/DIV[1]/P[3]/SPAN[1]/A[1]/SPAN[1]/text()");
		      
		      List <Map<String,String>> xpath = new ArrayList <Map<String,String>>();
		     
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/godfather/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/men_in_black_iii/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/silver_linings_playbook/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/end_of_watch/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/gambit_2012/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/the_host_2013/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/argo_2012/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/hotel_transylvania/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/madagascar_3_europes_most_wanted_2012/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/escape_from_planet_earth_3d/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/warm_bodies/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/the_twilight_saga_breaking_dawn_part_2/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/skyfall/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/madagascar_3_europes_most_wanted_2012/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/jab_tak_hai_jaan/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/men_in_black_iii/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/marvels_the_avengers/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/snow_white_and_the_huntsman/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/dark-shadows-2010/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/flight_2012/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/wreck_it_ralph/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/lincoln_2011/", xpTEST));
		      xpath.add (exp.findXpathOnHtmlPage("http://www.rottentomatoes.com/m/cloud_atlas_2012/", xpTEST));


	
		      

				
		      for(Map<String,String> obtainedInfoFromXpath : xpath){
			for(  Entry<String, String> s :obtainedInfoFromXpath.entrySet()){
				System.out.println(s.getKey()+"\t" +s.getValue());

}
	}




          
          
	}

}
