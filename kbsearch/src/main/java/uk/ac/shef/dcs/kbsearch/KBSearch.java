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

    protected static final String KB_SEARCH_RESULT_STOPLIST="kb.search.result.stoplistfile";

    protected KBSearchResultFilter resultFilter;

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

    public KBSearchResultFilter getResultFilter(){
        return resultFilter;
    }
    /**
     * given a content cell, fetch candidate entities from a KB
     * @param content
     * @return
     * @throws IOException
     */
    public abstract List<Entity> findEntityCandidates(String content) throws KBSearchException;

    /**
     * given a content cell fetch candidate entities that only match certain types from a KB
     * @param content
     * @param types
     * @return
     * @throws IOException
     */
    public abstract List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws KBSearchException;

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
    public abstract List<Attribute> findAttributesOfEntities(Entity ec) throws KBSearchException;

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
    public abstract List<Attribute> findAttributesOfClazz(String conceptId) throws KBSearchException;

    /**
     * get attributes that contain the property
     *
     *
     * @return a list of attributes, each of which is represented as a string array. It is up to implementing class
     * to decide what the array should be
     * @throws IOException
     */
    public abstract List<Attribute> findAttributesOfProperty(String propertyId) throws KBSearchException;

    public abstract double findGranularityOfClazz(String clazz) throws KBSearchException;

    public abstract List<Clazz> findRangeOfRelation(String relationURI) throws KBSearchException;

    public abstract double findEntityConceptSimilarity(String entity_id, String concept_url) throws KBSearchException;

    public abstract void cacheEntityConceptSimilarity(String entity_id, String concept_url, double score, boolean biDirectional,
                                                      boolean commit) throws KBSearchException;

    public abstract void commitChanges() throws KBSearchException;
    public abstract void closeConnection() throws KBSearchException;



    /*
    createSolrCacheQuery_XXX defines how a solr query should be constructed. If your implementing class
     want to benefit from solr cache, you should call these methods to generate a query string, which will
     be considered as the id of a record in the solr index. that query will be performed, to attempt to retrieve
     previously saved results if any.

     If there are no previously cached results, you have to perform your remote call to the KB, obtain the results,
     then cache the results in solr. Again you should call these methods to create a query string, which should be
     passed as the id of the record to be added to solr
     */
    protected String createSolrCacheQuery_findResources(String content){
        return content;
    }

    protected String createSolrCacheQuery_findAttributesOfResource(String resource) {
        return "ATTR_"+resource;
    }
    protected String createSolrCacheQuery_findClazzesOfResource(String resource){
        return "CLAZZ_"+resource;
    }

    protected String createSolrCacheQuery_findGranularityOfClazz(String clazz) {
        return "GRANULARITY_" + clazz;
    }

    protected String createSolrCacheQuery_findRangeOfRelation(String relation) {
        return "RANGE_" + relation;
    }

    protected String createSolrCacheQuery_findEntityConceptSimilarity(String entity, String concept){
        return entity+"<>"+concept;
    }


}
