package uk.ac.shef.dcs.oak.lodie.table.interpreter.maincol;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import uk.ac.shef.dcs.oak.util.SerializableUtils;
import uk.ac.shef.dcs.oak.websearch.bing.v2.WebSearchResultDoc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 caches interpretation results for a set of table cell values on the same row.
 The goal of headerwebsearchmatcher is to understand which column header is likely to be main subject column.
 It takes cells from each independent row, cast them as a query to websearch engines (e.g., bing, goolge), then
 interpret the search results to understand which cell value is likely to be the main subject of that row.
 */
class HeaderWebsearchMatcherCache {

    private SolrServer server;
    private static final String idFieldName = "id";
    private static final String valueFieldName = "value";


    public HeaderWebsearchMatcherCache(String solrHomePath, String coreName) {
        File configFile = new File(solrHomePath + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(solrHomePath,
                configFile);
        server = new EmbeddedSolrServer(container, coreName);
    }

    public HeaderWebsearchMatcherCache(SolrServer server){
        this.server=server;
    }

    //queryId - what is the query
    //interpretationResults: key in the map is a value from a single cell in a table row; value in the map is the score as a measure of confidence that cell should be main subject
    /*public void cache(String queryId, Map<String, Double> interpretationResults, boolean commit) throws IOException, SolrServerException {
        SolrInputDocument newDoc = new SolrInputDocument();
        newDoc.addField(idFieldName, queryId);
        newDoc.addField(valueFieldName, SerializableUtils.serializeBase64(interpretationResults));
        server.add(newDoc);
        if(commit)
            server.commit();
    }*/

    public void cache(String queryId, List<WebSearchResultDoc> doc, boolean commit) throws IOException, SolrServerException {
        SolrInputDocument newDoc = new SolrInputDocument();
        newDoc.addField(idFieldName, queryId);
        newDoc.addField(valueFieldName, SerializableUtils.serializeBase64(doc));
        server.add(newDoc);
        if(commit)
            server.commit();
    }

    public void commit() throws IOException, SolrServerException {
        server.commit();
    }

    public void shutdown() {
        server.shutdown();
    }
    /**
     * @param queryId
     * @return null if no cache has been created for this queryId;
     *         an empty List object if there are no results for the queryId (i.e., the query has been executed before but no results
     *         were found to match the query);
     */
    /*public Map<String, Double> retrieve(String queryId) throws SolrServerException, ClassNotFoundException, IOException {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", idFieldName + ":" + ClientUtils.escapeQueryChars(queryId));
        params.set("fl",idFieldName+","+valueFieldName);

        QueryResponse response = server.query(params);
        if (response.getResults().getNumFound() == 0)
            return null;

        SolrDocument doc = response.getResults().get(0);
        if(doc.getFieldValue(valueFieldName)==null)
            return null;

        Object data = doc.getFieldValue(valueFieldName);
        Object dataBytes = ((ArrayList)data).get(0);

        Object object =  SerializableUtils.deserializeBase64((byte[])dataBytes);
        return (Map<String, Double>) object;
    }*/

    public List<WebSearchResultDoc> retrieve(String queryId) throws SolrServerException, ClassNotFoundException, IOException {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", idFieldName + ":" + ClientUtils.escapeQueryChars(queryId));
        params.set("fl",idFieldName+","+valueFieldName);

        QueryResponse response = server.query(params);
        if (response.getResults().getNumFound() == 0)
            return null;

        SolrDocument doc = response.getResults().get(0);
        if(doc.getFieldValue(valueFieldName)==null)
            return null;

        Object data = doc.getFieldValue(valueFieldName);
        Object dataBytes = ((ArrayList)data).get(0);

        Object object =  SerializableUtils.deserializeBase64((byte[])dataBytes);
        return (List<WebSearchResultDoc>) object;
    }

    public static String toCacheId(String qParam, String targetFieldName) {
        return qParam.hashCode() + "_" + targetFieldName.hashCode();
    }

}
