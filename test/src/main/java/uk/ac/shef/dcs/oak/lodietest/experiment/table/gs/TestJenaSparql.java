package uk.ac.shef.dcs.oak.lodietest.experiment.table.gs;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import uk.ac.shef.dcs.oak.triplesearch.util.CommonSPARQLQueries;

import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 06/03/13
 * Time: 16:12
 */
public class TestJenaSparql {

    public static void main(String[] args) {
        List<String> queries = CommonSPARQLQueries.createQueryBatchFindClassAndEquivWithKeywordTBox("species");

        for (String q : queries) {
            QueryExecution qexec = QueryExecutionFactory.sparqlService("http://sparql.sindice.com/sparql", q);
            ResultSet rs = qexec.execSelect();

            System.out.println(q);
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();

                String subject = qs.get("?s").toString();
                String object = qs.get("?o") == null ? null : qs.get("?o").toString();
                System.out.println(subject + "\t" + object);
            }
        }

    }
}
