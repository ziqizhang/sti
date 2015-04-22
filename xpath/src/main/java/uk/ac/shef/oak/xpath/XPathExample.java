package uk.ac.shef.oak.xpath;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

public class XPathExample {

  public static void main(String[] args) 
   throws ParserConfigurationException, SAXException, 
          IOException, XPathExpressionException {

	  
	  
//	  String html="";
//	  
//		    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
//		    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
//		    Document doc = docBuilder.parse (html);

	  
	  
	    
	    
//	    CleanerProperties props = new CleanerProperties();
//	    
//	 // set some properties to non-default values
//	 props.setTranslateSpecialEntities(true);
//	 props.setTransResCharsToNCR(true);
//	 props.setOmitComments(true);
//	  
//	 // do parsing
//	 TagNode tagNode = new HtmlCleaner(props).clean(
////	     new URL("http://www.chinadaily.com.cn/")
//	     new URL(" http://www.rottentomatoes.com/m/godfather_part_ii/")
//	 );
//	  
//	 // serialize to xml file
//	 new PrettyXmlSerializer(props).writeToFile(
//	     tagNode, "godfather.xml", "utf-8"
//	 );
	 
	 
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    domFactory.setNamespaceAware(true); // never forget this!
    DocumentBuilder builder = domFactory.newDocumentBuilder();
//    Document doc = builder.parse("../resources/xpathexamples/books.xml");
    Document doc = builder.parse("godfather.xml");

    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
//    XPathExpression expr 
//     = xpath.compile("//book[author='Neal Stephenson']/title/text()");

    
    XPathExpression expr 
    = xpath.compile("/HTML[1]/BODY[1]/DIV[9]/DIV[2]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/DIV[7]/DIV[1]/P[2]/SPAN[1]/A[1]/SPAN[1]/text()");

    
    Object result = expr.evaluate(doc, XPathConstants.NODESET);
    NodeList nodes = (NodeList) result;
    for (int i = 0; i < nodes.getLength(); i++) {
        System.out.println(nodes.item(i).getNodeValue()); 
    }

    

//    
//    // Create a new JTidy instance and set options
//    Tidy tidy = new Tidy();
//    tidy.setXHTML(true); 
//
//    // Parse an HTML page into a DOM document
//    URL url = new URL("http://www.cs.grinnell.edu/~walker/fluency-book/labs/sample-table.html");        
//    Document doc = tidy.parseDOM(url.openStream(), System.out);
//
//    // Use XPath to obtain whatever you want from the (X)HTML
//    XPath xpath = XPathFactory.newInstance().newXPath();
//    XPathExpression expr = xpath.compile("//td[@valign = 'top']/a/text()");
//    NodeList nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
//    List<String> filenames = new ArrayList<String>();
//    for (int i = 0; i < nodes.getLength(); i++) {
//        filenames.add(nodes.item(i).getNodeValue()); 
//    }
//
//    System.out.println(filenames);
    

//    URL url= new URL("http://www.imdb.com/title/tt0071562/");
//    URL url= new URL("http://www.rottentomatoes.com/m/godfather_part_ii/");
//    URL url= new URL("http://en.wikipedia.org/wiki/Folding@home");
//    System.out.println("Parsing page "+url);
//
//    
//    String htmlStr =   "<div><table><td id='1234 foo 5678'>Hello</td>";
////    TagNode tagNode = new HtmlCleaner().clean(htmlStr);
//    TagNode tagNode = new HtmlCleaner().clean(url);
//
//    org.w3c.dom.Document doc2 = new DomSerializer(   new CleanerProperties()).createDOM(tagNode);
//    
//    System.out.println("Parsed page "+doc2);
//
//    
//    XPath xpath2 = XPathFactory.newInstance().newXPath();
//    String str = (String) xpath2.evaluate("//div//td[contains(@id, 'foo')]/text()", 
//                           doc2, XPathConstants.STRING);
//    System.out.println(str);
    

  }

}