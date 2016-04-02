package uk.ac.shef.dcs.kbsearch.model;

import java.io.Serializable;

/**
 * An attribute of a resource forms a triple with that resource. It must contain a relation, and a value which is the object of
 * the triple. The resource is always the subject
 */
public class Attribute implements Serializable{
    private static final long serialVersionUID = -8624557934000474692L;

    private String relationURI;
    private String value;
    private String valueURI; //in case 'value' is a resource, define its URI if available
    private boolean isDirect=true; //if this attribute is a direct attribute. this is generally 'true'. for freebase,
                              //triples that form an indirect relation with a resource can be returned. In which case this
                              //is used to indicate whether the attribute is a direct attribute of the resource or not

    public Attribute(String relationURI, String value) {
        this.relationURI =relationURI;
        this.value=value;
    }

    public void setRelationURI(String relationURI) {
        this.relationURI = relationURI;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRelationURI() {
        return relationURI;
    }

    public String getValueURI() {
        return valueURI;
    }

    public void setValueURI(String valueURI) {
        this.valueURI = valueURI;
    }

    public boolean equals(Object o){
        if(o instanceof Attribute){
            Attribute a = (Attribute)o;
            return a.getRelationURI().equals(this.getRelationURI()) && a.getValue().equals(this.getValue());
        }
        return false;
    }

    public boolean isDirect() {
        return isDirect;
    }

    public void setIsDirect(boolean isDirect) {
        this.isDirect = isDirect;
    }

    public String toString(){
        return "r="+ relationURI +",o="+value+" ("+valueURI+"), direct="+isDirect;
    }
}
