package uk.ac.shef.dcs.oak.sti.kb;

import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.oak.sti.rep.LTableContentCell;

import java.io.IOException;
import java.util.List;

/**
 */
public abstract class KnowledgeBaseSearcher {

    /**
     * given a content cell, fetch candidate entities from a KB
     * @param tcc
     * @return
     * @throws IOException
     */
    public abstract List<Entity> findEntityCandidates(LTableContentCell tcc) throws IOException;

    /**
     * given a content cell fetch candidate entities that only match certain types from a KB
     * @param tcc
     * @param types
     * @return
     * @throws IOException
     */
    public abstract List<Entity> findEntityCandidatesOfTypes(LTableContentCell tcc, String... types) throws IOException;

    /**
     * get triples that contain the entity candidate
     *
     * It is up to implementing class to decide whether the entity must be subject/object/either of triples
     *
     *
     * @param ec
     * @return a list of triples, each of which is represented as a string array. It is up to implementing class
     * to decide what the array should be
     * @throws IOException
     */
    public List<String[]> findTriplesOfEntityCandidates(Entity ec) throws IOException {
        return findTriplesOfEntityCandidates(ec.getId());
    }

    /**
     *
     */
    protected abstract List<String[]> findTriplesOfEntityCandidates(String entityId) throws IOException;

    /**
     * get triples that contain the concept
     *
     * It is up to implementing class to decide whether the concept must be subject/object/either of triples
     *
     *
     * @return a list of triples, each of which is represented as a string array. It is up to implementing class
     * to decide what the array should be
     * @throws IOException
     */
    public abstract List<String[]> findTriplesOfConcept(String conceptId) throws IOException;

    /**
     * get triples that contain the property
     *
     *
     * @return a list of triples, each of which is represented as a string array. It is up to implementing class
     * to decide what the array should be
     * @throws IOException
     */
    protected abstract List<String[]> findTriplesOfProperty(String propertyId) throws IOException;

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
