package uk.ac.shef.dcs.kbsearch.dbpedia;

import uk.ac.shef.dcs.kbsearch.model.Attribute;

/**
 * Created by Jan on 18.05.2016.
 */
public class DbpediaAttribute extends Attribute {

    public DbpediaAttribute(String relationURI, String value) {
        super(relationURI, value);
    }

    @Override
    public boolean isAlias() {
        return getRelationURI().equals(DbpediaEnum.RELATION_HASALIAS.getString());
    }

    @Override
    public boolean isDescription() {
        return getRelationURI().equals(DbpediaEnum.RELATION_HASDESCRIPTION.getString());
    }
}
