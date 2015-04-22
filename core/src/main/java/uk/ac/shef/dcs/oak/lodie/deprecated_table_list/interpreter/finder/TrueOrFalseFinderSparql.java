package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder;

import uk.ac.shef.dcs.oak.triplesearch.triple.SPARQLQueryAgent;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 29/04/13
 * Time: 13:00
 */
public class TrueOrFalseFinderSparql extends TrueOrFalseFinder {
    private String[] endPoints;

    public TrueOrFalseFinderSparql(QueryCache cache, String... endpoints) {
        super(cache);
        this.endPoints = endpoints;
    }

    @Override
    protected boolean hasStatementInKB(String statementSPARQL) {
        boolean yes = false;
        for (String endpoint : endPoints) {
            yes = SPARQLQueryAgent.hasStatement(statementSPARQL, endpoint);
            if (yes)
                yes=true;
        }
        try {
            cache.cache(String.valueOf(statementSPARQL.hashCode()), Boolean.valueOf(yes),true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return yes;
    }

    @Override
    protected Boolean hasStatementInCache(String statementSPARQL) {
        String idInCache = String.valueOf(statementSPARQL.hashCode());
        try {
            Object rs = cache.retrieve(idInCache);
            if (rs != null)
                return Boolean.valueOf(rs.toString());
            //return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
