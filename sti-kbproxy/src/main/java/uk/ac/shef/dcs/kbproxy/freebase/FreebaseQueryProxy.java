package uk.ac.shef.dcs.kbproxy.freebase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.util.StringUtils;


/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 18/01/14
 * Time: 22:06
 * To change this template use File | Settings | File Templates.
 */
public class FreebaseQueryProxy {


    public static Logger LOG = LoggerFactory.getLogger(FreebaseQueryProxy.class.getName());
    //private String BASE_QUERY_URL="https://www.googleapis.com/freebase/v1/mqlread";
    private JSONParser jsonParser;
    private FreebaseQueryInterrupter interrupter;
    private HttpTransport httpTransport;
    private HttpRequestFactory requestFactory;
    private Properties properties;

    private static final String FB_MAX_QUERY_PER_SECOND="fb.query.max.sec";

    private static final String FB_MAX_QUERY_PER_DAY="fb.query.max.day";

    private static final String FB_QUERY_API_URL_TOPIC ="fb.query.apiurl.topic";

    private static final String FB_QUERY_API_URL_SEARCH ="fb.query.apiurl.search";

    private static final String FB_QUERY_API_URL_MQL ="fb.query.apiurl.mql";

    private static final String FB_QUERY_API_KEY="fb.query.api.key";

    private static final String FB_HOMEPAGE="fb.homepage";

    private static final String FB_QUERY_PARAM_LIMIT="fb.query.param.limit";

    public FreebaseQueryProxy(Properties properties) throws IOException {
        this.properties=properties;
        interrupter = new FreebaseQueryInterrupter(Integer.valueOf(properties.get(FB_MAX_QUERY_PER_SECOND).toString()),
                Integer.valueOf(properties.get(FB_MAX_QUERY_PER_DAY).toString()));
        httpTransport = new NetHttpTransport();
        requestFactory = httpTransport.createRequestFactory();
        jsonParser = new JSONParser();
    }


    //given a topic id, returns its attributes.
    public List<Attribute> topicapi_getAttributesOfTopic(String id) throws IOException {
        Date start = new Date();
        List<Attribute> res = new ArrayList<>();
        GenericUrl url = new GenericUrl(properties.get(FB_QUERY_API_URL_TOPIC).toString() + id);
        url.put("key", properties.get(FB_QUERY_API_KEY));
        url.put("limit", 100);
        HttpRequest request = requestFactory.buildGetRequest(url);
        HttpResponse httpResponse = interrupter.executeQuery(request, true);
        try {
            JSONObject topic = (JSONObject) jsonParser.parse(httpResponse.parseAsString());
            JSONObject properties = (JSONObject) topic.get("property");
            parseTopicAPIResult(properties, res, true);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        LOG.debug("\tQueryFreebase (attributes):" + (new Date().getTime() - start.getTime()));
        return res;

    }

    public List<Attribute> topicapi_getTypesOfTopicID(String id) throws IOException {
        Date start = new Date();
        List<Attribute> res = new ArrayList<>();
        GenericUrl url = new GenericUrl(properties.get(FB_QUERY_API_URL_TOPIC).toString() + id);
        url.put("key", properties.get(FB_QUERY_API_KEY));
        url.put("filter", FreebaseEnum.RELATION_HASTYPE.getString());
        HttpRequest request = requestFactory.buildGetRequest(url);
        HttpResponse httpResponse = interrupter.executeQuery(request, true);
        try {
            JSONObject topic = (JSONObject) jsonParser.parse(httpResponse.parseAsString());
            JSONObject properties = (JSONObject) topic.get("property");
            if(properties!=null)
                parseTopicAPIResult(properties, res, true);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        LOG.debug("\tQueryFreebase (types):" + (new Date().getTime() - start.getTime()));
        return res;

    }

    public List<Attribute> topicapi_getAttributesOfTopicID(String id, String filter) throws IOException {
        Date start = new Date();
        List<Attribute> res = new ArrayList<>();
        GenericUrl url = new GenericUrl(properties.get(FB_QUERY_API_URL_TOPIC).toString() + id);
        url.put("key", properties.get(FB_QUERY_API_KEY));
        url.put("filter", filter);
        url.put("limit", 200);
        HttpRequest request = requestFactory.buildGetRequest(url);
        HttpResponse httpResponse = interrupter.executeQuery(request, true);
        try {
            JSONObject topic = (JSONObject) jsonParser.parse(httpResponse.parseAsString());
            JSONObject properties = (JSONObject) topic.get("property");
            parseTopicAPIResult(properties, res, true);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        LOG.debug("\tQueryFreebase (attributes):" + (new Date().getTime() - start.getTime()));
        return res;

    }

    private void parseTopicAPIResult(JSONObject json, List<Attribute> out, boolean directRelation) {
        /*if(json==null)
            System.out.println();*/
        @SuppressWarnings("unchecked")
        Iterator<String> prop_keys = json.keySet().iterator();
        while (prop_keys.hasNext()) {
            String prop = prop_keys.next();
            try {
                JSONObject propValueObj = (JSONObject) json.get(prop);
                JSONArray jsonArray = (JSONArray) propValueObj.get("values");
                Object c = propValueObj.get("valuetype");
                if (c != null && c.toString().equals("compound"))
                    parsePropertyValues(jsonArray, prop, out, directRelation, true);
                else
                    parsePropertyValues(jsonArray, prop, out, directRelation, false);
            } catch (Exception e) {
            }
        }
    }

    private FreebaseTopic parseSearchAPIResult(JSONObject json) {
        FreebaseTopic obj = new FreebaseTopic(json.get("mid").toString());
        Object o = json.get("mid");
        if (o != null)
            obj.setId(o.toString());
        obj.setLabel(json.get("name").toString());
        obj.setScore(Double.valueOf(json.get("score").toString()));

        obj.setLanguage(json.get("lang").toString());
        return obj;
    }

    private void parsePropertyValues(JSONArray json, String property, List<Attribute> out, boolean directRelation, boolean skipCompound) {
        Iterator entry = json.iterator();
        Object val = null, id = null, mid = null, more_props = null;
        while (entry.hasNext()) {
            JSONObject key = (JSONObject) entry.next();
            if (skipCompound) {
                more_props = key.get("property");
                if (more_props != null)
                    parseTopicAPIResult((JSONObject) more_props, out, false);
                continue;
            }

            val = key.get("text");
            if (property.equals(FreebaseEnum.RELATION_HASDESCRIPTION.getString())
                    || property.equals(FreebaseEnum.RELATION_HASDOCUMENTTEXT.getString())) {
                Object changeVal = key.get("value");
                if (changeVal != null)
                    val = changeVal;
            }
            id = key.get("id");
            mid = key.get("mid");
            if (id == null && mid != null) id = mid;
            Attribute attr = new FreebaseAttribute(property, val.toString());
            attr.setIsDirect(directRelation);
            if (val != null && id != null) {
                attr.setValueURI(id.toString());
                out.add(attr);
            }
            else if (val != null) {
                out.add(attr);
            }
        }

    }


    //operator - any means or; all means and
    public List<FreebaseTopic> searchapi_getTopicsByNameAndType(String name, String operator, boolean tokenMatch, int maxResult, String... types) throws IOException {
        List<String> query_tokens = StringUtils.splitToAlphaNumericTokens(name, true);

        Date start = new Date();
        HttpTransport httpTransport = new NetHttpTransport();
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
        List<FreebaseTopic> res = new ArrayList<>();

        GenericUrl url = new GenericUrl(properties.get(FB_QUERY_API_URL_SEARCH).toString());
        url.put("query", name);
        url.put("limit", 20);
        url.put("prefixed", true);
        url.put("key", properties.get(FB_QUERY_API_KEY));

        StringBuilder filter = new StringBuilder();
        for (String t : types) {
            filter.append("type:").append(t).append(" ");
        }

        if (filter.length() > 0)
            url.put("filter", "(" + operator + " " + filter.toString().trim() + ")");

        HttpRequest request = requestFactory.buildGetRequest(url);
        HttpResponse httpResponse = interrupter.executeQuery(request, true);
        JSONObject response;
        try {
            response = (JSONObject) jsonParser.parse(httpResponse.parseAsString());
            JSONArray results = (JSONArray) response.get("result");
            int count = 0;
            for (Object result : results) {
                FreebaseTopic top = parseSearchAPIResult((JSONObject) result);

                if (count < maxResult) {
                    if (tokenMatch) {
                        List<String> candidate_tokens = StringUtils.splitToAlphaNumericTokens(top.getLabel(), true);
                        candidate_tokens.retainAll(query_tokens);
                        if (candidate_tokens.size() > 0) {
                            res.add(top);
                            count++;
                        }
                    } else {
                        res.add(top);
                        count++;
                    }
                }

                //print or save this id
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        LOG.debug("\tQueryFreebase (search for topics):" + (new Date().getTime() - start.getTime()));
        return res;
    }


    public List<FreebaseTopic> mql_topics_with_name(int maxResults, String name, String operator, String... types) throws IOException {
        Set<String> query_tokens = new HashSet<String>();
        for (String t : name.split("\\s+")) {
            t = t.trim();
            if (t.length() > 0)
                query_tokens.add(t);
        }

        Date start = new Date();
        HttpTransport httpTransport = new NetHttpTransport();
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
        List<FreebaseTopic> res = new ArrayList<FreebaseTopic>();

        final Map<FreebaseTopic, Double> candidates = new HashMap<FreebaseTopic, Double>();
        int limit = 20;
        int iterations = maxResults % limit;
        iterations = iterations == 0 ? maxResults / limit : maxResults / limit + 1;
        String cursorPoint = "";
        for (int i = 0; i < iterations; i++) {
            String query = "[{\"mid\":null," +
                    "\"name\":null," +
                    "\"name~=\":\"" + name + "\"," +
                    "\""+FreebaseEnum.RELATION_HASTYPE+"\":[],";
            if (types.length > 0) {
                if (operator.equals("any")) {
                    query = query + "\"type|=\":[";
                    for (String t : types) {
                        query = query + "\"" + t + "\",";
                    }
                    if (query.endsWith(","))
                        query = query.substring(0, query.length() - 1).trim();
                    query = query + "],";
                } else if (operator.equals("and")) {
                    for (int n = 0; n < types.length; n++) {
                        String t = types[n];
                        if (n == 0)
                            query = query + "\"type\":\"" + t + "\",";
                        else
                            query = query + "\"and:type\":\"" + t + "\",";
                    }
                }
            }

            query = query +
                    "\"limit\":" + limit + "" +
                    "}]";

            GenericUrl url = new GenericUrl(properties.get(FB_QUERY_API_URL_MQL).toString());
            url.put("query", query);
            url.put("key", properties.get(FB_QUERY_API_KEY));
            url.put("cursor", cursorPoint);

            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = interrupter.executeQuery(request, true);
            System.out.print(limit * (i + 1));
            JSONObject response;
            try {
                response = (JSONObject) jsonParser.parse(httpResponse.parseAsString());
                cursorPoint = response.get("cursor").toString();
                JSONArray results = (JSONArray) response.get("result");

                for (Object result : results) {
                    JSONObject obj = (JSONObject) result;
                    String id = obj.get("mid").toString();
                    String e_name = obj.get("name").toString();
                    FreebaseTopic ent = new FreebaseTopic(id);
                    ent.setLabel(e_name);
                    if (obj.get(FreebaseEnum.RELATION_HASTYPE.getString()) != null) {
                        JSONArray jsonArray = (JSONArray) obj.get(FreebaseEnum.RELATION_HASTYPE.getString());
                        for (int n = 0; n < jsonArray.size(); n++) {
                            String the_type = jsonArray.get(n).toString();
                            if (!the_type.equals(FreebaseEnum.TYPE_COMMON_TOPIC.getString()) && !the_type.startsWith(FreebaseEnum.TYPE_USER.getString()))
                                ent.addType(new Clazz(the_type, the_type));
                        }
                    }
                    List<String> bow_ent = StringUtils.toBagOfWords(e_name, true, true, false);
                    List<String> bow_query = StringUtils.toBagOfWords(name, true, true,false);
                    int intersection = CollectionUtils.intersection(bow_ent, bow_query).size();
                    candidates.put(ent, ((double) intersection / bow_ent.size() + (double) intersection / bow_query.size()) / 2.0);
                    //print or save this id
                }

                if (results.size() < limit) {
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        LOG.debug("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));
        res.addAll(candidates.keySet());
        Collections.sort(res, (o1, o2) -> candidates.get(o2).compareTo(candidates.get(o1)));
        return res;
    }

    public List<String> mqlapi_topic_mids_with_wikipedia_pageid(String wikipedia_pageid) throws IOException {
        Date start = new Date();
        httpTransport = new NetHttpTransport();
        requestFactory = httpTransport.createRequestFactory();
        List<String> res = new ArrayList<String>();

        String query = "[{\"mid\":null," +
                "\"id\":\"/wikipedia/en_id/" + wikipedia_pageid + "\"" +
                "}]";

        GenericUrl url = new GenericUrl(properties.get(FB_QUERY_API_URL_MQL).toString());
        url.put("query", query);
        url.put("key", properties.get(FB_QUERY_API_KEY));

        HttpRequest request = requestFactory.buildGetRequest(url);
        HttpResponse httpResponse = interrupter.executeQuery(request, true);
        JSONObject response;
        try {
            response = (JSONObject) jsonParser.parse(httpResponse.parseAsString());
            JSONArray results = (JSONArray) response.get("result");

            for (Object result : results) {
                JSONObject obj = (JSONObject) result;
                String id = obj.get("mid").toString();
                res.add(id);
                //print or save this id
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        LOG.debug("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));

        return res;
    }

    //given a type search for any topics of that type and return their ids
    public List<String> mqlapi_topic_mids_with_name(String name, int maxResults) throws IOException {
        Date start = new Date();
        httpTransport = new NetHttpTransport();
        requestFactory = httpTransport.createRequestFactory();
        List<String> res = new ArrayList<String>();

        int limit = Integer.valueOf(properties.get(FB_QUERY_PARAM_LIMIT).toString());
        int iterations = maxResults % limit;
        iterations = iterations == 0 ? maxResults / limit : maxResults / limit + 1;
        String cursorPoint = "";
        for (int i = 0; i < iterations; i++) {
            String query = "[{\"mid\":null," +
                    "\"name\":\"" + name + "\"," +
                    "\"limit\":" + limit + "" +
                    "}]";

            GenericUrl url = new GenericUrl(properties.get(FB_QUERY_API_URL_MQL).toString());
            url.put("query", query);
            url.put("key", properties.get(FB_QUERY_API_KEY));
            url.put("cursor", cursorPoint);

            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = interrupter.executeQuery(request, true);
            System.out.println(limit * (i + 1));
            JSONObject response;
            try {
                response = (JSONObject) jsonParser.parse(httpResponse.parseAsString());
                cursorPoint = response.get("cursor").toString();
                JSONArray results = (JSONArray) response.get("result");

                for (Object result : results) {
                    JSONObject obj = (JSONObject) result;
                    String id = obj.get("mid").toString();
                    res.add(id);

                    //print or save this id
                }

                if (results.size() < limit) {
                    break;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        LOG.debug("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));

        return res;
    }

    public List<String> mqlapi_instances_of_type(String name, int maxResults) throws IOException {
        Date start = new Date();
        httpTransport = new NetHttpTransport();
        requestFactory = httpTransport.createRequestFactory();
        List<String> res = new ArrayList<String>();

        int limit = Integer.valueOf(properties.get(FB_QUERY_PARAM_LIMIT).toString());
        int iterations = maxResults % limit;
        iterations = iterations == 0 ? maxResults / limit : maxResults / limit + 1;
        String cursorPoint = "";
        for (int i = 0; i < iterations; i++) {
            String query = "[{\"name\":null," +
                    "\"type\":\"" + name + "\"," +
                    "\"limit\":" + limit + "" +
                    "}]";

            GenericUrl url = new GenericUrl(properties.get(FB_QUERY_API_URL_MQL).toString());
            url.put("query", query);
            url.put("key", properties.get(FB_QUERY_API_KEY));
            url.put("cursor", cursorPoint);

            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = interrupter.executeQuery(request, true);
            System.out.println(limit * (i + 1));
            JSONObject response;
            try {
                response = (JSONObject) jsonParser.parse(httpResponse.parseAsString());
                cursorPoint = response.get("cursor").toString();
                JSONArray results = (JSONArray) response.get("result");

                for (Object result : results) {
                    JSONObject obj = (JSONObject) result;
                    String id = obj.get("name").toString();
                    res.add(id);

                    //print or save this id
                }

                if (results.size() < limit) {
                    break;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        LOG.debug("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));

        return res;
    }

    /*public static void main(String[] args) throws IOException {
        FreebaseQueryProxy helper = new FreebaseQueryProxy("D:\\Work\\lodiedata\\tableminer_gs/freebase.properties");
        List<String> artist= helper.mqlapi_instances_of_type("/music/artist",10000);
        System.out.println(artist);
    }*/


    public double find_granularityForType(String type) throws IOException {
        if(type.startsWith("/m/")) //if the type id starts with "/m/" in strict sense it is a topic representing a concept
        //but is not listed as a type in freebase
            return 1.0;
        String url = properties.get(FB_HOMEPAGE).toString() +type+"?instances=";
        Date startTime = new Date();
        URL connection = new URL(url);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.openStream()));

        String result=null;
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            int start = inputLine.indexOf("span data-value=");
            if(start!=-1) {
                start+=16;
                int end = inputLine.indexOf(" ",16);
                if(start<end){
                    result = inputLine.substring(start,end).trim();
                    result = result.replaceAll("[^0-9]","");
                }
                break;
            }
        }
        in.close();
        LOG.debug("\tFetchingFreebasePage:" + (new Date().getTime() - startTime.getTime()));
        if(result!=null && result.length()>0)
            return new Double(result);
        return 0.0;
    }
}
