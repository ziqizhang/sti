package uk.ac.shef.dcs.kbsearch.sparql;

/**
 * Created by - on 10/06/2016.
 */
public enum RDFEnum {
    RELATION_HASLABEL("<http://www.w3.org/2000/01/rdf-schema#label>"),
    RELATION_HASTYPE("<http://www.w3.org/2000/01/rdf-schema#type>"),
    RELATION_HASCOMMENT("<http://www.w3.org/2000/01/rdf-schema#comment>");


    private String string;

    RDFEnum(String s){
        this.string=s;
    }

    public String getString(){
        return string;
    }
}
