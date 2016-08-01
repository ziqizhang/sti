package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;

/**
 * An TContext could be any textual content around an Table object.
 */
public class TContext implements Serializable, Comparable<TContext>{
    private static final long serialVersionUID = -8136777654860405913L;

    private String text;
    private double rankScore; //how relevant is this context to the table
    private TableContextType type;


    public TContext(String text, TableContextType type, double score){
        this.text=text;
        this.rankScore=score;
        this.type=type;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getImportanceScore() {
        return rankScore;
    }

    public void setRankScore(double rankScore) {
        this.rankScore = rankScore;
    }

    @Override
    public int compareTo(TContext o) {
        return new Double(getImportanceScore()).compareTo(o.getImportanceScore());
    }

    public TableContextType getType() {
        return type;
    }

    public void setType(TableContextType type) {
        this.type = type;
    }

    public enum TableContextType implements Serializable{

        CAPTION("Caption"),
        PAGETITLE("PageTitle"),//title of the page containing the table
        PARAGRAPH_BEFORE("Before"),//context occuring before table
        PARAGRAPH_AFTER("After");  //context occurring after table


        private String type;

        TableContextType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
