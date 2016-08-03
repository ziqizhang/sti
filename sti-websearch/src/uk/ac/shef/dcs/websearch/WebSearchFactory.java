package uk.ac.shef.dcs.websearch;

import uk.ac.shef.dcs.websearch.bing.v2.BingSearch;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Created by - on 17/03/2016.
 */
public class WebSearchFactory {
    public WebSearch createInstance(
                                   String propertyFile) throws WebSearchException {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(propertyFile));
            String className = properties.getProperty(WebSearch.WEB_SEARCH_CLASS);
            if (className.equals(BingSearch.class.getName())) {
                return (WebSearch) Class.forName(className).
                        getDeclaredConstructor(Properties.class).newInstance(properties
                );
            }
            else {
                throw new WebSearchException("Class: "+className+" not supported");
            }
        }catch (Exception e){
            throw new WebSearchException(e);
        }
    }
}
