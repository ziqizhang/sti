package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.relation;

import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.QueryCache;

import java.io.IOException;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 10/03/13
 * Time: 14:58
 */
public abstract class RelationFinder {

    protected QueryCache cache;

    public RelationFinder(QueryCache cache) {
        this.cache = cache;
    }

    /**
     * This method should implement the strategies for caching, if any
     *
     * @return
     */
    protected abstract List<String> findRelationBetweenInKB(String uri1, String uri2);

    protected abstract List<String> findRelationBetweenInCache(String uri1, String uri2);

    public List<String> findRelationBetween(String uri1, String uri2) throws IOException, SolrServerException {
        List<String> rs = findRelationBetweenInCache(uri1, uri2);
        if (rs == null) {
            rs = findRelationBetweenInKB(uri1, uri2);
        }
        return rs;
    }

}
