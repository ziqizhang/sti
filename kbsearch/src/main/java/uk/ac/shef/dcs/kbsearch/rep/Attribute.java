package uk.ac.shef.dcs.kbsearch.rep;

import java.util.HashMap;
import java.util.Map;

/**
 * An attribute of a resource forms a triple with that resource. It must contain a relation, and a value which is the object of
 * the triple. The resource is always the subject
 */
public class Attribute {

    private String relation;
    private String value;
    private boolean isLiteralValue;
    private Map<String, String> otherInfo = new HashMap<>();

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isLiteralValue() {
        return isLiteralValue;
    }

    public void setIsLiteralValue(boolean isLiteralValue) {
        this.isLiteralValue = isLiteralValue;
    }

    public Map<String, String> getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(Map<String, String> otherInfo) {
        this.otherInfo = otherInfo;
    }
}
