package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonSerializer;
import cz.cuni.mff.xrg.odalic.feedbacks.Ambiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.Classification;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnAmbiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnRelation;
import cz.cuni.mff.xrg.odalic.feedbacks.Disambiguation;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Domain class {@link Feedback} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "feedback")
public final class FeedbackValue implements Serializable {

  private static final long serialVersionUID = -7968455903789693405L;

  private Map<KnowledgeBase, ColumnPosition> subjectColumnPositions;

  private Set<ColumnIgnore> columnIgnores;

  private Set<ColumnAmbiguity> columnAmbiguities;

  private Set<Classification> classifications;

  private Set<ColumnRelation> columnRelations;

  private Set<Disambiguation> disambiguations;

  private Set<Ambiguity> ambiguities;

  public FeedbackValue() {
    subjectColumnPositions = ImmutableMap.of();
    columnIgnores = ImmutableSet.of();
    columnAmbiguities = ImmutableSet.of();
    classifications = ImmutableSet.of();
    columnRelations = ImmutableSet.of();
    disambiguations = ImmutableSet.of();
    ambiguities = ImmutableSet.of();
  }

  public FeedbackValue(Feedback adaptee) {
    subjectColumnPositions = adaptee.getSubjectColumnPositions();
    columnIgnores = adaptee.getColumnIgnores();
    columnAmbiguities = adaptee.getColumnAmbiguities();
    classifications = adaptee.getClassifications();
    columnRelations = adaptee.getColumnRelations();
    disambiguations = adaptee.getDisambiguations();
    ambiguities = adaptee.getAmbiguities();
  }

  /**
   * @return the subject column positions
   */
  @XmlElement
  @JsonDeserialize(keyUsing = KnowledgeBaseKeyJsonDeserializer.class)
  @JsonSerialize(keyUsing = KnowledgeBaseKeyJsonSerializer.class)
  public Map<KnowledgeBase, ColumnPosition> getSubjectColumnPositions() {
    return subjectColumnPositions;
  }

  /**
   * @param subjectColumnPositions the subject column positions to set
   */
  public void setSubjectColumnPosition(Map<? extends KnowledgeBase, ? extends ColumnPosition> subjectColumnPositions) {
    Preconditions.checkNotNull(subjectColumnPositions);
    
    this.subjectColumnPositions = ImmutableMap.copyOf(subjectColumnPositions);
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FeedbackValue [subjectColumnPositions=" + subjectColumnPositions + ", columnIgnores="
        + columnIgnores + ", columnAmbiguities=" + columnAmbiguities + ", classifications="
        + classifications + ", columnRelations=" + columnRelations + ", disambiguations="
        + disambiguations + ", ambiguities=" + ambiguities + "]";
  }
}
