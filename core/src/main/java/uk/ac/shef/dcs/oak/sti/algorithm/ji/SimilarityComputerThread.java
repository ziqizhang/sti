package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zqz on 14/05/2015.
 */
public class SimilarityComputerThread extends Thread{

    private Map<String[], Double> scores;
    private List<EntityCandidate[]> pairs;
    private EntityAndConceptScorer_Freebase simScorer;
    private KnowledgeBaseSearcher kbSearcher;
    private boolean finished=false;
    private String id;
    private boolean useCache;

    public SimilarityComputerThread(String id, boolean useCache,
                                    List<EntityCandidate[]> pairs, EntityAndConceptScorer_Freebase simScorer,
                                    KnowledgeBaseSearcher kbSearcher){
        scores=new HashMap<String[], Double>();
        this.pairs=pairs;
        this.simScorer=simScorer;
        this.kbSearcher=kbSearcher;
        this.id=id;
        this.useCache=useCache;
    }


    @Override
    public void run() {
        int count=0;
        for(EntityCandidate[] pair: pairs){
            ObjObj<Double, String> score=null;
            try {
                score = simScorer.computeEntityConceptSimilarity(pair[0], pair[1],kbSearcher, useCache);
                count++;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(score!=null) {
                int scoreSizeBefore=scores.size();
                scores.put(new String[]{pair[0].getId(), pair[1].getId(), score.getOtherObject()}, score.getMainObject());
                int scoreSizeAfter = scores.size();
                if(scoreSizeBefore==scoreSizeAfter)
                    System.out.println("fuck");

            }
        }
        finished=true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public Map<String[], Double> getScores(){
        return scores;
    }
}
