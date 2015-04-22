package uk.ac.shef.dcs.oak.triplesearch.sindiceapi;

import com.sindice.SindiceException;
import com.sindice.query.v2.LiveQuery;
import com.sindice.query.v3.CacheQuery;
import com.sindice.query.v3.GuruQuery;
import com.sindice.query.v3.GuruSearchResults;
import com.sindice.result.CacheResult;
import com.sindice.result.LiveResult;
import com.sindice.result.SearchResult;
import com.sindice.result.SearchResults;
import uk.ac.shef.dcs.oak.triplesearch.TripleSearchException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 20/09/12
 * Time: 16:22
 */
public class SindiceAPIProxy {

    private String endpoint;

    public SindiceAPIProxy(){
        this.endpoint= "http://api.sindice.com";
    }

    public SindiceAPIProxy(String endpoint) {
        this.endpoint=endpoint;
    }

    /**
     * If any parameter is not not needed either provide "null" or "" (i.e.,. empty string)
     *
     * @param keywordq keywords must be 'single quoted'
     * @param ntriplenq you can either use speific ontology concepts e.g., <http://dbpedia.org/concept/Cat>
     *                  in which case *exact match* will be applied; or you can do fuzzy match by only providing
     *                  a literal keyword e.g., "cat".
     *                  For details try sindice api online
     * @param filterfq see sindice online
     */
    public SearchResults search(String keywordq, String ntriplenq, String filterfq) throws TripleSearchException {
        GuruQuery q = null;
        try {
            q = new GuruQuery(URLEncoder.encode(keywordq, "UTF-8"),
                    URLEncoder.encode(ntriplenq, "UTF-8"),
                    URLEncoder.encode(filterfq, "UTF-8"), false, endpoint);
        } catch (UnsupportedEncodingException e) {
            throw new TripleSearchException("URL encoding failure. This should not happen", e);
        }

        if (q != null) {
            try {
                return q.doQuery();
            } catch (SindiceException e) {
                throw new TripleSearchException("Query failed due to Sindice exception: ", e);
            }
        } else {
            throw new TripleSearchException("Cannot create query object");
        }
    }

    /**
         * If any parameter is not not needed either provide "null" or "" (i.e.,. empty string)
         *
         * @param keywordq keywords must be 'single quoted'
         * @param ntriplenq you can either use speific ontology concepts e.g., <http://dbpedia.org/concept/Cat>
         *                  in which case *exact match* will be applied; or you can do fuzzy match by only providing
         *                  a literal keyword e.g., "cat".
         *                  For details try sindice api online
         * @param filterfq see sindice online
         */
        public GuruSearchResults search(String keywordq, String ntriplenq, String filterfq, int start, int count) throws TripleSearchException {
            GuruQuery q = null;
            try {
                q = new GuruQuery(URLEncoder.encode(keywordq, "UTF-8"),
                        URLEncoder.encode(ntriplenq, "UTF-8"),
                        URLEncoder.encode(filterfq, "UTF-8"), false, endpoint);
                q.setStart(start);
                q.setCount(count);
            } catch (UnsupportedEncodingException e) {
                throw new TripleSearchException("URL encoding failure. This should not happen", e);
            }

            if (q != null) {
                try {
                    return q.doQuery(1);
                } catch (SindiceException e) {
                    throw new TripleSearchException("Query failed due to Sindice exception: ", e);
                }
            } else {
                throw new TripleSearchException("Cannot create query object");
            }
        }

    /*
    reconstruct the webpage given its link as embedded in the SearchResult object
     */
    public String retrieveDocument(SearchResult r) throws TripleSearchException {
        try {
            URL url = new URL(URLDecoder.decode(r.getLink(),"UTF-8"));
            URLConnection connection = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            return response.toString();
        } catch (Exception e) {
            throw new TripleSearchException("Cannot read from url:" + r.getLink(), e);
        }
    }

    /*
    retrieved the "cachedresult" object associated with the SearchResult object from Sindice cache.
     This "cachedresult" is a snapshot of the webpage, lists triples, domains, ontologies etc.
     This object should be used instead of any23 to get triples in a faster way.
     */
    public CacheResult retrieveCachedTarget(SearchResult r) throws TripleSearchException {
        CacheQuery query = new CacheQuery(endpoint,
                r.getLink()
        );

        try {
            return query.performQuery();

        } catch (SindiceException e) {
            throw new TripleSearchException("Sindice search failure:", e);
        }

    }

    public LiveResult retrieveLiveTarget(SearchResult r) throws TripleSearchException {
        LiveQuery query = new LiveQuery(endpoint,r.getLink(),true);
        try {
            return query.performQuery();
        } catch (SindiceException e) {
            throw new TripleSearchException("Sindice search failure:", e);
        }
    }

    public static String toStringDocTypes(String... types){
        if(types.length==0)
            return "";
        String prefix = "format:(";
        for(int i=0; i<types.length; i++){
            prefix=prefix+types[i];
            if(i!=types.length-1)
                prefix=prefix+" OR ";
        }
        return prefix+")";
    }

}
