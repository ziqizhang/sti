package uk.ac.shef.dcs.websearch.bing.v2;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import uk.ac.shef.dcs.websearch.SearchResultParser;
import uk.ac.shef.dcs.websearch.WebSearch;

import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

/**
 */
public class BingSearch extends WebSearch{

    protected static final String BING_BASE_URL="bing.url";
    protected static final String BING_KEYS="bing.keys";
    protected SearchResultParser parser;

    protected List<String> accountKeyPool;
    protected Map<String, Date> obsoleteAccountKeys;
    protected String baseURL;// = "https://api.datamarket.azure.com/Bing/Search/Web?Query=";

    public BingSearch(Properties properties) throws IOException {
        super(properties);

        obsoleteAccountKeys = new HashMap<>();
        this.accountKeyPool = new ArrayList<>();
        for (String k : StringUtils.split(
                this.properties.getProperty(BING_KEYS),','
        )) {
            k = k.trim();
            if(k.length()>0)
                accountKeyPool.add(k);
        }
        this.baseURL = this.properties.getProperty(BING_BASE_URL);
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
                //fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0=
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

    @Override
    public SearchResultParser getResultParser() {
        if(parser==null)
            parser=new BingSearchResultParser();
        return parser;
    }

    /*public static void main(String[] args) throws IOException, APIKeysDepletedException {
        String[] accountKeys = new String[]{
                "8Yr8amTvrm5SM4XK3vM3KrLqOCT/ZhkwCfLEDtslE7o="};
        BingSearch searcher = new BingSearch(accountKeys);

        InputStream is = searcher.search("Sheffield University Sheffield");
        BingSearchResultParser parser = new BingSearchResultParser();
        List<WebSearchResultDoc> docs = parser.parse(is);


    }*/
}
