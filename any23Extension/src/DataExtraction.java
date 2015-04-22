//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.StringReader;
//import java.io.StringWriter;
//import java.io.UnsupportedEncodingException;
//import java.io.Writer;
//import java.net.URISyntaxException;
//import java.net.URL;
//
//import javax.xml.transform.dom.DOMResult;
//
//import org.apache.any23.Any23;
//import org.apache.any23.extractor.ExtractionException;
//import org.apache.any23.extractor.html.DomUtils;
//import org.apache.any23.extractor.html.HTMLDocument;
//import org.apache.any23.extractor.html.TagSoupParser;
//import org.apache.any23.http.HTTPClient;
//import org.apache.any23.source.DocumentSource;
//import org.apache.any23.source.HTTPDocumentSource;
//import org.apache.any23.writer.NTriplesWriter;
//import org.apache.any23.writer.TripleHandler;
//import org.apache.any23.writer.TripleHandlerException;
//import org.apache.xalan.xsltc.trax.SAX2DOM;
//import org.apache.xml.serialize.OutputFormat;
//import org.apache.xml.serialize.XMLSerializer;
//import org.ccil.cowan.tagsoup.Parser;
//import org.jsoup.Jsoup;
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.xml.sax.InputSource;
//import uk.ac.shef.oak.any23.extension.extractor.LAny23;
//import uk.ac.shef.oak.any23.extension.extractor.LNTripleWriter;
//import uk.ac.shef.oak.any23.extension.extractor.LTriple;
//
//import uk.ac.shef.wit.ie.wrapper.html.xpath.DOMUtil;
//
//
//public class DataExtraction {
//
//	/**
//	 * @param args
//	 * @throws IOException 
//	 */
//	public static void main(String[] args) throws IOException {
//		try {
////			/*1*/ Any23 runner = new Any23();
//			/*1*/ LAny23 runner = new LAny23("lodie-html-microdata");
//
//			/*2*/ runner.setHTTPUserAgent("test-user-agent");
//			/*3*/ HTTPClient httpClient = runner.getHTTPClient();
//			/*4*/ DocumentSource source = new HTTPDocumentSource(
//			         httpClient,
////			         "http://www.rentalinrome.com/semanticloft/semanticloft.htm"
////			         "http://www.imdb.com/title/tt0071562/"
////			         "http://www.imdb.com/title/tt0071562/"
////			         "http://www.imdb.com/title/tt0071562/fullcredits#cast"
////			         "http://www.rottentomatoes.com/m/godfather_part_ii/"
//			         "http://www.rottentomatoes.com/m/godfather_part_ii"
//			      );
//			
////			TagSoupParser tsp = new TagSoupParser(source.openInputStream(), "http://www.dcs.shef.ac.uk/~nsi");
////	
////			Document doc = tsp.getDOM();
////			
////			
////			Node node = doc.getParentNode();
////			System.out.println(doc.getNodeName()+doc.getNodeValue());
////
////			String xpath = "/HTML/BODY";
////
////			System.out.println(DomUtils.find(node,  xpath));
////			
//			/*5*/ ByteArrayOutputStream out = new ByteArrayOutputStream();
////			/*6*/ TripleHandler handler = new NTriplesWriter(out);
//			/*6*/ LNTripleWriter handler = new LNTripleWriter(out);
//
//			      try {
//			/*7*/     runner.extract(source, handler);
//			      } finally {
//			/*8*/     handler.close();
//			      }
//			/*9*/ String n3 = out.toString("UTF-8");
////			System.out.println(n3);
//			
//			
//			InputStream input = new URL("http://www.rottentomatoes.com/m/godfather_part_ii").openStream();
//
//			TagSoupParser tsp = new TagSoupParser(input, "utf-8");
//			Document doc = tsp.getDOM();
//			
//			int count = 0;
////			for(String[] s :handler.getOutput()){
////				System.out.print(count+"\t");
////
////				count++;
////				for(int i=3; i<s.length;i++){
////				try {
////					
////					System.out.println(s[i]);
////					if (s[i].length()>0){
////						String val = DomUtils.find(doc,  s[i]);
////					}
////
////				} catch (Exception e) {
////					System.out.println("trouble with xpath "+s[i]);
////				}
////
////			}
////				System.out.println();
////
////}
//			
//			
//			for(LTriple s :handler.getOutput()){
//				System.out.println(s.getTriple().getSubject()+ " "+s.getTriple().getPredicate()+ " "+s.getTriple().getObject());
//
//				try {
//					if (s.getsXPath()!=null/*&(s[i].contains("@")|s[i].contains("("))*/){
//						String val = DomUtils.find(doc,  s.getsXPath());
//					}
//					if (s.getpXPath()!=null/*&(s[i].contains("@")|s[i].contains("("))*/){
//						String val = DomUtils.find(doc,  s.getpXPath());
//
//					}
//					if (s.getoXPath()!=null/*&(s[i].contains("@")|s[i].contains("("))*/){
//						String val = DomUtils.find(doc,  s.getoXPath());
//					}
//				} catch (Exception e) {
//					System.out.println("trouble with xpath in "+s);
//				}
//
//}
////			int count = 0;
////			for(String[] s :handler.getOutput()){
////				System.out.print(count+"\t");
////
////				count++;
////				for(int i=0; i<s.length;i++){
////
////				System.out.print(s[i]+"\t");
////
////			}
////				System.out.println();
////
////}
//
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ExtractionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (TripleHandlerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//
//
//		
////		System.out.println(doc);
////		 
////		Writer out = new StringWriter();
////        XMLSerializer serializer = new XMLSerializer(out, new OutputFormat());
////        serializer.serialize(doc);
////        System.out.println(out.toString());
//		
//		
//		InputStream input = new URL("http://www.rottentomatoes.com/m/godfather_part_ii").openStream();
//		TagSoupParser tsp = new TagSoupParser(input, "utf-8");
//		Document doc = tsp.getDOM();
////		String xpath = "//HTML[1]/BODY[1]/DIV[9]/DIV[2]/DIV[1]/DIV[4]/DIV[2]/UL[1]/LI[37]/DIV[2]/A[1]/SPAN[1]/text()";
////		System.out.println(DomUtils.find(node,  xpath));
////		
//		String xpath = "/HTML[1]/BODY[1]/DIV[9]/DIV[2]/DIV[1]/DIV[4]/DIV[2]/UL[1]/LI[4]";
//		System.out.println("/HTML[1]/BODY[1]/DIV[9]/DIV[2]/DIV[1]/DIV[4]/DIV[2]/UL[1]/LI[4] "+DomUtils.find(doc,  xpath));
//		xpath = "/HTML[1]/BODY[1]/DIV[9]/DIV[2]/DIV[1]/DIV[4]/DIV[2]/UL[1]/LI[4]/DIV[2]/A[1]/@itemprop";
//		System.out.println("/HTML[1]/BODY[1]/DIV[9]/DIV[2]/DIV[1]/DIV[4]/DIV[2]/UL[1]/LI[4]/DIV[2]/A[1]/@itemprop "+DomUtils.find(doc,  xpath));
//		xpath = "/HTML[1]/BODY[1]/DIV[9]/DIV[2]/DIV[1]/DIV[4]/DIV[2]/UL[1]/LI[4]/DIV[2]/A[1]/@href";
//		System.out.println("/HTML[1]/BODY[1]/DIV[9]/DIV[2]/DIV[1]/DIV[4]/DIV[2]/UL[1]/LI[4]/DIV[2]/A[1]/@href "+DomUtils.find(doc,  xpath));
//		
//				
//				
////		 Document dc = parseDOM(new File("../resources/xpathexamples/godfather.html"));
////			System.out.println(dc);
//
////			 
////			  Writer ot = new StringWriter();
////	          XMLSerializer slr = new XMLSerializer(ot, new OutputFormat());
////	          slr.serialize(dc);
////	          System.out.println(ot.toString());
////			
//
//
//          
//          
//	}
//
////	
////	public static final Document parseDOM(final File fileToParse) {
////		  Parser p = new Parser();
////		  SAX2DOM sax2dom = null;
////		  org.w3c.dom.Node doc  = null;
////
////		  try { 
////
////		        URL url = new URL("http://stackoverflow.com/");
////		        p.setFeature(Parser.namespacesFeature, false);
////		        p.setFeature(Parser.namespacePrefixesFeature, false);
////		        sax2dom = new SAX2DOM();
////		        p.setContentHandler(sax2dom);
////		        p.parse(new InputSource(new InputStreamReader(url.openStream())));
////		        doc = sax2dom.getDOM();
////		        System.err.println(doc);
////		  } catch (Exception e) {
////		     // TODO handle exception
////		     e.printStackTrace();
////		  }
////
////
////          
////		  return (Document)doc;
////		 }
//}
