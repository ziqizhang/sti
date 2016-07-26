package uk.ac.shef.dcs.sti.core.model;

import uk.ac.shef.dcs.sti.core.subjectcol.TColumnDataType;
import uk.ac.shef.dcs.sti.core.subjectcol.TColumnFeature;

import java.io.Serializable;
import java.util.List;

/**
 */
public class TColumnHeader implements Serializable

{

    private static final long serialVersionUID = -1638925814000405913L;

    private String headerText;    //the raw text found in the table cell
    private String xPath; //xpath that extracts this value
    private List<TColumnDataType> type;
    private TColumnFeature feature;

    public TColumnHeader(String text){
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

    public List<TColumnDataType> getTypes() {
        return type;
    }

    public void setType(List<TColumnDataType> type) {
        this.type = type;
    }

    public TColumnFeature getFeature() {
        return feature;
    }

    public void setFeature(TColumnFeature feature) {
        this.feature = feature;
    }
}
