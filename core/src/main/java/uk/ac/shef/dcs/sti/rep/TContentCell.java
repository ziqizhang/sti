package uk.ac.shef.dcs.sti.rep;

import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;

import java.io.Serializable;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 01/10/12
 * Time: 15:46
 */
public class TContentCell implements Serializable {

    private static final long serialVersionUID = -8136725814000405913L;

    private String text;    //the raw text found in the table cell
    private String other_text;
    private String xPath; //xpath that extracts this value
    private DataTypeClassifier.DataType type;

    public TContentCell(String text){
        this.text=text;
        this.type=DataTypeClassifier.DataType.UNKNOWN;
        other_text="";
    }


    public String toString(){
        return "("+getText()+") "+ getType();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getxPath() {
        return xPath;
    }

    public void setxPath(String xPath) {
        this.xPath = xPath;
    }

    public DataTypeClassifier.DataType getType() {
        return type;
    }

    public void setType(DataTypeClassifier.DataType type) {
        this.type = type;
    }

    public String getOther_text() {
        return other_text;
    }

    public void setOther_text(String other_text) {
        this.other_text = other_text;
    }
}
