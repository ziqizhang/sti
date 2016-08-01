package uk.ac.shef.dcs.kbsearch;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearch;
import uk.ac.shef.dcs.kbsearch.sparql.DBpediaSearch;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by - on 17/03/2016.
 */
public class KBSearchFactory {


    public KBSearch createInstance(
            String kbSearchPropertyFile,
            EmbeddedSolrServer cacheEntity, EmbeddedSolrServer cacheConcept,
            EmbeddedSolrServer cacheProperty,EmbeddedSolrServer cacheSimilarity) throws KBSearchException {

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(kbSearchPropertyFile));

            String className = properties.getProperty(KBSearch.KB_SEARCH_CLASS);
            boolean fuzzyKeywords = Boolean.valueOf(properties.getProperty(KBSearch.KB_SEARCH_TRY_FUZZY_KEYWORD,"false"));
            if (className.equals(FreebaseSearch.class.getName())) {
                return (KBSearch) Class.forName(className).
                        getDeclaredConstructor(Properties.class,
                                Boolean.class,
                                EmbeddedSolrServer.class,
                                EmbeddedSolrServer.class,
                                EmbeddedSolrServer.class,
                                EmbeddedSolrServer.class).
                        newInstance(properties,
                                fuzzyKeywords, cacheEntity, cacheConcept, cacheProperty,
                                cacheSimilarity);
            }else if(className.equals(DBpediaSearch.class.getName())){
                return (KBSearch) Class.forName(className).
                        getDeclaredConstructor(Properties.class,
                                Boolean.class,
                                EmbeddedSolrServer.class,
                                EmbeddedSolrServer.class,
                                EmbeddedSolrServer.class,
                                EmbeddedSolrServer.class).
                        newInstance(properties,
                                fuzzyKeywords, cacheEntity, cacheConcept, cacheProperty,
                                cacheSimilarity);
            }

            else {
                throw new KBSearchException("Class:" + className + " not supported");
            }
        } catch (Exception e) {
            throw new KBSearchException(e);
        }
    }
}
