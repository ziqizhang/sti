package uk.ac.shef.dcs.sti.core.algorithm.ji;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 *
 */
public class CandidateConceptGenerator {
    private int multiThreads = 10;
    private boolean useCache=false;
    private KBSearch kbSearch;
    private ClassificationScorer_JI_adapted conceptScorer;
    private EntityAndConceptScorer_Freebase entityAndConceptScorer;

    public CandidateConceptGenerator(KBSearch kbSearch,
                                     ClassificationScorer_JI_adapted conceptScorer,
                                     EntityAndConceptScorer_Freebase entityAndConceptScorer,
                                     int multiThreads,
                                     boolean useCache) {
        this.kbSearch = kbSearch;
        this.useCache=useCache;
        this.conceptScorer = conceptScorer;
        this.entityAndConceptScorer = entityAndConceptScorer;
        this.multiThreads = multiThreads;
    }

    public void generateCandidateConcepts(TAnnotationJIFreebase tableAnnotation, Table table, int col) throws KBSearchException {
        List<Clazz> distinctTypes = new ArrayList<>();
        Map<String, List<String>> entityId_and_conceptURLs = new HashMap<String, List<String>>();
        Map<String, String> distinctTypeStrings = new HashMap<String, String>();
        Set<String> distinctEntityIds = new HashSet<String>();
        List<Entity> distinctEntities = new ArrayList<>();
        for (int r = 0; r < table.getNumRows(); r++) {
            TCellAnnotation[] cellAnnotations = tableAnnotation.getContentCellAnnotations(r, col);
            if (cellAnnotations.length > 0) {
                for (TCellAnnotation ca : cellAnnotations) {
                    Entity e = ca.getAnnotation();
                    if (ca.getScoreElements().get(
                            JIAdaptedEntityScorer.SCORE_CELL_FACTOR) ==0.0){
                        continue;
                    }
                    if(!distinctEntities.contains(e))
                        distinctEntities.add(e);
                    for (Clazz type : e.getTypes()) {
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
            List<Attribute> triples = kbSearch.findAttributesOfClazz(conceptId);
            Clazz concept = new Clazz(conceptId, conceptName);
            concept.setAttributes(triples);
            distinctTypes.add(concept);
        }

        //go thru every distinct type, create header annotation candidate
        TColumnHeaderAnnotation[] headerAnnotations = new TColumnHeaderAnnotation[distinctTypes.size()];
        int count = 0;
        for (Map.Entry<String, String> concept : distinctTypeStrings.entrySet()) {
            TColumnHeaderAnnotation ha = new TColumnHeaderAnnotation(table.getColumnHeader(col).getHeaderText(),
                    new Clazz(concept.getKey(), concept.getValue()), 0.0);
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
                Double sim = //entityAndConceptScorer.computeEntityConceptSimilarity(entityId, conceptId, kbSearch);
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
                double specificity = entityAndConceptScorer.computeConceptSpecificity(conceptId, kbSearch);
                double simScore = tableAnnotation.getScore_entityAndConcept(entityId, conceptId);
                tableAnnotation.setScore_entityAndConcept(entityId, conceptId, simScore + 1.0 + specificity);
            }
            cc++;
        }
        System.out.println(")");
    }

    private Map<String, Double> computeSimilarityMultiThread(int threads, Collection<Entity> entities,
                                                             Collection<Clazz> concepts,
                                                             boolean biDirectional)throws KBSearchException{
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
                    kbSearch);
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
                    start + "-" + end, useCache, selectedPairs, entityAndConceptScorer, kbSearch
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
                        kbSearch.cacheEntityConceptSimilarity(key[0], key[1], e.getValue(), biDirectional, false);
                        doCommit=true;
                    }
                    result.put(key[0] + "," + key[1], e.getValue());
                }
            }
        }
        if(useCache&&doCommit) {
            try {
                kbSearch.commitChanges();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
