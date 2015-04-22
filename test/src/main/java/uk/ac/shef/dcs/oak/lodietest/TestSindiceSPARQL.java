package uk.ac.shef.dcs.oak.lodietest;

import uk.ac.shef.dcs.oak.triplesearch.triple.SPARQLQueryAgent;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 24/09/12
 * Time: 12:12
 */
public class TestSindiceSPARQL {

        public static void main(String[] args) {

            SPARQLQueryAgent sparql = new SPARQLQueryAgent();

            String query ="PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> PREFIX rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?s WHERE{ ?s rdfs:type <http://dbpedia.org/ontology/Animal>.}\n";

            sparql.searchSPARQL(query,"http://sparql.sindice.com/sparql" /*"http://dbpedia.org/sparql"*/);
            System.out.println("END");
        }


}
