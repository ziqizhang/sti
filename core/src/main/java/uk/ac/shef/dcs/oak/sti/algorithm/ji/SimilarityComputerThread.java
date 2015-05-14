package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zqz on 14/05/2015.
 */
public class SimilarityComputerThread implements Runnable{

    private Map<String[], Double> scores;
    private List<String[]> pairs;
    private EntityAndConceptScorer_Freebase simScorer;
    private KnowledgeBaseSearcher kbSearcher;
    private boolean finished=false;
    private String id;

    public SimilarityComputerThread(String id, List<String[]> pairs, EntityAndConceptScorer_Freebase simScorer,
                                    KnowledgeBaseSearcher kbSearcher){
        scores=new HashMap<String[], Double>();
        this.pairs=pairs;
        this.simScorer=simScorer;
        this.kbSearcher=kbSearcher;
        this.id=id;
    }


    @Override
    public void run() {
        for(String[] pair: pairs){
            double score=-1.0;
            try {
                score = simScorer.computeEntityConceptSimilarity(pair[0], pair[1],kbSearcher);
            } catch (IOException e) {
                e.printStackTrace();
            }
            scores.put(pair,score);
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
