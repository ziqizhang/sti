package com.sindice.query.v2;

import com.sindice.SindiceException;
import com.sindice.query.SearchQuery;
import com.sindice.result.LiveResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 21/09/12
 * Time: 14:54
 */
public class LiveQuery extends SearchQuery {


    /*
    Take from sindice:
    Name	    Description	                                Possible values	Default value
    url	        url to the document	                        string	        NA
    content	    content of the document	                    string	        NA
    doReasoning	enable disable reasoning over extracted data	0/1	        0
    explicit	if set to 1 explicit triples are included in output	0/1	    1
    implicit	if set to 1 inferred triples are included in output	0/1	    1
    maxExplicit	truncates number of returned explicit triples	number	    -1
    maxImplicit	truncates number of returned implicit triples	number	    -1
    ontologies	if set to 1 ontologies used during reasoning process are included in output	0/1	1
    extractors	if set to 1 a list of used extractors is included in output	0/1	1
     */

    private String url;
    private int doReasoning = 0;
    private int explicit = 1;
    private int implicit = 1;
    private int maxExplicit = -1;
    private int maxImplicit = -1;
    private int ontologies = 1;
    private int extractors = 0;


    private boolean ignoreNodes = true;

    /**
     * @param url
     * @param ignoreNodes it's been noted that some triples have the form: _node p o, or s p _node.
     *                    where we dont know what is _node. it is useless so its better to filter them out
     */
    public LiveQuery(String sindiceEndpoint, String url, boolean ignoreNodes) {
        super(sindiceEndpoint + "/v2/live?");
        this.url = url;
        this.ignoreNodes = ignoreNodes;
    }

    /**
     * @param url         MUST BE ENCODED
     * @param doReasoning
     * @param explicit
     * @param implicit
     * @param ontologies
     */
    public LiveQuery(String sindiceEndpoint, String url, int doReasoning, int explicit, int implicit, int ontologies, boolean ignoreNodes) {
        this(sindiceEndpoint + "/v2/live?", url, ignoreNodes);
        this.doReasoning = doReasoning;
        this.explicit = explicit;
        this.implicit = implicit;
        this.ontologies = ontologies;
    }

    @Override
    protected String formURL(int page) {
        String queryString =
                sindiceEndpoint +
                        "url=" + url +
                        "&doReasoning=" + doReasoning +
                        "&explicit=" + explicit + "&implicit=" + implicit +
                        "&maxExplicit=" + maxExplicit + "&maxImplicit=" + maxImplicit +
                        "&ontologies=" + ontologies + "&extractors=" + extractors;
        return queryString;
    }

    /**
     * @return
     * @throws SindiceException
     */
    public LiveResult performQuery() throws SindiceException {
        LiveResult lr = new LiveResult();

        JSONObject main = performGETQueryAndParseResult(formURL(1));
        JSONObject metadata = null;

        try {
            metadata = (JSONObject) ((JSONObject) main.get("extractorResults")).get("metadata");
        } catch (NullPointerException n) {//this means that there are no such element in the returned results.
        }

        if (metadata != null) {
            /*TODO: code to get ontologies*/
            //metadata.get("ontologies");

            /*get explicit triples*/
            JSONArray exTriples = null;
            try {
                exTriples = (JSONArray) ((Map) metadata.get("explicit")).get("bindings");
            } catch (NullPointerException n) {  //this means that there are no such elements in the returned results
            }
            if (exTriples != null) {
                for (int i = 0; i < exTriples.size(); i++) {
                    JSONObject entry = (JSONObject) exTriples.get(i);
                    JSONObject subject = (JSONObject) entry.get("s");
                    if (subject.get("type").toString().endsWith("node") && ignoreNodes)
                        continue;
                    JSONObject predicate = (JSONObject) entry.get("p");
                    if (predicate.get("type").toString().endsWith("node") && ignoreNodes)
                        continue;
                    JSONObject object = (JSONObject) entry.get("o");
                    if (object.get("type").toString().endsWith("node") && ignoreNodes)
                        continue;

                    StringBuilder triple = new StringBuilder(subject.get("value").toString()).append(" ");
                    triple.append(predicate.get("value").toString()).append(" ").append(object.get("value"));

                    lr.addExplicit(triple.toString());
                }
            }

            JSONArray imTriples = null;
            try {
                imTriples = (JSONArray) ((Map) metadata.get("implicit")).get("bindings");
            } catch (NullPointerException n) {
            }
            if (imTriples != null) {
                for (int i = 0; i < imTriples.size(); i++) {
                    JSONObject entry = (JSONObject) imTriples.get(i);
                    JSONObject subject = (JSONObject) entry.get("s");
                    if (subject.get("type").toString().endsWith("node") && ignoreNodes)
                        continue;
                    JSONObject predicate = (JSONObject) entry.get("p");
                    if (predicate.get("type").toString().endsWith("node") && ignoreNodes)
                        continue;
                    JSONObject object = (JSONObject) entry.get("o");
                    if (object.get("type").toString().endsWith("node") && ignoreNodes)
                        continue;

                    StringBuilder triple = new StringBuilder(subject.get("value").toString()).append(" ");
                    triple.append(predicate.get("value").toString()).append(" ").append(object.get("value"));

                    lr.addImplicit(triple.toString());
                }
            }
        }

        return lr;
    }
}
