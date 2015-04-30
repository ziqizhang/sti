package uk.ac.shef.dcs.oak.sti.deprecated_table_list.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 09/10/12
 * Time: 11:07
 */
public class LList implements Serializable {
    private String sourceId;
    private String listId;
    private String typeURI;
    private List<LListItem> items;
    private List<String> contexts;

    public LList(String sourceId, String listId){
        this.sourceId=sourceId;
        this.listId=listId;
        items=new ArrayList<LListItem>();
        contexts=new ArrayList<String>();
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

    public void addItem(LListItem item){
        items.add(item);
    }
    public List<LListItem> getItems(){
        return items;
    }

    public String getTypeURI() {
        return typeURI;
    }

    public void setTypeURI(String typeURI) {
        this.typeURI = typeURI;
    }

    public List<String> getContexts() {
        return contexts;
    }

    public void addContext(String ctx) {
        this.contexts.add(ctx);
    }
}
