package uk.ac.shef.dcs.oak.triplesearch.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import uk.ac.shef.dcs.oak.triplesearch.util.StopLists;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 05/03/13
 * Time: 15:00
 */
public class SolrSearchTripleIndexProxy {
    public static final int DEFAULT_ROWS_IN_RESULT = 1000;

    public static final String TRIPLE_INDEX_SUBJECT_TEXT = "subject_text";
    public static final String TRIPLE_INDEX_SUBJECT = "subject";
    public static final String TRIPLE_INDEX_PREDICATE = "predicate";
    public static final String TRIPLE_INDEX_OBJECT = "object";
    public static final String URI_RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    public static final String URI_RDFS_CLASS = "http://www.w3.org/2000/01/rdf-schema#Class";
    public static final String URI_OWL_CLASS = "http://www.w3.org/2002/07/owl#Class";
    public static final String URI_RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
    public static final String URI_FOAF_NAME = "http://xmlns.com/foaf/0.1/#name";
    public static final String URI_DC_TITLE = "http://purl.org/dc/elements/1.1/#title";
    public static final String URI_SKOS_PRELABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";
    public static final String URI_SKOS_ALTLABEL = "http://www.w3.org/2004/02/skos/core#altLabel";
    public static final String URI_FB_NAME = "http://rdf.freebase.com/ns/type.object.name";
    public static final String URI_FB_NAMEID = "http://rdf.freebase.com/ns/m.06b";
    public static final String URI_BASEKB_NAME = "http://rdf.basekb.com/ns/type.object.name";
    public static final String URI_BASEKB_NAMEID = "http://rdf.basekb.com/ns/m.06b";


    public static List<String> searchInstanceLabels(String instanceURI, SolrServer server) {
        ModifiableSolrParams params = new ModifiableSolrParams();

        String q = TRIPLE_INDEX_SUBJECT + ":<" + ClientUtils.escapeQueryChars(instanceURI) + "> AND "
                + "(" + TRIPLE_INDEX_PREDICATE + ":" + ClientUtils.escapeQueryChars("<" + URI_RDFS_LABEL + ">") + " OR " +
                ClientUtils.escapeQueryChars("<" + URI_FOAF_NAME + ">") + " OR " + TRIPLE_INDEX_PREDICATE + ":" +
                ClientUtils.escapeQueryChars("<" + URI_DC_TITLE + ">") + " OR " + TRIPLE_INDEX_PREDICATE + ":" +
                ClientUtils.escapeQueryChars("<" + URI_SKOS_PRELABEL + ">") + " OR " + TRIPLE_INDEX_PREDICATE + ":" +
                ClientUtils.escapeQueryChars("<" + URI_SKOS_ALTLABEL + ">") + " OR " + TRIPLE_INDEX_PREDICATE + ":" +
                ClientUtils.escapeQueryChars("<" + URI_FB_NAME + ">") + " OR " + TRIPLE_INDEX_PREDICATE + ":" +
                ClientUtils.escapeQueryChars("<" + URI_FB_NAMEID + ">") + " OR " + TRIPLE_INDEX_PREDICATE + ":" +
                ClientUtils.escapeQueryChars("<" + URI_BASEKB_NAME + ">") + " OR " + TRIPLE_INDEX_PREDICATE + ":" +
                ClientUtils.escapeQueryChars("<" + URI_BASEKB_NAMEID + ">") + ")";
        int start = 0;
        params.set("q", q);
        params.set("rows", DEFAULT_ROWS_IN_RESULT);
        params.set("start", start);

        List<String> container = new ArrayList<String>();
        try {
            QueryResponse response = null;
            do {
                response = server.query(params);
                SolrDocumentList list = response.getResults();
                for (int i = 0; i < list.size(); i++) {
                    SolrDocument doc = list.get(i);
                    String object = doc.getFieldValue(TRIPLE_INDEX_OBJECT).toString();
                    if (object != null && object.indexOf("@") != -1 && !object.substring(object.lastIndexOf("@")).equalsIgnoreCase("en")) {
                        continue;
                    }
                    container.add(object);
                }

                start = start + DEFAULT_ROWS_IN_RESULT;
                params.set("start", start);
            } while (start < response.getResults().getNumFound());
        } catch (SolrServerException sse) {
            sse.printStackTrace();
        }

        return container;
    }

    public static SortedMap<String, List<String>> searchInstancesByKeywords(String keyword, SolrServer server) {
        ModifiableSolrParams params = new ModifiableSolrParams();

        int start = 0;

        params.set("q", TRIPLE_INDEX_SUBJECT_TEXT + ":" + keyword + " AND "
                + TRIPLE_INDEX_PREDICATE + ":" +
                ClientUtils.escapeQueryChars("<" + URI_RDF_TYPE + ">"));
        params.set("rows", DEFAULT_ROWS_IN_RESULT);
        params.set("start", start);

        TreeMap<String, List<String>> container = new TreeMap<String, List<String>>();
        try {
            QueryResponse response = null;
            do {
                response = server.query(params);
                SolrDocumentList list = response.getResults();
                for (int i = 0; i < list.size(); i++) {
                    SolrDocument doc = list.get(i);
                    String subjectURI = doc.getFieldValue(TRIPLE_INDEX_SUBJECT).toString();
                    String objectURI = doc.getFieldValue(TRIPLE_INDEX_OBJECT).toString();
                    List<String> classes = container.get(subjectURI);
                    if (classes == null)
                        classes = new ArrayList<String>();
                    if (!classes.contains(objectURI))
                        classes.add(objectURI);
                    container.put(subjectURI, classes);
                }

                start = start + DEFAULT_ROWS_IN_RESULT;
                params.set("start", start);
            } while (start < response.getResults().getNumFound());
        } catch (SolrServerException sse) {
            sse.printStackTrace();
        }

        return container;
    }

    /*
    finds classes in the triple index
     */
    public static SortedMap<String, List<String>> searchClassesByKeywords(String keyword, SolrServer server) {
        ModifiableSolrParams params = new ModifiableSolrParams();

        int start = 0;
        params.set("q", TRIPLE_INDEX_SUBJECT_TEXT + ":" + keyword + " AND "
                + TRIPLE_INDEX_PREDICATE + ":" + ClientUtils.escapeQueryChars("<" + URI_RDF_TYPE + ">") + " AND "
                + "(" + TRIPLE_INDEX_OBJECT + ":" + ClientUtils.escapeQueryChars("<" + URI_OWL_CLASS + ">") + " OR " +
                TRIPLE_INDEX_OBJECT + ":" +
                ClientUtils.escapeQueryChars("<" + URI_RDFS_CLASS + ">") + ")");
        params.set("rows", DEFAULT_ROWS_IN_RESULT);
        params.set("start", start);

        TreeMap<String, List<String>> container = new TreeMap<String, List<String>>();
        try {
            QueryResponse response = null;
            do {
                response = server.query(params);
                SolrDocumentList list = response.getResults();
                for (int i = 0; i < list.size(); i++) {
                    SolrDocument doc = list.get(i);
                    String subjectURI = doc.getFieldValue(TRIPLE_INDEX_SUBJECT).toString();
                    String objectURI = doc.getFieldValue(TRIPLE_INDEX_OBJECT).toString();
                    List<String> classes = container.get(subjectURI);
                    if (classes == null)
                        classes = new ArrayList<String>();
                    if (!classes.contains(objectURI))
                        classes.add(objectURI);
                    container.put(subjectURI, classes);
                }

                start = start + DEFAULT_ROWS_IN_RESULT;
                params.set("start", start);
            } while (start < response.getResults().getNumFound());
        } catch (SolrServerException sse) {
            sse.printStackTrace();
        }

        return container;
    }

    public static List<String> searchRelationBetween(String uri1, String uri2, SolrServer server) {
        ModifiableSolrParams params = new ModifiableSolrParams();

        int start = 0;
        params.set("q", TRIPLE_INDEX_SUBJECT + ":" + ClientUtils.escapeQueryChars(uri1) + " AND "
                + TRIPLE_INDEX_OBJECT + ":" + ClientUtils.escapeQueryChars(uri2));
        params.set("rows", DEFAULT_ROWS_IN_RESULT);
        params.set("start", start);

        List<String> rs = new ArrayList<String>();
        try {
            QueryResponse response = null;
            do {
                response = server.query(params);
                SolrDocumentList list = response.getResults();
                for (int i = 0; i < list.size(); i++) {
                    SolrDocument doc = list.get(i);
                    String predicate = doc.getFieldValue(TRIPLE_INDEX_PREDICATE).toString();
                    if (!rs.contains(predicate))
                        rs.add(predicate);
                }

                start = start + DEFAULT_ROWS_IN_RESULT;
                params.set("start", start);
            } while (start < response.getResults().getNumFound());
        } catch (SolrServerException sse) {
            sse.printStackTrace();
        }

        return rs;
    }

    public static List<String> searchSuperclassesOf(String uri, SolrServer server) {
        ModifiableSolrParams params = new ModifiableSolrParams();

        int start = 0;
        params.set("q", TRIPLE_INDEX_SUBJECT + ":" + ClientUtils.escapeQueryChars(uri) + " AND "
                + TRIPLE_INDEX_PREDICATE + ":" + ClientUtils.escapeQueryChars(URI_RDF_TYPE));
        params.set("rows", DEFAULT_ROWS_IN_RESULT);
        params.set("start", start);

        List<String> rs = new ArrayList<String>();
        try {
            QueryResponse response = null;
            do {
                response = server.query(params);
                SolrDocumentList list = response.getResults();
                for (int i = 0; i < list.size(); i++) {
                    SolrDocument doc = list.get(i);
                    String predicate = doc.getFieldValue(TRIPLE_INDEX_OBJECT).toString();
                    if (StopLists.isMeaninglessClass(predicate))
                        continue;
                    if (!rs.contains(predicate))
                        rs.add(predicate);
                }

                start = start + DEFAULT_ROWS_IN_RESULT;
                params.set("start", start);
            } while (start < response.getResults().getNumFound());
        } catch (SolrServerException sse) {
            sse.printStackTrace();
        }

        return rs;
    }
}
