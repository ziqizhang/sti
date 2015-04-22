package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder;

import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation.RelationFinder;
import uk.ac.shef.dcs.oak.triplesearch.triple.SPARQLQueryAgent;
import uk.ac.shef.dcs.oak.triplesearch.util.CommonSPARQLQueries;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 26/04/13
 * Time: 23:09
 */
@Deprecated
public class PropertySharedDomainFinderSparql extends RelationFinder {

    protected String[] sparqlEndPoints;

    public PropertySharedDomainFinderSparql(QueryCache cache, String... sparqlEndPoints) {
        super(cache);
        this.sparqlEndPoints = sparqlEndPoints;
    }

    /*
   Input params "uri1 uri2" must be embedded with "<>"
    */
    @Override
    protected List<String> findRelationBetweenInKB(String uri1, String uri2) {
        List<String> cands = new ArrayList<String>();

        String sparql = CommonSPARQLQueries.createQueryFindSharedDomainsBetweenProperties(uri1, uri2);
        for (String endpoint : sparqlEndPoints) {

            //System.out.println(query);
            ResultSet rs = SPARQLQueryAgent.searchSPARQL(sparql, endpoint);
            // try {
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                if (qs.get("?d").asNode() instanceof Node_Blank)
                    continue;
                String domain = qs.get("?d").toString();
                //Object o = qs.get("?o");
                /*if(o==null)
                System.out.println();*/

                if (!cands.contains(domain)) {
                    //System.out.println(/*qs.get("?s").asNode().getBlankNodeLabel()+*/","+qs.get("?s").toString());
                    cands.add(domain);
                }

                //System.out.println(subject + "\t" + object);
            }
            /* } catch (Exception e) {
                e.printStackTrace();
            }*/
            //System.out.println();
        }
        /*
       not exhaustively try all n-gram combinations of keywords. as long as one matches, stop
        */

        try {
            cache.cache(String.valueOf(CommonSPARQLQueries.createQueryFindSharedDomainsBetweenProperties(uri1, uri2).hashCode()),
                    cands, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cands;
    }

    @Override
    protected List<String> findRelationBetweenInCache(String uri1, String uri2) {
        String sparql = CommonSPARQLQueries.createQueryFindSharedDomainsBetweenProperties(uri1, uri2);
        String idInCache = String.valueOf(sparql.hashCode());
        try {
            return (List<String>)cache.retrieve(idInCache);
            //return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
