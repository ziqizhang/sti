package uk.ac.shef.dcs.oak.triplesearch.triple;

import com.hp.hpl.jena.query.*;


/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 20/09/12
 * Time: 16:23
 */
public class
        SPARQLQueryAgent {

    public static ResultSet searchSPARQL(String q, String endpoint) {
        Query query = QueryFactory.create(q);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);

        ResultSet results = qexec.execSelect();
        return results;
    }

    public static boolean hasStatement(String query, String endpoint) {
        QueryExecution qe = QueryExecutionFactory.sparqlService(
                endpoint, query);
        if (qe.execAsk()) {
            return true;
        } else {
            return false;
        }
    }

}
