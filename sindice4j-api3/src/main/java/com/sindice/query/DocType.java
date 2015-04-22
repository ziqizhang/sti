package com.sindice.query;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 21/09/12
 * Time: 13:14
 */
public enum DocType {

    RDF("RDF"),
    RDFA("RDFA"),
    MICRODATA("MICRODATA"),
    MICROFORMAT("MICROFORMAT"),
    XFN("XFN"),
    HCARD("HCARD"),
    HCALENDAR("HCALENDAR"),
    HLISTING("HLISTING"),
    HRESUME("HRESUME"),
    LICENSE("LICENSE"),
    GEO("GEO"),
    ADR("ADR");


    private String type;

    DocType(String type){
        this.type=type;
    }

    public String getType(){
        return type;
    }
}
