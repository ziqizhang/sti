package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.feedbacks.Ambiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.CellRelation;
import cz.cuni.mff.xrg.odalic.feedbacks.Classification;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnAmbiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnRelation;
import cz.cuni.mff.xrg.odalic.feedbacks.Disambiguation;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Domain class {@link Feedback} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "feedback")
public final class FeedbackValue implements Serializable {

  private static final long serialVersionUID = -7968455903789693405L;

  private ColumnPosition subjectColumnPosition;

  private Set<ColumnIgnore> columnIgnores;
  
  private Set<ColumnAmbiguity> columnAmbiguities;
  
  private Set<Classification> classifications;
  
  private Set<CellRelation> cellRelations;
  
  private Set<ColumnRelation> columnRelations;
  
  private Set<Disambiguation> disambiguations;
  
  private Set<Ambiguity> ambiguities;

  public FeedbackValue() {
    subjectColumnPosition = null;
    columnIgnores = ImmutableSet.of();
    columnAmbiguities = ImmutableSet.of();
    classifications = ImmutableSet.of();
    cellRelations = ImmutableSet.of();
    columnRelations = ImmutableSet.of();
    disambiguations = ImmutableSet.of();
    ambiguities = ImmutableSet.of();
  }
  
  public FeedbackValue(Feedback adaptee) {
    subjectColumnPosition = adaptee.getSubjectColumnPosition();
    columnIgnores = adaptee.getColumnIgnores();
    columnAmbiguities = adaptee.getColumnAmbiguities();
    classifications = adaptee.getClassifications();
    cellRelations = adaptee.getCellRelations();
    columnRelations = adaptee.getColumnRelations();
    disambiguations = adaptee.getDisambiguations();
    ambiguities = adaptee.getAmbiguities();
  }

  /**
   * @return the subject column position
   */
  @XmlElement
  @Nullable
  public ColumnPosition getSubjectColumnPosition() {
    return subjectColumnPosition;
  }

  /**
   * @param subjectColumnPosition the subject column position to set
   */
  public void setSubjectColumnPosition(ColumnPosition subjectColumnPosition) {
    this.subjectColumnPosition = subjectColumnPosition;
  }

  /**
   * @return the column ignores
   */
  @XmlElement
  public Set<ColumnIgnore> getColumnIgnores() {
    return columnIgnores;
  }

  /**
   * @param columnIgnores the column ignores to set
   */
  public void setColumnIgnores(Set<? extends ColumnIgnore> columnIgnores) {
    Preconditions.checkNotNull(columnIgnores);
    
    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
  }

  /**
   * @return the column ambiguities
   */
  @XmlElement
  public Set<ColumnAmbiguity> getColumnAmbiguities() {
    return columnAmbiguities;
  }

  /**
   * @param columnAmbiguities the column ambiguities to set
   */
  public void setColumnAmbiguities(Set<? extends ColumnAmbiguity> columnAmbiguities) {
    Preconditions.checkNotNull(columnAmbiguities);
    
    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
  }

  /**
   * @return the classifications
   */
  @XmlElement
  public Set<Classification> getClassifications() {
    return classifications;
  }

  /**
   * @param classifications the classifications to set
   */
  public void setClassifications(Set<? extends Classification> classifications) {
    Preconditions.checkNotNull(classifications);
    
    this.classifications = ImmutableSet.copyOf(classifications);
  }

  /**
   * @return the cell relations
   */
  @XmlElement
  public Set<CellRelation> getCellRelations() {
    return cellRelations;
  }

  /**
   * @param cellRelations the cellRelations to set
   */
  public void setCellRelations(Set<? extends CellRelation> cellRelations) {
    Preconditions.checkNotNull(cellRelations);
    
    this.cellRelations = ImmutableSet.copyOf(cellRelations);
  }

  /**
   * @return the column relations
   */
  @XmlElement
  public Set<ColumnRelation> getColumnRelations() {
    return columnRelations;
  }

  /**
   * @param columnRelations the column relations to set
   */
  public void setColumnRelations(Set<? extends ColumnRelation> columnRelations) {
    Preconditions.checkNotNull(columnRelations);
    
    this.columnRelations = ImmutableSet.copyOf(columnRelations);
  }

  /**
   * @return the disambiguations
   */
  @XmlElement
  public Set<Disambiguation> getDisambiguations() {
    return disambiguations;
  }

  /**
   * @param disambiguations the disambiguations to set
   */
  public void setDisambiguations(Set<? extends Disambiguation> disambiguations) {
    Preconditions.checkNotNull(disambiguations);
    
    this.disambiguations = ImmutableSet.copyOf(disambiguations);
  }

  /**
   * @return the ambiguities
   */
  @XmlElement
  public Set<Ambiguity> getAmbiguities() {
    return ambiguities;
  }

  /**
   * @param ambiguities the ambiguities to set
   */
  public void setAmbiguities(Set<? extends Ambiguity> ambiguities) {
    Preconditions.checkNotNull(ambiguities);
    
    this.ambiguities = ImmutableSet.copyOf(ambiguities);
  }

    
}
