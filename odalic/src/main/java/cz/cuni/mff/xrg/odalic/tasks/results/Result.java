package cz.cuni.mff.xrg.odalic.tasks.results;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ResultAdapter;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * <p>
 * This class represents a partial result of the table annotation process.
 * </p>
 * 
 * <p>
 * It includes all the data necessary to produce the final triples and also serves as the base for
 * user-defined hints to the annotating algorithm.
 * </p>
 * 
 * <p>
 * Any benchmarking, debugging or temporary result data are not included.
 * </p>
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(ResultAdapter.class)
public class Result implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private final Map<KnowledgeBase, ColumnPosition> subjectColumnPositions;

  private final List<HeaderAnnotation> headerAnnotations;

  private final CellAnnotation[][] cellAnnotations;

  private final Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelationAnnotations;
  
  private final List<String> warnings;

  public Result(Map<? extends KnowledgeBase, ? extends ColumnPosition> subjectColumnPositions,
      List<? extends HeaderAnnotation> headerAnnotations, CellAnnotation[][] cellAnnotations,
      Map<? extends ColumnRelationPosition, ? extends ColumnRelationAnnotation> columnRelationAnnotations,
      List<? extends String> warnings) {
    Preconditions.checkNotNull(subjectColumnPositions);
    Preconditions.checkNotNull(headerAnnotations);
    Preconditions.checkNotNull(cellAnnotations);
    Preconditions.checkNotNull(columnRelationAnnotations);
    Preconditions.checkNotNull(warnings);
    Preconditions.checkArgument(!cz.cuni.mff.xrg.odalic.util.Arrays.containsNull(cellAnnotations));
    Preconditions.checkArgument(cz.cuni.mff.xrg.odalic.util.Arrays.isMatrix(cellAnnotations));

    this.subjectColumnPositions = ImmutableMap.copyOf(subjectColumnPositions);
    this.headerAnnotations = ImmutableList.copyOf(headerAnnotations);
    this.cellAnnotations =
        cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
    this.columnRelationAnnotations = ImmutableMap.copyOf(columnRelationAnnotations);
    this.warnings = ImmutableList.copyOf(warnings);
  }

  /**
   * @return the subject column positions
   */
  @Nullable
  public Map<KnowledgeBase, ColumnPosition> getSubjectColumnPositions() {
    return subjectColumnPositions;
  }

  /**
   * @return the header annotations
   */
  public List<HeaderAnnotation> getHeaderAnnotations() {
    return headerAnnotations;
  }

  /**
   * @return the cell annotations
   */
  public CellAnnotation[][] getCellAnnotations() {
    return cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
  }

  /**
   * @return the column relation annotations
   */
  public Map<ColumnRelationPosition, ColumnRelationAnnotation> getColumnRelationAnnotations() {
    return columnRelationAnnotations;
  }

  /**
   * @return the warnings in order of appearance
   */
  public List<String> getWarnings() {
    return this.warnings;
  }
  
  /**
   * Computes hash code based on all its parts.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.deepHashCode(cellAnnotations);
    result = prime * result
        + ((columnRelationAnnotations == null) ? 0 : columnRelationAnnotations.hashCode());
    result = prime * result + ((headerAnnotations == null) ? 0 : headerAnnotations.hashCode());
    result =
        prime * result + ((subjectColumnPositions == null) ? 0 : subjectColumnPositions.hashCode());
    return result;
  }

  /**
   * Compares to another object for equality (only another Result composed from equal parts passes).
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
    Result other = (Result) obj;
    if (!Arrays.deepEquals(cellAnnotations, other.cellAnnotations)) {
      return false;
    }
    if (columnRelationAnnotations == null) {
      if (other.columnRelationAnnotations != null) {
        return false;
      }
    } else if (!columnRelationAnnotations.equals(other.columnRelationAnnotations)) {
      return false;
    }
    if (headerAnnotations == null) {
      if (other.headerAnnotations != null) {
        return false;
      }
    } else if (!headerAnnotations.equals(other.headerAnnotations)) {
      return false;
    }
    if (subjectColumnPositions == null) {
      if (other.subjectColumnPositions != null) {
        return false;
      }
    } else if (!subjectColumnPositions.equals(other.subjectColumnPositions)) {
      return false;
    }
    if (warnings == null) {
      if (other.warnings != null) {
        return false;
      }
    } else if (!warnings.equals(other.warnings)) {
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
    return "Result [subjectColumnPositions=" + subjectColumnPositions + ", headerAnnotations="
        + headerAnnotations + ", cellAnnotations=" + Arrays.deepToString(cellAnnotations)
        + ", columnRelationAnnotations=" + columnRelationAnnotations + ", warnings=" + warnings + "]";
  }
}
