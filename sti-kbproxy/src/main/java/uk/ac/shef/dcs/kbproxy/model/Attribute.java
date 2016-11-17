package uk.ac.shef.dcs.kbproxy.model;

import uk.ac.shef.dcs.kbproxy.KBDefinition;

import java.io.Serializable;

/**
 * An attribute of a resource forms a triple with that resource. It must contain a relation, and a value which is the object of
 * the triple. The resource is always the subject
 */
public abstract class Attribute implements Serializable{

    protected String relationURI;
    protected String value;
    protected String valueURI; //in case 'value' is a resource, define its URI if available
    protected boolean isDirect=true; //if this attribute is a direct attribute. this is generally 'true'. for freebase,
                              //triples that form an indirect relation with a resource can be returned. In which case this
                              //is used to indicate whether the attribute is a direct attribute of the resource or not


    public Attribute(String relationURI, String value) {
        this.relationURI =relationURI;
        this.value= fixValue(value);
    }

    // TODO: Fix the dependency on KBDefinition
    /**
     *
     * @return true if the attribute is about alias
     */
    public abstract boolean isAlias(KBDefinition definition);

    /**
     *
     * @return true if the attribute is about description
     */
    public abstract boolean isDescription(KBDefinition definition);

    public void setRelationURI(String relationURI) {
        this.relationURI = relationURI;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = fixValue(value);
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

    String fixValue(String originalValue) {
        Integer index = originalValue.indexOf("^^");
        if (originalValue.startsWith("http") && index > 0) {
            return originalValue.substring(0, index);
        }
        else {
            return originalValue;
        }
    }
}
