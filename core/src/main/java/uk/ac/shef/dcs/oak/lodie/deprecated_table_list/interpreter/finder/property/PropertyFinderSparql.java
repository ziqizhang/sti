package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property;

import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import org.apache.commons.lang3.text.WordUtils;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.triplesearch.triple.SPARQLQueryAgent;
import uk.ac.shef.dcs.oak.triplesearch.util.CommonSPARQLQueries;
import uk.ac.shef.dcs.oak.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 07/03/13
 * Time: 16:21
 */
public class PropertyFinderSparql extends PropertyFinder {
    protected String[] sparqlEndPoints;

    public PropertyFinderSparql(QueryCache cache, String... sparqlEndPoints) {
        super(cache);
        this.sparqlEndPoints = sparqlEndPoints;
    }

    @Override
    /*
    Default property finder looks for camelCase

    This will find property and also equivalent if any
     */
    protected List<String[]> findCandidatesInKB(String keyword) {
        List<String[]> cands = new ArrayList<String[]>();

        List<String> ngrams = getNGram(keyword);
        List<String> keywords =new ArrayList<String>();
        for(String n : ngrams){
            String[] parts = n.split("\\s+");
            String concat="";
            for(int i=0; i<parts.length;i++){
                if(i>0)
                    concat=concat+ WordUtils.capitalize(parts[i]);
                else
                    concat = WordUtils.uncapitalize(parts[i]);
                keywords.add(concat);
            }
        }


        for (String key : keywords) {
            List<String> sparql = CommonSPARQLQueries.createQueryBatchFindPropertyAndEquivWithKeywordTBox(key);
            for (String endpoint : sparqlEndPoints) {
                for (String query : sparql) {
                    //System.out.println(query);
                    ResultSet rs = SPARQLQueryAgent.searchSPARQL(query, endpoint);
                    // try {
                    while (rs.hasNext()) {
                        QuerySolution qs = rs.next();
                        if (qs.get("?s").asNode() instanceof Node_Blank)
                            continue;
                        String subject = qs.get("?s").toString();

                        String object = qs.get("?o") == null ? "" : qs.get("?o").toString();

                        String[] toAdd = new String[]{subject, object};

                        if (!CollectionUtils.array_list_contains(cands,toAdd))
                            cands.add(toAdd);
                    }
                }
            }

            if (cands.size() > 0)
                break;
        }

        try {
            cache.cache(String.valueOf(CommonSPARQLQueries.createQueryBatchFindPropertyAndEquivWithKeywordTBox(keyword).toString().hashCode()),
                    cands, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cands;


    }

    @Override
    protected List<String[]> findCandidatesInCache(String keyword) {
        String sparql = CommonSPARQLQueries.createQueryBatchFindPropertyAndEquivWithKeywordTBox(keyword).toString();
        String idInCache = String.valueOf(sparql.hashCode());
        try {
            return (List<String[]>)cache.retrieve(idInCache);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
