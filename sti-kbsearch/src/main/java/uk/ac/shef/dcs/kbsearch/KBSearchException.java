package uk.ac.shef.dcs.kbsearch;

/**
 *
 */
public class KBSearchException extends Exception {

    public KBSearchException(String msg){
        super(msg);
    }

    public KBSearchException(Exception e){
        super(e);
    }
}
