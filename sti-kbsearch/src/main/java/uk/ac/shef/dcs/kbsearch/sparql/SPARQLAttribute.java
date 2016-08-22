package uk.ac.shef.dcs.kbsearch.sparql;

import uk.ac.shef.dcs.kbsearch.KBDefinition;
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
        return definition.getPredicateLabel().contains(getRelationURI()) ||
                definition.getPredicateName().contains(getRelationURI());
    }

    @Override
    public boolean isDescription(KBDefinition definition) {
        return definition.getPredicateDescription().contains(getRelationURI());
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
