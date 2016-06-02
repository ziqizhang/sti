package uk.ac.shef.dcs.kbsearch.dbpedia;

import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Entity;

import java.io.IOException;
import java.util.List;

/**
 * Created by Jan on 02.06.2016.
 */
public class DBpediaQueryProxy {
    public double find_granularityForType(String clazz) throws IOException {
        return -1.0;
    }

    public List<Attribute> topicapi_getAttributesOfTopic(String clazzId) {
        return null;
    }

    public List<Entity> searchapi_getTopicsByNameAndType(String text, String any, boolean b, int i) {
        return null;
    }
}
