package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder;

import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 29/04/13
 * Time: 12:41
 */
public abstract class TrueOrFalseFinder {
    protected QueryCache cache;

    public TrueOrFalseFinder(QueryCache cache) {
        this.cache = cache;
    }

    /**
     * This method should implement the strategies for caching, if any
     * @return
     */
    protected abstract boolean hasStatementInKB(String statementSPARQL);

    protected abstract Boolean hasStatementInCache(String statementSPARQL);

    public boolean hasStatement(String statement) throws IOException, SolrServerException {
        Boolean rs = hasStatementInCache(statement);
        if (rs == null) {
            rs = hasStatementInKB(statement);
        }
        return rs;
    }


}
