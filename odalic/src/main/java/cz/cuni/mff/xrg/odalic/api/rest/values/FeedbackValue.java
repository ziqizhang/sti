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

@XmlRootElement(name = "feedback")
public class FeedbackValue implements Serializable {

  private static final long serialVersionUID = -7968455903789693405L;

  @XmlElement
  private ColumnPosition subjectColumnPosition;

  @XmlElement
  private Set<ColumnIgnore> columnIgnores;
  
  @XmlElement
  private Set<ColumnAmbiguity> columnAmbiguities;
  
  @XmlElement
  private Set<Classification> classifications;
  
  @XmlElement
  private Set<CellRelation> cellRelations;
  
  @XmlElement
  private Set<ColumnRelation> columnRelations;
  
  @XmlElement
  private Set<Disambiguation> disambiguations;
  
  @XmlElement
  private Set<Ambiguity> ambiguities;

  public FeedbackValue() {
    this.subjectColumnPosition = null;
    this.columnIgnores = ImmutableSet.of();
    this.columnAmbiguities = ImmutableSet.of();
    this.classifications = ImmutableSet.of();
    this.cellRelations = ImmutableSet.of();
    this.columnRelations = ImmutableSet.of();
    this.disambiguations = ImmutableSet.of();
    this.ambiguities = ImmutableSet.of();
  }
  
  public FeedbackValue(Feedback adaptee) {
    Preconditions.checkNotNull(adaptee);
    
    this.subjectColumnPosition = adaptee.getSubjectColumnPosition();
    this.columnIgnores = adaptee.getColumnIgnores();
    this.columnAmbiguities = adaptee.getColumnAmbiguities();
    this.classifications = adaptee.getClassifications();
    this.cellRelations = adaptee.getCellRelations();
    this.columnRelations = adaptee.getColumnRelations();
    this.disambiguations = adaptee.getDisambiguations();
    this.ambiguities = adaptee.getAmbiguities();
  }

  /**
   * @return the subjectColumnPosition
   */
  @Nullable
  public ColumnPosition getSubjectColumnPosition() {
    return subjectColumnPosition;
  }

  /**
   * @param subjectColumnPosition the subjectColumnPosition to set
   */
  public void setSubjectColumnPosition(ColumnPosition subjectColumnPosition) {
    this.subjectColumnPosition = subjectColumnPosition;
  }

  /**
   * @return the columnIgnores
   */
  public Set<ColumnIgnore> getColumnIgnores() {
    return columnIgnores;
  }

  /**
   * @param columnIgnores the columnIgnores to set
   */
  public void setColumnIgnores(Set<ColumnIgnore> columnIgnores) {
    Preconditions.checkNotNull(columnIgnores);
    
    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
  }

  /**
   * @return the columnAmbiguities
   */
  public Set<ColumnAmbiguity> getColumnAmbiguities() {
    return columnAmbiguities;
  }

  /**
   * @param columnAmbiguities the columnAmbiguities to set
   */
  public void setColumnAmbiguities(Set<ColumnAmbiguity> columnAmbiguities) {
    Preconditions.checkNotNull(columnAmbiguities);
    
    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
  }

  /**
   * @return the classifications
   */
  public Set<Classification> getClassifications() {
    return classifications;
  }

  /**
   * @param classifications the classifications to set
   */
  public void setClassifications(Set<Classification> classifications) {
    Preconditions.checkNotNull(classifications);
    
    this.classifications = ImmutableSet.copyOf(classifications);
  }

  /**
   * @return the cellRelations
   */
  public Set<CellRelation> getCellRelations() {
    return cellRelations;
  }

  /**
   * @param cellRelations the cellRelations to set
   */
  public void setCellRelations(Set<CellRelation> cellRelations) {
    Preconditions.checkNotNull(cellRelations);
    
    this.cellRelations = ImmutableSet.copyOf(cellRelations);
  }

  /**
   * @return the columnRelations
   */
  public Set<ColumnRelation> getColumnRelations() {
    return columnRelations;
  }

  /**
   * @param columnRelations the columnRelations to set
   */
  public void setColumnRelations(Set<ColumnRelation> columnRelations) {
    Preconditions.checkNotNull(columnRelations);
    
    this.columnRelations = ImmutableSet.copyOf(columnRelations);
  }

  /**
   * @return the disambiguations
   */
  public Set<Disambiguation> getDisambiguations() {
    return disambiguations;
  }

  /**
   * @param disambiguations the disambiguations to set
   */
  public void setDisambiguations(Set<Disambiguation> disambiguations) {
    Preconditions.checkNotNull(disambiguations);
    
    this.disambiguations = ImmutableSet.copyOf(disambiguations);
  }

  /**
   * @return the ambiguities
   */
  public Set<Ambiguity> getAmbiguities() {
    return ambiguities;
  }

  /**
   * @param ambiguities the ambiguities to set
   */
  public void setAmbiguities(Set<Ambiguity> ambiguities) {
    Preconditions.checkNotNull(ambiguities);
    
    this.ambiguities = ImmutableSet.copyOf(ambiguities);
  }

    
}
