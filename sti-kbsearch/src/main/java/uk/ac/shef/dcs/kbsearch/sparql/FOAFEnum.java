package uk.ac.shef.dcs.kbsearch.sparql;

/**
 * Created by - on 10/06/2016.
 */
public enum  FOAFEnum {

    RELATION_HASLABEL("http://xmlns.com/foaf/0.1/name");


    private String string;

    FOAFEnum(String s){
        this.string=s;
    }

    public String getString(){
        return string;
    }
}
