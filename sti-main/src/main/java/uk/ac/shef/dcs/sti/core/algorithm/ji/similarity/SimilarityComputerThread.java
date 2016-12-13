package uk.ac.shef.dcs.sti.core.algorithm.ji.similarity;

import javafx.util.Pair;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zqz on 14/05/2015.
 */
public class SimilarityComputerThread extends Thread{

    private Map<String[], Double> scores;
    private List<Pair<Entity, Clazz>> pairs;
    private EntityAndClazzSimilarityScorer simScorer;
    private KBProxy kbSearch;
    private boolean finished=false;
    private boolean useCache;
    private static final Logger LOG = LoggerFactory.getLogger(SimilarityComputerThread.class.getName());

    public SimilarityComputerThread( boolean useCache,
                                    List<Pair<Entity, Clazz>> pairs, EntityAndClazzSimilarityScorer simScorer,
                                    KBProxy kbSearch){
        scores=new HashMap<>();
        this.pairs=pairs;
        this.simScorer=simScorer;
        this.kbSearch = kbSearch;
        this.useCache=useCache;
    }


    @Override
    public void run() {
        for(Pair<Entity, Clazz> pair: pairs){
            Pair<Double, Boolean> score=null;
            try {
                score = simScorer.computeEntityConceptSimilarity(pair.getKey(), pair.getValue(), kbSearch, useCache);
            } catch (Exception e) {
                LOG.error("Failed to compute similarity for pair:"+pair);
                LOG.error(ExceptionUtils.getFullStackTrace(e));
            }
            if(score!=null) {
                scores.put(new String[]{pair.getKey().getId(), pair.getValue().getId(), score.getValue().toString()}, score.getKey());
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
