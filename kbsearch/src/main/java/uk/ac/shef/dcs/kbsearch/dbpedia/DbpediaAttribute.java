package uk.ac.shef.dcs.kbsearch.dbpedia;

import uk.ac.shef.dcs.kbsearch.model.Attribute;

/**
 * Created by Jan on 18.05.2016.
 */
public class DBpediaAttribute extends Attribute {

    public DBpediaAttribute(String relationURI, String value) {
        super(relationURI, value);
    }

    @Override
    public boolean isAlias() {
        return getRelationURI().equals(DBpediaEnum.RELATION_HASALIAS.getString());
    }

    @Override
    public boolean isDescription() {
        return getRelationURI().equals(DBpediaEnum.RELATION_HASDESCRIPTION.getString());
    }
}
