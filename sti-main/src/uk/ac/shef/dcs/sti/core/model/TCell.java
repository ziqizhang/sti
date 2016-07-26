package uk.ac.shef.dcs.sti.core.model;

import uk.ac.shef.dcs.sti.util.DataTypeClassifier;

import java.io.Serializable;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 01/10/12
 * Time: 15:46
 */
public class TCell implements Serializable {

    private static final long serialVersionUID = -8136725814000405913L;

    private String text;    //the raw text found in the table cell
    private String otherText;
    private String xPath; //xpath that extracts this value
    private DataTypeClassifier.DataType type;

    public TCell(String text){
        this.text=text;
        this.type=DataTypeClassifier.DataType.UNKNOWN;
        otherText ="";
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

    public String getOtherText() {
        return otherText;
    }

    public void setOtherText(String otherText) {
        this.otherText = otherText;
    }
}
