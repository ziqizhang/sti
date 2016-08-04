package uk.ac.shef.dcs.kbsearch.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by - on 15/03/2016.
 */
public class Entity extends Resource {
    private static final long serialVersionUID = -1208425814000405913L;
    protected List<Clazz> types=new ArrayList<>();
    protected Set<String> typeIds=new HashSet<>();
    protected Set<String> typeNames =new HashSet<>();

    public Entity(String id, String label){
        this.id=id;
        this.label=label;
    }

    public void addType(Clazz c){
        if(!types.contains(c)) {
            types.add(c);
            typeIds.add(c.getId());
            typeNames.add(c.getLabel());
        }
    }

    public Set<String> getTypeIds() {
        return typeIds;
    }

    public Set<String> getTypeNames() {
        return typeNames;
    }

    public boolean hasType(String typeId) {
        return typeIds.contains(typeId);
    }

    public List<Clazz> getTypes() {
        return types;
    }

    public void clearTypes(){
        types.clear();
        typeIds.clear();
        typeNames.clear();
    }
}
