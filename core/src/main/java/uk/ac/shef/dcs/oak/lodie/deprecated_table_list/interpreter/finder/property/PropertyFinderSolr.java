package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.property;

import org.apache.solr.client.solrj.SolrServer;
import uk.ac.shef.dcs.oak.triplesearch.solr.SolrSearchSchemaIndexProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 07/03/13
 * Time: 16:42
 */
public class PropertyFinderSolr extends PropertyFinder {

    private SolrServer server;

    public PropertyFinderSolr(SolrServer server) {
        super(null);
        this.server = server;
    }


    @Override
    /**
     * todo:MAYBE also add equivalent properties
     */
    protected List<String[]> findCandidatesInKB(String text) {
        List<String[]> output = new ArrayList<String[]>();
        List<String> res = SolrSearchSchemaIndexProxy.searchPropertiesByKeyword(text, server);
        for(String s: res){
            output.add(new String[]{s, ""});
        }
        return output;
    }

    /**
     * This method simply repeats findCandidatesInKB. because the schema index is very small there is no need to cache
     *
     * @param text
     * @return
     */
    @Override
    protected List<String[]> findCandidatesInCache(String text) {
        return findCandidatesInKB(text);
    }
}
