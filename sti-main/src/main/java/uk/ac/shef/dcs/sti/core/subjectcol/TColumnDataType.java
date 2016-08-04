package uk.ac.shef.dcs.sti.core.subjectcol;

import uk.ac.shef.dcs.sti.util.DataTypeClassifier;

import java.io.Serializable;

/**
 */
public class TColumnDataType implements Serializable, Comparable<TColumnDataType>{

    private static final long serialVersionUID = -1638925814006765913L;

    private DataTypeClassifier.DataType type; //what is the type of this column
    private int supportingRows;          //how many rows contain this type of data in this column

    public TColumnDataType(DataTypeClassifier.DataType type, int supportingRows){
        this.type = type;
        this.supportingRows = supportingRows;
    }

    @Override
    public int compareTo(TColumnDataType o) {
        if(o.getType().equals(DataTypeClassifier.DataType.EMPTY)&&!getType().equals(DataTypeClassifier.DataType.EMPTY))
            return -1;
        else if(getType().equals(DataTypeClassifier.DataType.EMPTY)&&!o.getType().equals(DataTypeClassifier.DataType.EMPTY))
            return 1;

        return new Integer(o.getSupportingRows()).compareTo(getSupportingRows());
    }

    public int getSupportingRows() {
        return supportingRows;
    }

    public void setSupportingRows(int supportingRows) {
        this.supportingRows = supportingRows;
    }

    public DataTypeClassifier.DataType getType() {
        return type;
    }

    public void setType(DataTypeClassifier.DataType type) {
        this.type = type;
    }

    public boolean equals(Object o){
        if(o instanceof TColumnDataType){
            return ((TColumnDataType) o).getType().equals(getType());
        }
        return false;
    }

    public String toString(){
        return type +","+ getSupportingRows();
    }
}
