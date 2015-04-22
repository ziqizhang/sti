package uk.ac.shef.oak.xpath;
import java.net.URL;

import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.ccil.cowan.tagsoup.Parser;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XPathTagSoupTest {

    public static void main(String args[]) throws Exception {

        // print the 'src' attributes of <img> tags
        // from http://www.yahoo.com/
        // using the TagSoup parser

//        SAXParserImpl.newInstance(null).parse(
//            new URL("http://www.rottentomatoes.com/m/godfather_part_ii/").openConnection().getInputStream(),
////                new URL("http://www.imdb.com/title/tt0071562").openConnection().getInputStream(),
//
//            new DefaultHandler() {
//                public void startElement(String uri, String localName,
//                                         String name, Attributes a)
//                {
//                    if (name.equalsIgnoreCase("img"))
//                        System.out.println(a.getValue("src"));
//                }
//            }
//        );
        
        
        
        
        
        URL url = new URL("http://www.rottentomatoes.com/m/godfather_part_ii/");
        Parser p = new Parser();
        p.setFeature("http://xml.org/sax/features/namespace-prefixes",true);
        // to define the html: prefix (off by default)
        SAX2DOM sax2dom = new SAX2DOM();
        p.setContentHandler(sax2dom);
        p.parse(new InputSource(url.openStream()));
        Node doc = sax2dom.getDOM();
        String titlePath = "/html:html/html:head/html:title";
        XObject title = XPathAPI.eval(doc,titlePath);
        System.out.println("Title is '"+title+"'");
        
        
//        XPathFactory factory = XPathFactory.newInstance();
//        XPath xpath = factory.newXPath();
//        XPathExpression expr = xpath.compile("/HTML[1]/BODY[1]/DIV");
//
//        Object result = expr.evaluate(doc, XPathConstants.NODESET);
//
//        NodeList nodes = (NodeList) result;
//        for (int i = 0; i < nodes.getLength(); i++) {
//        	System.out.println(nodes.item(i).getNodeValue()); 
//        }
//        
//        /HTML[1]/BODY[1]/DIV[9]/DIV[2]/DIV[1]/DIV[4]/DIV[2]/UL[1]/LI[10]/DIV[2]/A[1]/SPAN[1]/text()
        
        Elements res = Jsoup.connect("http://www.rottentomatoes.com/m/godfather_part_ii/").get().select("html>body>div").eq(8).select("div");     

//        Elements res = Jsoup.connect("http://www.rottentomatoes.com/m/godfather_part_ii/").get().select("html>body>div").eq(9);     
        System.out.println(res);
    }
}