package uk.ac.shef.dcs.kbsearch.freebase;

import com.google.api.client.http.HttpResponseException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.rep.Attribute;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.util.SolrCache;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;


/**
 */
public class FreebaseSearch extends KBSearch {

    private static Logger LOG = Logger.getLogger(FreebaseSearch.class.getName());
    public static String NAME_SIMILARITY_CACHE = "similarity";
    private static final boolean AUTO_COMMIT =true;

    //two propperties for debugging purposes.In practice both should be false. set to true
    //if you want to deliberately trigger calls to FB apis
    private static final boolean ALWAYS_CALL_REMOTE_SEARCHAPI =false;
    private static final boolean ALWAYS_CALL_REMOTE_TOPICAPI =false;
    private FreebaseQueryHelper searcher;

    protected Map<String, SolrCache> otherCache;


    public FreebaseSearch(String kbSearchPropertyFile, Boolean fuzzyKeywords,
                          EmbeddedSolrServer cacheEntity, EmbeddedSolrServer cacheConcept,
                          EmbeddedSolrServer cacheProperty) throws IOException {
        super(kbSearchPropertyFile, fuzzyKeywords, cacheEntity, cacheConcept, cacheProperty);
        searcher = new FreebaseQueryHelper(properties);
        otherCache = new HashMap<>();

    }

    public void registerOtherCache(String name, EmbeddedSolrServer cacheServer) {
        otherCache.put(name, new SolrCache(cacheServer));
    }

    @Override
    public List<Entity> findEntityCandidates(String content) throws IOException {
        String query = createQuery_findEntities(content);
        return find_matchingEntitiesForText(query);
    }

    @Override
    public List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws IOException {
        String query = createQuery_findEntities(content);
        return find_matchingEntitiesForText(query, types);
    }

    protected List<Entity> find_matchingEntitiesForText(String text, String... types) throws IOException {
        boolean forceQuery = false;
        text = StringEscapeUtils.unescapeXml(text);
        int bracket = text.indexOf("(");
        if (bracket != -1) {
            text = text.substring(0, bracket).trim();
        }
        List<String> query_tokens = StringUtils.splitToAlphaNumericTokens(text, true);
        if (query_tokens.size() == 0)
            return new ArrayList<>();
        if (ALWAYS_CALL_REMOTE_SEARCHAPI)
            forceQuery = true;

        List<Entity> result = null;
        if (!forceQuery) {
            try {
                result = (List<Entity>) cacheEntity.retrieve(toSolrKey(text));
                if (result != null)
                    LOG.info("QUERY (cache load)=" + toSolrKey(text) + "|" + text);
            } catch (Exception e) {
            }
        }
        if (result == null) {
            result = new ArrayList<>();
            //firstly fetch candidate freebase topics
            List<FreebaseTopic> topics = searcher.searchapi_getTopicsByNameAndType(text, "any", true, 20); //search api does not retrieve complete types, find types for them
            for (FreebaseTopic ec : topics) {
                //Next get attributes for each topic
                //instantiate facts and types
                List<Attribute> attributes = findAttributesOfEntityCandidates(ec);
                ec.setAttributes(attributes);
                for (Attribute attr: attributes) {
                    if (attr.getRelation().equals("/type/object/type") &&
                            attr.getOtherInfo().get(FreebaseQueryHelper.FB_NESTED_TRIPLE_OF_TOPIC).equals("n") &&
                            !ec.hasType(attr.getValueURI()))
                        ec.addType(new Clazz(attr.getValueURI(), attr.getValue()));
                }
            }

            Iterator<FreebaseTopic> it = topics.iterator();
            while (it.hasNext()) {
                FreebaseTopic ec = it.next();
                List<String> cell_text_tokens = StringUtils.splitToAlphaNumericTokens(ec.getLabel(), true);
                int size = cell_text_tokens.size();
                cell_text_tokens.removeAll(query_tokens);
                if (cell_text_tokens.size() == size)
                    it.remove(); //the entity name does not contain any query word. remove it
            }

            if (topics.size() == 0 && fuzzyKeywords) { //does the query has conjunection? if so, we may need to try again with split queries
                String[] queries = text.split("\\band\\b");
                if (queries.length < 2) {
                    queries = text.split("\\bor\\b");
                    if (queries.length < 2) {
                        queries = text.split("/");
                        if (queries.length < 2) {
                            queries = text.split(",");
                        }
                    }
                }
                if (queries.length > 1) {
                    for (String q : queries) {
                        q = q.trim();
                        if (q.length() < 1) continue;
                        result.addAll(find_matchingEntitiesForText(q, types));
                    }
                }
            }

            result.addAll(topics);
            try {
                cacheEntity.cache(toSolrKey(text), result, AUTO_COMMIT);
                LOG.warn("QUERY (cache save)=" + toSolrKey(text) + "|" + text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int beforeFiltering = result.size();
        if (types.length > 0) {
            Iterator<Entity> it = result.iterator();
            while (it.hasNext()) {
                Entity ec = it.next();
                boolean typeSatisfied = false;
                for (String t : types) {
                    if (ec.hasType(t)) {
                        typeSatisfied = true;
                        break;
                    }
                }
                if (!typeSatisfied)
                    it.remove();
            }
        }

        //filter EC types
        String id = "|";
        for (Entity ec : result) {
            id = id + ec.getId() + ",";
            //ec.setTypes(FreebaseSearchResultFilter.filterTypes(ec.getTypes()));
            List<Clazz> filteredTypes = FreebaseSearchResultFilter.filterTypes(ec.getTypes());
            ec.getTypes().clear();
            for (Clazz ft : filteredTypes)
                ec.addType(ft);
        }

        System.out.println("(QUERY_KB:" + beforeFiltering + " => " + result.size() + id);
        return result;
    }


    protected List<Attribute> findAttributesOfEntityCandidates(String entityId) throws IOException {
        return find_triples(entityId, cacheEntity);
    }



    @Override
    protected List<Attribute> findAttributesOfProperty(String propertyId) throws IOException {
        return find_triples(propertyId, cacheProperty);
    }

    @Override
    public List<Attribute> findAttributesOfConcept(String conceptId) throws IOException {
        //return find_triplesForEntity(conceptId);
        boolean forceQuery = false;
        if (ALWAYS_CALL_REMOTE_TOPICAPI)
            forceQuery = true;
        List<Attribute> attributes = new ArrayList<>();
        String query = createQuery_findFacts(conceptId);
        if (query.length() == 0) return attributes;

        try {
            attributes = (List<Attribute>) cacheConcept.retrieve(toSolrKey(query));
            if (attributes != null)
                LOG.warn("QUERY (cache load)=" + toSolrKey(query) + "|" + query);
        } catch (Exception e) {
        }
        if (attributes == null || forceQuery) {
            attributes = new ArrayList<>();
            List<Attribute> retrievedAttributes = searcher.topicapi_getAttributesOfTopic(conceptId);
            //check firstly, is this a concept?
            boolean isConcept = false;
            for (Attribute f : retrievedAttributes) {
                if (f.getRelation().equals("/type/object/type") && f.getValueURI() != null && f.getValueURI().equals("/type/type")) {
                    isConcept = true;
                    break;
                }
            }
            if (!isConcept) {
                try {
                    cacheConcept.cache(toSolrKey(query), attributes, AUTO_COMMIT);
                    LOG.warn("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return attributes;
            }

            //ok, this is a concept. We need to deep-fetch its properties, and find out the range of their properties
            System.out.println(">>" + retrievedAttributes.size());
            for (Attribute f : retrievedAttributes) {
                if (f.getRelation().equals("/type/type/properties")) { //this is a property of a concept, we need to process it further
                    String propertyId = f.getValueURI();
                    if (propertyId == null) continue;

                    List<Attribute> attrOfProperty = findAttributesOfProperty(propertyId);
                    for (Attribute t : attrOfProperty) {
                        if (t.getRelation().equals("/type/property/expected_type")) {
                            String rangeLabel = t.getValue();
                            String rangeURL = t.getValueURI();
                            Attribute attr = new Attribute(f.getValueURI(), rangeLabel);
                            attr.setValueURI(rangeURL);
                            attr.getOtherInfo().put(FreebaseQueryHelper.FB_NESTED_TRIPLE_OF_TOPIC,"n");

                            //attributes.add(new String[]{f[2], rangeLabel, rangeURL, "n"});
                        }
                    }
                } else {
                    attributes.add(f);
                }
            }
            try {
                cacheConcept.cache(toSolrKey(query), attributes, AUTO_COMMIT);
                LOG.warn("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //filtering
        Iterator<Attribute> it = attributes.iterator();
        while (it.hasNext()) {
            Attribute f = it.next();
            if (FreebaseSearchResultFilter.ignoreFactByPredicate(f.getRelation()))
                it.remove();
        }
        return attributes;
    }

    @Override
    public double find_granularityForConcept(String type) throws IOException {
        String query = createQuery_findGranularity(type);
        Double result = null;
        try {
            Object o = cacheConcept.retrieve(toSolrKey(query));
            if (o != null) {
                LOG.warn("QUERY (cache load)=" + toSolrKey(query) + "|" + type);
                return (Double) o;
            }
        } catch (Exception e) {
        }

        if (result == null) {
            try {
                double granularity = searcher.find_granularityForType(type);
                result = granularity;
                try {
                    cacheConcept.cache(toSolrKey(query), result, AUTO_COMMIT);
                    LOG.warn("QUERY (cache save)=" + toSolrKey(query) + "|" + type);
                } catch (Exception e) {
                    System.out.println("FAILED:" + type);
                    e.printStackTrace();
                }
            } catch (IOException ioe) {
                LOG.warn("ERROR(Instances of Type): Unable to fetch freebase page of instances of type: " + type);
            }
        }
        if (result == null)
            return -1.0;
        return result;
    }

    @Override
    public List<Clazz> find_rangeOfRelation(String relationURI) throws IOException {
        List<Attribute> attributes =
                findAttributesOfEntityCandidates(new Entity(relationURI, relationURI));
        List<Clazz> types = new ArrayList<>();
        for (Attribute attr : attributes) {
            if (attr.getRelation().equals("/type/property/expected_type")) {
                types.add(new Clazz(attr.getValueURI(), attr.getValue()));
            }
        }
        return types;
    }

    public List<Clazz> find_typesForEntity_filtered(String id) throws IOException {
        String query = createQuery_findTypes(id);
        List<Clazz> result = null;
        try {
            result = (List<Clazz>) cacheEntity.retrieve(toSolrKey(query));
            if (result != null) {
                LOG.warn("QUERY (cache load)=" + toSolrKey(query) + "|" + query);
            }
        } catch (Exception e) {
        }
        if (result == null) {
            result = new ArrayList<>();
            List<Attribute> attributes = searcher.topicapi_getTypesOfTopicID(id);
            for (Attribute attr : attributes) {
                String type = attr.getValueURI(); //this is the id of the type
                result.add(new Clazz(type, attr.getValue()));
            }
            try {
                cacheEntity.cache(toSolrKey(query), result, AUTO_COMMIT);
                // debug_helper_method(id, facts);
                LOG.warn("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return FreebaseSearchResultFilter.filterTypes(result);
    }

    private List<Attribute> find_triples(String id, SolrCache cache) throws IOException {
        boolean forceQuery = false;
        if (ALWAYS_CALL_REMOTE_TOPICAPI)
            forceQuery = true;

        String query = createQuery_findFacts(id);
        if (query.length() == 0)
            return new ArrayList<>();
        List<Attribute> result = null;
        try {
            result = (List<Attribute>) cache.retrieve(toSolrKey(query));
            if (result != null)
                LOG.warn("QUERY (cache load)=" + toSolrKey(query) + "|" + query);
        } catch (Exception e) {
        }
        if (result == null || forceQuery) {
            List<Attribute> facts;
            try {
                facts = searcher.topicapi_getAttributesOfTopic(id);
            } catch (HttpResponseException e) {
                if (donotRepeatQuery(e))
                    facts = new ArrayList<>();
                else
                    throw e;
            }
            result = new ArrayList<>();
            result.addAll(facts);
            try {
                cache.cache(toSolrKey(query), result, AUTO_COMMIT);
                LOG.warn("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //filtering
        Iterator<Attribute> it = result.iterator();
        while (it.hasNext()) {
            Attribute attr = it.next();
            if (FreebaseSearchResultFilter.ignoreFactByPredicate(attr.getRelation()))
                it.remove();
        }
        return result;
    }

    public double find_similarity(String id1, String id2) {
        String query = id1 + "<>" + id2;
        Object result = null;
        try {
            result = otherCache.get(NAME_SIMILARITY_CACHE).retrieve(toSolrKey(query));
            if (result != null)
                LOG.warn("QUERY (cache load)=" + toSolrKey(query) + "|" + query);
        } catch (Exception e) {
        }
        if(result==null)
            return -1.0;
        return (Double) result;
    }

    public void saveSimilarity(String id1, String id2, double score, boolean biDirectional,
                               boolean commit) {
        String query = id1 + "<>" + id2;
        try {
            otherCache.get(NAME_SIMILARITY_CACHE).cache(toSolrKey(query), score, commit);
            LOG.warn("QUERY (cache saving)=" + toSolrKey(query) + "|" + query);
            if(biDirectional){
                query = id2 + "<>" + id1;
                otherCache.get(NAME_SIMILARITY_CACHE).cache(toSolrKey(query), score, commit);
                LOG.warn("QUERY (cache saving)=" + toSolrKey(query) + "|" + query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commitChanges() throws IOException, SolrServerException {
        cacheConcept.commit();
        cacheEntity.commit();
        cacheProperty.commit();
        for(SolrCache cache: otherCache.values())
            cache.commit();
    }

    private boolean donotRepeatQuery(HttpResponseException e) {
        String message = e.getContent();
        if (message.contains("\"reason\": \"notFound\""))
            return true;
        return false;
    }


    private String toSolrKey(String text) {
        //return String.valueOf(text.hashCode());
        return String.valueOf(text);
    }


    @Override
    public void finalizeConnection() throws IOException {
        if (cacheEntity != null)
            cacheEntity.shutdown();
        if (cacheConcept != null)
            cacheConcept.shutdown();
        if (cacheProperty != null)
            cacheProperty.shutdown();
    }
}
