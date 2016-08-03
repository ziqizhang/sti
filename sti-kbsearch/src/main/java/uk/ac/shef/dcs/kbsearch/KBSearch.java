package uk.ac.shef.dcs.kbsearch;

import org.apache.log4j.Logger;
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
    protected boolean fuzzyKeywords;

    protected static final String KB_SEARCH_RESULT_STOPLIST="kb.search.result.stoplistfile";
    protected static final String KB_SEARCH_CLASS = "kb.search.class";
    protected static final String KB_SEARCH_TRY_FUZZY_KEYWORD = "kb.search.tryfuzzykeyword";
    
    protected static final boolean AUTO_COMMIT = true;
    protected static final boolean ALWAYS_CALL_REMOTE_SEARCHAPI = false;

    protected KBSearchResultFilter resultFilter;

    protected final Logger log = Logger.getLogger(getClass());

    /**
     *
     * @param fuzzyKeywords given a query string, kbsearch will firstly try to fetch results matching the exact query. when no match is
     *                      found, you can set fuzzyKeywords to true, to let kbsearch to break the query string based on conjunective words.
     *                      So if the query string is "tom and jerry", it will try "tom" and "jerry"
     * @param cacheEntity the solr instance to cache retrieved entities from the kb. pass null if not needed
     * @param cacheConcept the solr instance to cache retrieved classes from the kb. pass null if not needed
     * @param cacheProperty the solr instance to cache retrieved properties from the kb. pass null if not needed
     * @param cacheSimilarity the solr instance to cache computed semantic similarity between entity and class. pass null if not needed
     * @throws IOException
     */
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


    /**
     * Given a string, fetch candidate entities (resources) from the KB
     * Candidate entities are those resources for which label or part of the label matches the given content
     * @param content
     * @return
     * @throws IOException
     */
    public abstract List<Entity> findEntityCandidates(String content) throws KBSearchException;

    /**
     * Given a string,  fetch candidate entities (resources) from the KB that only match certain types
     * @param content
     * @param types
     * @return
     * @throws KBSearchException
     */
    public abstract List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws KBSearchException;

    /**
     * Get attributes of the entity candidate
     * (all predicates and object values of the triples where the candidate entity is the subject).
     *
     * Note: Certain predicates may be blacklisted.
     * @throws KBSearchException
     */
    public abstract List<Attribute> findAttributesOfEntities(Entity ec) throws KBSearchException;

    /**
     * get attributes of the class
    * @throws KBSearchException
     */
    public abstract List<Attribute> findAttributesOfClazz(String clazzId) throws KBSearchException;

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
    public double findGranularityOfClazz(String clazz) throws KBSearchException {
        return 0;
    }

    /**
     * compute the seamntic similarity between an entity and a class
     * @param entity_id
     * @param clazz_url
     * @return
     * @throws KBSearchException
     */
    public double findEntityClazzSimilarity(String entity_id, String clazz_url) throws KBSearchException {
        return 0;
    }

    /**
     * save the computed semantic similarity between the entity and class
     * @param entity_id
     * @param clazz_url
     * @param score
     * @param biDirectional
     * @param commit
     * @throws KBSearchException
     */
    public void cacheEntityClazzSimilarity(String entity_id, String clazz_url, double score, boolean biDirectional,
                                                    boolean commit) throws KBSearchException {
        String query = createSolrCacheQuery_findEntityClazzSimilarity(entity_id, clazz_url);
        try {
            cacheSimilarity.cache(query, score, commit);
            log.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
            if (biDirectional) {
                query = clazz_url + "<>" + entity_id;
                cacheSimilarity.cache(query, score, commit);
                log.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    public void commitChanges() throws KBSearchException {

        try {
            cacheConcept.commit();
            cacheEntity.commit();
            cacheProperty.commit();
        } catch (Exception e) {
            throw new KBSearchException(e);
        }
    }


    public void closeConnection() throws KBSearchException {

        try {
            if (cacheEntity != null)
                cacheEntity.shutdown();
            if (cacheConcept != null)
                cacheConcept.shutdown();
            if (cacheProperty != null) {
                cacheProperty.shutdown();
            }
        } catch (Exception e) {
            throw new KBSearchException(e);
        }

    }



    //TODO the properties below should be moved to a different class (SolrCacheHelper?) and renamed properly
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

    protected String createSolrCacheQuery_findEntityClazzSimilarity(String entity, String concept){
        return entity+"<>"+concept;
    }


}
