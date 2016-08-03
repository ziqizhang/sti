package uk.ac.shef.dcs.websearch;

/**
 * Created by - on 07/04/2016.
 */
public class WebSearchException extends Exception {
    public WebSearchException(Exception e){
        super(e);
    }

    public WebSearchException(String e){
        super(e);
    }
}
