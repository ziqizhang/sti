package uk.ac.shef.dcs.kbsearch.dbpedia;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Entity;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by Jan on 18.05.2016.
 */
public class DbpediaSearch extends KBSearch {

    public DbpediaSearch(Properties properties, Boolean fuzzyKeywords,
                         EmbeddedSolrServer cacheEntity, EmbeddedSolrServer cacheConcept,
                         EmbeddedSolrServer cacheProperty, EmbeddedSolrServer cacheSimilarity) throws IOException {
        super(fuzzyKeywords, cacheEntity, cacheConcept, cacheProperty,cacheSimilarity);
    }

    @Override
    public List<Entity> findEntityCandidates(String content) throws KBSearchException {
        return null;
    }

    @Override
    public List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws KBSearchException {
        return null;
    }

    @Override
    public List<Attribute> findAttributesOfEntities(Entity ec) throws KBSearchException {
        return null;
    }

    @Override
    public List<Attribute> findAttributesOfClazz(String clazzId) throws KBSearchException {
        return null;
    }

    @Override
    public List<Attribute> findAttributesOfProperty(String propertyId) throws KBSearchException {
        return null;
    }

    @Override
    public double findGranularityOfClazz(String clazz) throws KBSearchException {
        return 0;
    }

    @Override
    public double findEntityClazzSimilarity(String entity_id, String clazz_url) throws KBSearchException {
        return 0;
    }

    @Override
    public void cacheEntityClazztSimilarity(String entity_id, String clazz_url, double score, boolean biDirectional, boolean commit) throws KBSearchException {

    }

    @Override
    public void commitChanges() throws KBSearchException {

    }

    @Override
    public void closeConnection() throws KBSearchException {

    }
}
