package uk.ac.shef.dcs.kbsearch.sparql;

import javafx.util.Pair;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.Levenshtein;
import uk.ac.shef.dcs.kbsearch.KBSearch;

import java.io.IOException;
import java.util.*;


/**
 * test queries:
 *
 * SELECT DISTINCT ?s ?o WHERE {
 ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o .
 FILTER ( regex (str(?o), "\\bcat\\b", "i") ) }


 SELECT DISTINCT ?s WHERE {
 ?s <http://www.w3.org/2000/01/rdf-schema#label> "Nature Cat"@en .
 }

 SELECT DISTINCT ?p ?o WHERE {
 wd:Q21043336 ?p ?o .
 }


 */
public abstract class SPARQLSearch extends KBSearch {

    protected String sparqlEndpoint;

    protected StringMetric stringMetric = new Levenshtein();
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

    protected String createRegexQuery(String content){
        String query = "SELECT DISTINCT ?s ?o WHERE {"+
            "?s "+RDFEnum.RELATION_HASTYPE.getString()+" ?o ."+
            "FILTER ( regex (str(?o), \"\\b"+content+"\\b\", \"i\") ) }";
        return query;
    }

    protected String createRegexQuery(String content, String... types){
        StringBuilder query = new StringBuilder("SELECT DISTINCT ?s ?o WHERE {").append(
                "?s ").append(RDFEnum.RELATION_HASTYPE.getString()).append(" ?o .").append("\n");

        if(types.length>0){
            query.append("{?s a <").append(types[0]).append(">}\n");
            for(int i=1; i<types.length; i++){
                query.append("UNION { ?s a <").append(types[i]).append(">}\n");
            }
            query.append(".\n");
        }

        query.append("FILTER ( regex (str(?o), \"\\b").append(content).append("\\b\", \"i\") ) }");
        return query.toString();
    }

    protected String createExactMatchQueries(String content){
        String query = "SELECT DISTINCT ?s WHERE {"+
            "?s "+RDFEnum.RELATION_HASTYPE.getString()+" \""+content+"\"@en . }";

        return query;

    }

    protected String createExactMatchQueries(String content, String... types){
        StringBuilder query = new StringBuilder("SELECT DISTINCT ?s WHERE {").append(
                "?s ").append(RDFEnum.RELATION_HASTYPE.getString()).append(" \"").append(content).append("\"@en . ");
        if(types.length>0){
            query.append("{?s a <").append(types[0]).append(">}\n");
            for(int i=1; i<types.length; i++){
                query.append("UNION { ?s a <").append(types[i]).append(">}\n");
            }
            query.append(".\n");
        }

        return query.toString();

    }

    protected List<Pair<String, String>> queryByLabel(String sparqlQuery){
        org.apache.jena.query.Query query = QueryFactory.create(sparqlQuery);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);

        List<Pair<String, String>> out = new ArrayList<>();
        ResultSet rs = qexec.execSelect();
        if(rs.hasNext()){
            QuerySolution qs = rs.next();
            RDFNode range = qs.get("?s");
            String r = range.toString();
            RDFNode domain = qs.get("?o");
            String d=null;
            if(domain!=null)
                d=domain.toString();
            out.add(new Pair<>(r,d));
        }
        return out;
    }

    protected void rank(List<Pair<String, String>> candidates, String originalQueryLabel){
        final Map<Pair<String, String>, Double> scores = new HashMap<>();
        for(Pair<String, String> p : candidates){
            String label = p.getValue();
            double s = stringMetric.compare(label, originalQueryLabel);
            scores.put(p, s);
        }

        Collections.sort(candidates, (o1, o2) -> {
            Double s1 = scores.get(o1);
            Double s2 = scores.get(o2);
            return s2.compareTo(s1);
        });
    }
}
