package uk.ac.shef.dcs.kbsearch.model;

import uk.ac.shef.dcs.kbsearch.KBDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public abstract class Resource implements Serializable {
    private static final long serialVersionUID = -1208424489000405913L;

    protected String id; //id, uri
    protected String label; //label
    protected List<Attribute> attributes;
    private String description; //a description, if available
    private Set<String> aliases; //aliases, if available

    public String getDescription(KBDefinition definition){
        if(description==null||description.equals("")){
            for(Attribute attr: getAttributes()){
                if(attr.isDirect()&&attr.isDescription(definition)) {
                    description = attr.getValue();
                    break;
                }
            }
        }
        if(description==null)
            description="";
        return description;
    }

    public Set<String> getAliases(KBDefinition definition){
        if(aliases==null) {
            aliases = new HashSet<>();
            return aliases;
        }
        if(aliases.size()==0){
            for(Attribute attr: getAttributes()){
                if(attr.isDirect()&&attr.isAlias(definition) && !attr.getValue().equals(getLabel())) {
                    aliases.add(attr.getValue());
                }
            }
        }
        return aliases;
    }

    public List<Attribute> getAttributes() {
        if(attributes==null)
            attributes=new ArrayList<>();
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean equals(Object o) {
        if (o instanceof Resource) {
            Resource r = (Resource) o;
            return r.getId().equals(this.getId());
        }
        return false;
    }

}
