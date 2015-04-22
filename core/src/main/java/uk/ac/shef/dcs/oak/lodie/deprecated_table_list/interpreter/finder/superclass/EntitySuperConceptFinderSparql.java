package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.superclass;

import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.entity.EntityFinderSparql;
import uk.ac.shef.dcs.oak.triplesearch.triple.SPARQLQueryAgent;
import uk.ac.shef.dcs.oak.triplesearch.util.StopLists;
import uk.ac.shef.dcs.oak.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 11/03/13
 * Time: 17:05
 */
public class EntitySuperConceptFinderSparql extends SuperConceptFinder {
    protected String[] sparqlEndPoints;

    public EntitySuperConceptFinderSparql(QueryCache cache, String... sparqlEndPoints) {
        super(cache);
        this.sparqlEndPoints = sparqlEndPoints;
    }

    @Override
    //this will find concepts also their equivalent concepts
    protected List<String[]> findCandidatesInKB(String uri) {
        List<String[]> cands = new ArrayList<String[]>();

        String sparql = EntityFinderSparql.createCacheQueryId_findSuperclassesAndEquivOfURI(uri);
        for (String endpoint : sparqlEndPoints) {

            //System.out.println(query);
            ResultSet rs = SPARQLQueryAgent.searchSPARQL(sparql, endpoint);
            // try {
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                if (qs.get("?o").asNode() instanceof Node_Blank)
                    continue;
                String object = qs.get("?o").toString();
                if (StopLists.isMeaninglessClass(object))
                    continue;

                RDFNode e = qs.get("?e");
                String equiv = "";
                if (e!=null && !(e.asNode() instanceof Node_Blank) &&
                        !StopLists.isMeaninglessClass(e.toString())) {
                    equiv =e.toString();
                }

                String[] toAdd = new String[]{object, equiv};
                if (!CollectionUtils.array_list_contains(cands,toAdd))
                    cands.add(toAdd);
                //System.out.println(subject + "\t" + object);
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
    protected List<String[]> findCandidatesInCache(String uri) {
        String sparql = EntityFinderSparql.createCacheQueryId_findSuperclassesAndEquivOfURI(uri);
        String idInCache = String.valueOf(sparql.hashCode());
        try {
            return (List<String[]>)cache.retrieve(idInCache);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
