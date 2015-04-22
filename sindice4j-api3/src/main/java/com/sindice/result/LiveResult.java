package com.sindice.result;

import sun.net.idn.StringPrep;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 21/09/12
 * Time: 15:24
 */
public class LiveResult implements QueryResult {

    private List<String> explicit;
    private List<String> implicit;

    public LiveResult(){
        explicit=new ArrayList<String>();
        implicit=new ArrayList<String>();
    }


    public List<String> getExplicitContent() {
        return explicit;
    }

    public void addExplicit(String e) {
        this.explicit.add(e);
    }

    public List<String> getImplicitContent() {
        return implicit;
    }

    public void addImplicit(String i) {
        this.implicit.add(i);
    }
}
