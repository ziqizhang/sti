package uk.ac.shef.oak.any23.xpath;
import org.apache.any23.Any23;
import org.apache.any23.extractor.ExtractionException;
import org.apache.any23.extractor.html.DomUtils;
import org.apache.any23.extractor.html.TagSoupParser;
import org.apache.any23.http.HTTPClient;
import org.apache.any23.source.DocumentSource;
import org.apache.any23.source.HTTPDocumentSource;
import org.apache.any23.writer.NTriplesWriter;
import org.apache.any23.writer.TripleHandler;
import org.apache.any23.writer.TripleHandlerException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import uk.ac.shef.dcs.oak.triplesearch.Triple;
import uk.ac.shef.oak.any23.extension.extractor.LAny23;
import uk.ac.shef.oak.any23.extension.extractor.LNTripleWriter;
import uk.ac.shef.oak.any23.extension.extractor.LNTripleWriternoXpath;
import uk.ac.shef.oak.any23.extension.extractor.LTriple;

import javax.xml.xpath.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Anna Lisa Gentile (a.l.gentile@dcs.shef.ac.uk)
 *
 *
 *
 */
public class ExtractXpath {

	
	public Document getDomForHtmlPage(String pageUrl){
		Document doc = null;
		InputStream input;
		try {
			input = new URL(pageUrl).openStream();


		TagSoupParser tsp = new TagSoupParser(input, "utf-8");
		doc = tsp.getDOM();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return doc;
		
	}
	
	
	public String findXpathNodeOnHtmlPage(String pageUrl, String xp) throws XPathExpressionException{
		String val ="";
//      String relaxedXp = DOMUtil.removeXPathPositionFilters(xp);
//	    System.out.println(xp +" "+ relaxedXp); 

	    
		InputStream input;
		try {
			input = new URL(pageUrl).openStream();


		TagSoupParser tsp = new TagSoupParser(input, "utf-8");
		Document doc = tsp.getDOM();

		
		 XPathFactory factory = XPathFactory.newInstance();
		  XPath xpath = factory.newXPath();

		  XPathExpression expr = xpath.compile(xp);
//		  XPathExpression exprRelaxed = xpath.compile(relaxedXp);

		  Object result = expr.evaluate(doc, XPathConstants.NODESET);
		  NodeList nodes = (NodeList) result;
		  


//		  System.out.println("nodes from original exp ("+ expr.toString()+") "+nodes.getLength()); 

		  for (int i = 0; i < nodes.getLength(); i++) {
		    System.out.println(nodes.item(i)); 
//		    String[] xplist = DomUtils.getXPathListForNode(nodes.item(i));
//		    
//		    for (String s : xplist){
//			    System.out.println(s); 
//		    }

		  }
		  
		  /*
		  System.out.println("nodes from relaxed exp"+exprRelaxed); 
		  Object result2 = exprRelaxed.evaluate(doc, XPathConstants.NODESET);
		  NodeList nodes2 = (NodeList) result2;
		  for (int i = 0; i < nodes2.getLength(); i++) {
			    System.out.println(nodes2.item(i)); 
//			    String[] xplist = DomUtils.getXPathListForNode(nodes2.item(i));
//			    
//			    for (String s : xplist){
//				    System.out.println(s); 
//			    }

			  }*/
		  
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		

			
		return val;
		
	}
	
	
	
	
	
	public static NodeList findXpathNodeOnHtmlPage(Document doc, String xp) throws XPathExpressionException{
//        String relaxedXp = DOMUtil.removeXPathPositionFilters(xp);

//	    System.out.println(xp +" "+ relaxedXp); 

	    

		 XPathFactory factory = XPathFactory.newInstance();
		  XPath xpath = factory.newXPath();
		  XPathExpression expr = xpath.compile(xp);
//		  XPathExpression exprRelaxed = xpath.compile(relaxedXp);

		  Object result = expr.evaluate(doc, XPathConstants.NODESET);
		  NodeList nodes = (NodeList) result;
		  


//		  System.out.println("nodes from original exp"+expr.toString()); 
//
//		  for (int i = 0; i < nodes.getLength(); i++) {
//		    System.out.println(nodes.item(i)); 
////		    String[] xplist = DomUtils.getXPathListForNode(nodes.item(i));
////		    
////		    for (String s : xplist){
////			    System.out.println(s); 
////		    }
//
//		  }
		  
		  /*
		  System.out.println("nodes from relaxed exp"+exprRelaxed); 
		  Object result2 = exprRelaxed.evaluate(doc, XPathConstants.NODESET);
		  NodeList nodes2 = (NodeList) result2;
		  for (int i = 0; i < nodes2.getLength(); i++) {
			    System.out.println(nodes2.item(i)); 
//			    String[] xplist = DomUtils.getXPathListForNode(nodes2.item(i));
//			    
//			    for (String s : xplist){
//				    System.out.println(s); 
//			    }

			  }*/
			
		return nodes;
	}
	
	
	
	public String findXpathOnHtmlPage(String pageUrl, String xpath){
		String val ="";
		InputStream input;
		try {
			input = new URL(pageUrl).openStream();


		TagSoupParser tsp = new TagSoupParser(input, "utf-8");
		Document doc = tsp.getDOM();

		val = DomUtils.find(doc,  xpath);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return val;
		
	}
		
		public Map<String,String > findXpathOnHtmlPage(String pageUrl, Set<String> xpath){
			
			Map<String,String > results = new HashMap<String,String >();
			InputStream input;
			try {
				input = new URL(pageUrl).openStream();

			TagSoupParser tsp = new TagSoupParser(input, "utf-8");
			Document doc = tsp.getDOM();


			for(String  s :xpath){
				String val = DomUtils.find(doc,  s);
				results.put(s, val);
			}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return results;
			
		}
		
		
		//TODO this method doesn't work, use the one provided in HtmlUtil
//		public String getTextFromHtmlPage(String pageUrl){
//			
//			String results = "";
//			InputStream input;
//			try {
//				input = new URL(pageUrl).openStream();
//
//			TagSoupParser tsp = new TagSoupParser(input, "utf-8");
//			Document doc = tsp.getDOM();
//			results = doc.getTextContent();
//			System.out.println("*********************");
//			System.out.println(results);
//			System.out.println("*********************");
//
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			return results;
//			
//		}
		
		
		public List<LTriple> getTriplesAndXpathFromPage(LAny23 runner, String url){

			try {

				/*3*/ HTTPClient httpClient = runner.getHTTPClient();
				/*4*/ DocumentSource source = new HTTPDocumentSource(
				         httpClient, url );
				
			
				httpClient.openInputStream(url);
				/*5*/ ByteArrayOutputStream out = new ByteArrayOutputStream();
//				/*6*/ TripleHandler handler = new NTriplesWriter(out);
				/*6*/ LNTripleWriter handler = new LNTripleWriter(out);
				      try {
				/*7*/     runner.extract(source, handler);
				      }catch(IOException e){
							e.printStackTrace();
				      }catch(ExtractionException e){
							e.printStackTrace();
					      }catch(Exception e){
							e.printStackTrace();
					      }finally {
//					    	  httpClient.close();
				/*8*/     handler.close();
				      }
				      return handler.getOutput();

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  catch (TripleHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		

		@Deprecated
		public List<LTriple> getTriplesAndXpathFromWebPage(String url){

			try {
				//TODO make the method flexible to all annotation formats
				
//				/*1*/ Any23 runner = new Any23();
//				/*1*/ LAny23 runner = new LAny23("lodie-html-microdata");
				/*1*/ LAny23 runner = new LAny23("lodie-html-rdfa11");

				/*2*/ runner.setHTTPUserAgent("test-user-agent");
				/*3*/ HTTPClient httpClient = runner.getHTTPClient();
				/*4*/ DocumentSource source = new HTTPDocumentSource(
				         httpClient, url );
				
			
				/*5*/ ByteArrayOutputStream out = new ByteArrayOutputStream();
//				/*6*/ TripleHandler handler = new NTriplesWriter(out);
				/*6*/ LNTripleWriter handler = new LNTripleWriter(out);
				      try {
				/*7*/     runner.extract(source, handler);
				      }catch(IOException e){
							e.printStackTrace();
				      }catch(ExtractionException e){
							e.printStackTrace();
					      }catch(Exception e){
							e.printStackTrace();
					      }finally {
				      
					    	  httpClient.close();
				/*8*/     handler.close();
				      }
				      return handler.getOutput();

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  catch (TripleHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		
		
		public List<Triple> getTriplesFromWebPage(String url){

			try {
				//TODO make the method flexible to all annotation formats
				
//				/*1*/ LAny23 runner = new LAny23("lodie-html-microdata");
				/*1*/ Any23 runner = new Any23();

				/*2*/ runner.setHTTPUserAgent("test-user-agent");
				/*3*/ HTTPClient httpClient = runner.getHTTPClient();
				/*4*/ DocumentSource source = new HTTPDocumentSource(
				         httpClient, url );
				
			
				/*5*/ ByteArrayOutputStream out = new ByteArrayOutputStream();
//				/*6*/ TripleHandler handler = new NTriplesWriter(out);
				/*6*/ LNTripleWriternoXpath handler = new LNTripleWriternoXpath(out);

				      try {
				/*7*/     runner.extract(source, handler);
				      } finally {
				/*8*/     handler.close();
				      }
								      
				      return handler.getOutput();


			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExtractionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TripleHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

		}
		
	/**
	 * @param args
	 * @throws IOException 
	 * @throws  
	 */
	public static void main(String[] args) throws IOException {
			
			      ExtractXpath exp = new ExtractXpath();

//					System.out.println("******* example of usage for extracting xpath *******");
//
//			      List<LTriple> tripleXp = exp.getTriplesAndXpathFromWebPage("http://www.goodreads.com/book/show/660523.Secrets_of_the_Morning");
//
//			      Set<String> properties = new HashSet<String>();
//
//			      Set<String> xp = new HashSet<String>();
//
//					for(LTriple s :tripleXp){
//
//						try {
//							if (s.getsXPath()!=null/*&(s[i].contains("@")|s[i].contains("("))*/){
//								xp.add(s.getsXPath());
//							}
//							if (s.getpXPath()!=null&(s.getpXPath().contains("@itemprop"))){
//								xp.add(s.getpXPath());
//								if ((s.getpXPath().contains("@itemprop"))){
//								properties.add(s.getTriple().getPredicate().toString());
//								}
//							}
//							if (s.getoXPath()!=null/*&(s[i].contains("@")|s[i].contains("("))*/){
//								xp.add(s.getoXPath());
//							}
//							System.out.println("*********************************************");
//							System.out.println(s.getTriple().getSubject()+ " "+s.getsXPath());
//							System.out.println(s.getTriple().getPredicate()+ " "+s.getpXPath());
//							System.out.println(s.getTriple().getObject()+ " "+s.getoXPath());
////					          ExtractXpath.printX(doc, seedNodes);
//
//						} catch (Exception e) {
//							System.out.println("trouble with xpath in "+s);
//						}
//
//		}
//					System.out.println("***************************************************");
//					System.out.println(properties);

			      
			      
					
					
//					try {
//						exp.findXpathNodeOnHtmlPage("http://www.rottentomatoes.com/m/godfather/", "/HTML[1]/BODY[1]/DIV[9]/DIV[2]/DIV[1]/DIV[1]/DIV[2]/DIV[2]/DIV[7]/DIV[1]/P/SPAN/A/SPAN/text()");
//					} catch (XPathExpressionException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					System.out.println(("***************************************"));
//
//					System.out.println(("/HTML/*/*/*/*/*/*/*/*/*/*/*/*/*/text()"));


					try {
//					exp.findXpathNodeOnHtmlPage("http://www.rottentomatoes.com/m/godfather/", "/HTML/*/*/*/*/*/*/*/*/*/*/*/*/*/text()");
//						exp.findXpathNodeOnHtmlPage("http://www.amazon.co.uk/Short-Second-Life-Bree-Tanner/dp/1907411178", "/HTML/*/text()");

//					exp.findXpathNodeOnHtmlPage("http://www.amazon.co.uk/Short-Second-Life-Bree-Tanner/dp/1907411178", "/HTML/*/*/*/*/*/*/*/*/*/*/*/*/*/text()");
					exp.findXpathNodeOnHtmlPage("http://www.amazon.co.uk/Short-Second-Life-Bree-Tanner/dp/1907411178", "/HTML/*/*/*/*/*/*/*/*/#text()");

					
					} catch (XPathExpressionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					

//					System.out.println(("***************************************"));
//
//					System.out.println(("/HTML/*/text()"));
//
//					
//					try {
//					exp.findXpathNodeOnHtmlPage("http://www.rottentomatoes.com/m/godfather/", "/HTML/*/text()");
//					} catch (XPathExpressionException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}		
					
//					File file = new File ("/Users/annalisa/Desktop/The Godfather, Part II - Rotten Tomatoes.html");
//
//					try {
//						IEXPathWrapper.parseparse(file );
//					} catch (ParserConfigurationException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					
					
					 
//					Node contextNode = null;
//					CommonNode commonNode = null;
//					try {
//						IEXPathWrapper.relax(contextNode, commonNode);
//						System.out.println();
//					} catch (XPathExpressionException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					
          
	}
	
	
	  
//	public void printXpath(Set<String> seedNodes){
//        final Node node = this.findXpathOnHtmlPage(pageUrl, xpath);
//
//        for (final String seedNodeXPath : seedNodes)
//        {
////            final Seed seed = seedNode.getSeed();
////            final Concept concept = seed.getConcept();
////            String seedNodeXPath = DOMUtil.getXPath(seedNode.getNode());
////            seedNodeXPath = "." + seedNodeXPath.substring(commonNodeXPath.length());
//            // TODO removing filters may be too relaxed
//            seedNodeXPath = DOMUtil.removeXPathPositionFilters(seedNodeXPath);
//            //System.out.println("SeedNodeXPath (" + concept + "): " + seedNodeXPath);
//      
//            XPath xpath;
//
//            final NodeList otherNodeList =
//                    (NodeList) xpath.evaluate(seedNodeXPath, node, XPathConstants.NODESET);
//
//            for (int j = 0; j < otherNodeList.getLength(); j++)
//            {
//                final Node otherNode = otherNodeList.item(j);
//                if (otherNode != null)
//                {
//                    final HtmlNode htmlNode = HtmlNode.getNode(otherNode);
//                    final String text = htmlNode.getText();
//                    //System.out.println("        " + concept.getName() + " : " + text.replaceAll("[\\s]+", " "));
//                    if (otherNode != seedNode.getNode() && !"".equals(text) &&
//                        (concept.getPattern() == null || concept.getPattern().matcher(text).matches()))
//                    {
//                        nodes.get(seedNode.getSeed()).add(htmlNode);
//                    }
//                }
//            }
//        }
//	}
}
