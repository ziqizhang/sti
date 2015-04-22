package uk.ac.shef.dcs.oak.lodietest;

import com.sindice.result.SearchResult;
import com.sindice.result.SearchResults;
import uk.ac.shef.dcs.oak.triplesearch.Triple;
import uk.ac.shef.dcs.oak.triplesearch.sindiceapi.SindiceAPIProxy;
import uk.ac.shef.dcs.oak.triplesearch.sindiceapi.TripleExtractor;
import uk.ac.shef.dcs.oak.triplesearch.sindiceapi.TripleExtractorSindiceCacheAPI;
import uk.ac.shef.dcs.oak.util.FileUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 20/09/12
 * Time: 16:00
 */
public class TestSindiceAPIProxy {
    /*
    args[0] - list of input test queries in a text file (see [lodie_root]/resources/triplesearch/benchmark_sindice_speed/sindice_queries.txt
    args[1] - max number of pages to process. A query may return hundreds or thousands of pages. Use this number to control
            a max number of pages you wish to process.
     */
    public static void main(String[] args) throws IOException {
        //String query = "q=&nq=(" +URLEncoder.encode("* <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Airline>","UTF-8")+")";
        //String query = "q=Rome&fq=class:city";
        //String query ="nq="+URLEncoder.encode("* <label> 'Rome'")+"&fq="+URLEncoder.encode("class:city");

        List<String> lines = FileUtils.readList(args[0], false);
        Iterator<String> lit = lines.iterator();
        while (lit.hasNext()) {
            String l = lit.next();
            if (l.startsWith("#"))
                lit.remove();
        }

        int pagesToProcess = Integer.valueOf(args[1]);
        SindiceAPIProxy sindice = new SindiceAPIProxy();
        TripleExtractor cextractor = new TripleExtractorSindiceCacheAPI(sindice);
        //TripleExtractor lextractor = new TripleExtractorSindiceLiveAPI(sindice,1,1);


        Date begin = new Date();
        System.out.println("Benchmarking started at " + begin + ", total=" + lines.size());
        System.out.println("----------");
        int count = 0;
        int totalToProcess=-1;
        PrintWriter p = new PrintWriter(args[2]);
        for (String l : lines) {
            count++;
            String[] queryParams = FileUtils.splitCSV(l);

            try {
                Date perQueryStart = new Date();
                System.out.println(count + " - New query @ " + perQueryStart);
                SearchResults rs = sindice.search(queryParams[0],
                        queryParams[1],
                        queryParams[2]);
                System.out.println("\t- query ends @" + new Date() + " total: " + rs.getTotalResults());
                totalToProcess=rs.getTotalResults();

                System.out.println("\t- process each page for " + pagesToProcess + " total pages max. @ " + new Date());
                int currentPage = 1;
                while (currentPage <= pagesToProcess && rs != null && rs.size() != 0) {
                    Iterator<SearchResult> it = rs.iterator();
                    while (it.hasNext()) {
                        SearchResult r = it.next();
                        /*try {
                            sindice.retrieveDocument(r);
                        } catch (Exception e) {
                            System.err.println("Cannot open url, skipped:" + r.getLink());
                        }*/
                        System.out.print("+");
                        //Set<Triple> triples = lextractor.extract(r);
                        Set<Triple> triples=cextractor.extract(r);
                        System.out.print("-");
                        System.out.print(triples.size());
                    }
                    System.out.println();
                    currentPage++;
                    rs = rs.nextPage();
                }
                Date perQueryEnd = new Date();
                System.out.println("\t- process ends @ " + perQueryEnd);
                p.println("\"" + l + "\"," + (perQueryStart.getTime() - perQueryEnd.getTime())+","+totalToProcess);
            } catch (Exception e) {
                System.err.println("Exception, skipped");
                e.printStackTrace();
                p.println("\"" + l + "\",TIMEOUT,"+totalToProcess);
            }


        }

        p.close();
        Date end = new Date();
        System.out.println("----------");
        System.out.println("Completed at " + end);
        System.out.println("Total time taken: " + (end.getTime() - begin.getTime()) + " msec");
        System.out.println("Average per query: " + (end.getTime() - begin.getTime()) / lines.size() + " msec");
    }
}
