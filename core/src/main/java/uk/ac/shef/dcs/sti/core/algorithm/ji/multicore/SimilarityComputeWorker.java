package uk.ac.shef.dcs.sti.core.algorithm.ji.multicore;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.core.algorithm.ji.EntityAndConceptScorer_Freebase;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by zqz on 18/05/2015.
 */
public class SimilarityComputeWorker implements Callable<Map<String[], Double>> {

    private List<Pair<Entity, Clazz>> pairs;
    private EntityAndConceptScorer_Freebase simScorer;
    private KBSearch kbSearch;
    private boolean finished=false;
    private String id;
    private boolean useCache;

    public SimilarityComputeWorker(String id, boolean useCache,
                                    List<Pair<Entity, Clazz>> pairs, EntityAndConceptScorer_Freebase simScorer,
                                    KBSearch kbSearch){
        this.pairs=pairs;
        this.simScorer=simScorer;
        this.kbSearch = kbSearch;
        this.id=id;
        this.useCache=useCache;
    }
    @Override
    public Map<String[], Double> call() throws Exception {
        Map<String[], Double> scores = new HashMap<String[], Double>();
        for(Pair<Entity, Clazz> pair: pairs){
            Pair<Double, String> score=null;
            try {
                score = simScorer.computeEntityConceptSimilarity(pair.getKey(), pair.getValue(), kbSearch, useCache);
            } catch (KBSearchException e) {
                e.printStackTrace();
            }
            if(score!=null) {
                scores.put(new String[]{pair.getKey().getId(), pair.getValue().getId(),
                        score.getValue()}, score.getKey());
            }
        }
        return scores;
    }
}
