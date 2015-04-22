package uk.ac.shef.dcs.oak.websearch.bing.v2;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.String;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

/**
 */
public class BingWebSearch {

    private List<String> accountKeyPool;
    private Map<String, Date> obsoleteAccountKeys;
    private String baseURL = "https://api.datamarket.azure.com/Bing/Search/Web?Query=";

    public BingWebSearch(String... keys) {
        obsoleteAccountKeys = new HashMap<String, Date>();
        this.accountKeyPool = new ArrayList<String>();
        for (String k : keys)
            accountKeyPool.add(k);
    }

    public BingWebSearch(String baseURL, String... keys) {
        obsoleteAccountKeys = new HashMap<String, Date>();
        this.accountKeyPool = new ArrayList<String>();
        for (String k : keys)
            accountKeyPool.add(k);
        this.baseURL = baseURL;
    }

    public InputStream search(String query) throws IOException, APIKeysDepletedException {
        query = "'" + query + "'";
        query = URLEncoder.encode(query, "UTF-8");
        query = baseURL + query + "&$format=json&$top=20";

        if (accountKeyPool.size() == obsoleteAccountKeys.size()) {
            throw new APIKeysDepletedException();
        }

        for (String k : accountKeyPool) {
            if (obsoleteAccountKeys.get(k) != null)
                continue;

            URL url = new URL(query);
            URLConnection urlConnection = url.openConnection();
            byte[] accountKeyBytes = Base64.encodeBase64((k + ":" + k).getBytes());     //first k being username; second k being password.
            String accountKeyEnc = new String(accountKeyBytes);
            urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

            InputStream is = null;
            boolean keyInvalid = false;
            try {
                is = urlConnection.getInputStream();
            } catch (IOException ioe) {
                System.err.println("> Bing search exception. Apps built on top may produce incorrect results.");
                if (ioe.getMessage().contains("Server returned HTTP response code: 401") || ioe.getMessage().contains("Server returned HTTP response code: 503")) {
                    keyInvalid = true;
                    ioe.printStackTrace();
                } else
                    throw ioe;
            }
            if (keyInvalid) {
                obsoleteAccountKeys.put(k, new Date());
                continue;
            }
            return is;
        }
        return null;
    }

    public static void main(String[] args) throws IOException, APIKeysDepletedException {
        String[] accountKeys = new String[]{
                "8Yr8amTvrm5SM4XK3vM3KrLqOCT/ZhkwCfLEDtslE7o="};
        BingWebSearch searcher = new BingWebSearch(accountKeys);

        InputStream is = searcher.search("Sheffield University Sheffield");
        BingWebSearchResultParser parser = new BingWebSearchResultParser();
        List<WebSearchResultDoc> docs = parser.parse(is);


    }
}
