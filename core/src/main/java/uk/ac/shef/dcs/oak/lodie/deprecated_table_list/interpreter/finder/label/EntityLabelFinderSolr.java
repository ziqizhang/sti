package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.label;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;
import uk.ac.shef.dcs.oak.triplesearch.solr.SolrSearchTripleIndexProxy;

import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 08/03/13
 * Time: 13:06
 */
public class EntityLabelFinderSolr extends LabelFinder {

    private SolrServer server;

    public EntityLabelFinderSolr(QueryCache cache, SolrServer server) {
        super(cache);
        this.server = server;
    }

    @Override
    protected List<String> findCandidatesInKB(String uri) {
        List<String> labels = SolrSearchTripleIndexProxy.searchInstanceLabels(uri, server);
        try {
            cache.cache(String.valueOf(createCacheQueryId_findLabelsOfInstance(uri).hashCode())
                    , labels, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return labels;
    }

    @Override
    protected List<String> findCandidatesInCache(String text) {
        String queryId = createCacheQueryId_findLabelsOfInstance(text);
        try {
            return (List<String>)cache.retrieve(String.valueOf(queryId.hashCode()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
   find all labels of an instance
    */
    protected String createCacheQueryId_findLabelsOfInstance(String uri) {
        String q = "q=" +
                SolrSearchTripleIndexProxy.TRIPLE_INDEX_SUBJECT + ":" + uri + " AND "
                + "(" + SolrSearchTripleIndexProxy.TRIPLE_INDEX_PREDICATE + ":" + ClientUtils.escapeQueryChars("<" + SolrSearchTripleIndexProxy.URI_RDFS_LABEL + ">") + " OR " +
                ClientUtils.escapeQueryChars("<" + SolrSearchTripleIndexProxy.URI_FOAF_NAME + ">") + " OR " +
                ClientUtils.escapeQueryChars("<" + SolrSearchTripleIndexProxy.URI_DC_TITLE + ">") + " OR " +
                ClientUtils.escapeQueryChars("<" + SolrSearchTripleIndexProxy.URI_SKOS_PRELABEL + ">") + " OR " +
                ClientUtils.escapeQueryChars("<" + SolrSearchTripleIndexProxy.URI_SKOS_ALTLABEL + ">") + " OR " +
                ClientUtils.escapeQueryChars("<" + SolrSearchTripleIndexProxy.URI_FB_NAME + ">") + " OR " +
                ClientUtils.escapeQueryChars("<" + SolrSearchTripleIndexProxy.URI_FB_NAMEID + ">") + " OR " +
                ClientUtils.escapeQueryChars("<" + SolrSearchTripleIndexProxy.URI_BASEKB_NAME + ">") + " OR " +
                ClientUtils.escapeQueryChars("<" + SolrSearchTripleIndexProxy.URI_BASEKB_NAMEID + ">") + ")";
        return String.valueOf(Integer.valueOf(q));
    }


}
