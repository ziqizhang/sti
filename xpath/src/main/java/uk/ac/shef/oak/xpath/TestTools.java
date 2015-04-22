/**
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Initial Developer of the Original Code is Sheffield University.
 * Portions created by Sheffield University are
 * Copyright &copy; 2005 Sheffield University (Web Intelligence Group)
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Neil Ireson (N.Ireson@dcs.shef.ac.uk)
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */

// Import log4j classes.

package uk.ac.shef.oak.xpath;



public class TestTools
{
	

    /**
     * Main method for command line interface.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args)
    {

        //TODO get content of html page

        try {

          	    String url1 = "http://www.imdb.com/title/tt0068646/";
          	    
          	  String url1b ="http://www.imdb.com/rg/parked-domains/imdb.com/title/tt0071562/";
        	    String url2 = "http://wiki.apache.org/nutch/Nutch2Tutorial";
        	    String url3 = "http://www.rottentomatoes.com/m/road_to_avonlea_the_movie/";
//        	    String html = Jsoup.connect(url1b).get().html();
//                System.out.println(html);
//
//                
//                
//        	    org.jsoup.nodes.Document jsDocument = Jsoup.connect(url2).get();
//                System.out.println(jsDocument);


//			Document document;
//			try {
//				document = DOMUtil.parse(preprocessHTML(html).getBytes(), "utf8");
//				document.setDocumentURI(url2);
//		          System.out.println(document);
//
//			} catch (ParserConfigurationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}


			
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		
		
		
		
//		/*1*/ Any23 runner = new Any23();
//		/*2*/ final String content = "@prefix foo: <http://example.org/ns#> .   " +
//		                             "@prefix : <http://other.example.org/ns#> ." +
//		                             "foo:bar foo: : .                          " +
//		                             ":bar : foo:bar .                           ";
////		    The second argument of StringDocumentSource() must be a valid URI.
//		/*3*/ DocumentSource source = new StringDocumentSource(content, "http://host.com/service");
//		/*4*/ ByteArrayOutputStream out = new ByteArrayOutputStream();
//		/*5*/ TripleHandler handler = new NTriplesWriter(out);
//		      try {
//		/*6*/     runner.extract(source, handler);
//		      } catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ExtractionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} finally {
//		/*7*/     try {
//			handler.close();
//		} catch (TripleHandlerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		      }
//		/*8*/ try {
//			String nt = out.toString("UTF-8");
//			System.out.println(nt);
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
    }

}
