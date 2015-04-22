package uk.ac.shef.dcs.oak.lodie.table.interpreter.content;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.lodie.util.GenericSearchCache_SOLR;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 21/01/14
 * Time: 16:53
 * To change this template use File | Settings | File Templates.
 */
public abstract class KBSearcher {

    protected GenericSearchCache_SOLR solrCache;
    protected boolean split_at_conjunction;

    public KBSearcher(String solrHomePath, String coreName, boolean split_at_conjunction){
        solrCache = new GenericSearchCache_SOLR(solrHomePath, coreName);
        this.split_at_conjunction=split_at_conjunction;
    }

    public KBSearcher(SolrServer server, boolean split_at_conjunection){
        solrCache = new GenericSearchCache_SOLR(server);
        this.split_at_conjunction=split_at_conjunection;
    }

    //match cells to entities
    public abstract List<EntityCandidate> find_matchingEntitiesForCell(LTableContentCell tcc) throws IOException;

    //match cells to entities, only those entities whose types are matched with requirement
    public abstract List<EntityCandidate> find_matchingEntities_with_type_forCell(LTableContentCell tcc, String... types) throws IOException;

    //get triples for entities
    public abstract List<String[]> find_triplesForEntity(EntityCandidate ec) throws IOException;

    public abstract double find_granularityForType(String type) throws IOException;

    protected String createQuery_findEntities(LTableContentCell tcc){
        return tcc.getText();
    }

    protected String createQuery_findFacts(EntityCandidate ec) {
        return createQuery_findFacts(ec.getId());
    }

    protected String createQuery_findFacts(String query) {
        return "FACTS_"+query;
    }

    protected String createQuery_findTypes(String query){
        return "TYPES_"+query;
    }

    public abstract List<String[]> find_expected_types_of_relation(String majority_relation_name) throws IOException;

    public void shutdownCache(){
        solrCache.shutdown();
    }

}
