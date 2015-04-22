package uk.ac.shef.oak.xpathExperiment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import uk.ac.shef.oak.any23.xpath.ExtractXpath;
import uk.ac.shef.oak.any23.xpath.HtmlDocument;
import uk.ac.shef.wit.ie.wrapper.html.xpath.DOMUtil;


public class TestXpathCandidate {
	
	
	//logging managment
	private static Logger l4j = Logger.getLogger(TestXpathCandidate.class);
//    static{
//	PropertyConfigurator.configure("./properties/log4j.properties");
//    }	

	String stringExampleFile;
	
	public TestXpathCandidate(String examples) {
		l4j.info("initializing XpathCandidateExtractor");
		this.stringExampleFile= examples;
		populateAnnotations();
	}

	
	
	private Set<String> annotations;
	
	
 
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

		//TODO remove this
//		this.annotations.add("The Forgotten Garden");
//		this.annotations.add("The Kite Runner [Hardcover]");
//		this.annotations.add("The Kite Runner");
//		this.annotations.add("Even Now (Lost Love Series #1)");
//		l4j.info("***Remove hard-coded annotations from code***");
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
	 * @return 
	 */
	
	
	public static Map<String,String> getMatchingXpathFromPage(File f, String xpath){
		Map<String,String> xp = new HashMap<String,String>();
		HtmlDocument h = new HtmlDocument(f);

			Document doc = h.getPageDom();
//			ExtractXpath exp = new ExtractXpath();
			try {
				 NodeList nodes = ExtractXpath.findXpathNodeOnHtmlPage(doc, xpath);
				 
				  for (int i = 0; i < nodes.getLength(); i++) {
						  String v = nodes.item(i).getNodeValue();
						  if (v!=null){
							  System.out.println(nodes.item(i).getAttributes());
//						  String xpn = DomUtils.getXPathForNode(nodes.item(i));
						  String xpn = DOMUtil.getXPath(nodes.item(i));

//						  l4j.info("matching node "+v+" "+xpn); 
						  
						  xp.put(xpn, v);

						  }
					  
					  }
			} catch (XPathExpressionException e) {
				l4j.error("problems with xpath "+xpath);
				e.printStackTrace();
			}

		
		return xp;

	}
	//TODO implement pruning
		private static Set<String> getAmazonPrunedCandidates(){
			Set<String> cand = new HashSet<String>();
			cand.add("/HTML[1]/BODY[1]/DIV[7]/H1[1]/SPAN[1]/text()[1]");//18.0, 
			cand.add("/HTML[1]/BODY[1]/TABLE[9]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//13.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/I[1]/text()[1]");//12.0
			cand.add("/HTML[1]/BODY[1]/DIV[7]/SPAN[1]/A[1]/text()[1]");//12.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//11.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//11.0
			cand.add("/HTML[1]/BODY[1]/TABLE[12]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//11.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//10.0
			cand.add("/HTML[1]/BODY[1]/TABLE[8]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//10.0
			cand.add("/HTML[1]/BODY[1]/DIV[5]/H1[1]/SPAN[1]/text()[1]");//10.0
			cand.add("/HTML[1]/BODY[1]/TABLE[6]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//10.0
			cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/B[1]/A[1]/text()[1]");//10.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//9.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//9.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//9.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//9.0
			cand.add("/HTML[1]/BODY[1]/TABLE[14]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//9.0
			cand.add("/HTML[1]/BODY[1]/TABLE[16]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//9.0
			cand.add("/HTML[1]/BODY[1]/TABLE[11]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//9.0
			cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/B[1]/A[1]/text()[1]");//9.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//8.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//8.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//8.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//8.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//8.0
			cand.add("/HTML[1]/BODY[1]/TABLE[15]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//8.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//8.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//8.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//8.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//8.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//8.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[2]/DIV[6]/H1[1]/SPAN[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/TABLE[13]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/I[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[8]/H1[1]/SPAN[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//7.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//6.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/TABLE[7]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/I[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[2]/DIV[7]/H1[1]/SPAN[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[5]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[8]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/TABLE[10]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//5.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/I[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/A[4]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/I[2]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/TABLE[7]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/I[2]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/I[2]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/P[1]/STRONG[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/I[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/I[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/I[2]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/TABLE[17]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//4.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/I[2]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/P[1]/STRONG[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[2]/DIV[7]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[9]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/EM[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/A[5]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/P[1]/I[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/P[1]/STRONG[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/P[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/TABLE[4]/TBODY[1]/TR[1]/TD[1]/DIV[1]/DIV[1]/SPAN[1]/I[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[2]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/P[1]/STRONG[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/SPAN[2]/A[1]/B[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/A[3]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[16]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[4]/text()[1]");//3.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[9]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[2]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[54]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[3]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[2]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/A[4]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/P[1]/STRONG[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[3]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[5]/A[5]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/FORM[1]/LABEL[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[1]/SPAN[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[3]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[2]/I[2]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[2]/LI[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[2]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[2]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[13]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[2]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[2]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/A[3]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[6]/H1[1]/SPAN[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[6]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[2]/LI[5]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[2]/LI[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[7]/A[4]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/P[1]/STRONG[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[2]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/I[2]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[2]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/FORM[1]/LABEL[18]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[15]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[20]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/I[2]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[2]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/A[3]/EM[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/EM[2]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/I[2]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[4]/H1[1]/SPAN[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/EM[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/DIV[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/I[5]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/UL[1]/LI[1]/SPAN[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[2]/LI[3]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[6]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[4]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[3]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[2]/LI[5]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/EM[2]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[5]/A[4]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[5]/H1[1]/SPAN[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/I[5]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/I[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[10]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[3]/SPAN[1]/STRONG[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/DIV[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[6]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[5]/A[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/UL[1]/LI[6]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/TABLE[12]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/text()[3]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/UL[2]/LI[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/P[5]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[4]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[18]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/I[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[1]/EM[7]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/text()[115]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[18]/A[5]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/P[1]/B[1]/A[2]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/TABLE[12]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/text()[5]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[7]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/TABLE[5]/TBODY[1]/TR[1]/TD[2]/DIV[2]/SPAN[2]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[2]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[5]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/P[3]/A[1]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/UL[1]/LI[7]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/TABLE[5]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[6]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[8]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[5]/DIV[2]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[14]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[2]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[8]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/B[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[8]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[26]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/P[1]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/B[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[5]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[7]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[18]/A[6]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/DIV[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/I[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[18]/A[5]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[10]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/A[8]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/I[9]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[5]/DIV[3]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/P[1]/STRONG[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[2]/LI[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[7]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/TABLE[3]/TBODY[1]/TR[4]/TD[2]/B[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[12]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/A[13]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/P[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[8]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[18]/A[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/A[9]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[56]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[2]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/B[3]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[5]/DIV[2]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/UL[1]/LI[6]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/P[6]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/ST1:STATE[1]/ST1:PLACE[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/UL[2]/LI[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[16]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/I[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[3]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[3]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[1]/FORM[1]/LABEL[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/UL[1]/LI[1]/SPAN[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[7]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/text()[131]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/UL[2]/LI[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[3]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[1]/FORM[1]/LABEL[23]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[10]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/A[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/B[8]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/ST1:STATE[1]/ST1:PLACE[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[19]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[4]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[13]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[33]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[4]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[5]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[3]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[5]/DIV[2]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[12]/I[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[5]/DIV[2]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[14]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[7]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/FORM[1]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/DIV[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/P[24]/I[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[7]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[6]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/P[4]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[8]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[54]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/I[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[2]/P[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[3]/A[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/TABLE[3]/TBODY[1]/TR[1]/TD[1]/DIV[1]/DIV[1]/SPAN[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[1]/P[1]/I[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[9]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/I[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[1]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[2]/DIV[6]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[1]/UL[1]/LI[3]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/A[7]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[34]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[6]/A[6]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[1]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/P[10]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[7]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[23]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[3]/A[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/P[1]/I[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[2]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[2]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[6]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[4]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/EM[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/ST1:STATE[2]/ST1:PLACE[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[7]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[16]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[5]/DIV[2]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/SPAN[1]/STRONG[1]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[12]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[6]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[8]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[5]/DIV[2]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[6]/H1[1]/SPAN[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/I[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[2]/A[7]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/UL[2]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[3]/A[1]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[2]/LI[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[5]/STRONG[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[15]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[20]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/B[8]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/DIV[2]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[7]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/P[6]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[7]/DIV[1]/DIV[4]/DIV[1]/DIV[1]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/FORM[1]/LABEL[13]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[1]/I[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/A[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/EM[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[1]/B[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[13]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/UL[1]/LI[5]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/text()[103]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/text()[164]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/I[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/SPAN[2]/STRONG[1]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[9]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[14]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[5]/DIV[2]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/P[8]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/P[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/P[1]/STRONG[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[7]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/DIV[2]/I[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[18]/A[6]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/I[6]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[5]/DIV[2]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[17]/A[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[8]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[3]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[2]/B[5]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[2]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[9]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[3]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/text()[167]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[17]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/text()[100]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/FORM[1]/LABEL[16]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[35]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[56]/DIV[1]/UL[1]/LI[10]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/I[8]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/I[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/I[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[13]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[18]/A[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/DIV[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[5]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[9]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/A[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/P[1]/B[1]/A[3]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[10]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[2]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[2]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[34]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[54]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/A[1]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[8]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/P[15]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[5]/DIV[3]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[6]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[18]/SPAN[1]/STRONG[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[6]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[1]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[8]/DIV[1]/DIV[1]/SPAN[2]/A[1]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[34]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[9]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[25]/I[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[2]/DIV[1]/SPAN[2]/B[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[5]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[6]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/P[2]/I[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/P[1]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[3]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[3]/A[6]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[1]/I[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[2]/LI[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[2]/LI[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[2]/P[4]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[54]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/I[1]/A[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[6]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[3]/STRONG[1]/FONT[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[34]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[2]/A[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[1]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/I[9]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[2]/LI[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[5]/DIV[2]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[2]/SPAN[2]/A[1]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[1]/B[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[2]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[35]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[7]/H1[1]/SPAN[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/STRONG[1]/A[8]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[2]/LI[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[17]/A[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/UL[1]/LI[2]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/ST1:STATE[2]/ST1:PLACE[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/A[3]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[5]/STRONG[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[5]/A[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/I[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[15]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[32]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[2]/STRONG[1]/FONT[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[18]/A[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/I[8]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/A[2]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/UL[2]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/SPAN[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[3]/STRONG[1]/FONT[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[35]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[8]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/I[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/I[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[16]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/EM[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[1]/FORM[1]/LABEL[16]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/text()[9]");//1.0
			cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[9]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/A[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/EM[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[18]/A[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/I[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/TABLE[15]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/text()[3]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[2]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[34]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/DIV[4]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[6]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[54]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[1]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[3]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/EM[7]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[6]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[2]/DIV[8]/DIV[1]/DIV[11]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[3]/I[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[4]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/B[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[7]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/P[8]/A[1]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[35]/DIV[5]/DIV[2]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/FORM[1]/LABEL[17]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[16]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[13]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[5]/DIV[2]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[1]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[2]/P[2]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[17]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[5]/DIV[2]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[7]/SPAN[1]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/DIV[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[2]/A[7]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[1]/FORM[1]/LABEL[14]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[1]/SPAN[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[2]/B[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/P[1]/B[1]/A[9]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/A[2]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[1]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[15]/A[1]/EM[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[8]/TD[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/DIV[1]/SPAN[2]/A[1]/B[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[5]/DIV[2]/UL[1]/LI[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[6]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[2]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/A[6]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[8]/I[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/UL[1]/LI[1]/DIV[1]/I[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/EM[2]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[17]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[8]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
			cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//1.0

			
			return cand;
			
		}
	
//TODO this is a quick fix, load candidates from txt file
	private static Set<String> getAmazonCandidates(){
		Set<String> cand = new HashSet<String>();

	//working one
		
cand.add("HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[1]/H1[1]/SPAN[1]/text()[1]");//correct one

cand.add("/HTML[1]/BODY[1]/DIV[7]/H1[1]/SPAN[1]/text()[1]");//18.0
cand.add("/HTML[1]/BODY[1]/TABLE[9]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//13.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/I[1]/text()[1]");//12.0
cand.add("/HTML[1]/BODY[1]/DIV[7]/SPAN[1]/A[1]/text()[1]");//12.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//11.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//11.0
cand.add("/HTML[1]/BODY[1]/TABLE[12]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//11.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//10.0
cand.add("/HTML[1]/BODY[1]/TABLE[8]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//10.0
cand.add("/HTML[1]/BODY[1]/DIV[5]/H1[1]/SPAN[1]/text()[1]");//10.0
cand.add("/HTML[1]/BODY[1]/TABLE[6]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//10.0
cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/B[1]/A[1]/text()[1]");//10.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//9.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//9.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//9.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//9.0
cand.add("/HTML[1]/BODY[1]/TABLE[14]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//9.0
cand.add("/HTML[1]/BODY[1]/TABLE[16]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//9.0
cand.add("/HTML[1]/BODY[1]/TABLE[11]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//9.0
cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/B[1]/A[1]/text()[1]");//9.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//8.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//8.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//8.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//8.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//8.0
cand.add("/HTML[1]/BODY[1]/TABLE[15]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//8.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//8.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//8.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//8.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//8.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//8.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[2]/DIV[6]/H1[1]/SPAN[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/TABLE[13]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/I[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[8]/H1[1]/SPAN[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//7.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//6.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/TABLE[7]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/I[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[2]/DIV[7]/H1[1]/SPAN[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[5]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[8]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/TABLE[10]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//5.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/I[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/A[4]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/I[2]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/TABLE[7]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/I[2]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/I[2]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/P[1]/STRONG[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/I[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/I[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/I[2]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/TABLE[17]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//4.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/I[2]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/P[1]/STRONG[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[2]/DIV[7]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[9]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/EM[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/A[5]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/P[1]/I[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/P[1]/STRONG[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/P[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/TABLE[4]/TBODY[1]/TR[1]/TD[1]/DIV[1]/DIV[1]/SPAN[1]/I[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[2]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/P[1]/STRONG[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/SPAN[2]/A[1]/B[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/A[3]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[16]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[4]/text()[1]");//3.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[9]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[2]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[54]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[3]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[2]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/A[4]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/P[1]/STRONG[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[3]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[5]/A[5]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/FORM[1]/LABEL[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[1]/SPAN[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[3]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[2]/I[2]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[2]/LI[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[2]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[2]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[13]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[2]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[2]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/A[3]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[6]/H1[1]/SPAN[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[6]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[2]/LI[5]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[2]/LI[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[7]/A[4]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/P[1]/STRONG[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[2]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/I[2]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[2]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/FORM[1]/LABEL[18]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[15]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[20]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/I[2]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[2]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/A[3]/EM[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/EM[2]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/I[2]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[4]/H1[1]/SPAN[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/EM[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/DIV[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/I[5]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/UL[1]/LI[1]/SPAN[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[2]/LI[3]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[6]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[4]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[3]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[2]/LI[5]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/EM[2]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[5]/A[4]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[5]/H1[1]/SPAN[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/I[5]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/I[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//2.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[10]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[3]/SPAN[1]/STRONG[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/DIV[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[6]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[5]/A[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/UL[1]/LI[6]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/TABLE[12]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/text()[3]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/UL[2]/LI[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/P[5]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[4]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[18]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/I[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[1]/EM[7]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[35]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/text()[115]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[18]/A[5]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/P[1]/B[1]/A[2]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/TABLE[12]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/text()[5]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[7]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/TABLE[5]/TBODY[1]/TR[1]/TD[2]/DIV[2]/SPAN[2]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[2]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[5]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/P[3]/A[1]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[32]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/UL[1]/LI[7]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/TABLE[5]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[6]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[8]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[5]/DIV[2]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[14]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[2]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[8]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/B[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[8]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[3]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[26]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/P[1]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/B[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[5]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[7]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[18]/A[6]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/DIV[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/I[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[18]/A[5]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[10]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/A[8]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/I[9]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[5]/DIV[3]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/P[1]/STRONG[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[2]/LI[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[7]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/TABLE[3]/TBODY[1]/TR[4]/TD[2]/B[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[12]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/A[13]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/P[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[8]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[18]/A[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/A[9]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[56]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[2]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/B[3]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[5]/DIV[2]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/UL[1]/LI[6]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/P[6]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/ST1:STATE[1]/ST1:PLACE[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/UL[2]/LI[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[16]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/I[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[3]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[3]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[1]/FORM[1]/LABEL[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/UL[1]/LI[1]/SPAN[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[7]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/text()[131]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/UL[2]/LI[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[3]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[1]/FORM[1]/LABEL[23]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[10]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/A[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/B[8]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/ST1:STATE[1]/ST1:PLACE[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[19]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[4]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[13]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[4]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[5]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[3]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[5]/DIV[2]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[12]/I[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[5]/DIV[2]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[14]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[7]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/FORM[1]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/DIV[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/P[24]/I[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[7]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[6]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/P[4]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[8]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[54]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/I[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[2]/P[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[3]/A[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/TABLE[3]/TBODY[1]/TR[1]/TD[1]/DIV[1]/DIV[1]/SPAN[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[1]/P[1]/I[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[9]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/I[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[1]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[2]/DIV[6]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[1]/UL[1]/LI[3]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/A[7]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[34]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[6]/A[6]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[1]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/P[10]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[7]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[23]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[3]/A[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/P[1]/I[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[2]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[2]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[6]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[4]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/EM[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/ST1:STATE[2]/ST1:PLACE[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[7]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[16]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[5]/DIV[2]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/SPAN[1]/STRONG[1]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[12]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[6]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[8]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[1]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[5]/DIV[2]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[6]/H1[1]/SPAN[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/I[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[2]/A[7]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/UL[2]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[3]/A[1]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[2]/LI[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[5]/STRONG[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[15]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[20]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/B[8]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/DIV[2]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[3]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[1]/DIV[1]/DIV[4]/A[1]/SPAN[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[7]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/P[6]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[7]/DIV[1]/DIV[4]/DIV[1]/DIV[1]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/FORM[1]/LABEL[13]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[1]/I[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/A[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/EM[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[1]/B[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[13]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/UL[1]/LI[5]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/text()[103]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/text()[164]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/I[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[1]/DIV[1]/DIV[3]/A[1]/SPAN[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/SPAN[2]/STRONG[1]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[2]/TABLE[1]/TBODY[1]/TR[9]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/DIV[3]/SPAN[2]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[6]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[14]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[5]/DIV[2]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/P[8]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/P[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/P[1]/STRONG[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[7]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/DIV[2]/I[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[18]/A[6]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/I[6]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[5]/DIV[2]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[17]/A[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[8]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[3]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[2]/B[5]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[2]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[9]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[3]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/text()[167]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[17]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/text()[100]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/FORM[1]/LABEL[16]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[35]/DIV[1]/FORM[1]/LABEL[10]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[56]/DIV[1]/UL[1]/LI[10]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/I[8]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/FORM[1]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/I[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/I[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[13]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[18]/A[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/DIV[1]/DIV[1]/SPAN[2]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/FORM[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[5]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[9]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/A[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/P[1]/B[1]/A[3]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[10]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[2]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[2]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[34]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[54]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/A[1]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[3]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[8]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/P[15]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[5]/DIV[3]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[6]/DIV[1]/DIV[1]/UL[1]/LI[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[18]/SPAN[1]/STRONG[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[6]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[1]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[8]/DIV[1]/DIV[1]/SPAN[2]/A[1]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[55]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[34]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[9]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[25]/I[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[2]/DIV[1]/SPAN[2]/B[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[5]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[6]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/FORM[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/P[2]/I[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/P[1]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[3]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[3]/A[6]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[1]/I[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/FORM[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/UL[2]/LI[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/UL[2]/LI[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[2]/P[4]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[54]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/I[1]/A[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[6]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[7]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[3]/STRONG[1]/FONT[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[34]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/I[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[2]/A[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[1]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[13]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/I[9]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/UL[2]/LI[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[5]/DIV[2]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/UL[1]/LI[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[2]/SPAN[2]/A[1]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[1]/B[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[2]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[35]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[7]/H1[1]/SPAN[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/STRONG[1]/A[8]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[2]/LI[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[17]/A[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/UL[1]/LI[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/UL[1]/LI[2]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/ST1:STATE[2]/ST1:PLACE[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/A[3]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[5]/STRONG[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[37]/DIV[1]/FORM[1]/LABEL[9]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[5]/A[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[4]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/I[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[15]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[32]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[2]/STRONG[1]/FONT[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[18]/A[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[7]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[8]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/DIV[6]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[2]/I[8]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/A[2]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[33]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/UL[2]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[51]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/SPAN[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[3]/STRONG[1]/FONT[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[35]/DIV[2]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[5]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[8]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[4]/TD[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/I[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[4]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/I[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[16]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/UL[1]/LI[6]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/EM[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[1]/FORM[1]/LABEL[16]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/text()[9]");//1.0
cand.add("/HTML[1]/BODY[1]/LI[1]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/P[9]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/A[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/EM[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[18]/A[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/UL[2]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[2]/I[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/TABLE[15]/TBODY[1]/TR[1]/TD[1]/A[1]/DIV[1]/text()[3]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[2]/TABLE[1]/TBODY[1]/TR[6]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/DIV[1]/DIV[2]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[39]/DIV[3]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[1]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[34]/DIV[1]/UL[1]/LI[5]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/DIV[1]/DIV[6]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/DIV[4]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[6]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[5]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/FORM[1]/LABEL[11]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/UL[1]/LI[4]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[54]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[1]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/P[3]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/EM[7]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/TABLE[1]/TBODY[1]/TR[3]/TD[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[8]/TD[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[6]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[26]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[25]/DIV[1]/DIV[1]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[2]/DIV[8]/DIV[1]/DIV[11]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[3]/I[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/DIV[4]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/FORM[1]/LABEL[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/B[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/UL[1]/LI[4]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[7]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/P[8]/A[1]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[35]/DIV[5]/DIV[2]/UL[1]/LI[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/FORM[1]/LABEL[17]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[12]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[4]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/UL[1]/LI[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[1]/FORM[1]/LABEL[6]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[16]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[40]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[5]/DIV[2]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[52]/DIV[1]/FORM[1]/LABEL[13]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[14]/DIV[1]/DIV[1]/P[1]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[2]/P[2]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[5]/DIV[4]/DIV[1]/DIV[2]/FORM[1]/DIV[1]/SELECT[1]/OPTION[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[53]/DIV[1]/FORM[1]/LABEL[17]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/UL[1]/LI[2]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[5]/DIV[2]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[3]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/FORM[1]/LABEL[5]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[7]/SPAN[1]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/DIV[1]/LI[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[2]/A[7]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[30]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[1]/FORM[1]/LABEL[14]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/FORM[1]/NOSCRIPT[1]/UL[1]/LI[1]/SPAN[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[2]/B[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[5]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[17]/DIV[1]/DIV[1]/P[1]/B[1]/A[9]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[13]/DIV[1]/DIV[1]/A[2]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[46]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[3]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[9]/DIV[1]/DIV[1]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[1]/P[15]/A[1]/EM[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/DIV[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[42]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[8]/TD[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[1]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[32]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[4]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[49]/DIV[1]/UL[1]/LI[1]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[11]/DIV[1]/DIV[1]/SPAN[2]/A[1]/B[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[44]/DIV[5]/DIV[2]/UL[1]/LI[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[1]/UL[1]/LI[3]/A[3]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[34]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[23]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[48]/DIV[1]/UL[1]/LI[4]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[28]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/DIV[3]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[38]/DIV[2]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/DIV[1]/DIV[7]/TABLE[1]/TBODY[1]/TR[2]/TD[4]/DIV[1]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[6]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[27]/DIV[1]/DIV[1]/DIV[2]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[45]/DIV[1]/UL[1]/LI[2]/A[4]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[41]/DIV[1]/DIV[1]/LI[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/A[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[16]/DIV[1]/DIV[1]/DIV[5]/DIV[3]/UL[1]/LI[2]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[50]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[47]/DIV[1]/FORM[1]/LABEL[7]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[15]/DIV[1]/DIV[1]/A[6]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/DIV[1]/FORM[1]/FIELDSET[1]/UL[1]/LI[3]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[18]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/P[8]/I[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[29]/DIV[1]/DIV[1]/DIV[5]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[22]/DIV[1]/DIV[1]/DIV[6]/TABLE[1]/TBODY[1]/TR[2]/TD[3]/DIV[1]/DIV[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[43]/DIV[1]/UL[1]/LI[1]/A[5]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[19]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[12]/DIV[1]/UL[1]/LI[1]/DIV[1]/I[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[24]/DIV[1]/UL[1]/LI[2]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[21]/DIV[1]/DIV[2]/EM[2]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[20]/DIV[1]/DIV[1]/P[17]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[36]/DIV[2]/TABLE[1]/TBODY[1]/TR[5]/TD[1]/DIV[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[8]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[4]/P[1]/A[1]/text()[1]");//1.0
cand.add("/HTML[1]/BODY[1]/DIV[31]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/A[1]/text()[1]");//1.0
		
		
		return cand;
		
	}
	

	private static String detectWorkingXpath(Set<String> candidateXpaths){
		for (String x : candidateXpaths){
			if  (workingXpath(x)){
				return x;
			}
		}
		return "";
		
	}
	
 
	
	private static boolean workingXpath(String xps) {
		//TODO fix this, ad hoc for testing
//		String htmlFolder= "/Users/annalisa/Documents/CORPORAandDATASETS/swde-17477/book/book-amazon-2000";
		String htmlFolder= "/Users/annalisa/Documents/CORPORAandDATASETS/swde-17477/testSET/restaurant/restaurant-usdiners-2000";

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			try {
				XPathExpression expr = xpath.compile(xps);
				System.out.println(expr);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


				File folder = new File(htmlFolder);
				if (folder.isDirectory()) {
					for (File f : folder.listFiles()) {
						Map<String, String> n = TestXpathCandidate
								.getMatchingXpathFromPage(f, xps);
						l4j.debug("******* page " + f.getAbsolutePath());
						l4j.debug("*******g etMarchingXpathFromPage " + n.size());

						if (n.size()>0){
							return true;
						}else{
							return false;
						}
					}
				} else {
					l4j.debug("******* NOT a DIR " + folder.getAbsolutePath());
				}

				return false;
			
	
	}



	public static void main(String[] args) throws IOException {

		// TODO pass by command line
		// String htmlFolder=
		// "/Users/annalisa/Documents/CORPORAandDATASETS/testBook/book-amazon(correct)";
		// String examples = "./gazeteers/book_titles.txt";

		// TODO uncomment following to generate baseline gazetteer, the one
		// containg all true annotations
		// String examples = "./gazeteers/baseline_book_titles.txt";

		HashMap<String, String> wx = new HashMap<String, String>();
		//working ones
//		wx.put("deepdiscount",
//				"/HTML[1]/BODY[1]/DIV[1]/DIV[2]/DIV[2]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/H1[1]/text()[1]");
//		wx.put("abebooks",
//				"/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[2]/DIV[1]/H2[1]/text()[1]");
//		wx.put("waterstones",
//				"/HTML[1]/BODY[1]/DIV[1]/DIV[3]/DIV[3]/DIV[9]/DIV[4]/DIV[2]/H3[1]/A[1]/text()[1]");//misses some paths
//		wx.put("barnesandnoble",
//				"/HTML[1]/BODY[1]/DIV[3]/DIV[2]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/DIV[1]/H1[1]/text()[1]");
//		wx.put("borders",
//		"/HTML[1]/BODY[1]/DIV[2]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/DIV[1]/DIV[7]/DIV[1]/DIV[3]/DIV[1]/H1[1]/text()[1]");//63.0
		//		"/HTML[1]/BODY[1]/DIV[2]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/DIV[1]/DIV[6]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/DIV[1]/DIV[2]/H2[1]/B[1]/A[1]/text()[1]");//63.0
		//		"/HTML[1]/BODY[1]/DIV[2]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/DIV[1]/DIV[7]/DIV[1]/DIV[3]/DIV[1]/DIV[1]/A[1]/text()[1]");//10.0
		//		"/HTML[1]/BODY[1]/DIV[2]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/DIV[1]/DIV[6]/DIV[1]/DIV[1]/DIV[1]/DIV[1]/DIV[2]/DIV[1]/DIV[2]/A[1]/text()[1]");//10.0
		
//		wx.put("christianbook",
//				"/HTML[1]/BODY[1]/DIV[1]/DIV[2]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/DIV[3]/DIV[1]/DIV[1]/H1[1]/text()[1]");
//		wx.put("bookdepository",
		//book titles: this is working but is not the first result
//				"/HTML[1]/BODY[1]/DIV[1]/DIV[2]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/DIV[2]/UL[1]/LI[1]/DIV[1]/DIV[1]/H1[1]/STRONG[1]/SPAN[1]/text()[1]");//73
//		"/HTML[1]/BODY[1]/DIV[1]/DIV[2]/DIV[2]/DIV[1]/DIV[2]/UL[1]/LI[3]/DIV[1]/H3[1]/A[1]/text()[1]");//100
//		"/HTML[1]/BODY[1]/DIV[1]/DIV[2]/DIV[2]/DIV[1]/DIV[2]/UL[1]/LI[5]/DIV[1]/H3[1]/A[1]/text()[1]");//88
//		"/HTML[1]/BODY[1]/DIV[1]/DIV[2]/DIV[2]/DIV[1]/DIV[2]/UL[1]/LI[2]/DIV[1]/H3[1]/A[1]/text()[1]");//85			
//		"/HTML[1]/BODY[1]/DIV[1]/DIV[2]/DIV[2]/DIV[1]/DIV[2]/UL[1]/LI[4]/DIV[1]/H3[1]/A[1]/text()[1]");//79
//		"/HTML[1]/BODY[1]/DIV[1]/DIV[2]/DIV[2]/DIV[1]/DIV[2]/UL[1]/LI[1]/DIV[1]/H3[1]/A[1]/text()[1]");//77			
//		"/HTML[1]/BODY[1]/DIV[1]/DIV[2]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/DIV[2]/UL[1]/LI[1]/DIV[1]/DIV[1]/H1[1]/STRONG[1]/SPAN[1]/text()[1]");//73
		//authors
//		"/HTML[1]/BODY[1]/DIV[1]/DIV[2]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/DIV[2]/UL[1]/LI[1]/DIV[1]/DIV[1]/H1[1]/STRONG[1]/SPAN[3]/SPAN[1]/A[1]/text()[1]");
		
//		wx.put("usdiners", "/HTML[1]/BODY[1]/TABLE[4]/TBODY[1]/TR[5]/TD[2]/B[1]/text()[1]");
//		wx.put("usdiners", "/HTML[1]/BODY[1]/TABLE[4]/TBODY[1]/TR[5]/TD[2]/B[1]/text()[2]");
//		wx.put("usdiners", "/HTML[1]/BODY[1]/TABLE[4]/TBODY[1]/TR[5]/TD[2]/SPAN[1]/text()[1]");
//		wx.put("usdiners", "/HTML[1]/BODY[1]/TABLE[4]/TBODY[1]/TR[5]/TD[2]/B[2]/text()[2]");
//		wx.put("usdiners", "/HTML[1]/BODY[1]/DIV[3]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/DIV[1]/TABLE[2]/TBODY[1]/TR[6]/TD[1]/CODE[1]/NOBR[1]/B[1]/text()[1]");
//		wx.put("usdiners", "/HTML[1]/BODY[1]/TABLE[2]/TBODY[1]/TR[3]/TD[3]/FORM[1]/SELECT[1]/OPTION[35]/text()[1]");
//		wx.put("usdiners", "/HTML[1]/BODY[1]/P[3]/A[2]/text()[1]");
//		wx.put("usdiners", "/HTML[1]/BODY[1]/TABLE[3]/TBODY[1]/TR[1]/TD[2]/NOBR[1]/FONT[1]/BIG[1]/A[1]/text()[1]");

//		wx.put("amazon", "/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");
	


			String relaxedXp = DOMUtil.removeXPathPositionFilters("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");
			System.out.println("/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");
			System.out.println(relaxedXp);
			
			wx.put("amazon", relaxedXp);
			wx.put("amazon2", "/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");
			wx.put("amazon2", "/HTML[1]/BODY[1]/DIV[1]/FORM[1]/DIV[1]/SPAN[1]/A[2]/text()[1]");


//		wx.put("booksamillion",
//		//books titles
//// "/HTML[1]/BODY[1]/DIV[2]/DIV[1]/H2[1]/text()[1]");//116.0
//		//authors
//		"/HTML[1]/BODY[1]/DIV[2]/DIV[1]/H3[1]/A[1]/text()[1]");
		
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[3]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//91.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[6]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//88.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[2]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//87.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[5]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//87.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[8]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//85.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[4]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//82.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[11]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//74.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[7]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//73.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[1]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//72.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[14]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//71.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[9]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//69.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[13]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//68.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[15]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//62.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[10]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//60.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[12]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//55.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[17]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//53.0
// "/HTML[1]/BODY[1]/DIV[2]/DIV[2]/DIV[1]/DL[18]/DT[1]/DIV[1]/A[1]/DIV[1]/text()[1]");//51.0

		
//		String xp = TestXpathCandidate.detectWorkingXpath(TestXpathCandidate.getAmazonPrunedCandidates());
//		
//		//NOT working ones
//		wx.put("amazon", xp);

//		wx.put("buy",
//		"/HTML[1]/BODY[1]/FORM[1]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[1]/H1[1]/A[1]/text()[1]");//66.0
//		"/HTML[1]/BODY[1]/FORM[1]/DIV[6]/A[1]/text()[1]");//8.0
//		"/HTML[1]/BODY[1]/FORM[1]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[3]/TD[1]/A[1]/text()[1]");//5.0
		//"/HTML[1]/BODY[1]/FORM[1]/DIV[11]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[4]/I[1]/text()[1]");//4.0
		//"/HTML[1]/BODY[1]/FORM[1]/TABLE[4]/TBODY[1]/TR[1]/TD[1]/DIV[1]/SPAN[1]/A[1]/text()[1]");//3.0
		//"/HTML[1]/BODY[1]/FORM[1]/DIV[12]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/DIV[9]/DIV[4]/BLOCKQUOTE[1]/FONT[1]/B[1]/text()[1]");//2.0



		for (String xps : wx.keySet()) {
			String htmlFolder = args[0] + xps + "-4";
			System.out.println(htmlFolder);

			String winningXpath = wx.get(xps);
			l4j.info("Winning Xpath \t" + winningXpath);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			try {
				XPathExpression expr = xpath.compile(winningXpath);
				System.out.println(expr);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Set<String> x = new HashSet<String>();
			x.add(winningXpath);
			int i = 0;
			for (String s : x) {
				i++;
				l4j.debug("Xpath " + i + " of " + x.size() + "\t" + s);

				File folder = new File(htmlFolder);
				if (folder.isDirectory()) {
					for (File f : folder.listFiles()) {
						Map<String, String> n = TestXpathCandidate
								.getMatchingXpathFromPage(f, s);
						l4j.debug("******* page " + f.getAbsolutePath());
						l4j.debug("*******g etMarchingXpathFromPage " + n.size());

						for (String nod : n.keySet()) {
							l4j.debug("nodes from original exp (" + nod + ") "
									+ n.get(nod));
							String id = f.getName().substring(0,
									f.getName().lastIndexOf(".htm"));
							l4j.warn(id + "\t" + n.get(nod));

						}
					}
				} else {
					l4j.debug("******* NOT a DIR " + folder.getAbsolutePath());

				}

			}
		}

	}
}
