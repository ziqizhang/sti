package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import org.eclipse.rdf4j.model.IRI;

import com.google.common.base.Preconditions;

public class TriplePattern {
  
  private String subjectPattern;
  
  private IRI predicate;
  
  private String objectPattern;
  
  public TriplePattern(String subjectPattern, IRI predicate, String objectPattern) {
    Preconditions.checkNotNull(subjectPattern);
    Preconditions.checkNotNull(predicate);
    Preconditions.checkNotNull(objectPattern);
    
    this.subjectPattern = subjectPattern;
    this.predicate = predicate;
    this.objectPattern = objectPattern;
  }
  
  public String getSubjectPattern() {
    return subjectPattern;
  }
  
  public IRI getPredicate() {
    return predicate;
  }
  
  public String getObjectPattern() {
    return objectPattern;
  }
}
