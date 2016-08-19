package cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp;

import com.google.common.base.Preconditions;
import org.eclipse.rdf4j.model.IRI;

/**
 * Created by tomasknap on 18/08/16.
 * 
 * @author Tomáš Knap
 *
 */
public class DataPropertyTriplePattern extends TriplePattern {

    private String objectColumn;

    public DataPropertyTriplePattern(String subjectPattern, IRI predicate, String objectColumn) {
        super(subjectPattern, predicate);
        Preconditions.checkNotNull(objectColumn);
        this.objectColumn = objectColumn;
    }

    public String getObjectColumnName() {
        return objectColumn;
    }

}
