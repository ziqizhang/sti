package uk.ac.shef.dcs.kbproxy.freebase;

/**
 * Created by - on 31/03/2016.
 */
public enum FreebaseEnum {

    RELATION_HASALIAS("/common/topic/alias"),
    RELATION_HASTYPE("/type/object/type"),
    RELATION_RANGEOFPROPERTY("/type/property/expected_type"),
    RELATION_HASNAME("/type/object/name"),
    RELATION_HASDESCRIPTION("/common/topic/description"),
    RELATION_HASDOCUMENTTEXT("/common/document/text"),

    TYPE_TYPE("/type/type"),
    TYPE_PROPERTYOFTYPE("/type/type/properties"),
    TYPE_COMMON_TOPIC("/common/topic"),
    TYPE_USER("/user");


    private String string;

    FreebaseEnum(String s){
        this.string=s;
    }

    public String getString(){
        return string;
    }
}
