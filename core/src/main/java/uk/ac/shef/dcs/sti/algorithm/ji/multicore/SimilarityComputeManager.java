package uk.ac.shef.dcs.sti.algorithm.ji.multicore;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.sti.algorithm.ji.EntityAndConceptScorer_Freebase;
import uk.ac.shef.dcs.kbsearch.rep.Clazz;
import uk.ac.shef.dcs.kbsearch.rep.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zqz on 18/05/2015.
 */
public class SimilarityComputeManager {

    public static Map<String, Double> compute(int threadsPerCPU, List<Pair<Entity, Clazz>> pairs, boolean useCache,
                               EntityAndConceptScorer_Freebase entityAndConceptScorer,
                               KBSearch kbSearch) throws InterruptedException {
        if(pairs.size()==0)
            return new HashMap<>();

        int cpus = Runtime.getRuntime().availableProcessors();
        int totalThreads = cpus* threadsPerCPU;

        int size = pairs.size() / totalThreads;
        if (size <=1) {
            totalThreads=pairs.size();
            size = pairs.size()/totalThreads;
        }else {
            size = pairs.size()/totalThreads;
            int actualThreads = pairs.size()/size;
            if(pairs.size()%size>0)
                actualThreads++;
            totalThreads=actualThreads;
        }
        ExecutorService pool = Executors.newFixedThreadPool(totalThreads);
        System.out.print(totalThreads + " threads, each processing " + size + " pairs...");

        List<SimilarityComputeWorker> tasks = new ArrayList<SimilarityComputeWorker>();
        for (int t = 0; t < totalThreads; t++) {
            int start = t * size;
            int end = start + size;
            List<Pair<Entity, Clazz>> selectedPairs = new ArrayList<>();
            for (int j = start; j < end && j < pairs.size(); j++) {
                selectedPairs.add(pairs.get(j));
            }
            SimilarityComputeWorker worker = new SimilarityComputeWorker(
                    start + "-" + end, useCache, selectedPairs, entityAndConceptScorer, kbSearch
            );
            tasks.add(worker);
        }


        List<Future<Map<String[], Double>>> results = pool.invokeAll(tasks);

        Map<String, Double> finalScores = new HashMap<String, Double>();
        boolean doCommit=false;
        for (Future<Map<String[], Double>> result : results) {
            if (result.isCancelled()) {
                System.err.println(result.toString()+", a worker failed because it is cancelled.");
            } else {
                try {
                    Map<String[], Double> oneResult = result.get();
                    for (Map.Entry<String[], Double> e : oneResult.entrySet()) {
                        String[] key = e.getKey();
                        if (e.getValue() != -1) {
                            if(useCache&& !key[2].equals("cache")) {
                                kbSearch.saveSimilarity(key[0], key[1], e.getValue(), true, false);
                                doCommit=true;
                            }
                            finalScores.put(key[0] + "," + key[1], e.getValue());
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
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
        return finalScores;
    }
}
