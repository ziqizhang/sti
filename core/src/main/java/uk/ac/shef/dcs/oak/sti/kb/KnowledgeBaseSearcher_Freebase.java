package uk.ac.shef.dcs.oak.sti.kb;

import com.google.api.client.http.HttpResponseException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.solr.client.solrj.SolrServer;
import uk.ac.shef.dcs.oak.sti.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.oak.sti.util.SearchCacheSolr;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.triplesearch.freebase.EntityCandidate_FreebaseTopic;
import uk.ac.shef.dcs.oak.triplesearch.freebase.FreebaseQueryHelper;
import uk.ac.shef.dcs.oak.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 */
public class KnowledgeBaseSearcher_Freebase extends KnowledgeBaseSearcher {

    private boolean commit;
    private FreebaseQueryHelper searcher;
    private static Logger log = Logger.getLogger(KnowledgeBaseSearcher_Freebase.class.getName());
    protected SearchCacheSolr cacheEntity;
    protected SearchCacheSolr cacheConcept;
    protected SearchCacheSolr cacheProperty;
    protected boolean split_at_conjunction;


    public KnowledgeBaseSearcher_Freebase(String freebase_properties, boolean split_at_conjunection,
                                          SolrServer cacheEntity, SolrServer cacheConcept,
                                          SolrServer cacheProperty) throws IOException {
        searcher = new FreebaseQueryHelper(freebase_properties);
        if (TableMinerConstants.COMMIT_SOLR_PER_FILE)
            commit = false;
        else
            commit = true;
        if (cacheEntity != null)
            this.cacheEntity = new SearchCacheSolr(cacheEntity);
        if (cacheConcept != null)
            this.cacheConcept = new SearchCacheSolr(cacheConcept);
        if (cacheProperty != null)
            this.cacheProperty = new SearchCacheSolr(cacheProperty);
        this.split_at_conjunction = split_at_conjunection;
    }


    @Override
    public List<EntityCandidate> findEntitiesForCell(LTableContentCell tcc) throws IOException {
        String query = createQuery_findEntities(tcc);
        return find_matchingEntitiesForText_clientFilterTypes(query);
    }

    @Override
    public List<EntityCandidate> findEntitiesOfTypesForCell(LTableContentCell tcc, String... types) throws IOException {
        String query = createQuery_findEntities(tcc);
        return find_matchingEntitiesForText_clientFilterTypes(query, types);
    }

    protected List<EntityCandidate> find_matchingEntitiesForText_clientFilterTypes(String text, String... types) throws IOException {
        boolean forceQuery = false;
        text = StringEscapeUtils.unescapeXml(text);
        int bracket = text.indexOf("(");
        if (bracket != -1) {
            text = text.substring(0, bracket).trim();
        }
        List<String> query_tokens = StringUtils.toAlphaNumericTokens(text, true);
        if (query_tokens.size() == 0)
            return new ArrayList<EntityCandidate>();
        if (TableMinerConstants.FORCE_SEARCHAPI_QUERY)
            forceQuery = true;

        List<EntityCandidate> result = null;
        if (!forceQuery) {
            try {
                result = (List<EntityCandidate>) cacheEntity.retrieve(toSolrKey(text));
                if (result != null)
                    log.warning("QUERY (cache load)=" + toSolrKey(text) + "|" + text);
            } catch (Exception e) {
            }
        }
        if (result == null) {
            result = new ArrayList<EntityCandidate>();
            //List<EntityCandidate_FreebaseTopic> topics = searcher.searchapi_topics_with_name_and_type(text, "any",true,15,types);
            List<EntityCandidate_FreebaseTopic> topics = searcher.searchapi_topics_with_name_and_type(text, "any", true, 20); //search api does not retrieve complete types, find types for them
            for (EntityCandidate_FreebaseTopic ec : topics) {
                //find_triplesForEntityId()
                //instantiate facts and types
                List<String[]> facts = find_triplesForEntity_filtered(ec);
                ec.setFacts(facts);
                for (String[] f : facts) {
                    if (f[0].equals("/type/object/type") &&
                            f[3].equals("n") &&
                            !ec.hasTypeId(f[2]))
                        ec.addType(new String[]{f[2], f[1]});
                }
            }

            Iterator<EntityCandidate_FreebaseTopic> it = topics.iterator();
            while (it.hasNext()) {
                EntityCandidate_FreebaseTopic ec = it.next();
                List<String> cell_text_tokens = StringUtils.toAlphaNumericTokens(ec.getName(), true);
                int size = cell_text_tokens.size();
                cell_text_tokens.removeAll(query_tokens);
                if (cell_text_tokens.size() == size)
                    it.remove(); //the entity name does not contain any query word. remove it
            }

            if (topics.size() == 0 && split_at_conjunction) { //does the query has conjunection? if so, we may need to try again with split queries
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
                cacheEntity.cache(toSolrKey(text), result, commit);
                log.warning("QUERY (cache save)=" + toSolrKey(text) + "|" + text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int beforeFiltering = result.size();
        if (types.length > 0) {
            Iterator<EntityCandidate> it = result.iterator();
            while (it.hasNext()) {
                EntityCandidate ec = it.next();
                boolean typeSatisfied = false;
                for (String t : types) {
                    if (ec.hasTypeId(t)) {
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
        for (EntityCandidate ec : result) {
            id = id + ec.getId() + ",";
            //ec.setTypes(KnowledgeBaseFreebaseFilter.filterTypes(ec.getTypes()));
            List<String[]> filteredTypes = KnowledgeBaseFreebaseFilter.filterTypes(ec.getTypes());
            ec.getTypes().clear();
            for(String[] ft:filteredTypes)
                ec.addType(ft);
        }

        System.out.println("(QUERY_KB:" + beforeFiltering + " => " + result.size() + id);
        return result;
    }


    public List<String[]> find_triplesForEntity_filtered(String entityId) throws IOException {
        return find_triples_filtered(entityId, cacheEntity);
    }

    @Override
    public List<String[]> find_triplesForEntity_filtered(EntityCandidate ec) throws IOException {
        return find_triplesForEntity_filtered(ec.getId());
    }

    @Override
    public List<String[]> find_triplesForProperty_filtered(String propertyId) throws IOException {
        return find_triples_filtered(propertyId, cacheProperty);
    }

    @Override
    public List<String[]> find_triplesForConcept_filtered(String conceptId) throws IOException {
        //return find_triplesForEntity(conceptId);
        boolean forceQuery = false;
        if (TableMinerConstants.FORCE_TOPICAPI_QUERY)
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
            facts = new ArrayList<String[]>();
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
                    cacheConcept.cache(toSolrKey(query), facts, commit);
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

                    List<String[]> triples4Property = find_triplesForProperty_filtered(propertyId);
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
                cacheConcept.cache(toSolrKey(query), facts, commit);
                log.warning("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //filtering
        Iterator<String[]> it =facts.iterator();
        while(it.hasNext()){
            String[] f = it.next();
            if (KnowledgeBaseFreebaseFilter.ignoreFactWithPredicate(f[0]))
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
                    cacheConcept.cache(toSolrKey(query), result, commit);
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
        List<String[]> triples = find_triplesForEntity_filtered(new EntityCandidate(majority_relation_name, majority_relation_name));
        List<String[]> types = new ArrayList<String[]>();
        for (String[] t : triples) {
            if (t[0].equals("/type/property/expected_type")) {
                types.add(new String[]{t[2], t[1]});
            }
        }
        return types;
    }

    public List<String[]> find_typesForEntity_filtered(String id) throws IOException {
        String query = createQuery_findTypes(id);
        List<String[]> result = null;
        try {
            result = (List<String[]>) cacheEntity.retrieve(toSolrKey(query));
            if (result != null) {
                log.warning("QUERY (cache load)=" + toSolrKey(query) + "|" + query);
            }
        } catch (Exception e) {
        }
        if (result == null) {
            result = new ArrayList<String[]>();
            List<String[]> facts = searcher.topicapi_types_of_id(id);
            for (String[] f : facts) {
                String type = f[2]; //this is the id of the type
                result.add(new String[]{type, f[1]});
            }
            try {
                cacheEntity.cache(toSolrKey(query), result, commit);
                // debug_helper_method(id, facts);
                log.warning("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return KnowledgeBaseFreebaseFilter.filterTypes(result);
    }

    private List<String[]> find_triples_filtered(String id, SearchCacheSolr cache) throws IOException {
        boolean forceQuery = false;
        if (TableMinerConstants.FORCE_TOPICAPI_QUERY)
            forceQuery = true;

        String query = createQuery_findFacts(id);
        if (query.length() == 0)
            return new ArrayList<String[]>();
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
                    facts = new ArrayList<String[]>();
                else
                    throw e;
            }
            result = new ArrayList<String[]>();
            result.addAll(facts);
            try {
                cache.cache(toSolrKey(query), result, commit);
                log.warning("QUERY (cache save)=" + toSolrKey(query) + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //filtering
        Iterator<String[]> it = result.iterator();
        while (it.hasNext()) {
            String[] fact = it.next();
            if (KnowledgeBaseFreebaseFilter.ignoreFactWithPredicate(fact[0]))
                it.remove();
        }
        return result;
    }

    private boolean donotRepeatQuery(HttpResponseException e) {
        String message = e.getContent();
        if(message.contains("\"reason\": \"notFound\""))
            return true;
        return false;
    }


    private String toSolrKey(String text) {
        //return String.valueOf(text.hashCode());
        return String.valueOf(text);
    }


    @Override
    public void finalizeConnection() {
        if (cacheEntity != null)
            cacheEntity.shutdown();
        if (cacheConcept != null)
            cacheConcept.shutdown();
        if (cacheProperty != null)
            cacheProperty.shutdown();
    }
}
