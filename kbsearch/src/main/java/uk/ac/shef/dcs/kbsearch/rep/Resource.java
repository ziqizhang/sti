package uk.ac.shef.dcs.kbsearch.rep;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
public abstract class Resource implements Serializable{
    private static final long serialVersionUID = -1208424489000405913L;

    protected String id;
    protected String label;
    protected List<Attribute> attributes;

    public List<Attribute> getAttributes(){
        return attributes;
    }
    public void setAttributes(List<Attribute> attributes){
        this.attributes = attributes;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id=id;
    }

    public String getLabel(){
        return label;
    }
    public void setLabel(String label){
        this.label=label;
    }

    public boolean equals(Object o){
        if(o instanceof Resource){
            Resource r = (Resource)o;
            return r.getId().equals(this.getId());
        }
        return false;
    }
}
