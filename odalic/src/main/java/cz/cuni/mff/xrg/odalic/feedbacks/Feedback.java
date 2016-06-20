package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.feedbacks.types.ColumnPosition;

@XmlRootElement(name = "feedback")
public final class Feedback implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  @XmlElement
  private final ColumnPosition subjectColumnPosition;

  @XmlElement
  private final Set<ColumnIgnore> columnIgnores;
  
  @XmlElement
  private final Set<ColumnAmbiguity> columnAmbiguities;
  
  @XmlElement
  private final Set<Classification> classifications;
  
  @XmlElement
  private final Set<CellRelation> cellRelations;
  
  @XmlElement
  private final Set<ColumnRelation> columnRelations;
  
  @XmlElement
  private final Set<Disambiguation> disambiguations;
  
  @XmlElement
  private final Set<Ambiguity> ambiguities;

  @SuppressWarnings("unused")
  private Feedback() {
    this.subjectColumnPosition = null;
    this.columnIgnores = ImmutableSet.of();
    this.columnAmbiguities = ImmutableSet.of();
    this.classifications = ImmutableSet.of();
    this.cellRelations = ImmutableSet.of();
    this.columnRelations = ImmutableSet.of();
    this.disambiguations = ImmutableSet.of();
    this.ambiguities = ImmutableSet.of();
  }
  
  /**
   * @param columnIgnores
   * @param columnAmbiguities
   * @param classifications
   * @param cellRelations
   * @param columnRelations
   * @param disambiguations
   * @param ambiguities
   */
  public Feedback(Set<? extends ColumnIgnore> columnIgnores,
      Set<? extends ColumnAmbiguity> columnAmbiguities, Set<? extends Classification> classifications,
      Set<? extends CellRelation> cellRelations, Set<? extends ColumnRelation> columnRelations,
      Set<? extends Disambiguation> disambiguations, Set<? extends Ambiguity> ambiguities) {
    checkMandatory(columnIgnores, columnAmbiguities, classifications, cellRelations,
        columnRelations, disambiguations, ambiguities);
    
    this.subjectColumnPosition = null;
    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
    this.classifications = ImmutableSet.copyOf(classifications);
    this.cellRelations = ImmutableSet.copyOf(cellRelations);
    this.columnRelations = ImmutableSet.copyOf(columnRelations);
    this.disambiguations = ImmutableSet.copyOf(disambiguations);
    this.ambiguities = ImmutableSet.copyOf(ambiguities);
  }

  private static void checkMandatory(Set<? extends ColumnIgnore> columnIgnores,
      Set<? extends ColumnAmbiguity> columnAmbiguities,
      Set<? extends Classification> classifications, Set<? extends CellRelation> cellRelations,
      Set<? extends ColumnRelation> columnRelations, Set<? extends Disambiguation> disambiguations,
      Set<? extends Ambiguity> ambiguities) {
    Preconditions.checkNotNull(columnIgnores);
    Preconditions.checkNotNull(columnAmbiguities);
    Preconditions.checkNotNull(classifications);
    Preconditions.checkNotNull(cellRelations);
    Preconditions.checkNotNull(columnRelations);
    Preconditions.checkNotNull(disambiguations);
    Preconditions.checkNotNull(ambiguities);
  }
  
  /**
   * @param subjectColumnIndex
   * @param columnIgnores
   * @param columnAmbiguities
   * @param classifications
   * @param cellRelations
   * @param columnRelations
   * @param disambiguations
   * @param ambiguities
   */
  public Feedback(ColumnPosition subjectColumnPosition, Set<? extends ColumnIgnore> columnIgnores,
      Set<? extends ColumnAmbiguity> columnAmbiguities, Set<? extends Classification> classifications,
      Set<? extends CellRelation> cellRelations, Set<? extends ColumnRelation> columnRelations,
      Set<? extends Disambiguation> disambiguations, Set<? extends Ambiguity> ambiguities) {
    Preconditions.checkNotNull(subjectColumnPosition);
    checkMandatory(columnIgnores, columnAmbiguities, classifications, cellRelations, columnRelations, disambiguations, ambiguities);
    
    this.subjectColumnPosition = subjectColumnPosition;
    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
    this.classifications = ImmutableSet.copyOf(classifications);
    this.cellRelations = ImmutableSet.copyOf(cellRelations);
    this.columnRelations = ImmutableSet.copyOf(columnRelations);
    this.disambiguations = ImmutableSet.copyOf(disambiguations);
    this.ambiguities = ImmutableSet.copyOf(ambiguities);
  }

  /**
   * @return the subjectColumnPosition
   */
  @Nullable
  public ColumnPosition getSubjectColumnPosition() {
    return subjectColumnPosition;
  }

  /**
   * @return the columnIgnores
   */
  public Set<ColumnIgnore> getColumnIgnores() {
    return ImmutableSet.copyOf(columnIgnores);
  }

  /**
   * @return the columnAmbiguities
   */
  public Set<ColumnAmbiguity> getColumnAmbiguities() {
    return ImmutableSet.copyOf(columnAmbiguities);
  }

  /**
   * @return the classifications
   */
  public Set<Classification> getClassifications() {
    return ImmutableSet.copyOf(classifications);
  }

  /**
   * @return the cellRelations
   */
  public Set<CellRelation> getCellRelations() {
    return ImmutableSet.copyOf(cellRelations);
  }

  /**
   * @return the columnRelations
   */
  public Set<ColumnRelation> getColumnRelations() {
    return ImmutableSet.copyOf(columnRelations);
  }

  /**
   * @return the disambiguations
   */
  public Set<Disambiguation> getDisambiguations() {
    return ImmutableSet.copyOf(disambiguations);
  }

  /**
   * @return the ambiguities
   */
  public Set<Ambiguity> getAmbiguities() {
    return ImmutableSet.copyOf(ambiguities);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ambiguities == null) ? 0 : ambiguities.hashCode());
    result = prime * result + ((cellRelations == null) ? 0 : cellRelations.hashCode());
    result = prime * result + ((classifications == null) ? 0 : classifications.hashCode());
    result = prime * result + ((columnAmbiguities == null) ? 0 : columnAmbiguities.hashCode());
    result = prime * result + ((columnIgnores == null) ? 0 : columnIgnores.hashCode());
    result = prime * result + ((columnRelations == null) ? 0 : columnRelations.hashCode());
    result = prime * result + ((disambiguations == null) ? 0 : disambiguations.hashCode());
    result =
        prime * result + ((subjectColumnPosition == null) ? 0 : subjectColumnPosition.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Feedback other = (Feedback) obj;
    if (ambiguities == null) {
      if (other.ambiguities != null) {
        return false;
      }
    } else if (!ambiguities.equals(other.ambiguities)) {
      return false;
    }
    if (cellRelations == null) {
      if (other.cellRelations != null) {
        return false;
      }
    } else if (!cellRelations.equals(other.cellRelations)) {
      return false;
    }
    if (classifications == null) {
      if (other.classifications != null) {
        return false;
      }
    } else if (!classifications.equals(other.classifications)) {
      return false;
    }
    if (columnAmbiguities == null) {
      if (other.columnAmbiguities != null) {
        return false;
      }
    } else if (!columnAmbiguities.equals(other.columnAmbiguities)) {
      return false;
    }
    if (columnIgnores == null) {
      if (other.columnIgnores != null) {
        return false;
      }
    } else if (!columnIgnores.equals(other.columnIgnores)) {
      return false;
    }
    if (columnRelations == null) {
      if (other.columnRelations != null) {
        return false;
      }
    } else if (!columnRelations.equals(other.columnRelations)) {
      return false;
    }
    if (disambiguations == null) {
      if (other.disambiguations != null) {
        return false;
      }
    } else if (!disambiguations.equals(other.disambiguations)) {
      return false;
    }
    if (subjectColumnPosition == null) {
      if (other.subjectColumnPosition != null) {
        return false;
      }
    } else if (!subjectColumnPosition.equals(other.subjectColumnPosition)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Feedback [subjectColumnPosition=" + subjectColumnPosition + ", columnIgnores="
        + columnIgnores + ", columnAmbiguities=" + columnAmbiguities + ", classifications="
        + classifications + ", cellRelations=" + cellRelations + ", columnRelations="
        + columnRelations + ", disambiguations=" + disambiguations + ", ambiguities=" + ambiguities
        + "]";
  }
}
