package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.triplesearch.solr.SolrSearchTripleIndexProxy;

import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 10/03/13
 * Time: 15:16
 */
public class RelationFinderSolr extends RelationFinder {

    private SolrServer server;

    public RelationFinderSolr(QueryCache cache, SolrServer server) {
        super(cache);
        this.server = server;
    }

    /*
    Input params "uri1, uri2" must be embedded with "<>"
     */
    @Override
    protected List<String> findRelationBetweenInKB(String uri1, String uri2) {
        List<String> relations = SolrSearchTripleIndexProxy.searchRelationBetween(uri1, uri2, server);
        //cache

        try {
            cache.cache(
                    String.valueOf(createCacheQueryId_findRelationBetween(uri1, uri2).hashCode())
                    , relations, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return relations;
    }

    @Override
    protected List<String> findRelationBetweenInCache(String uri1, String uri2) {
        String queryId = createCacheQueryId_findRelationBetween(uri1, uri2);
        try {
            return (List<String>)cache.retrieve(String.valueOf(queryId.hashCode()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
   find all instances that are "typeof" something
    */
    private String createCacheQueryId_findRelationBetween(String uri1, String uri2) {
        return String.valueOf(("q="
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_SUBJECT + ":" + ClientUtils.escapeQueryChars("<"+uri1+">") + " AND "
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_OBJECT + ":" + ClientUtils.escapeQueryChars("<"+uri2+">")+"|"
                + SolrSearchTripleIndexProxy.TRIPLE_INDEX_PREDICATE).hashCode());
    }

}
