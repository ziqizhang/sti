package uk.ac.shef.dcs.sti.core.algorithm.ji.similarity;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;

import java.util.*;

/**
 * Created by - on 13/04/2016.
 */
public class SimilarityComputerManager {


    private static final Logger LOG = Logger.getLogger(SimilarityComputerManager.class.getName());
    private KBSearch kbSearch;
    private boolean useCache;
    private EntityAndClazzSimilarityScorer scorer;
    public SimilarityComputerManager(boolean useCache, KBSearch kbSearch,
                                     EntityAndClazzSimilarityScorer scorer){

        this.useCache=useCache;
        this.kbSearch=kbSearch;
        this.scorer=scorer;
    }
    public Map<String, Double> computeSemanticSimilarity(int threads, Collection<Entity> entities,
                                                          Collection<Clazz> concepts,
                                                          boolean biDirectional) throws KBSearchException {
        Map<String, Double> result = new HashMap<>();
        List<Pair<Entity, Clazz>> pairs = new ArrayList<>();
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
        } else {
            size = pairs.size() / threads;
            int actualThreads = pairs.size() / size;
            if (pairs.size() % size > 0)
                actualThreads++;
            threads = actualThreads;
        }
        LOG.info("\t\t\t>>" + threads + " threads, each processing " + size + " pairs...");
        for (int t = 0; t < threads; t++) {
            int start = t * size;
            int end = start + size;
            List<Pair<Entity, Clazz>> selectedPairs = new ArrayList<>();
            for (int j = start; j < end && j < pairs.size(); j++) {
                selectedPairs.add(pairs.get(j));
            }
            SimilarityComputerThread thread = new SimilarityComputerThread(
                    useCache, selectedPairs, scorer, kbSearch
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
        LOG.info("\t\t\t>> saving similarity scores...");
        boolean doCommit = false;
        for (SimilarityComputerThread worker : workers) {
            for (Map.Entry<String[], Double> e : worker.getScores().entrySet()) {
                String[] key = e.getKey();
                if (e.getValue() != -1) {
                    if (useCache && !key[2].equals("cache")) {
                        kbSearch.cacheEntityClazzSimilarity(key[0], key[1], e.getValue(), biDirectional, false);
                        doCommit = true;
                    }
                    result.put(key[0] + "," + key[1], e.getValue());
                }
            }
        }
        if (useCache && doCommit) {
            try {
                kbSearch.commitChanges();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
