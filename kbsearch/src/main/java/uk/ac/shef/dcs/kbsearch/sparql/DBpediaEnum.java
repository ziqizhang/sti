package uk.ac.shef.dcs.kbsearch.sparql;

/**
 * Created by - on 10/06/2016.
 */
public enum DBpediaEnum {

    RELATION_HASNAME("http://dbpedia.org/property/name"),
    RELATION_HASFULLNAME("http://dbpedia.org/property/fullname"),
    RELATION_HASABSTRACT("<http://dbpedia.org/ontology/abstract>");


    private String string;

    DBpediaEnum(String s){
        this.string=s;
    }

    public String getString(){
        return string;
    }
}
