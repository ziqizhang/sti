package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.FeedbackAdapter;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * User feedback for the result of annotating algorithm. Expresses also input constraints for the
 * next run.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(FeedbackAdapter.class)
public final class Feedback implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private final ColumnPosition subjectColumnPosition;

  private final Set<ColumnIgnore> columnIgnores;

  private final Set<Classification> classifications;

  private final Set<ColumnAmbiguity> columnAmbiguities;

  private final Set<Ambiguity> ambiguities;

  private final Set<Disambiguation> disambiguations;

  private final Set<CellRelation> cellRelations;

  private final Set<ColumnRelation> columnRelations;


  /**
   * Creates empty feedback.
   */
  public Feedback() {
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
   * Creates feedback.
   * 
   * @param subjectColumnPosition position of the subject column (optional)
   * @param columnIgnores ignored columns
   * @param columnAmbiguities columns whose cells will not be disambiguated
   * @param classifications classification hints for columns
   * @param cellRelations hints with relations between cells on the same rows
   * @param columnRelations hints with relation between columns
   * @param disambiguations custom disambiguations
   * @param ambiguities hints for cells to be left ambiguous
   */
  public Feedback(@Nullable ColumnPosition subjectColumnPosition,
      Set<? extends ColumnIgnore> columnIgnores, Set<? extends ColumnAmbiguity> columnAmbiguities,
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
   * @return the subject column position
   */
  @Nullable
  public ColumnPosition getSubjectColumnPosition() {
    return subjectColumnPosition;
  }

  /**
   * @return ignored columns
   */
  public Set<ColumnIgnore> getColumnIgnores() {
    return columnIgnores;
  }

  /**
   * @return ambiguous columns
   */
  public Set<ColumnAmbiguity> getColumnAmbiguities() {
    return columnAmbiguities;
  }

  /**
   * @return the classifications
   */
  public Set<Classification> getClassifications() {
    return classifications;
  }

  /**
   * @return the cell relations
   */
  public Set<CellRelation> getCellRelations() {
    return cellRelations;
  }

  /**
   * @return the column relations
   */
  public Set<ColumnRelation> getColumnRelations() {
    return columnRelations;
  }

  /**
   * @return the disambiguations
   */
  public Set<Disambiguation> getDisambiguations() {
    return disambiguations;
  }

  /**
   * @return the forced ambiguous cells
   */
  public Set<Ambiguity> getAmbiguities() {
    return ambiguities;
  }

  /*
   * (non-Javadoc)
   * 
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

  /*
   * (non-Javadoc)
   * 
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

  /*
   * (non-Javadoc)
   * 
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
