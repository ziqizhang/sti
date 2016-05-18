package uk.ac.shef.dcs.kbsearch.dbpedia;

/**
 * Created by Jan on 18.05.2016.
 */
public enum DbpediaEnum {

    RELATION_HASALIAS("http://dbpedia.org/ontology/alias"),
    RELATION_HASDESCRIPTION("http://dbpedia.org/ontology/description");

    private String string;

    DbpediaEnum(String s){
        this.string=s;
    }

    public String getString(){
        return string;
    }
}
