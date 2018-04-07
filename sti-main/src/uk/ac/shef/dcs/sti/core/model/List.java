package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 09/10/12
 * Time: 11:07
 */
public class List implements Serializable {
    private static final long serialVersionUID = -8136725813759687613L;
    private String sourceId;
    private String listId;
    private java.util.List<ListItem> items;
    private java.util.List<String> contexts;

    public List(String sourceId, String listId){
        this.sourceId=sourceId;
        this.listId=listId;
        items=new ArrayList<>();
        contexts=new ArrayList<>();
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getListId() {
        return listId;
    }

    public String toString(){
        return getSourceId()+","+getListId();
    }

    public void addItem(ListItem item){
        items.add(item);
    }
    public java.util.List<ListItem> getItems(){
        return items;
    }

    public java.util.List<String> getContexts() {
        return contexts;
    }

    public void addContext(String ctx) {
        this.contexts.add(ctx);
    }
}
