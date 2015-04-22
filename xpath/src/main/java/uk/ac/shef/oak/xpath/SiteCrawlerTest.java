/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.shef.oak.xpath;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.any23.plugin.crawler.CrawlerListener;
import org.apache.any23.plugin.crawler.SiteCrawler;

import edu.uci.ics.crawler4j.crawler.Page;


public class SiteCrawlerTest  {

    /**
     * Tests the main crawler use case.
     *
     * @throws Exception
     */
    public static void main(String [] args) throws Exception {

        final File tmpFile = File.createTempFile("site-crawler-test", ".storage");
        tmpFile.delete();

        final SiteCrawler controller = new SiteCrawler(tmpFile);
        controller.setMaxPages(10);
        System.out.println("Crawler4j: Setting max num of pages to: " + controller.getMaxPages());
        controller.setPolitenessDelay(500);
        System.out.println("Crawler4j: Setting Politeness delay to: " + controller.getPolitenessDelay() + "ms");

        final Set<String> distinctPages = new HashSet<String>();
        controller.addListener(new CrawlerListener() {
            public void visitedPage(Page page) {
//              	 ExtractXpath myDownloader = new ExtractXpath();

                distinctPages.add( page.getWebURL().getURL() );
                Iterator<String> it = distinctPages.iterator();
                while (it.hasNext()) {
                	System.out.println("Crawler4j: Fetching page - " + it.next());
//                	   Document doc = myDownloader.getDomForHtmlPage("http://ics.uci.edu");
//
//                	    if (doc != null) {
//
//              	          try {
//                  			  Writer ot = new StringWriter();
//                  	          XMLSerializer slr = new XMLSerializer(ot, new OutputFormat());
//							slr.serialize(doc);
//							System.out.println(ot.toString());  
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//              	                        	    }
}
            }
        });

        controller.start( new URL("http://schema.org/"), false);

        synchronized (SiteCrawlerTest.class) {
        	SiteCrawlerTest.class.wait(15 * 1000);
        }
        
        controller.stop();

        System.out.println("Distinct pages: " + distinctPages.size());
        System.out.println("Expected some page crawled."+ (distinctPages.size() > 0));
        
        
        
    }

    
    /* create filename based on specified URL */
    static String makeFilename(String url) {
        return url.replaceAll("http://|https://|file://", "");
    }    
}
