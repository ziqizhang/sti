package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property;

import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.CandidateFinder;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.triplesearch.triple.SPARQLQueryAgent;
import uk.ac.shef.dcs.oak.triplesearch.util.CommonSPARQLQueries;
import uk.ac.shef.dcs.oak.triplesearch.util.StopLists;
import uk.ac.shef.dcs.oak.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 26/04/13
 * Time: 09:54
 */
public class PredicateOfObjectAndPredDomainFinderSparql extends CandidateFinder {

    protected String[] sparqlEndPoints;

    public PredicateOfObjectAndPredDomainFinderSparql(QueryCache cache, String... sparqlEndPoints) {
        super(cache);
        this.sparqlEndPoints = sparqlEndPoints;
    }

    @Override
    /**
     * String[0] - pred; [1] - d; [2] - equivalent
     */
    protected List<String[]> findCandidatesInKB(String keyword) {
        List<String[]> output = new ArrayList<String[]>();
        List<String> keywords = getVariants(keyword);

        for (String key : keywords) {

            String sparql = CommonSPARQLQueries.createQueryFindPredicatesOfObjectAndPredDomain(key);
            for (String endpoint : sparqlEndPoints) {

                ResultSet rs = SPARQLQueryAgent.searchSPARQL(sparql, endpoint);
                // try {
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    if (qs.get("?p").asNode() instanceof Node_Blank)
                        continue;
                    String predicate = qs.get("?p").toString();
                    if (predicate.contains("wiki"))
                        continue;

                    RDFNode domain = qs.get("?d");
                    String d = "";
                    if (domain != null && !(domain.asNode() instanceof Node_Blank))
                        d = domain.toString();
                    if(StopLists.isMeaninglessClass(d))
                        continue;

                    RDFNode equivOfd = qs.get("?e");
                    String ed = "";
                    if (equivOfd != null && !(equivOfd.asNode() instanceof Node_Blank)&&
                            !StopLists.isMeaninglessClass(equivOfd.toString()))
                        ed = equivOfd.toString();

                    String[] toAdd = new String[]{predicate, d, ed};

                    if (!CollectionUtils.array_list_contains(output, toAdd))
                        output.add(toAdd);
                    //System.out.println(subject + "\t" + object);
                }
            }
            /*
           not exhaustively try all n-gram combinations of keywords. as long as one matches, stop
            */
            if (output.size() > 0)
                break;
        }

        try {
            cache.cache(String.valueOf(
                    CommonSPARQLQueries.createQueryFindClassWithKeywordTBox(keyword).hashCode()
            ),
                    output, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;


    }

    @Override
    protected List<String[]> findCandidatesInCache(String keyword) {
        String sparql = CommonSPARQLQueries.createQueryFindPredicatesOfObjectAndPredDomain(keyword);
        String idInCache = String.valueOf(sparql.hashCode());
        try {
            return (List<String[]>) cache.retrieve(idInCache);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
