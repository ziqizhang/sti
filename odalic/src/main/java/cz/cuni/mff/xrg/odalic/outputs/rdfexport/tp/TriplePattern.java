package cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp;

import org.eclipse.rdf4j.model.IRI;

import com.google.common.base.Preconditions;

/**
 * 
 * @author Josef Janoušek
 * @author Tomáš Knap
 *
 */
public class TriplePattern {
  
  private String subjectPattern;
  
  private IRI predicate;
  
  public TriplePattern(String subjectPattern, IRI predicate) {
    Preconditions.checkNotNull(subjectPattern);
    Preconditions.checkNotNull(predicate);

    this.subjectPattern = subjectPattern;
    this.predicate = predicate;
  }
  
  public String getSubjectPattern() {
    return subjectPattern;
  }
  
  public IRI getPredicate() {
    return predicate;
  }
  

}
