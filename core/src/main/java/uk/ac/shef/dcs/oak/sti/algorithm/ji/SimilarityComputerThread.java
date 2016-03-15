package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import javafx.util.Pair;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.triplesearch.rep.Clazz;
import uk.ac.shef.dcs.oak.triplesearch.rep.Entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zqz on 14/05/2015.
 */
public class SimilarityComputerThread extends Thread{

    private Map<String[], Double> scores;
    private List<Pair<Entity, Clazz>> pairs;
    private EntityAndConceptScorer_Freebase simScorer;
    private KnowledgeBaseSearcher kbSearcher;
    private boolean finished=false;
    private String id;
    private boolean useCache;

    public SimilarityComputerThread(String id, boolean useCache,
                                    List<Pair<Entity, Clazz>> pairs, EntityAndConceptScorer_Freebase simScorer,
                                    KnowledgeBaseSearcher kbSearcher){
        scores=new HashMap<>();
        this.pairs=pairs;
        this.simScorer=simScorer;
        this.kbSearcher=kbSearcher;
        this.id=id;
        this.useCache=useCache;
    }


    @Override
    public void run() {
        int count=0;
        for(Pair<Entity, Clazz> pair: pairs){
            Pair<Double, String> score=null;
            try {
                score = simScorer.computeEntityConceptSimilarity(pair.getKey(), pair.getValue(),kbSearcher, useCache);
                count++;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(score!=null) {
                scores.put(new String[]{pair.getKey().getId(), pair.getValue().getId(), score.getValue()}, score.getKey());
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
