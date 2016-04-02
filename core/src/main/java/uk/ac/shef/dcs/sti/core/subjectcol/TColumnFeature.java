package uk.ac.shef.dcs.sti.core.subjectcol;

import java.io.Serializable;

/**
 */
public class TColumnFeature implements Serializable {

    private static final long serialVersionUID = -1208225814300474918L;

    private int colId;
    private int numRows;

    private TColumnDataType mostFrequentDataType;
    private boolean isFirstNEColumn;
    private boolean isOnlyNEColumn;
    private boolean isOnlyNonEmptyNEColumn;
    private boolean isOnlyNonDuplicateNEColumn;
    private double cellValueDiversity;
    private double tokenValueDiversity;
    private double contextMatchScore;
    private double webSearchScore;
    private int emptyCells;
    private boolean isIvalidPOS;
    private boolean isCode_or_Acronym;

    public TColumnFeature(int colId, int numRows){
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

    public TColumnDataType getMostFrequentDataType() {
        return mostFrequentDataType;
    }

    public void setMostFrequentDataType(TColumnDataType mostFrequentDataType) {
        this.mostFrequentDataType = mostFrequentDataType;
    }

    public boolean isFirstNEColumn() {
        return isFirstNEColumn;
    }

    public void setFirstNEColumn(boolean firstNEColumn) {
        isFirstNEColumn = firstNEColumn;
    }

    public double getUniqueCellCount() {
        return cellValueDiversity;
    }

    public void setUniqueCellCount(double valueDiversity) {
        this.cellValueDiversity = valueDiversity;
    }

    public double getCMScore() {
        return contextMatchScore;
    }

    public void setContextMatchScore(double contextMatchScore) {
        this.contextMatchScore = contextMatchScore;
    }

    public double getWSScore() {
        return webSearchScore;
    }

    public void setWebSearchScore(double webSearchScore) {
        this.webSearchScore = webSearchScore;
    }

    public boolean isOnlyNEColumn() {
        return isOnlyNEColumn;
    }

    public void setOnlyNEColumn(boolean onlyNEColumn) {
        isOnlyNEColumn = onlyNEColumn;
    }

    public int getEmptyCellCount() {
        return emptyCells;
    }

    public void setEmptyCellCount(int emptyCells) {
        this.emptyCells = emptyCells;
    }

    public boolean isInvalidPOS() {
        return isIvalidPOS;
    }

    public void setInvalidPOS(boolean ivalidPOS) {
        isIvalidPOS = ivalidPOS;
    }

    public boolean isAcronymColumn() {
        return isCode_or_Acronym;
    }

    public void setAcronymColumn(boolean code_or_Acronym) {
        isCode_or_Acronym = code_or_Acronym;
    }

    public String toString(){
        return String.valueOf(colId);
    }

    public double getUniqueTokenCount() {
        return tokenValueDiversity;
    }

    public void setUniqueTokenCount(double tokenValueDiversity) {
        this.tokenValueDiversity = tokenValueDiversity;
    }

    public boolean isOnlyNonEmptyNEColumn() {
        return isOnlyNonEmptyNEColumn;
    }

    public void setIsOnlyNonEmptyNEColumn(boolean isOnlyNonEmptyNEColumn) {
        this.isOnlyNonEmptyNEColumn = isOnlyNonEmptyNEColumn;
    }

    public boolean isOnlyNonDuplicateNEColumn() {
        return isOnlyNonDuplicateNEColumn;
    }

    public void setIsOnlyNonDuplicateNEColumn(boolean isOnlyNonDuplicateNEColumn) {
        this.isOnlyNonDuplicateNEColumn = isOnlyNonDuplicateNEColumn;
    }
}
