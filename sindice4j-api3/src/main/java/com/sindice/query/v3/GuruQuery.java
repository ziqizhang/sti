package com.sindice.query.v3;

import com.sindice.SindiceException;
import com.sindice.query.SearchQuery;
import com.sindice.result.SearchResult;
import com.sindice.result.SearchResults;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.ParseException;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 20/09/12
 * Time: 12:32
 */
public class GuruQuery extends SearchQuery {

    private boolean sortByDate;

    private String q;
    private String nq;
    private String fq;

    /*
    both are "secret" params that im told not to spread publicly. Using this two params you can fetch more than the limited
    max # of results returned by sindice - by default sindice only allows max of 1000 results, 10 per page and a total of 100 pages.
     */
    private int start=0;  //the starting id of result in the returned result set.
    private int count=10;  //the number of results on each page.

    public int getStart(){
        return start;
    }
    public void setStart(int start){
        this.start=start;
    }
    public int getCount(){
        return count;
    }
    public void setCount(int count){
        this.count=count;
    }

    /**
     * The query params should follow the syntax defined here: http://sindice.com/developers/searchapiv3#SindicePublicAPI-SearchAPIv3
     * and should be ENCODED; should NOT include the "page" and "format" parameters
     * <p/>
     * If any parameter is not not needed either provide "null" or "" (i.e.,. empty string)
     */
    public GuruQuery(String q, String nq, String fq, boolean sortByDate, String endpoint) {
        super(endpoint+"/v3/search?");
        this.q = q;
        this.nq = nq;
        this.fq = fq;

        int nullParam = 0;
        if (this.q == null || this.q.length() == 0) {
            nullParam++;
        }
        if (this.nq == null || this.nq.length() == 0) {
            nullParam++;
        }
        if (this.fq == null || this.fq.length() == 0) {
            nullParam++;
        }

        if (nullParam == 3)
            throw new IllegalArgumentException("at least one query parameter must be specified");

        this.sortByDate = sortByDate;
    }

    /**
         * The query params should follow the syntax defined here: http://sindice.com/developers/searchapiv3#SindicePublicAPI-SearchAPIv3
         * and should be ENCODED; should NOT include the "page" and "format" parameters
         * <p/>
         * If any parameter is not not needed either provide "null" or "" (i.e.,. empty string)
         */
        public GuruQuery(String q, String nq, String fq, boolean sortByDate, String endpoint, int start, int count) {
            super(endpoint+"/v3/search?");
            this.q = q;
            this.nq = nq;
            this.fq = fq;
            this.start=start>-1? start:0;
            this.count=count>10?count:10;

            int nullParam = 0;
            if (this.q == null || this.q.length() == 0) {
                nullParam++;
            }
            if (this.nq == null || this.nq.length() == 0) {
                nullParam++;
            }
            if (this.fq == null || this.fq.length() == 0) {
                nullParam++;
            }

            if (nullParam == 3)
                throw new IllegalArgumentException("at least one query parameter must be specified");

            this.sortByDate = sortByDate;
        }

    @Override
    protected String formURL(int page) {
        String queryString =
                sindiceEndpoint +
                        "q=" + q +
                        "&nq=" + nq +
                        "&fq=" + fq + "&start="+getStart()+"&count="+getCount()+ "&interface=guru";
        return queryString;
    }

    @Override
    /**
     * Perform the query.
     *
     * @param page the maximum number of results per page
     * @return {@link com.sindice.result.SearchResults} wrapping results.
     */
    public GuruSearchResults doQuery(int page) throws SindiceException {
        GuruSearchResults results = new GuruSearchResults(this);
        results.setCurrentPage(page);
        JSONObject main = performGETQueryAndParseResult(formURL(page));

        results.setTotalResults(((Long) main.get("totalResults")).intValue());
        JSONArray entities = (JSONArray) main.get("entries");
        for (Object object : entities) {
            JSONObject obj = (JSONObject) object;
            SearchResult res = new SearchResult();
            res.setLink((String) obj.get("link"));
            results.add(res);
            try {
                res.setUpdated((String) obj.get("updated"));
            } catch (ParseException e) {
                throw new SindiceException(
                        "A problem occured while processing Sindice output." +
                                " Please report the issue to Sindice.",
                        e
                );
            }
            JSONObject o = (JSONObject) ((JSONArray) obj.get("title")).get(0);

            res.setTitle((String) o.get("value"));
            JSONArray formats = (JSONArray) obj.get("formats");
            for (Object object2 : formats) {
                res.addFormat((String) object2);
            }

            res.setTiples(Integer.valueOf(obj.get("explicit_content_size").toString()));
            res.setBytes(Integer.valueOf(obj.get("explicit_content_length").toString()));

        }
        results.setResultsPerPage(((Long) main.get("itemsPerPage"))
                .intValue());
        return results;
    }



}
