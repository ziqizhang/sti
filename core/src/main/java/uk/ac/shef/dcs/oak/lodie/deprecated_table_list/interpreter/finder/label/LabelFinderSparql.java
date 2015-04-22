package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.label;

import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.triplesearch.triple.SPARQLQueryAgent;
import uk.ac.shef.dcs.oak.triplesearch.util.CommonSPARQLQueries;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 08/03/13
 * Time: 13:06
 * <p/>
 * todo: remove non-english labels
 */
public class LabelFinderSparql extends LabelFinder {

    protected String[] sparqlEndPoints;

    public LabelFinderSparql(QueryCache cache, String... sparqlEndPoints) {
        super(cache);
        this.sparqlEndPoints = sparqlEndPoints;
    }

    @Override
    protected List<String> findCandidatesInKB(String uri) {
        List<String> cands = new ArrayList<String>();

        List<String> sparql = CommonSPARQLQueries.createQueryBatchFindLabelsOfResource(uri);
        for (String endpoint : sparqlEndPoints) {
            for (String query : sparql) {
                //System.out.println(query);
                ResultSet rs = SPARQLQueryAgent.searchSPARQL(query, endpoint);
                // try {
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    if (!(qs.get("?o").asNode() instanceof Node_Literal))
                        continue;

                    String object = qs.get("?o").toString();
                    if (object != null && object.indexOf("@") != -1 && !object.substring(object.lastIndexOf("@")+1).equalsIgnoreCase("en")) {
                        continue;
                    }
                    if (!cands.contains(object))
                        cands.add(object);
                    //System.out.println(subject + "\t" + object);
                }
                /* } catch (Exception e) {
                    e.printStackTrace();
                }*/
                //System.out.println();
            }
        }
        /*
       not exhaustively try all n-gram combinations of keywords. as long as one matches, stop
        */


        try {
            cache.cache(String.valueOf(sparql.toString().hashCode()),
                    cands, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cands;


    }

    @Override
    protected List<String> findCandidatesInCache(String uri) {
        String sparql = CommonSPARQLQueries.createQueryBatchFindLabelsOfResource(uri).toString();
        String idInCache = String.valueOf(sparql.hashCode());
        try {
            return (List<String>)cache.retrieve(idInCache);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
