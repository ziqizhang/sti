package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseFreebaseFilter;
import uk.ac.shef.dcs.oak.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.HeaderAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;

import java.io.IOException;
import java.util.*;

/**
 *
 */
public class CandidateConceptGenerator {
    private int multiThreads = 10;
    private boolean useCache=false;
    private KnowledgeBaseSearcher kbSearcher;
    private ClassificationScorer_JI_adapted conceptScorer;
    private EntityAndConceptScorer_Freebase entityAndConceptScorer;

    public CandidateConceptGenerator(KnowledgeBaseSearcher kbSearcher,
                                     ClassificationScorer_JI_adapted conceptScorer,
                                     EntityAndConceptScorer_Freebase entityAndConceptScorer,
                                     int multiThreads,
                                     boolean useCache) {
        this.kbSearcher = kbSearcher;
        this.useCache=useCache;
        this.conceptScorer = conceptScorer;
        this.entityAndConceptScorer = entityAndConceptScorer;
        this.multiThreads = multiThreads;
    }

    public void generateCandidateConcepts(LTableAnnotation_JI_Freebase tableAnnotation, LTable table, int col) throws IOException {
        List<EntityCandidate> distinctTypes = new ArrayList<EntityCandidate>();
        Map<String, List<String>> entityId_and_conceptURLs = new HashMap<String, List<String>>();
        Map<String, String> distinctTypeStrings = new HashMap<String, String>();
        Set<String> distinctEntityIds = new HashSet<String>();
        List<EntityCandidate> distinctEntities = new ArrayList<EntityCandidate>();
        for (int r = 0; r < table.getNumRows(); r++) {
            CellAnnotation[] cellAnnotations = tableAnnotation.getContentCellAnnotations(r, col);
            if (cellAnnotations.length > 0) {
                for (CellAnnotation ca : cellAnnotations) {
                    EntityCandidate e = ca.getAnnotation();
                    if (ca.getScore_element_map().get(
                            DisambiguationScorer_JI_adapted.SCORE_CELL_FACTOR) ==0.0){
                        continue;
                    }
                    if(!distinctEntities.contains(e))
                        distinctEntities.add(e);
                    for (String[] type : KnowledgeBaseFreebaseFilter.filterTypes(e.getTypes())) {
                        String url = type[0];
                        String label = type[1];
                        distinctTypeStrings.put(url, label);
                        List<String> conceptURLs = entityId_and_conceptURLs.get(e.getId());
                        if (conceptURLs == null)
                            conceptURLs = new ArrayList<String>();
                        if (!conceptURLs.contains(url))
                            conceptURLs.add(url);
                        entityId_and_conceptURLs.put(e.getId(), conceptURLs);
                    }
                }
            }
        }

        //fetch all concept entities
        for(Map.Entry<String, String> ent: distinctTypeStrings.entrySet()){
            String conceptId = ent.getKey();
            String conceptName = ent.getValue();
            List<String[]> triples =kbSearcher.find_triplesForConcept_filtered(conceptId);
            EntityCandidate concept = new EntityCandidate();
            concept.setId(conceptId);
            concept.setName(conceptName);
            concept.setFacts(triples);
            distinctTypes.add(concept);
        }

        //go thru every distinct type, create header annotation candidate
        HeaderAnnotation[] headerAnnotations = new HeaderAnnotation[distinctTypes.size()];
        int count = 0;
        for (Map.Entry<String, String> concept : distinctTypeStrings.entrySet()) {
            HeaderAnnotation ha = new HeaderAnnotation(table.getColumnHeader(col).getHeaderText(),
                    concept.getKey(), concept.getValue(), 0.0);
            Map<String, Double> score_elements = conceptScorer.score(ha, table.getColumnHeader(col));
            conceptScorer.compute_final_score(score_elements);
            ha.setFinalScore(score_elements.get(ClassificationScorer_JI_adapted.SCORE_HEADER_FACTOR));
            ha.setScoreElements(score_elements);
            headerAnnotations[count] = ha;
            count++;
        }
        Arrays.sort(headerAnnotations);
        tableAnnotation.setHeaderAnnotation(col, headerAnnotations);

        //go thru every entity-concept pair, compute their scores
        System.out.print("-E_and_C_scores-(tot.Ent:" + distinctEntities.size() + "-tot.Cpt:" + distinctTypes.size() + ">");
        int cc = 0;

        Map<String, Double> simScores =
                computeSimilarityMultiThread(multiThreads, distinctEntities, distinctTypes, true);
        for (EntityCandidate entity : distinctEntities) {
            for (EntityCandidate concept : distinctTypes) {
                //cc++;
                Double sim = //entityAndConceptScorer.computeEntityConceptSimilarity(entityId, conceptId, kbSearcher);
                        simScores.get(entity.getId() + "," + concept.getId());
                if (sim == null)
                    System.err.println("fuck");;
                tableAnnotation.setScore_entityAndConcept(entity.getId(), concept.getId(), sim);
                //if(cc%50==0) System.out.print(cc + ",");
            }
        }
        System.out.println(")");
        //then update scores for every entity-concept pair where the entity votes for the concept
        System.out.print("-E_and_C_scores_update-(tot.Ent:" + distinctEntities.size() + ">");
        cc = 0;
        for (Map.Entry<String, List<String>> entry : entityId_and_conceptURLs.entrySet()) {
            String entityId = entry.getKey();
            List<String> conceptIds = entry.getValue();
            System.out.print(cc + "=" + conceptIds.size() + ",");
            for (String conceptId : conceptIds) {
                double specificity = entityAndConceptScorer.computeConceptSpecificity(conceptId, kbSearcher);
                double simScore = tableAnnotation.getScore_entityAndConcept(entityId, conceptId);
                tableAnnotation.setScore_entityAndConcept(entityId, conceptId, simScore + 1.0 + specificity);
            }
            cc++;
        }
        System.out.println(")");
    }

    private Map<String, Double> computeSimilarityMultiThread(int threads, Collection<EntityCandidate> entities,
                                                             Collection<EntityCandidate> concepts,
                                                             boolean biDirectional) {
        Map<String, Double> result = new HashMap<String, Double>();
        List<EntityCandidate[]> pairs = new ArrayList<EntityCandidate[]>();
        int cc=0;
        for (EntityCandidate e : entities) {
            for (EntityCandidate c : concepts) {
                pairs.add(new EntityCandidate[]{e, c});
                /*if(e.equals("/m/045clt")&&c.equals("/organization/organization"))
                    System.out.println(c);*/
                cc++;
            }
        }

        List<SimilarityComputerThread> workers = new ArrayList<SimilarityComputerThread>();
        int size = pairs.size() / threads;
        if (size < 5) {
            threads = 1;
            size = pairs.size();
        }else {
            size = pairs.size()/threads;
            int actualThreads = pairs.size()/size;
            if(pairs.size()%size>0)
                actualThreads++;
            threads=actualThreads;
        }
        System.out.print(threads + " threads, each processing " + size + " pairs...");
        for (int t = 0; t < threads; t++) {
            int start = t * size;
            int end = start + size;
            List<EntityCandidate[]> selectedPairs = new ArrayList<EntityCandidate[]>();
            for (int j = start; j < end && j < pairs.size(); j++) {
                selectedPairs.add(pairs.get(j));
            }
            SimilarityComputerThread thread = new SimilarityComputerThread(
                    start + "-" + end, useCache, selectedPairs, entityAndConceptScorer, kbSearcher
            );
            workers.add(thread);
        }

        //start all workers
        for (SimilarityComputerThread w : workers)
            w.start();

        boolean allFinished = false;
        while (!allFinished) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            int finished = 0;
            for (SimilarityComputerThread w : workers) {
                if (w.isFinished())
                    finished++;
            }
            if (finished == workers.size())
                allFinished = true;
        }

        //collect results and caching
        System.out.print("saving similarity scores...");
        boolean doCommit=false;
        for (SimilarityComputerThread worker : workers) {
            for (Map.Entry<String[], Double> e : worker.getScores().entrySet()) {
                String[] key = e.getKey();
                if (e.getValue() != -1) {
                    if(useCache&& !key[2].equals("cache")) {
                        kbSearcher.saveSimilarity(key[0], key[1], e.getValue(), biDirectional, false);
                        doCommit=true;
                    }
                    result.put(key[0] + "," + key[1], e.getValue());
                }
            }
        }
        if(useCache&&doCommit) {
            try {
                kbSearcher.commitChanges();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
