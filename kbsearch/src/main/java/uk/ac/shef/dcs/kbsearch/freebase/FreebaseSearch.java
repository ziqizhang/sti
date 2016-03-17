package uk.ac.shef.dcs.kbsearch.freebase;

import com.google.api.client.http.HttpResponseException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.util.SolrUtils;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;

import java.util.logging.Logger;

/**
 */
public class FreebaseSearch extends KBSearch {

    private static Logger log = Logger.getLogger(FreebaseSearch.class.getName());
    public static String NAME_SIMILARITY_CACHE = "similarity";
    private static final boolean AUTO_COMMIT =true;

    //two propperties for debugging purposes.In practice both should be false. set to true
    //if you want to deliberately trigger calls to FB apis
    private static final boolean ALWAYS_CALL_REMOTE_SEARCHAPI =false;
    private static final boolean ALWAYS_CALL_REMOTE_TOPICAPI =false;
    private FreebaseQueryHelper searcher;

    protected Map<String, SolrUtils> otherCache;


    public FreebaseSearch(String kbSearchPropertyFile, boolean fuzzyKeywords,
                          EmbeddedSolrServer cacheEntity, EmbeddedSolrServer cacheConcept,
                          EmbeddedSolrServer cacheProperty) throws IOException {
        super(kbSearchPropertyFile, fuzzyKeywords, cacheEntity, cacheConcept, cacheProperty);
        searcher = new FreebaseQueryHelper(properties);
        otherCache = new HashMap<>();

    }

    public void registerOtherCache(String name, EmbeddedSolrServer cacheServer) {
        otherCache.put(name, new SolrUtils(cacheServer));
    }

    @Override
    public List<Entity> findEntityCandidates(String content) throws IOException {
        String query = createQuery_findEntities(content);
        return find_matchingEntitiesForText_clientFilterTypes(query);
    }

    @Override
    public List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws IOException {
        String query = createQuery_findEntities(content);
        return find_matchingEntitiesForText_clientFilterTypes(query, types);
    }

    protected List<Entity> find_matchingEntitiesForText_clientFilterTypes(String text, String... types) throws IOException {
        boolean forceQuery = false;
        text = StringEscapeUtils.unescapeXml(text);
        int bracket = text.indexOf("(");
        if (bracket != -1) {
            text = text.substring(0, bracket).trim();
        }
        List<String> query_tokens = StringUtils.toAlphaNumericTokens(text, true);
        if (query_tokens.size() == 0)
            return new ArrayList<>();
        if (ALWAYS_CALL_REMOTE_SEARCHAPI)
            forceQuery = true;

        List<Entity> result = null;
        if (!forceQuery) {
            try {
                result = (List<Entity>) cacheEntity.retrieve(toSolrKey(text));
                if (result != null)
                    log.warning("QUERY (cache load)=" + toSolrKey(text) + "|" + text);
            } catch (Exception e) {
            }
        }
        if (result == null) {
            result = new ArrayList<>();
            //List<EntityCandidate_FreebaseTopic> topics = searcher.searchapi_topics_with_name_and_type(text, "any",true,15,types);
            List<FreebaseEntity> topics = searcher.searchapi_topics_with_name_and_type(text, "any", true, 20); //search api does not retrieve complete types, find types for them
            for (FreebaseEntity ec : topics) {
                //find_triplesForEntityId()
                //instantiate facts and types
                List<String[]> facts = findTriplesOfEntityCandidates(ec);
                ec.setTriples(facts);
                for (String[] f : facts) {
                    if (f[0].equals("/type/object/type") &&
                            f[3].equals("n") &&
                            !ec.hasType(f[2]))
                        ec.addType(new Clazz(f[2], f[1]));
                }
            }

            Iterator<FreebaseEntity> it = topics.iterator();
            while (it.hasNext()) {
                FreebaseEntity ec = it.next();
                List<String> cell_text_tokens = StringUtils.toAlphaNumericTokens(ec.getLabel(), true);
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
                        result.addAll(find_matchingEntitiesForText_clientFilterTypes(q, types));
                    }
                }
            }

            result.addAll(topics);
            try {
                cacheEntity.cache(toSolrKey(text), result, AUTO_COMMIT);
                log.warning("QUERY (cache save)=" + toSolrKey(text) + "|" + text);
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


    protected List<String[]> findTriplesOfEntityCandidates(String entityId) throws IOException {
        return find_triples_filtered(entityId, cacheEntity);
    }



    @Override
    protected List<String[]> findTriplesOfProperty(String propertyId) throws IOException {
        return find_triples_filtered(propertyId, cacheProperty);
    }

    @Override
    public List<String[]> findTriplesOfConcept(String conceptId) throws IOException {
        //return find_triplesForEntity(conceptId);
        boolean forceQuery = false;
        if (ALWAYS_CALL_REMOTE_TOPICAPI)
            forceQuery = true;
        List<String[]> facts = new ArrayList<String[]>();
        String query = createQuery_findFacts(conceptId);
        if (query.length() == 0) return facts;

        try {
            facts = (List<String[]>) cacheConcept.retrieve(toSolrKey(query));
            if (facts != null)
                log.warning("QUERY (cache load)=" + toSolrKey(query) + "|" + query);
        } catch (Exception e) {
        }
        if (facts == null || forceQuery) {
            facts = new ArrayList<>();
            List<String[]> retrievedFacts = searcher.topicapi_facts_of_id(conceptId);
            //check firstly, is this a concept?
            boolean isConcept = false;
            for (String[] f : retrievedFacts) {
                if (f[0].equals("/type/object/type") && f[2] != null && f[2].equals("/type/type")) {
                    isConcept = true;
                    break;
                }
            }
            if (!isConcept) {
                try {
                    cacheConcept.cache(toSolrKey(query), facts, AUTO_COMMIT);
                    log.warning("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return facts;
            }

            //ok, this is a concept. We need to deep-fetch its properties, and find out the range of their properties
            System.out.println(">>" + retrievedFacts.size());
            for (String[] f : retrievedFacts) {
                if (f[0].equals("/type/type/properties")) { //this is a property of a concept, we need to process it further
                    String propertyId = f[2];
                    if (f[2] == null) continue;

                    List<String[]> triples4Property = findTriplesOfProperty(propertyId);
                    for (String[] t : triples4Property) {
                        if (t[0].equals("/type/property/expected_type")) {
                            String rangeLabel = t[1];
                            String rangeURL = t[2];
                            facts.add(new String[]{f[2], rangeLabel, rangeURL, "n"});
                        }
                    }
                } else {
                    facts.add(f);
                }
            }
            try {
                cacheConcept.cache(toSolrKey(query), facts, AUTO_COMMIT);
                log.warning("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //filtering
        Iterator<String[]> it = facts.iterator();
        while (it.hasNext()) {
            String[] f = it.next();
            if (FreebaseSearchResultFilter.ignoreFactWithPredicate(f[0]))
                it.remove();
        }
        return facts;
    }

    @Override
    public double find_granularityForConcept(String type) throws IOException {
        String query = createQuery_findGranularity(type);
        Double result = null;
        try {
            Object o = cacheConcept.retrieve(toSolrKey(query));
            if (o != null) {
                log.warning("QUERY (cache load)=" + toSolrKey(query) + "|" + type);
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
                    log.warning("QUERY (cache save)=" + toSolrKey(query) + "|" + type);
                } catch (Exception e) {
                    System.out.println("FAILED:" + type);
                    e.printStackTrace();
                }
            } catch (IOException ioe) {
                log.warning("ERROR(Instances of Type): Unable to fetch freebase page of instances of type: " + type);
            }
        }
        if (result == null)
            return -1.0;
        return result;
    }

    @Override
    public List<String[]> find_expected_types_of_relation(String majority_relation_name) throws IOException {
        List<String[]> triples =
                findTriplesOfEntityCandidates(new Entity(majority_relation_name, majority_relation_name));
        List<String[]> types = new ArrayList<String[]>();
        for (String[] t : triples) {
            if (t[0].equals("/type/property/expected_type")) {
                types.add(new String[]{t[2], t[1]});
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
                log.warning("QUERY (cache load)=" + toSolrKey(query) + "|" + query);
            }
        } catch (Exception e) {
        }
        if (result == null) {
            result = new ArrayList<Clazz>();
            List<String[]> facts = searcher.topicapi_types_of_id(id);
            for (String[] f : facts) {
                String type = f[2]; //this is the id of the type
                result.add(new Clazz(type, f[1]));
            }
            try {
                cacheEntity.cache(toSolrKey(query), result, AUTO_COMMIT);
                // debug_helper_method(id, facts);
                log.warning("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return FreebaseSearchResultFilter.filterTypes(result);
    }

    private List<String[]> find_triples_filtered(String id, SolrUtils cache) throws IOException {
        boolean forceQuery = false;
        if (ALWAYS_CALL_REMOTE_TOPICAPI)
            forceQuery = true;

        String query = createQuery_findFacts(id);
        if (query.length() == 0)
            return new ArrayList<>();
        List<String[]> result = null;
        try {
            result = (List<String[]>) cache.retrieve(toSolrKey(query));
            if (result != null)
                log.warning("QUERY (cache load)=" + toSolrKey(query) + "|" + query);
        } catch (Exception e) {
        }
        if (result == null || forceQuery) {
            List<String[]> facts;
            try {
                facts = searcher.topicapi_facts_of_id(id);
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
                log.warning("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //filtering
        Iterator<String[]> it = result.iterator();
        while (it.hasNext()) {
            String[] fact = it.next();
            if (FreebaseSearchResultFilter.ignoreFactWithPredicate(fact[0]))
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
                log.warning("QUERY (cache load)=" + toSolrKey(query) + "|" + query);
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
            log.warning("QUERY (cache saving)=" + toSolrKey(query) + "|" + query);
            if(biDirectional){
                query = id2 + "<>" + id1;
                otherCache.get(NAME_SIMILARITY_CACHE).cache(toSolrKey(query), score, commit);
                log.warning("QUERY (cache saving)=" + toSolrKey(query) + "|" + query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commitChanges() throws IOException, SolrServerException {
        cacheConcept.commit();
        cacheEntity.commit();
        cacheProperty.commit();
        for(SolrUtils cache: otherCache.values())
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
