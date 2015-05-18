package uk.ac.shef.dcs.oak.sti.algorithm.ji.multicore;

import uk.ac.shef.dcs.oak.sti.algorithm.ji.EntityAndConceptScorer_Freebase;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by zqz on 18/05/2015.
 */
public class SimilarityComputeWorker implements Callable<Map<String[], Double>> {

    private List<EntityCandidate[]> pairs;
    private EntityAndConceptScorer_Freebase simScorer;
    private KnowledgeBaseSearcher kbSearcher;
    private boolean finished=false;
    private String id;
    private boolean useCache;

    public SimilarityComputeWorker(String id, boolean useCache,
                                    List<EntityCandidate[]> pairs, EntityAndConceptScorer_Freebase simScorer,
                                    KnowledgeBaseSearcher kbSearcher){
        this.pairs=pairs;
        this.simScorer=simScorer;
        this.kbSearcher=kbSearcher;
        this.id=id;
        this.useCache=useCache;
    }
    @Override
    public Map<String[], Double> call() throws Exception {
        Map<String[], Double> scores = new HashMap<String[], Double>();
        for(EntityCandidate[] pair: pairs){
            ObjObj<Double, String> score=null;
            try {
                score = simScorer.computeEntityConceptSimilarity(pair[0], pair[1],kbSearcher, useCache);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(score!=null) {
                scores.put(new String[]{pair[0].getId(), pair[1].getId(), score.getOtherObject()}, score.getMainObject());
            }
        }
        return scores;
    }
}
