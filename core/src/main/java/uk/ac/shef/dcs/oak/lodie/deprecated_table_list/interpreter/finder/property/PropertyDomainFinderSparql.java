package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property;

import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.CandidateFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.triplesearch.triple.SPARQLQueryAgent;
import uk.ac.shef.dcs.oak.triplesearch.util.CommonSPARQLQueries;
import uk.ac.shef.dcs.oak.triplesearch.util.StopLists;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 26/04/13
 * Time: 23:18
 */
public class PropertyDomainFinderSparql extends CandidateFinder {
    protected String[] sparqlEndPoints;

    public PropertyDomainFinderSparql(QueryCache cache, String... sparqlEndPoints) {
        super(cache);
        this.sparqlEndPoints = sparqlEndPoints;
    }

    @Override
    /*
   Default property finder looks for camelCase
    */
    protected List<String> findCandidatesInKB(String keyword) {
        List<String> cands = new ArrayList<String>();


        String sparql = CommonSPARQLQueries.createQueryFindDomainOfProperty(keyword);
        for (String endpoint : sparqlEndPoints) {

                //System.out.println(query);
                ResultSet rs = SPARQLQueryAgent.searchSPARQL(sparql, endpoint);
                // try {
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    if (qs.get("?d").asNode() instanceof Node_Blank)
                        continue;
                    String subject = qs.get("?d").toString();
                    if(StopLists.isMeaninglessClass(subject))
                        continue;

                    if (!cands.contains(subject))
                        cands.add(subject);
                    //System.out.println(subject + "\t" + object);
                }
                /* } catch (Exception e) {
                    e.printStackTrace();
                }*/
                //System.out.println();

            /*
           not exhaustively try all n-gram combinations of keywords. as long as one matches, stop
            */
            if (cands.size() > 0)
                break;
        }

        try {
            cache.cache(String.valueOf(CommonSPARQLQueries.createQueryFindDomainOfProperty(keyword).toString().hashCode()),
                    cands, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cands;


    }

    @Override
    protected List<String> findCandidatesInCache(String keyword) {
        String sparql = CommonSPARQLQueries.createQueryFindDomainOfProperty(keyword).toString();
        String idInCache = String.valueOf(sparql.hashCode());
        try {
            return (List<String>)cache.retrieve(idInCache);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
