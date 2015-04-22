package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 05/03/13
 * Time: 12:09
 */
public class QueryCache {

    private SolrServer server;
    private static final String idFieldName = "id";
    private static final String valueFieldName = "value";

    public QueryCache(String solrHomePath, String coreName) {
        File configFile = new File(solrHomePath + File.separator + "solr.xml");
        CoreContainer container = new CoreContainer(solrHomePath,
                configFile);
        server = new EmbeddedSolrServer(container, coreName);
    }

    public void cache(String queryId, Object values, boolean commit) throws IOException, SolrServerException {
        SolrInputDocument newDoc = new SolrInputDocument();
        newDoc.addField(idFieldName, queryId);
        newDoc.addField(valueFieldName, SerializableUtils.serializeBase64(values));
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
    public List retrieve(String queryId) throws SolrServerException, ClassNotFoundException, IOException {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", idFieldName + ":" + ClientUtils.escapeQueryChars(queryId));
        params.set("fl",idFieldName+","+valueFieldName);

        QueryResponse response = server.query(params);
        if (response.getResults().getNumFound() == 0)
            return null;

        List<String[]> rs = new ArrayList<String[]>();
        SolrDocument doc = response.getResults().get(0);
        if(doc.getFieldValue(valueFieldName)==null)
            return rs;

        Object data = doc.getFieldValue(valueFieldName);
        Object dataBytes = ((ArrayList)data).get(0);

        Object object =  SerializableUtils.deserializeBase64((byte[])dataBytes);
        return (List) object;
    }

    public static String toCacheId(String qParam, String targetFieldName) {
        return qParam.hashCode() + "_" + targetFieldName.hashCode();
    }

}
