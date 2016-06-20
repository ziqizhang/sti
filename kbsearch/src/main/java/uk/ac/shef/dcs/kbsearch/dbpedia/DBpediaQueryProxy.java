package uk.ac.shef.dcs.kbsearch.dbpedia;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
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
        Query query = QueryFactory.create("test");

        return null;
    }

    public List<Entity> searchapi_getTopicsByNameAndType(String text, String any, boolean b, int i) {
        return null;
    }
}
