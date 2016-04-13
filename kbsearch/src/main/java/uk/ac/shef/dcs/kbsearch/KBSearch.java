package uk.ac.shef.dcs.kbsearch;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.util.SolrCache;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 */
public abstract class KBSearch {

    protected SolrCache cacheEntity;
    protected SolrCache cacheConcept;
    protected SolrCache cacheProperty;
    protected SolrCache cacheSimilarity;
    protected Map<String, SolrCache> otherCache;
    protected boolean fuzzyKeywords;

    protected static final String KB_SEARCH_RESULT_STOPLIST="kb.search.result.stoplistfile";
    protected static final String KB_SEARCH_CLASS = "kb.search.class";
    protected static final String KB_SEARCH_TRY_FUZZY_KEYWORD = "kb.search.tryfuzzykeyword";


    protected KBSearchResultFilter resultFilter;

    public KBSearch(Boolean fuzzyKeywords,
                    EmbeddedSolrServer cacheEntity, EmbeddedSolrServer cacheConcept,
                    EmbeddedSolrServer cacheProperty, EmbeddedSolrServer cacheSimilarity) throws IOException {

        if (cacheEntity != null)
            this.cacheEntity = new SolrCache(cacheEntity);
        if (cacheConcept != null)
            this.cacheConcept = new SolrCache(cacheConcept);
        if (cacheProperty != null)
            this.cacheProperty = new SolrCache(cacheProperty);
        if(cacheSimilarity!=null)
            this.cacheSimilarity=new SolrCache(cacheSimilarity);
        this.fuzzyKeywords = fuzzyKeywords;

    }


    public void registerOtherCache(String name, EmbeddedSolrServer cacheServer) {
        otherCache.put(name, new SolrCache(cacheServer));
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
     * @throws KBSearchException
     */
    public abstract List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws KBSearchException;

    /**
     * get attributes of the entity candidate
     * @throws KBSearchException
     */
    public abstract List<Attribute> findAttributesOfEntities(Entity ec) throws KBSearchException;

    /**
     * get attributes of the concept
    * @throws KBSearchException
     */
    public abstract List<Attribute> findAttributesOfClazz(String conceptId) throws KBSearchException;

    /**
     * get attributes of the property
     *
     * @throws KBSearchException
     */
    public abstract List<Attribute> findAttributesOfProperty(String propertyId) throws KBSearchException;

    /**
     *
     * @param clazz
     * @return the granularity of the class in the KB.
     * @throws KBSearchException if the method is not supported
     */
    public abstract double findGranularityOfClazz(String clazz) throws KBSearchException;

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

    protected String createSolrCacheQuery_findGranularityOfClazz(String clazz) {
        return "GRANULARITY_" + clazz;
    }

    protected String createSolrCacheQuery_findEntityConceptSimilarity(String entity, String concept){
        return entity+"<>"+concept;
    }


}
