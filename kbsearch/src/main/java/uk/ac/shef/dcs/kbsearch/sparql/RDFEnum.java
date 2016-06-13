package uk.ac.shef.dcs.kbsearch.sparql;

/**
 * Created by - on 10/06/2016.
 */
public enum RDFEnum {
    //RDFS has different versions (2000/1999), so we define patterns and shorthands instead of exact URIs
    RELATION_HASLABEL_SUFFIX_PATTERN("#label"),
    RELATION_HASTYPE_SUFFIX_PATTERN("#type"),
    RELATION_HASCOMMENT_SUFFIX_PATTERN("#comment"),

    //
    RELATION_HASLABEL("http://www.w3.org/2000/01/rdf-schema#label"),
    RELATION_HASTYPE("https://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
    RELATION_HASCOMMENT("http://www.w3.org/2000/01/rdf-schema#comment");

    private String string;

    RDFEnum(String s){
        this.string=s;
    }

    public String getString(){
        return string;
    }
}
