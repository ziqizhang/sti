package uk.ac.shef.dcs.kbsearch;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearch;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by - on 17/03/2016.
 */
public class KBSearchFactory {



    public KBSearch createInstance(String className,
                                     String kbSearchPropertyFile, boolean fuzzyKeywords,
                                     EmbeddedSolrServer cacheEntity, EmbeddedSolrServer cacheConcept,
                                     EmbeddedSolrServer cacheProperty) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if(className.equals(FreebaseSearch.class.getName())){
            return (KBSearch) Class.forName(className).
                    getDeclaredConstructor(String.class,
                            Boolean.class,
                            EmbeddedSolrServer.class,
                            EmbeddedSolrServer.class,
                            EmbeddedSolrServer.class).
                    newInstance(kbSearchPropertyFile,
                    fuzzyKeywords, cacheEntity, cacheConcept, cacheProperty);
        }
        return null;
    }
}
