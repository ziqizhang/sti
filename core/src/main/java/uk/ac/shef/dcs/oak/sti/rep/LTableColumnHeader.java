package uk.ac.shef.dcs.oak.sti.rep;

import uk.ac.shef.dcs.oak.sti.algorithm.tm.maincol.ColumnDataType;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.maincol.ColumnFeature;

import java.io.Serializable;
import java.util.List;

/**
 */
public class LTableColumnHeader implements Serializable

{
    private String headerText;    //the raw text found in the table cell
    private String xPath; //xpath that extracts this value
    private List<ColumnDataType> type;
    private ColumnFeature feature;

    public LTableColumnHeader(String text){
        this.headerText=text;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String text) {
        this.headerText = text;
    }

    public String getHeaderXPath() {
        return xPath;
    }

    public void setHeaderXPath(String xPath) {
        this.xPath = xPath;
    }

    public List<ColumnDataType> getTypes() {
        return type;
    }

    public void setType(List<ColumnDataType> type) {
        this.type = type;
    }

    public ColumnFeature getFeature() {
        return feature;
    }

    public void setFeature(ColumnFeature feature) {
        this.feature = feature;
    }
}
