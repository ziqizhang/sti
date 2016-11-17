package uk.ac.shef.dcs.kbproxy.model;

/**
 * Created by - on 15/03/2016.
 */
public class Clazz extends Resource{
    private static final long serialVersionUID = -1208425814000474692L;

    public Clazz (String id, String label){
        this.id=id;
        this.label=label;
    }

    public String toString(){
        return "id="+id+", label="+label;
    }

    public boolean equals(Object o){
        if (o instanceof Clazz){
            Clazz c = (Clazz)o;
            return c.getId().equals(id) && c.getLabel().equals(label);
        }
        return false;
    }
}
