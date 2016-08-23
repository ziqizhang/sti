package cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp;

import com.google.common.base.Preconditions;
import org.eclipse.rdf4j.model.IRI;

/**
 * 
 * 
 * @author Josef Janoušek
 *
 */
public class ObjectListPropertyTriplePattern extends ObjectPropertyTriplePattern {

    private String separator;

    public ObjectListPropertyTriplePattern(String subjectPattern, IRI predicate, String objectPattern, String separator) {
        super(subjectPattern, predicate, objectPattern);
        Preconditions.checkNotNull(separator);
        this.separator = separator;
    }

    public String getSeparator() {
      return separator;
    }

}
