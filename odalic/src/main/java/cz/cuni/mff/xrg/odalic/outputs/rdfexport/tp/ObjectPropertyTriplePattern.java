package cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp;

import com.google.common.base.Preconditions;
import org.eclipse.rdf4j.model.IRI;

/**
 * Created by tomasknap on 18/08/16.
 * 
 * @author Tomáš Knap
 *
 */
public class ObjectPropertyTriplePattern extends TriplePattern {

    private String objectPattern;

    public ObjectPropertyTriplePattern(String subjectPattern, IRI predicate, String objectPattern) {
        super(subjectPattern, predicate);
        Preconditions.checkNotNull(objectPattern);
        this.objectPattern = objectPattern;
    }

    public String getObjectPattern() {
        return objectPattern;
    }

}
