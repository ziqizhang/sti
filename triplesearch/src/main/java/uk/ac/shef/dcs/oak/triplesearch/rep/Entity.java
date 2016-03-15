package uk.ac.shef.dcs.oak.triplesearch.rep;

import java.util.List;
import java.util.Set;

/**
 * Created by - on 15/03/2016.
 */
public class Entity extends Resource {
    private static final long serialVersionUID = -1208425814000405913L;
    protected List<Clazz> types;
    protected Set<String> typeIds;
    protected Set<String> typeNames;

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
        return typeId.contains(typeId);
    }

    public List<Clazz> getTypes() {
        return types;
    }
}
