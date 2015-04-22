package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.label;

import org.apache.solr.client.solrj.SolrServer;
import uk.ac.shef.dcs.oak.triplesearch.solr.SolrSearchSchemaIndexProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 08/03/13
 * Time: 13:06
 * <p/>
 *
 */
public class ConceptLabelFinderSolr extends LabelFinder {
    private SolrServer server;

    public ConceptLabelFinderSolr(SolrServer server) {
        super(null);
        this.server = server;
    }


    @Override
    /**
     * String - label
     */
    protected List<String> findCandidatesInKB(String uri) {
        List<String> rs = new ArrayList<String>();
        String label = SolrSearchSchemaIndexProxy.searchClassLocalname(uri, server);
        if (label != null && label.indexOf("@") != -1 && !label.substring(label.lastIndexOf("@")).equalsIgnoreCase("en")) {
        }
        if (label != null)
            rs.add(label);
        return rs;
    }

    /**
     * This method simply repeats findCandidatesInKB. because the schema index is very small there is no need to cache
     *
     * @param text
     * @return
     */
    @Override
    protected List<String> findCandidatesInCache(String text) {
        return findCandidatesInKB(text);
    }
}
