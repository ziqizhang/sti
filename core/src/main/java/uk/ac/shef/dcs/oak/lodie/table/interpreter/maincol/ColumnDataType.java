package uk.ac.shef.dcs.oak.lodie.table.interpreter.maincol;

import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.DataTypeClassifier;

import java.io.Serializable;

/**
 */
public class ColumnDataType implements Serializable, Comparable<ColumnDataType>{

    private DataTypeClassifier.DataType candidateType; //what is the type of this column
    private int countRows;          //how many rows contain this type of data in this column

    public ColumnDataType(DataTypeClassifier.DataType candidateType, int countRows){
        this.candidateType=candidateType;
        this.countRows=countRows;
    }

    @Override
    public int compareTo(ColumnDataType o) {
        if(o.getCandidateType().equals(DataTypeClassifier.DataType.EMPTY)&&!getCandidateType().equals(DataTypeClassifier.DataType.EMPTY))
            return -1;
        else if(getCandidateType().equals(DataTypeClassifier.DataType.EMPTY)&&!o.getCandidateType().equals(DataTypeClassifier.DataType.EMPTY))
            return 1;

        return new Integer(o.getCountRows()).compareTo(getCountRows());
    }

    public int getCountRows() {
        return countRows;
    }

    public void setCountRows(int countRows) {
        this.countRows = countRows;
    }

    public DataTypeClassifier.DataType getCandidateType() {
        return candidateType;
    }

    public void setCandidateType(DataTypeClassifier.DataType candidateType) {
        this.candidateType = candidateType;
    }

    public boolean equals(Object o){
        if(o instanceof ColumnDataType){
            return ((ColumnDataType) o).getCandidateType().equals(getCandidateType());
        }
        return false;
    }

    public String toString(){
        return candidateType+","+getCountRows();
    }
}
