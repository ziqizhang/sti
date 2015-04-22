package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.finder.concept;

import org.apache.solr.client.solrj.SolrServer;
import uk.ac.shef.dcs.oak.triplesearch.solr.SolrSearchSchemaIndexProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 28/02/13
 * Time: 15:06
 *
 * This ConceptFinder finds *classes* matching keywords. Classes are defined in ontologies, T-Box
 * It uses the "ontology schema" index
 */
public class ConceptFinderSolr extends ConceptFinder {

    private SolrServer server;

    public ConceptFinderSolr(SolrServer server) {
        super(null);
        this.server=server;
    }


    @Override
    /**
     * String[0] - the concept matching text; currently does not record equivalent classes
     * todo: MAYBE also add finding equiv concepts
     */
    protected List<String[]> findCandidatesInKB(String text) {
        List<String> classes= SolrSearchSchemaIndexProxy.searchClassesByKeyword(text, server);
        List<String[]> output = new ArrayList<String[]>();
        for(String c: classes){
            output.add(new String[]{c, ""});
        }
        return output;
    }

    /**
     * This method simply repeats findCandidatesInKB. because the schema index is very small there is no need to cache
     * @param text
     * @return
     */
    @Override
    protected List<String[]> findCandidatesInCache(String text) {
        return findCandidatesInKB(text);
    }

}
