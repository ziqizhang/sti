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

    protected Properties properties = new Properties();


    public WebSearch(String propertyFile) throws IOException {
        properties.load(new FileInputStream(propertyFile));
    }

    public abstract InputStream search(String s) throws Exception;
}
