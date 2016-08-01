package uk.ac.shef.dcs.util;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 21/01/14
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */
public class SolrCache {

    private EmbeddedSolrServer server;
    private static final String idFieldName = "id";
    private static final String valueFieldName = "value";

    public SolrCache(EmbeddedSolrServer server) {
        this.server=server;
    }

    //queryId - what is the query
    //interpretationResults: key in the map is a value from a single cell in a table row; value in the map is the score as a measure of confidence that cell should be main subject
    public void cache(String queryId, Object obj, boolean commit) throws IOException, SolrServerException {
        SolrInputDocument newDoc = new SolrInputDocument();
        newDoc.addField(idFieldName, queryId);
        newDoc.addField(valueFieldName, SerializationUtils.serializeBase64(obj));
       // newDoc.addField(valueTextFieldName, ojbectToString(obj));
        server.add(newDoc);
        if(commit)
            server.commit();
    }


    public void commit() throws IOException, SolrServerException {
        server.commit();
    }

    public void shutdown() throws IOException {
        server.close();
    }
    /**
     * @param queryId
     * @return null if no cache has been created for this queryId;
     *         an empty List object if there are no results for the queryId (i.e., the query has been executed before but no results
     *         were found to match the query);
     */
    public Object retrieve(String queryId) throws SolrServerException, ClassNotFoundException, IOException {
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

        Object object =  SerializationUtils.deserializeBase64((byte[]) dataBytes);
        return object;
    }

}