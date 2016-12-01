package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.FeedbackAdapter;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import jersey.repackaged.com.google.common.collect.ImmutableMap;

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

  private final Map<KnowledgeBase, ColumnPosition> subjectColumnPositions;

  private final Set<ColumnIgnore> columnIgnores;

  private final Set<Classification> classifications;

  private final Set<ColumnAmbiguity> columnAmbiguities;

  private final Set<Ambiguity> ambiguities;

  private final Set<Disambiguation> disambiguations;

  private final Set<ColumnRelation> columnRelations;


  /**
   * Creates empty feedback.
   */
  public Feedback() {
    this.subjectColumnPositions = ImmutableMap.of();
    this.columnIgnores = ImmutableSet.of();
    this.columnAmbiguities = ImmutableSet.of();
    this.classifications = ImmutableSet.of();
    this.columnRelations = ImmutableSet.of();
    this.disambiguations = ImmutableSet.of();
    this.ambiguities = ImmutableSet.of();
  }

  /**
   * Creates feedback.
   * 
   * @param subjectColumnPositions positions of the subject columns
   * @param columnIgnores ignored columns
   * @param columnAmbiguities columns whose cells will not be disambiguated
   * @param classifications classification hints for columns
   * @param columnRelations hints with relation between columns
   * @param disambiguations custom disambiguations
   * @param ambiguities hints for cells to be left ambiguous
   */
  public Feedback(Map<? extends KnowledgeBase, ? extends ColumnPosition> subjectColumnPositions,
      Set<? extends ColumnIgnore> columnIgnores, Set<? extends ColumnAmbiguity> columnAmbiguities,
      Set<? extends Classification> classifications, Set<? extends ColumnRelation> columnRelations,
      Set<? extends Disambiguation> disambiguations, Set<? extends Ambiguity> ambiguities) {
    Preconditions.checkNotNull(columnIgnores);
    Preconditions.checkNotNull(columnAmbiguities);
    Preconditions.checkNotNull(classifications);
    Preconditions.checkNotNull(columnRelations);
    Preconditions.checkNotNull(disambiguations);
    Preconditions.checkNotNull(ambiguities);

    this.subjectColumnPositions = ImmutableMap.copyOf(subjectColumnPositions);
    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
    this.classifications = ImmutableSet.copyOf(classifications);
    this.columnRelations = ImmutableSet.copyOf(columnRelations);
    this.disambiguations = ImmutableSet.copyOf(disambiguations);
    this.ambiguities = ImmutableSet.copyOf(ambiguities);

    this.checkConflicts();
  }

  /**
   * @return the subject column position
   */
  @Nullable
  public Map<KnowledgeBase, ColumnPosition> getSubjectColumnPositions() {
    return subjectColumnPositions;
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
    result = prime * result + ((classifications == null) ? 0 : classifications.hashCode());
    result = prime * result + ((columnAmbiguities == null) ? 0 : columnAmbiguities.hashCode());
    result = prime * result + ((columnIgnores == null) ? 0 : columnIgnores.hashCode());
    result = prime * result + ((columnRelations == null) ? 0 : columnRelations.hashCode());
    result = prime * result + ((disambiguations == null) ? 0 : disambiguations.hashCode());
    result =
        prime * result + ((subjectColumnPositions == null) ? 0 : subjectColumnPositions.hashCode());
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
    if (subjectColumnPositions == null) {
      if (other.subjectColumnPositions != null) {
        return false;
      }
    } else if (!subjectColumnPositions.equals(other.subjectColumnPositions)) {
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
    return "Feedback [subjectColumnPositions=" + subjectColumnPositions + ", columnIgnores="
        + columnIgnores + ", columnAmbiguities=" + columnAmbiguities + ", classifications="
        + classifications + ", columnRelations=" + columnRelations + ", disambiguations="
        + disambiguations + ", ambiguities=" + ambiguities + "]";
  }

  private void checkConflicts() {
    // check the conflict when ignore columns contain the subject column
    if (subjectColumnPositions == null) {
      return;
    }

    for (KnowledgeBase base : subjectColumnPositions.keySet()) {
      ColumnPosition subjCol = subjectColumnPositions.get(base);
      if (subjCol == null) {
        return;
      }

      if (columnIgnores.stream().anyMatch(e -> e.getPosition().getIndex() == subjCol.getIndex())) {
        throw new IllegalArgumentException("The column (position: " + subjCol.getIndex() +
            ") which is ignored does not have to be a subject column.");
      }

      for (Classification classification : classifications) {
        if (classification.getPosition().getIndex() == subjCol.getIndex() &&
            classification.getAnnotation().getChosen().get(base) != null &&
            classification.getAnnotation().getChosen().get(base).isEmpty()) {
          throw new IllegalArgumentException("The column (position: " + subjCol.getIndex() +
              ") which has empty chosen classification set (for " + base.getName() +
              " KB) does not have to be a subject column (for that KB).");
        }
      }
    }
  }
}
