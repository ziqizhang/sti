package uk.ac.shef.dcs.sti;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 22/10/12
 * Time: 12:42
 */
public class STIException extends Exception {
    public STIException(String s, Exception e) {
        super(s, e);
    }

    public STIException(String s) {
        super(s);
    }

    public STIException(Exception e){
        super(e);
    }

}
