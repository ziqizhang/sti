package uk.ac.shef.dcs.sti.rep;

import java.io.Serializable;

/**
 * An LTableContext could be any textual content around an Table object.
 */
public class LTableContext implements Serializable, Comparable<LTableContext>{
    private String text;
    private double rankScore; //how relevant is this context to the table
    private TableContextType type;


    public LTableContext(String text, TableContextType type, double score){
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

    public double getRankScore() {
        return rankScore;
    }

    public void setRankScore(double rankScore) {
        this.rankScore = rankScore;
    }

    @Override
    public int compareTo(LTableContext o) {
        return new Double(getRankScore()).compareTo(o.getRankScore());
    }

    public TableContextType getType() {
        return type;
    }

    public void setType(TableContextType type) {
        this.type = type;
    }

    public static enum TableContextType implements Serializable{

        CAPTION("Caption"),
        PAGETITLE("PageTitle"),//title of the page containing the table
        BEFORE("Before"),//context occuring before table
        AFTER("After");  //context occurring after table


        private String type;

        private TableContextType(String type) {
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
