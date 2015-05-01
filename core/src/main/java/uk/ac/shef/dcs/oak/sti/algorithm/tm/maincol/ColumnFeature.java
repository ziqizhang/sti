package uk.ac.shef.dcs.oak.sti.algorithm.tm.maincol;

import java.io.Serializable;

/**
 */
public class ColumnFeature implements Serializable {

    private int colId;
    private int numRows;

    private ColumnDataType mostDataType;
    private boolean isFirstNEColumn;
    private boolean isTheOnlyNEColumn;
    private double cellValueDiversity;
    private double tokenValueDiversity;
    private double contextMatchScore;
    private double webSearchScore;
    private int emptyCells;
    private boolean isIvalidPOS;
    private boolean isCode_or_Acronym;

    public ColumnFeature(int colId, int numRows){
        this.colId=colId;
        this.numRows=numRows;
    }

    public int getColId() {
        return colId;
    }

    public void setColId(int colId) {
        this.colId = colId;
    }

    public int getNumRows() {
        return numRows;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public ColumnDataType getMostDataType() {
        return mostDataType;
    }

    public void setMostDataType(ColumnDataType mostDataType) {
        this.mostDataType = mostDataType;
    }

    public boolean isFirstNEColumn() {
        return isFirstNEColumn;
    }

    public void setFirstNEColumn(boolean firstNEColumn) {
        isFirstNEColumn = firstNEColumn;
    }

    public double getCellValueDiversity() {
        return cellValueDiversity;
    }

    public void setCellValueDiversity(double valueDiversity) {
        this.cellValueDiversity = valueDiversity;
    }

    public double getContextMatchScore() {
        return contextMatchScore;
    }

    public void setContextMatchScore(double contextMatchScore) {
        this.contextMatchScore = contextMatchScore;
    }

    public double getWebSearchScore() {
        return webSearchScore;
    }

    public void setWebSearchScore(double webSearchScore) {
        this.webSearchScore = webSearchScore;
    }

    public boolean isTheOnlyNEColumn() {
        return isTheOnlyNEColumn;
    }

    public void setTheOnlyNEColumn(boolean theOnlyNEColumn) {
        isTheOnlyNEColumn = theOnlyNEColumn;
    }

    public int getEmptyCells() {
        return emptyCells;
    }

    public void setEmptyCells(int emptyCells) {
        this.emptyCells = emptyCells;
    }

    public boolean isInvalidPOS() {
        return isIvalidPOS;
    }

    public void setInvalidPOS(boolean ivalidPOS) {
        isIvalidPOS = ivalidPOS;
    }

    public boolean isCode_or_Acronym() {
        return isCode_or_Acronym;
    }

    public void setCode_or_Acronym(boolean code_or_Acronym) {
        isCode_or_Acronym = code_or_Acronym;
    }

    public String toString(){
        return String.valueOf(colId);
    }

    public double getTokenValueDiversity() {
        return tokenValueDiversity;
    }

    public void setTokenValueDiversity(double tokenValueDiversity) {
        this.tokenValueDiversity = tokenValueDiversity;
    }
}
