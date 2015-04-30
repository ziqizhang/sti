package uk.ac.shef.dcs.oak.kbsearch.freebase;

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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.ac.shef.dcs.oak.util.CollectionUtils;
import uk.ac.shef.dcs.oak.util.StringUtils;

import java.io.FileInputStream;
import java.util.logging.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 18/01/14
 * Time: 22:06
 * To change this template use File | Settings | File Templates.
 */
public class FreebaseQueryHelper {

    public static Properties properties = new Properties();
    public static Logger log = Logger.getLogger(FreebaseQueryHelper.class.getName());
    //private String BASE_QUERY_URL="https://www.googleapis.com/freebase/v1/mqlread";
    private JSONParser jsonParser;
    private FreebaseQueryInterrupter interrupter;
    private HttpTransport httpTransport;
    private HttpRequestFactory requestFactory;

    public FreebaseQueryHelper(String freebasePropertyFile) throws IOException {
        properties.load(new FileInputStream(freebasePropertyFile));
        interrupter = new FreebaseQueryInterrupter(Integer.valueOf(properties.get("FREEBASE_MAX_QUERY_PER_SECOND").toString()),
                Integer.valueOf(properties.get("FREEBASE_MAX_QUERY_PER_DAY").toString()));
        httpTransport = new NetHttpTransport();
        requestFactory = httpTransport.createRequestFactory();
        jsonParser = new JSONParser();
    }


    //given a topic id, returns its facts. result is a list of string array, where in each array, value 0 is the property name; value 1 is the value;
    //value 2 could be null or a string, when it is an id of a topic (if null then value of property is not topic); value 4 is "y" or "n", meaning if
    //if the property is a nested property of the topic of interest.
    public List<String[]> topicapi_facts_of_id(String id) throws IOException {
        Date start = new Date();
        List<String[]> res = new ArrayList<String[]>();
        GenericUrl url = new GenericUrl(properties.get("FREEBASE_TOPIC_QUERY_URL").toString() + id);
        url.put("key", properties.get("FREEBASE_API_KEY"));
        url.put("limit", 100);
        HttpRequest request = requestFactory.buildGetRequest(url);
        HttpResponse httpResponse = interrupter.executeQuery(request, true);
        try {
            JSONObject topic = (JSONObject) jsonParser.parse(httpResponse.parseAsString());
            JSONObject properties = (JSONObject) topic.get("property");
            parseProperties_of_topicapi(properties, res, false);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        log.warning("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));
        return res;

    }

    public List<String[]> topicapi_types_of_id(String id) throws IOException {
        Date start = new Date();
        List<String[]> res = new ArrayList<String[]>();
        GenericUrl url = new GenericUrl(properties.get("FREEBASE_TOPIC_QUERY_URL").toString() + id);
        url.put("key", properties.get("FREEBASE_API_KEY"));
        url.put("filter", "/type/object/type");
        HttpRequest request = requestFactory.buildGetRequest(url);
        HttpResponse httpResponse = interrupter.executeQuery(request, true);
        try {
            JSONObject topic = (JSONObject) jsonParser.parse(httpResponse.parseAsString());
            JSONObject properties = (JSONObject) topic.get("property");
            if(properties!=null)
                parseProperties_of_topicapi(properties, res, false);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        log.warning("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));
        return res;

    }

    //[] = {property, val.toString(), id.toString(), nested_flag}  (nested flag=y/n)
    public List<String[]> topicapi_facts_of_id_with_filter(String id, String filter) throws IOException {
        Date start = new Date();
        List<String[]> res = new ArrayList<String[]>();
        GenericUrl url = new GenericUrl(properties.get("FREEBASE_TOPIC_QUERY_URL").toString() + id);
        url.put("key", properties.get("FREEBASE_API_KEY"));
        url.put("filter", filter);
        url.put("limit", 200);
        HttpRequest request = requestFactory.buildGetRequest(url);
        HttpResponse httpResponse = interrupter.executeQuery(request, true);
        try {
            JSONObject topic = (JSONObject) jsonParser.parse(httpResponse.parseAsString());
            JSONObject properties = (JSONObject) topic.get("property");
            parseProperties_of_topicapi(properties, res, false);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        log.warning("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));
        return res;

    }

    private void parseProperties_of_topicapi(JSONObject json, List<String[]> out, boolean nested) {
        /*if(json==null)
            System.out.println();*/
        Iterator<String> prop_keys = json.keySet().iterator();
        while (prop_keys.hasNext()) {
            String prop = prop_keys.next();
            try {
                JSONObject propValueObj = (JSONObject) json.get(prop);
                JSONArray jsonArray = (JSONArray) propValueObj.get("values");
                Object c = propValueObj.get("valuetype");
                if (c != null && c.toString().equals("compound"))
                    parsePropertyValues(jsonArray, prop, out, nested, true);
                else
                    parsePropertyValues(jsonArray, prop, out, nested, false);
            } catch (Exception e) {
            }
        }
    }

    private Entity_FreebaseTopic parseProperties_of_searchapi(JSONObject json) {
        Entity_FreebaseTopic obj = new Entity_FreebaseTopic(json.get("mid").toString());
        Object o = json.get("mid");
        if (o != null)
            obj.setId(o.toString());
        obj.setName(json.get("name").toString());
        obj.setScore(Double.valueOf(json.get("score").toString()));

       /* o = json.get("notable");
        if (o != null) {
            JSONObject notable = (JSONObject) o;
            obj.addType(new String[]{notable.get("id").toString(), notable.get("name").toString()});       //todo: wrong!!! types will be incomplete
        }*/

        obj.setLanguage(json.get("lang").toString());
        return obj;
    }

    private void parsePropertyValues(JSONArray json, String property, List<String[]> out, boolean nested, boolean skipCompound) {
        Iterator entry = json.iterator();
        Object val = null, id = null, mid = null, more_props = null;
        String nested_flag = nested ? "y" : "n";
        while (entry.hasNext()) {
            JSONObject key = (JSONObject) entry.next();
            if (skipCompound) {
                more_props = key.get("property");
                if (more_props != null)
                    parseProperties_of_topicapi((JSONObject) more_props, out, true);
                continue;
            }

            val = key.get("text");
            if (property.equals("/common/topic/description") || property.equals("/common/document/text")) {
                Object changeVal = key.get("value");
                if (changeVal != null)
                    val = changeVal;
            }
            id = key.get("id");
            mid = key.get("mid");
            if (id == null && mid != null) id = mid;
            if (val != null && id != null)
                out.add(new String[]{property, val.toString(), id.toString(), nested_flag});
            else if (val != null)
                out.add(new String[]{property, val.toString(), null, nested_flag});
        }

    }


    //operator - any means or; all means and
    public List<Entity_FreebaseTopic> searchapi_topics_with_name_and_type(String name, String operator, boolean tokenMatch, int maxResult, String... types) throws IOException {
        Set<String> query_tokens = new HashSet<String>();
        for (String t : name.split("[\\s+/\\-,]")) {
            t = t.trim();
            if (t.length() > 0)
                query_tokens.add(t);
        }

        Date start = new Date();
        HttpTransport httpTransport = new NetHttpTransport();
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
        List<Entity_FreebaseTopic> res = new ArrayList<Entity_FreebaseTopic>();

        GenericUrl url = new GenericUrl(properties.get("FREEBASE_SEARCH_QUERY_URL").toString());
        url.put("query", name);
        url.put("limit", 20);
        url.put("prefixed", true);
        url.put("key", properties.get("FREEBASE_API_KEY"));

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
                Entity_FreebaseTopic top = parseProperties_of_searchapi((JSONObject) result);

                if (count < maxResult) {
                    if (tokenMatch) {
                        Set<String> candidate_tokens = new HashSet<String>();

                        for (String t : top.getName().split("[\\s+/\\-,]")) {
                            t = t.trim();
                            if (t.length() > 0)
                                candidate_tokens.add(t);
                        }
                        candidate_tokens.retainAll(query_tokens);
                        if (candidate_tokens.size() > 0)
                            res.add(top);
                    } else
                        res.add(top);
                }

                //print or save this id
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        log.warning("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));
        return res;
    }


    public List<Entity_FreebaseTopic> mql_topics_with_name(int maxResults, String name, String operator, String... types) throws IOException {
        Set<String> query_tokens = new HashSet<String>();
        for (String t : name.split("\\s+")) {
            t = t.trim();
            if (t.length() > 0)
                query_tokens.add(t);
        }

        Date start = new Date();
        HttpTransport httpTransport = new NetHttpTransport();
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
        List<Entity_FreebaseTopic> res = new ArrayList<Entity_FreebaseTopic>();

        final Map<Entity_FreebaseTopic, Double> candidates = new HashMap<Entity_FreebaseTopic, Double>();
        int limit = 20;
        int iterations = maxResults % limit;
        iterations = iterations == 0 ? maxResults / limit : maxResults / limit + 1;
        String cursorPoint = "";
        for (int i = 0; i < iterations; i++) {
            String query = "[{\"mid\":null," +
                    "\"name\":null," +
                    "\"name~=\":\"" + name + "\"," +
                    "\"/type/object/type\":[],";
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

            GenericUrl url = new GenericUrl(properties.get("FREEBASE_MQL_QUERY_URL").toString());
            url.put("query", query);
            url.put("key", properties.get("FREEBASE_API_KEY"));
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
                    Entity_FreebaseTopic ent = new Entity_FreebaseTopic(id);
                    ent.setName(e_name);
                    if (obj.get("/type/object/type") != null) {
                        JSONArray jsonArray = (JSONArray) obj.get("/type/object/type");
                        for (int n = 0; n < jsonArray.size(); n++) {
                            String the_type = jsonArray.get(n).toString();
                            if (!the_type.equals("/common/topic") && !the_type.startsWith("/user/"))
                                ent.addType(new String[]{the_type, the_type});
                        }
                    }
                    List<String> bow_ent = StringUtils.toBagOfWords(e_name, true, true,false);
                    List<String> bow_query = StringUtils.toBagOfWords(name, true, true,false);
                    int intersection = CollectionUtils.intersection(bow_ent, bow_query);
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

        log.warning("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));
        res.addAll(candidates.keySet());
        Collections.sort(res, new Comparator<Entity_FreebaseTopic>() {
            @Override
            public int compare(Entity_FreebaseTopic o1, Entity_FreebaseTopic o2) {
                return candidates.get(o2).compareTo(candidates.get(o1));
            }
        });
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

        GenericUrl url = new GenericUrl(properties.get("FREEBASE_MQL_QUERY_URL").toString());
        url.put("query", query);
        url.put("key", properties.get("FREEBASE_API_KEY"));

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

        log.warning("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));

        return res;
    }

    //given a type search for any topics of that type and return their ids
    public List<String> mqlapi_topic_mids_with_name(String name, int maxResults) throws IOException {
        Date start = new Date();
        httpTransport = new NetHttpTransport();
        requestFactory = httpTransport.createRequestFactory();
        List<String> res = new ArrayList<String>();

        int limit = Integer.valueOf(properties.get("FREEBASE_LIMIT").toString());
        int iterations = maxResults % limit;
        iterations = iterations == 0 ? maxResults / limit : maxResults / limit + 1;
        String cursorPoint = "";
        for (int i = 0; i < iterations; i++) {
            String query = "[{\"mid\":null," +
                    "\"name\":\"" + name + "\"," +
                    "\"limit\":" + limit + "" +
                    "}]";

            GenericUrl url = new GenericUrl(properties.get("FREEBASE_MQL_QUERY_URL").toString());
            url.put("query", query);
            url.put("key", properties.get("FREEBASE_API_KEY"));
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
        log.warning("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));

        return res;
    }

    public List<String> mqlapi_instances_of_type(String name, int maxResults) throws IOException {
        Date start = new Date();
        httpTransport = new NetHttpTransport();
        requestFactory = httpTransport.createRequestFactory();
        List<String> res = new ArrayList<String>();

        int limit = Integer.valueOf(properties.get("FREEBASE_LIMIT").toString());
        int iterations = maxResults % limit;
        iterations = iterations == 0 ? maxResults / limit : maxResults / limit + 1;
        String cursorPoint = "";
        for (int i = 0; i < iterations; i++) {
            String query = "[{\"name\":null," +
                    "\"type\":\"" + name + "\"," +
                    "\"limit\":" + limit + "" +
                    "}]";

            GenericUrl url = new GenericUrl(properties.get("FREEBASE_MQL_QUERY_URL").toString());
            url.put("query", query);
            url.put("key", properties.get("FREEBASE_API_KEY"));
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
        log.warning("\tQueryFreebase:" + (new Date().getTime() - start.getTime()));

        return res;
    }


    //given a type search for any topics of that type and return their ids
    public List<String> savePagesFor_topicIds_with_type(String type, int maxResults) throws IOException {
        httpTransport = new NetHttpTransport();
        requestFactory = httpTransport.createRequestFactory();
        List<String> res = new ArrayList<String>();

        int limit = Integer.valueOf(properties.get("FREEBASE_LIMIT").toString());
        int iterations = maxResults % limit;
        iterations = iterations == 0 ? maxResults / limit : maxResults / limit + 1;
        String cursorPoint = "";
        for (int i = 0; i < iterations; i++) {
            String query = "[{\"mid\":null," +
                    "\"name\":null," +
                    "\"type\":\"" + type + "\"," +
                    "\"limit\":" + limit + "" +
                    "}]";

            GenericUrl url = new GenericUrl(properties.get("FREEBASE_BASE_QUERY_URL").toString());
            url.put("query", query);
            url.put("key", properties.get("FREEBASE_API_KEY"));
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


                    //http://www.freebase.com/m/0vhypk
                    //TODO get html for "http://www.freebase.com/" + id

                }

                if (results.size() < limit) {
                    break;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    // public abstract List<String> searchFactsOfTopic(String topic);

    public static void main(String[] args) throws IOException {
        FreebaseQueryHelper helper = new FreebaseQueryHelper("D:\\Work\\lodiedata\\tableminer_gs/freebase.properties");
        List<String> artist= helper.mqlapi_instances_of_type("/music/artist",10000);
        System.out.println(artist);
    }


    public double find_granularityForType(String type) throws IOException {
        if(type.startsWith("/m/")) //if the type id starts with "/m/" in strict sense it is a topic representing a concept
        //but is not listed as a type in freebase
            return 1.0;
        String url = properties.get("FREEBASE_HOMEPAGE").toString() +type+"?instances=";
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
        log.warning("\tFetchingFreebasePage:" + (new Date().getTime() - startTime.getTime()));
        if(result!=null && result.length()>0)
            return new Double(result);
        return 0.0;
    }
}
