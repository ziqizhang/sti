package uk.ac.shef.dcs.kbsearch.sparql;

import uk.ac.shef.dcs.kbsearch.model.Attribute;

/**
 * Created by - on 10/06/2016.
 */
public class DBpediaAttribute extends Attribute {
    private String valueURI;

    public DBpediaAttribute(String relationURI, String value) {
        super(relationURI, value);
    }

    @Override
    public boolean isAlias() {
        return getRelationURI().equals(RDFEnum.RELATION_HASLABEL.getString()) ||
                getRelationURI().equals(FOAFEnum.RELATION_HASLABEL.getString()) ||
                getRelationURI().equals(DBpediaEnum.RELATION_HASFULLNAME.getString()) ||
                getRelationURI().equals(DBpediaEnum.RELATION_HASNAME.getString());
    }

    @Override
    public boolean isDescription() {
        return getRelationURI().equals(DBpediaEnum.RELATION_HASABSTRACT.getString()) ||
                getRelationURI().equals(RDFEnum.RELATION_HASCOMMENT.getString()) ||
                getRelationURI().equals(DCEnum.RELATION_HASDESCRIPTION.getString());
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
