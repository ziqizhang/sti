package uk.ac.shef.dcs.oak.triplesearch.freebase;

import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 23/01/14
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class EntityCandidate_FreebaseTopic extends EntityCandidate{
    private String mid;
    private String language;
    private double score;

    public EntityCandidate_FreebaseTopic(String mid){
        super();
        this.mid=mid;
        this.id=mid;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

}
