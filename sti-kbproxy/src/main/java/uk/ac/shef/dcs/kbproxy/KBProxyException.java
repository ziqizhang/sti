package uk.ac.shef.dcs.kbproxy;

/**
 *
 */
public class KBProxyException extends Exception {

    public KBProxyException(String msg){
        super(msg);
    }

    public KBProxyException(Exception e){
        super(e);
    }

    public KBProxyException(String msg, Exception e){
        super(msg, e);
    }
}
