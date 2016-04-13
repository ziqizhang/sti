package uk.ac.shef.dcs.sti.core.algorithm.ji;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.algorithm.ji.multicore.SimilarityComputerThread;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 *
 */
public class CandidateConceptGenerator {

    private static final Logger LOG = Logger.getLogger(CandidateConceptGenerator.class.getName());
    private int threads = 10;
    private boolean useCache=false;
    private KBSearch kbSearch;
    private JIClazzScorer clazzScorer;
    private EntityAndConceptScorer_Freebase entityAndConceptScorer;

    public CandidateConceptGenerator(KBSearch kbSearch,
                                     JIClazzScorer clazzScorer,
                                     EntityAndConceptScorer_Freebase entityAndConceptScorer,
                                     int threads,
                                     boolean useCache) {
        this.kbSearch = kbSearch;
        this.useCache=useCache;
        this.clazzScorer = clazzScorer;
        this.entityAndConceptScorer = entityAndConceptScorer;
        this.threads = threads;
    }

    public void generateInitialColumnAnnotations(TAnnotationJI tableAnnotation, Table table, int col) throws KBSearchException {
        List<Clazz> distinctTypes = new ArrayList<>();
        Map<String, List<String>> entityId_and_conceptURLs = new HashMap<>();
        Map<String, String> distinctTypeStrings = new HashMap<>();
        List<Entity> distinctEntities = new ArrayList<>();

        for (int r = 0; r < table.getNumRows(); r++) {
            TCellAnnotation[] cellAnnotations = tableAnnotation.getContentCellAnnotations(r, col);
            if (cellAnnotations.length > 0) {
                for (TCellAnnotation ca : cellAnnotations) {
                    Entity e = ca.getAnnotation();
                    if (ca.getScoreElements().get(
                            JIAdaptedEntityScorer.SCORE_FINAL) ==0.0){
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
                            conceptURLs = new ArrayList<>();
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
            if(!distinctTypes.contains(concept))
                distinctTypes.add(concept);
        }

        //go thru every distinct type, create header annotation candidate
        TColumnHeaderAnnotation[] headerAnnotations = new TColumnHeaderAnnotation[distinctTypes.size()];
        int count = 0;
        for (Map.Entry<String, String> concept : distinctTypeStrings.entrySet()) {
            TColumnHeaderAnnotation ha = new TColumnHeaderAnnotation(table.getColumnHeader(col).getHeaderText(),
                    new Clazz(concept.getKey(), concept.getValue()), 0.0);
            Map<String, Double> score_elements = clazzScorer.score(ha, table.getColumnHeader(col));
            clazzScorer.compute_final_score(score_elements);
            ha.setFinalScore(score_elements.get(JIClazzScorer.SCORE_HEADER_FACTOR));
            ha.setScoreElements(score_elements);
            headerAnnotations[count] = ha;
            count++;
        }
        Arrays.sort(headerAnnotations);
        tableAnnotation.setHeaderAnnotation(col, headerAnnotations);

        //go thru every entity-concept pair, compute their scores
        LOG.info("\t\t>> entity-clazz similarity (Ent:" + distinctEntities.size() + "Clz:" + distinctTypes.size() + ")");
        Map<String, Double> simScores =
                computeSemanticSimilarity(threads, distinctEntities, distinctTypes, true);
        for (Entity entity : distinctEntities) {
            for (Clazz concept : distinctTypes) {
                Double sim = simScores.get(entity.getId() + "," + concept.getId());
                assert sim!=null;
                tableAnnotation.setScoreEntityAndConceptSimilarity(entity.getId(), concept.getId(), sim);
            }
        }

        //then update scores for every entity-concept pair where the entity votes for the concept
        LOG.info("\t\t>> entity-clazz similarity (Ent:" + distinctEntities.size() + ")");
        int cc = 0;
        for (Map.Entry<String, List<String>> entry : entityId_and_conceptURLs.entrySet()) {
            String entityId = entry.getKey();
            List<String> conceptIds = entry.getValue();
            System.out.print(cc + "=" + conceptIds.size() + ",");
            for (String conceptId : conceptIds) {
                double specificity = entityAndConceptScorer.computeConceptSpecificity(conceptId, kbSearch);
                double simScore = tableAnnotation.getScoreEntityAndConceptSimilarity(entityId, conceptId);
                tableAnnotation.setScoreEntityAndConceptSimilarity(
                        entityId, conceptId, simScore + 1.0 + specificity);
            }
            cc++;
        }
        System.out.println(")");
    }

    private Map<String, Double> computeSemanticSimilarity(int threads, Collection<Entity> entities,
                                                          Collection<Clazz> concepts,
                                                          boolean biDirectional)throws KBSearchException{
        Map<String, Double> result = new HashMap<>();
        List<Pair<Entity,Clazz>> pairs = new ArrayList<>();
        for (Entity e : entities) {
            for (Clazz c : concepts) {
                pairs.add(new Pair<>(e, c));
            }
        }

        Collections.shuffle(pairs);

        List<SimilarityComputerThread> workers = new ArrayList<>();
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
