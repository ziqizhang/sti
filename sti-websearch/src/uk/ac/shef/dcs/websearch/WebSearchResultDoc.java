package uk.ac.shef.dcs.websearch;

import java.io.Serializable;

/**
 */
public class WebSearchResultDoc implements Serializable {

    private static final long serialVersionUID = -1208625714080495013L;

    private String id;
    private String title;
    private String description;
    private String url;
    private double score;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
