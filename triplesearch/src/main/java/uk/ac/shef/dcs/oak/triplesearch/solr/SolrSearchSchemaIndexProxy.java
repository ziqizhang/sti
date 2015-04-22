package uk.ac.shef.dcs.oak.triplesearch.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 06/03/13
 * Time: 12:29
 */
public class SolrSearchSchemaIndexProxy {
    public static final String SCHEMA_INDEX_CLASS_DEPTH="classDepth";
    public static final String SCHEMA_INDEX_CLASS_LOCALNAME_TEXT = "classLocalname_text";
    public static final String SCHEMA_INDEX_CLASS_LOCALNAME = "classLocalname";
    public static final String SCHEMA_INDEX_CLASS_URI = "classURI";
    public static final String SCHEMA_INDEX_EQUIVALENT_CLASS = "classEqvlt";
    public static final String SCHEMA_INDEX_PROPERTY_LOCALNAME_TEXT = "propertyLocalname_text";
    public static final String SCHEMA_INDEX_PROPERTY_LOCALNAME = "propertyLocalname";
    public static final String SCHEMA_INDEX_PROPERTY_URI = "propertyURI";


    public static final String SCHEMA_INDEX_CORE_CLASS = "class";
    public static final String SCHEMA_INDEX_CORE_STATEMENT = "statement";
    public static final String SCHEMA_INDEX_CORE_PROPERTY = "property";


    public static String searchPropertyLocalname(String propertyURI, SolrServer server) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", SCHEMA_INDEX_PROPERTY_URI + ":" + ClientUtils.escapeQueryChars(propertyURI));
        params.set("rows", "1000");    //set a number high enough such that all possible results can be retrieved

        List<String> result = new ArrayList<String>();

        try {
            QueryResponse response = server.query(params);
            SolrDocumentList results = response.getResults();
            for (int i = 0; i < results.size(); ++i) {
                SolrDocument d = results.get(i);
                Object value = d.getFieldValue(SCHEMA_INDEX_PROPERTY_LOCALNAME);
                if (value != null && !result.contains(value.toString()))
                    return value.toString();
            }
        } catch (SolrServerException sse) {
            sse.printStackTrace();
        }
        return null;
    }

    public static String searchClassLocalname(String classURI, SolrServer server) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", SCHEMA_INDEX_CLASS_URI + ":" + ClientUtils.escapeQueryChars(classURI));
        params.set("rows", "1000");    //set a number high enough such that all possible results can be retrieved

        List<String> result = new ArrayList<String>();

        try {
            QueryResponse response = server.query(params);
            SolrDocumentList results = response.getResults();
            for (int i = 0; i < results.size(); ++i) {
                SolrDocument d = results.get(i);
                Object value = d.getFieldValue(SCHEMA_INDEX_CLASS_LOCALNAME);
                if (value != null && !result.contains(value.toString()))
                    return value.toString();
            }
        } catch (SolrServerException sse) {
            sse.printStackTrace();
        }
        return null;
    }

    /**
     * Max # of results returned is 1000 ONLY
     *
     * @param keyword
     * @param server
     * @return
     */
    public static List<String> searchClassesByKeyword(String keyword, SolrServer server) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", SCHEMA_INDEX_CLASS_LOCALNAME_TEXT + ":" + keyword);
        params.set("rows", "1000");    //set a number high enough such that all possible results can be retrieved

        List<String> result = new ArrayList<String>();

        try {
            QueryResponse response = server.query(params);
            SolrDocumentList results = response.getResults();
            for (int i = 0; i < results.size(); ++i) {
                SolrDocument d = results.get(i);
                Object value = d.getFieldValue(SCHEMA_INDEX_CLASS_URI);
                if (value != null && !result.contains(value.toString()))
                    result.add(value.toString());
                Collection<Object> values = d.getFieldValues(SCHEMA_INDEX_EQUIVALENT_CLASS);
                if (values != null) {
                    for (Object o : values) {
                        if (!result.contains(o.toString()))
                            result.add(o.toString());
                    }
                }
            }
        } catch (SolrServerException sse) {
            sse.printStackTrace();
        }

        return result;
    }

    /*
    returns -1 if no info recorded
     */
    public static int searchClassDepth(String concept, SolrServer server) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", SCHEMA_INDEX_CLASS_URI + ":" + concept);
        params.set("rows", "1000");    //set a number high enough such that all possible results can be retrieved

        List<String> result = new ArrayList<String>();

        try {
            QueryResponse response = server.query(params);
            SolrDocumentList results = response.getResults();
            for (int i = 0; i < results.size(); ++i) {
                SolrDocument d = results.get(i);
                Object value = d.getFieldValue(SCHEMA_INDEX_CLASS_DEPTH);
                if (value != null)
                    return Integer.valueOf(value.toString());
            }
        } catch (SolrServerException sse) {
            sse.printStackTrace();
        }

        return -1;
    }

    /**
     * Max # of results returned is 1000 ONLY
     *
     * @param keyword
     * @param server
     * @return
     */
    public static List<String> searchPropertiesByKeyword(String keyword, SolrServer server) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", SCHEMA_INDEX_PROPERTY_LOCALNAME_TEXT + ":" + keyword);
        params.set("rows", "1000");    //set a number high enough such that all possible results can be retrieved

        List<String> result = new ArrayList<String>();

        try {
            QueryResponse response = server.query(params);
            SolrDocumentList results = response.getResults();
            for (int i = 0; i < results.size(); ++i) {
                SolrDocument d = results.get(i);
                Object value = d.getFieldValue(SCHEMA_INDEX_PROPERTY_URI);
                if (value != null && !result.contains(value.toString()))
                    result.add(value.toString());
            }
        } catch (SolrServerException sse) {
            sse.printStackTrace();
        }

        return result;
    }


}
