package uk.ac.shef.dcs.websearch.bing.v2;

/**
 */
public class APIKeysDepletedException extends Exception {
    public APIKeysDepletedException(){
        super("All API keys have outdated, search cannot continue.");
    }
}
