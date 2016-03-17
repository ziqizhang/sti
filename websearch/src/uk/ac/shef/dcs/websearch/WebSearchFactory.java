package uk.ac.shef.dcs.websearch;

import uk.ac.shef.dcs.websearch.bing.v2.BingSearch;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by - on 17/03/2016.
 */
public class WebSearchFactory {
    public WebSearch createInstance(String className,
                                   String propertyFile) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if(className.equals(BingSearch.class.getName())){
            return (WebSearch) Class.forName(className).
                    getDeclaredConstructor(InputStream.class).newInstance(propertyFile
                    );
        }
        return null;
    }
}
