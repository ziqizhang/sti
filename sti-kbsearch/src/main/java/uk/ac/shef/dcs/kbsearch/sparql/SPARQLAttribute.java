package uk.ac.shef.dcs.kbsearch.sparql;

import uk.ac.shef.dcs.kbsearch.KBDefinition;
import uk.ac.shef.dcs.kbsearch.KBSearchUtis;
import uk.ac.shef.dcs.kbsearch.model.Attribute;

/**
 * Created by - on 10/06/2016.
 *
 * TODO no need for this class, alias predicates, description predicates can be stored in config.
 *
 */
public class SPARQLAttribute extends Attribute {

    public SPARQLAttribute(String relationURI, String value) {
        super(relationURI, value);
    }

    @Override
    public boolean isAlias(KBDefinition definition) {
        return KBSearchUtis.contains(definition.getPredicateLabel(), getRelationURI()) ||
                KBSearchUtis.contains(definition.getPredicateName(), getRelationURI());
    }

    @Override
    public boolean isDescription(KBDefinition definition) {
        return KBSearchUtis.contains(definition.getPredicateDescription(), getRelationURI());
    }

    @Override
    public String getValueURI() {
        return valueURI;
    }

    @Override
    public void setValueURI(String valueURI) {
        this.valueURI = valueURI;
    }
}
