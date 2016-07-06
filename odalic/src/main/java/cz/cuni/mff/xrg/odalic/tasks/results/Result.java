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
import cz.cuni.mff.xrg.odalic.positions.CellRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;

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

  private final ColumnPosition subjectColumnPosition;

  private final List<HeaderAnnotation> headerAnnotations;

  private final CellAnnotation[][] cellAnnotations;

  private final Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelationAnnotations;

  private final Map<CellRelationPosition, CellRelationAnnotation> cellRelationAnnotations;

  /**
   * Creates new annotation result representation.
   * 
   * @param subjectColumnPosition suggested position of the subject column
   * @param headerAnnotations suggested header annotations
   * @param cellAnnotations suggested cell annotations
   * @param columnRelationAnnotations suggested annotations for the relations between two columns
   * @param cellRelationAnnotations suggested annotation for relations existing between two cells at
   *        the same row
   */
  public Result(List<HeaderAnnotation> headerAnnotations,
      CellAnnotation[][] cellAnnotations,
      Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelationAnnotations,
      Map<CellRelationPosition, CellRelationAnnotation> cellRelationAnnotations) {
    checkMandatory(headerAnnotations, cellAnnotations, columnRelationAnnotations,
        cellRelationAnnotations);
    
    this.subjectColumnPosition = null;
    this.headerAnnotations = ImmutableList.copyOf(headerAnnotations);
    this.cellAnnotations = cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
    this.columnRelationAnnotations = ImmutableMap.copyOf(columnRelationAnnotations);
    this.cellRelationAnnotations = ImmutableMap.copyOf(cellRelationAnnotations);
  }

  private static void checkMandatory(List<HeaderAnnotation> headerAnnotations,
      CellAnnotation[][] cellAnnotations,
      Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelationAnnotations,
      Map<CellRelationPosition, CellRelationAnnotation> cellRelationAnnotations) {

    Preconditions.checkArgument(!cz.cuni.mff.xrg.odalic.util.Arrays.containsNull(cellAnnotations));
    Preconditions.checkArgument(cz.cuni.mff.xrg.odalic.util.Arrays.isMatrix(cellAnnotations));
  }
  
  /**
   * @param subjectColumnPosition
   * @param headerAnnotations
   * @param cellAnnotations
   * @param columnRelationAnnotations
   * @param cellRelationAnnotations
   */
  public Result(ColumnPosition subjectColumnPosition,
      List<HeaderAnnotation> headerAnnotations,
      CellAnnotation[][] cellAnnotations,
      Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelationAnnotations,
      Map<CellRelationPosition, CellRelationAnnotation> cellRelationAnnotations) {
    Preconditions.checkNotNull(subjectColumnPosition);
    Preconditions.checkNotNull(headerAnnotations);
    Preconditions.checkNotNull(cellAnnotations);
    Preconditions.checkNotNull(columnRelationAnnotations);
    Preconditions.checkNotNull(cellRelationAnnotations);
    Preconditions.checkArgument(!cz.cuni.mff.xrg.odalic.util.Arrays.containsNull(cellAnnotations));
    Preconditions.checkArgument(cz.cuni.mff.xrg.odalic.util.Arrays.isMatrix(cellAnnotations));

    this.subjectColumnPosition = subjectColumnPosition;
    this.headerAnnotations = ImmutableList.copyOf(headerAnnotations);
    this.cellAnnotations =
        cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
    this.columnRelationAnnotations = ImmutableMap.copyOf(columnRelationAnnotations);
    this.cellRelationAnnotations = ImmutableMap.copyOf(cellRelationAnnotations);
  }

  /**
   * @return the subject column position
   */
  @Nullable
  public ColumnPosition getSubjectColumnPosition() {
    return subjectColumnPosition;
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
   * @return the cell relation annotations
   */
  public Map<CellRelationPosition, CellRelationAnnotation> getCellRelationAnnotations() {
    return cellRelationAnnotations;
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
        + ((cellRelationAnnotations == null) ? 0 : cellRelationAnnotations.hashCode());
    result = prime * result
        + ((columnRelationAnnotations == null) ? 0 : columnRelationAnnotations.hashCode());
    result = prime * result + ((headerAnnotations == null) ? 0 : headerAnnotations.hashCode());
    result =
        prime * result + ((subjectColumnPosition == null) ? 0 : subjectColumnPosition.hashCode());
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
    if (cellRelationAnnotations == null) {
      if (other.cellRelationAnnotations != null) {
        return false;
      }
    } else if (!cellRelationAnnotations.equals(other.cellRelationAnnotations)) {
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
    return "Result [subjectColumnPosition=" + subjectColumnPosition + ", headerAnnotations="
        + headerAnnotations + ", cellAnnotations=" + Arrays.deepToString(cellAnnotations)
        + ", columnRelationAnnotations=" + columnRelationAnnotations + ", cellRelationAnnotations="
        + cellRelationAnnotations + "]";
  }
}
