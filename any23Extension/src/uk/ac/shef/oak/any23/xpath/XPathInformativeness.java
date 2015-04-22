package uk.ac.shef.oak.any23.xpath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author annalisa
 *
 */
public class XPathInformativeness {

	private String pageUrl; //url of the page
	private String pageHtml; //html content of page
	private Map <String, Double> xpathInformativeness; //weight for each xpath in the page
	
	public XPathInformativeness(String pageUrl, String pageHtml,
		Map<String, Double> xpathInformativeness) {
		super();
		this.pageUrl = pageUrl;
		this.pageHtml = pageHtml;
		this.xpathInformativeness = xpathInformativeness;
	}

	public XPathInformativeness(String pageUrl, String pageHtml,
			Set<String> xpathInformativeness) {
			super();
			this.pageUrl = pageUrl;
			this.pageHtml = pageHtml;
			this.xpathInformativeness = new HashMap<String,Double>();
			for (String s: xpathInformativeness){
			this.xpathInformativeness.put(s, 0.0);
			}
		}
	
	
	public XPathInformativeness(String pageUrl, String pageHtml) {
			super();
			this.pageUrl = pageUrl;
			this.pageHtml = pageHtml;
			
			Set<String> xpathInformativeness = new HashSet<String>();
			//TODO get xpath from page
			
			this.xpathInformativeness = new HashMap<String,Double>();
			for (String s: xpathInformativeness){
			this.xpathInformativeness.put(s, 0.0);
			}
		}
	
	
	public Map<String, Double> weightXpathOnPage(){
		
		//TODO weight the xpath
		return this.xpathInformativeness;
		
	}
	
	
	
	public String getPageUrl() {
		return pageUrl;
	}

	public String getPageHtml() {
		return pageHtml;
	}

	public Map<String, Double> getXpathInformativeness() {
		return xpathInformativeness;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
