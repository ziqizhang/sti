package uk.ac.shef.dcs.kbsearch.sparql;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.KBSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by - on 04/05/2016.
 */
public abstract class SPARQLSearch extends KBSearch {

    protected String sparqlEndpoint;
    /**
     * @param fuzzyKeywords   given a query string, kbsearch will firstly try to fetch results matching the exact query. when no match is
     *                        found, you can set fuzzyKeywords to true, to let kbsearch to break the query string based on conjunective words.
     *                        So if the query string is "tom and jerry", it will try "tom" and "jerry"
     * @param cacheEntity     the solr instance to cache retrieved entities from the kb. pass null if not needed
     * @param cacheConcept    the solr instance to cache retrieved classes from the kb. pass null if not needed
     * @param cacheProperty   the solr instance to cache retrieved properties from the kb. pass null if not needed
     * @param cacheSimilarity the solr instance to cache computed semantic similarity between entity and class. pass null if not needed
     * @throws IOException
     */
    public SPARQLSearch(String sparqlEndpoint, Boolean fuzzyKeywords,
                        EmbeddedSolrServer cacheEntity,
                        EmbeddedSolrServer cacheConcept,
                        EmbeddedSolrServer cacheProperty,
                        EmbeddedSolrServer cacheSimilarity) throws IOException {
        super(fuzzyKeywords, cacheEntity, cacheConcept, cacheProperty, cacheSimilarity);
        this.sparqlEndpoint=sparqlEndpoint;
    }

    protected List<String> createRegexQuery(String content){
        String query = "SELECT DISTINCT ?s ?o WHERE {"+
            "?s <http://www.w3.org/2000/01/rdf-schema#label> ?o ."+
            "FILTER ( regex (str(?term), \"\\b"+content+"\\b\", \"i\") )";
        List<String> rs = new ArrayList<>();
        rs.add(query);
        return rs;
    }

    protected List<String> createExactMatchQueries(String content){
        return null;
    }
}
