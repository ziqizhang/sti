package uk.ac.shef.dcs.kbproxy.sparql;

import uk.ac.shef.dcs.kbproxy.KBDefinition;
import uk.ac.shef.dcs.kbproxy.KBProxyUtils;
import uk.ac.shef.dcs.kbproxy.model.Attribute;

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
        return KBProxyUtils.contains(definition.getPredicateLabel(), getRelationURI()) ||
                KBProxyUtils.contains(definition.getPredicateName(), getRelationURI());
    }

    @Override
    public boolean isDescription(KBDefinition definition) {
        return KBProxyUtils.contains(definition.getPredicateDescription(), getRelationURI());
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
