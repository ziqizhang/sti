package uk.ac.shef.dcs.kbsearch.dbpedia;

/**
 * Created by Jan on 18.05.2016.
 */
public enum DBpediaEnum {

    RELATION_HASALIAS("http://dbpedia.org/ontology/alias"),
    RELATION_HASTYPE("/type/object/type"),
    RELATION_RANGEOFPROPERTY("/type/property/expected_type"),
    RELATION_HASNAME("/type/object/name"),
    RELATION_HASDESCRIPTION("http://dbpedia.org/ontology/description"),

    TYPE_TYPE("/type/type"),
    TYPE_PROPERTYOFTYPE("/type/type/properties"),
    TYPE_COMMON_TOPIC("/common/topic");

    private String string;

    DBpediaEnum(String s){
        this.string=s;
    }

    public String getString(){
        return string;
    }
}
