package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.superclass;

import org.apache.solr.client.solrj.SolrServer;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.entity.EntityFinderSolr;
import uk.ac.shef.dcs.oak.triplesearch.solr.SolrSearchTripleIndexProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 11/03/13
 * Time: 17:05
 */
public class EntitySuperConceptFinderSolr extends SuperConceptFinder {
    private SolrServer server;

    public EntitySuperConceptFinderSolr(QueryCache cache, SolrServer server) {
        super(cache);
        this.server = server;
    }

    @Override
    /**
     * todo: MAYBE also add equivalent concepts to super concepts
     */
    protected List<String[]> findCandidatesInKB(String uri) {
        List<String> labels = SolrSearchTripleIndexProxy.searchSuperclassesOf(uri, server);
        List<String[]> output = new ArrayList<String[]>();
        for(String l : labels)
            output.add(new String[]{l, ""});
        try {
            cache.cache(String.valueOf(EntityFinderSolr.createCacheQueryId_findSuperClassesOf(uri).hashCode())
                    , labels, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    @Override
    protected List<String[]> findCandidatesInCache(String text) {
        String queryId = EntityFinderSolr.createCacheQueryId_findSuperClassesOf(text);
        try {
            return (List<String[]>)cache.retrieve(String.valueOf(queryId.hashCode()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
