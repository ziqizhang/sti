package uk.ac.shef.oak.any23.extension.extractor;

import org.openrdf.model.Statement;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 09/11/12
 * Time: 11:16
 */
public class LTriple {

    private Statement triple;
    private String sXPath;
    private String pXPath;
    private String oXPath;

    public LTriple(Statement stmt){
        this.triple=stmt;
    }

    public Statement getTriple() {
        return triple;
    }

    public void setTriple(Statement triple) {
        this.triple = triple;
    }

    public String getsXPath() {
        return sXPath;
    }

    public void setsXPath(String sXPath) {
        this.sXPath = sXPath;
    }

    public String getpXPath() {
        return pXPath;
    }

    public void setpXPath(String pXPath) {
        this.pXPath = pXPath;
    }

    public String getoXPath() {
        return oXPath;
    }

    public void setoXPath(String oXPath) {
        this.oXPath = oXPath;
    }

    public String toString(){
        return triple.toString();
    }


}
