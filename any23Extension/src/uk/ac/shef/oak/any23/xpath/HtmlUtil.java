package uk.ac.shef.oak.any23.xpath;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import org.apache.any23.extractor.html.TagSoupParser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import de.l3s.boilerpipe.extractors.KeepEverythingExtractor;
import de.l3s.boilerpipe.extractors.KeepEverythingWithMinKWordsExtractor;
import de.l3s.boilerpipe.extractors.LargestContentExtractor;
import de.l3s.boilerpipe.extractors.NumWordsRulesExtractor;

/**
 * @author annalisa
 *
 * this class contains methods that I'll eventually move in HtmlDocument class
 */
public class HtmlUtil {
	
//	private static final ThreadLocal<Parser> parser = new ThreadLocal<Parser>() {
//	    protected Parser initialValue() {
//	        return new Parser();
//	    }
//	};
	

	    public static String extractText(String html) throws IOException,
	            SAXException {
	        Parser p = new Parser();
	        Handler h = new Handler();
	        p.setContentHandler(h);
	        p.parse(new InputSource(new StringReader(html)));
	        return h.getText();
	    }
	    
	    
public String readHtmlContentFromUrl(String url){
	
	String html = "";
			
	HttpClient client = new HttpClient();
	HttpMethod method = new GetMethod(url);
	 
	try {
	 client.executeMethod(method);
	 
	 byte[] responseBody = method.getResponseBody();
	 
	 html = new String(responseBody);
	 
	} catch (Exception e) {
	 
	 e.printStackTrace();
	 
	} finally {
	 
	 method.releaseConnection();
	}
	return html;
}
		


public String getTextFromHtmlPage(String htmlContent){

	String txtContent = "";


	try {
		KeepEverythingWithMinKWordsExtractor kemw = new KeepEverythingWithMinKWordsExtractor(2);

		txtContent=kemw.getText(htmlContent);
	} catch (BoilerpipeProcessingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	

return txtContent;

}

		/**
		 * @param args
		 * @throws BoilerpipeProcessingException 
		 */
		public static void main(String[] args) throws BoilerpipeProcessingException {
			try {
				
				
				HtmlUtil hu = new HtmlUtil();
				
					String html = hu.readHtmlContentFromUrl("http://www.rottentomatoes.com/m/godfather_part_ii/");
					String txtFromHtml = hu.getTextFromHtmlPage("http://www.rottentomatoes.com/m/godfather_part_ii/");
				      System.out.println(txtFromHtml);

//					DefaultExtractor de = new DefaultExtractor();
//					System.out.println(de.getText(html));
//					 KeepEverythingExtractor ke = new  KeepEverythingExtractor();
//						System.out.println(KeepEverythingExtractor.INSTANCE.getText(html));
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");

						System.out.println(LargestContentExtractor.getInstance().getText(html));
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						
						KeepEverythingWithMinKWordsExtractor kemw = new KeepEverythingWithMinKWordsExtractor(2);
						System.out.println(kemw.getText(html));
						
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						System.out.println("**********************************************");
						
						System.out.println(NumWordsRulesExtractor.getInstance().getText(html));

//				TagSoupParser tsp = new TagSoupParser(input, "utf-8");
//				System.out.println(HtmlUtil.extractText(html));
					HtmlUtil.extractText(html);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}


	class Handler extends DefaultHandler {
	    private StringBuilder sb = new StringBuilder();
	    private boolean keep = true;
	    public void characters(char[] ch, int start, int length)
	            throws SAXException {
	        if (keep) {
	            sb.append(ch, start, length);
	        }
	    }
	    public String getText() {
	        return sb.toString();
	    }
	    public void startElement(String uri, String localName, String qName,
	            Attributes atts) throws SAXException {
	        if (localName.equalsIgnoreCase("script")) {
	            keep = false;
	        }
	    }
	    public void endElement(String uri, String localName, String qName)
	            throws SAXException {
	        keep = true;
	    }




}
