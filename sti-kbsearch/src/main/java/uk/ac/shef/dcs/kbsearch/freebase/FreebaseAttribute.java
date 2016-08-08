package uk.ac.shef.dcs.kbsearch.freebase;

import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.KBDefinition;

/**
 * Created by - on 06/04/2016.
 */
public class FreebaseAttribute extends Attribute {

    private static final long serialVersionUID = -1208426557010474692L;


    public FreebaseAttribute(String relationURI, String value) {
        super(relationURI, value);
    }

    @Override
    public boolean isAlias(KBDefinition definition) {
        return getRelationURI().equals(FreebaseEnum.RELATION_HASALIAS.getString());
    }

    @Override
    public boolean isDescription(KBDefinition definition) {
        return getRelationURI().equals(FreebaseEnum.RELATION_HASDESCRIPTION.getString());
    }
}
