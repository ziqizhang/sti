package uk.ac.shef.dcs.kbsearch.sparql;

/**
 * Created by - on 10/06/2016.
 */
public enum DCEnum {

    RELATION_HASLABEL("<http://www.w3.org/2000/01/rdf-schema#label>"),
    RELATION_HASDESCRIPTION("<http://purl.org/dc/elements/1.1/description>");


    private String string;

    DCEnum(String s){
        this.string=s;
    }

    public String getString(){
        return string;
    }


}
