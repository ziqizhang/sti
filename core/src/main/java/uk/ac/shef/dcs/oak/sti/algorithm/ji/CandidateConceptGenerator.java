package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import javafx.util.Pair;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseFreebaseFilter;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;
import uk.ac.shef.dcs.kbsearch.rep.Entity;

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
        List<Clazz> distinctTypes = new ArrayList<>();
        Map<String, List<String>> entityId_and_conceptURLs = new HashMap<String, List<String>>();
        Map<String, String> distinctTypeStrings = new HashMap<String, String>();
        Set<String> distinctEntityIds = new HashSet<String>();
        List<Entity> distinctEntities = new ArrayList<>();
        for (int r = 0; r < table.getNumRows(); r++) {
            CellAnnotation[] cellAnnotations = tableAnnotation.getContentCellAnnotations(r, col);
            if (cellAnnotations.length > 0) {
                for (CellAnnotation ca : cellAnnotations) {
                    Entity e = ca.getAnnotation();
                    if (ca.getScore_element_map().get(
                            DisambiguationScorer_JI_adapted.SCORE_CELL_FACTOR) ==0.0){
                        continue;
                    }
                    if(!distinctEntities.contains(e))
                        distinctEntities.add(e);
                    for (Clazz type : KnowledgeBaseFreebaseFilter.filterTypes(e.getTypes())) {
                        String url = type.getId();
                        String label = type.getLabel();
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
            List<String[]> triples =kbSearcher.findTriplesOfConcept(conceptId);
            Clazz concept = new Clazz(conceptId, conceptName);
            concept.setTriples(triples);
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
        for (Entity entity : distinctEntities) {
            for (Clazz concept : distinctTypes) {
                //cc++;
                Double sim = //entityAndConceptScorer.computeEntityConceptSimilarity(entityId, conceptId, kbSearcher);
                        simScores.get(entity.getId() + "," + concept.getId());
                if(sim==null)
                    System.out.println("fuck");
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

    private Map<String, Double> computeSimilarityMultiThread(int threads, Collection<Entity> entities,
                                                             Collection<Clazz> concepts,
                                                             boolean biDirectional) {
        Map<String, Double> result = new HashMap<String, Double>();
        List<Pair<Entity,Clazz>> pairs = new ArrayList<>();
        int cc=0;
        for (Entity e : entities) {
            for (Clazz c : concepts) {
                pairs.add(new Pair<>(e, c));
                /*if(e.equals("/m/045clt")&&c.equals("/organization/organization"))
                    System.out.println(c);*/
                cc++;
            }
        }

        Collections.shuffle(pairs);

        /*try {
            result = SimilarityComputeManager.compute(multiThreads, pairs, useCache, entityAndConceptScorer,
                    kbSearcher);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;*/

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
            List<Pair<Entity, Clazz>> selectedPairs = new ArrayList<>();
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
