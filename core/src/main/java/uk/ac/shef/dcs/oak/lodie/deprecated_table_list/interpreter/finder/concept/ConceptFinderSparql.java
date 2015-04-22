package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.concept;

import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.triplesearch.triple.SPARQLQueryAgent;
import uk.ac.shef.dcs.oak.triplesearch.util.CommonSPARQLQueries;
import uk.ac.shef.dcs.oak.triplesearch.util.StopLists;
import uk.ac.shef.dcs.oak.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 28/02/13
 * Time: 14:57
 */
public class ConceptFinderSparql extends ConceptFinder {

    protected String[] sparqlEndPoints;

    public ConceptFinderSparql(QueryCache cache, String... sparqlEndPoints) {
        super(cache);
        this.sparqlEndPoints = sparqlEndPoints;
    }

    @Override
    /**
     * String[0] - concept; [1] - equivalent concept
     */
    protected List<String[]> findCandidatesInKB(String keyword) {
        List<String[]> cands = new ArrayList<String[]>();
        List<String> keywords = getNGram(keyword);

        for (String key : keywords) {

            List<String> sparql = CommonSPARQLQueries.createQueryBatchFindClassAndEquivWithKeywordTBox(key);
            for (String endpoint : sparqlEndPoints) {
                for (String query : sparql) {
                    //System.out.println(query);
                    ResultSet rs = SPARQLQueryAgent.searchSPARQL(query, endpoint);
                    // try {
                    while (rs.hasNext()) {
                        QuerySolution qs = rs.next();
                        if (qs.get("?s").asNode() instanceof Node_Blank)
                            continue;
                        String concept = qs.get("?s").toString();
                        RDFNode equiv = qs.get("?o");
                        String eq = "";
                        if (equiv != null && !(equiv.asNode() instanceof Node_Blank)
                                && !StopLists.isMeaninglessClass(equiv.toString()))
                        eq = equiv.toString();

                        String[] toAdd = new String[]{concept, eq};
                        if (!StopLists.isMeaninglessClass(concept)&&
                                !CollectionUtils.array_list_contains(cands, toAdd))
                            cands.add(toAdd);

                    }

                }
            }
            /*
           not exhaustively try all n-gram combinations of keywords. as long as one matches, stop
            */
            if (cands.size() > 0)
                break;
        }

        try {
            cache.cache(String.valueOf(
                    CommonSPARQLQueries.createQueryFindClassWithKeywordTBox(keyword).hashCode()
            ),
                    cands, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cands;


    }

    @Override
    protected List<String[]> findCandidatesInCache(String keyword) {
        String sparql = CommonSPARQLQueries.createQueryFindClassWithKeywordTBox(keyword);
        String idInCache = String.valueOf(sparql.hashCode());
        try {
            return (List<String[]>) cache.retrieve(idInCache);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
