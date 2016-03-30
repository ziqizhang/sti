package uk.ac.shef.dcs.kbsearch;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.rep.Attribute;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.util.SolrCache;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 */
public abstract class KBSearch {

    protected SolrCache cacheEntity;
    protected SolrCache cacheConcept;
    protected SolrCache cacheProperty;
    protected boolean fuzzyKeywords;
    protected Properties properties = new Properties();

    public KBSearch(String kbSearchPropertyFile, Boolean fuzzyKeywords,
                    EmbeddedSolrServer cacheEntity, EmbeddedSolrServer cacheConcept,
                    EmbeddedSolrServer cacheProperty) throws IOException {
        properties.load(new FileInputStream(kbSearchPropertyFile));
        if (cacheEntity != null)
            this.cacheEntity = new SolrCache(cacheEntity);
        if (cacheConcept != null)
            this.cacheConcept = new SolrCache(cacheConcept);
        if (cacheProperty != null)
            this.cacheProperty = new SolrCache(cacheProperty);
        this.fuzzyKeywords = fuzzyKeywords;
    }

    /**
     * given a content cell, fetch candidate entities from a KB
     * @param content
     * @return
     * @throws IOException
     */
    public abstract List<Entity> findEntityCandidates(String content) throws IOException;

    /**
     * given a content cell fetch candidate entities that only match certain types from a KB
     * @param content
     * @param types
     * @return
     * @throws IOException
     */
    public abstract List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws IOException;

    /**
     * get attributes that contain the entity candidate
     *
     * It is up to implementing class to decide whether the entity must be subject/object/either of attributes
     *
     *
     * @param ec
     * @return a list of attributes, each of which is represented as a string array. It is up to implementing class
     * to decide what the array should be
     * @throws IOException
     */
    public List<Attribute> findAttributesOfEntityCandidates(Entity ec) throws IOException {
        return findAttributesOfEntityCandidates(ec.getId());
    }

    /**
     *
     */
    protected abstract List<Attribute> findAttributesOfEntityCandidates(String entityId) throws IOException;

    /**
     * get attributes that contain the concept
     *
     * It is up to implementing class to decide whether the concept must be subject/object/either of attributes
     *
     *
     * @return a list of attributes, each of which is represented as a string array. It is up to implementing class
     * to decide what the array should be
     * @throws IOException
     */
    public abstract List<Attribute> findAttributesOfConcept(String conceptId) throws IOException;

    /**
     * get attributes that contain the property
     *
     *
     * @return a list of attributes, each of which is represented as a string array. It is up to implementing class
     * to decide what the array should be
     * @throws IOException
     */
    protected abstract List<Attribute> findAttributesOfProperty(String propertyId) throws IOException;

    public abstract double find_granularityForConcept(String type) throws IOException;

    protected String createQuery_findEntities(String content){
        return content;
    }

    protected String createQuery_findFacts(String query) {
        return "FACTS_"+query;
    }

    protected String createQuery_findTypes(String query){
        return "TYPES_"+query;
    }

    protected String createQuery_findGranularity(String query) {
        return "GRANULARITY_" + query;
    }
    public abstract List<Clazz> find_rangeOfRelation(String relationURI) throws IOException;

    public abstract void finalizeConnection() throws IOException;

    public abstract double find_similarity(String entity_id, String concept_url);

    public abstract void saveSimilarity(String entity_id, String concept_url, double score, boolean biDirectional,
                                        boolean commit);

    public abstract void commitChanges() throws IOException, SolrServerException;
}
