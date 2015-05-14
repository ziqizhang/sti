package uk.ac.shef.dcs.oak.sti.kb;

import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.shef.dcs.oak.sti.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;

import java.io.IOException;
import java.util.List;

/**
 */
public abstract class KnowledgeBaseSearcher {

    //match cells to entities
    public abstract List<EntityCandidate> findEntitiesForCell(LTableContentCell tcc) throws IOException;

    //match cells to entities, only those entities whose types are matched with requirement
    public abstract List<EntityCandidate> findEntitiesOfTypesForCell(LTableContentCell tcc, String... types) throws IOException;

    //get triples for entities
    public abstract List<String[]> find_triplesForEntity_filtered(EntityCandidate ec) throws IOException;

    public abstract List<String[]> find_triplesForEntity_filtered(String entityId) throws IOException;

    public abstract List<String[]> find_triplesForConcept_filtered(String conceptId) throws IOException;

    public abstract List<String[]> find_triplesForProperty_filtered(String propertyId) throws IOException;

    public abstract double find_granularityForConcept(String type) throws IOException;

    protected String createQuery_findEntities(LTableContentCell tcc){
        return tcc.getText();
    }

    protected String createQuery_findFacts(String query) {
        return "FACTS_"+query;
    }

    protected String createQuery_findTypes(String query){
        return "TYPES_"+query;
    }

    protected String createQuery_findGranularity(String query) {
        return "GRANULARITY_" + query;
    }
    public abstract List<String[]> find_expected_types_of_relation(String majority_relation_name) throws IOException;

    public abstract void finalizeConnection();

    public abstract double find_similarity(String entity_id, String concept_url);

    public abstract void saveSimilarity(String entity_id, String concept_url, double score, boolean biDirectional,
                                        boolean commit);

    public abstract void commitChanges() throws IOException, SolrServerException;
}
