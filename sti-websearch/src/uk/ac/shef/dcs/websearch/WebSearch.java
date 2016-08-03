package uk.ac.shef.dcs.websearch;

import javax.rmi.PortableRemoteObject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by - on 17/03/2016.
 */
public abstract class WebSearch {

    protected static final String WEB_SEARCH_CLASS = "web.search.class";

    protected Properties properties;


    public WebSearch(Properties properties) throws IOException {
        this.properties=properties;
    }

    public abstract InputStream search(String s) throws Exception;

    public abstract SearchResultParser getResultParser();
}
