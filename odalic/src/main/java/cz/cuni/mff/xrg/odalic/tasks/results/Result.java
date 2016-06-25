package cz.cuni.mff.xrg.odalic.tasks.results;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.jena.ext.com.google.common.collect.ImmutableMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import cz.cuni.mff.xrg.odalic.positions.CellRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;

@XmlRootElement(name = "result")
public class Result implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  @XmlElement
  private final ColumnPosition subjectColumnPosition;
  
  @XmlElement
  private final List<HeaderAnnotation> headerAnnotations;
  
  @XmlElement
  private final CellAnnotation[][] cellAnnotations;
  
  @XmlElement
  private final Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelationAnnotations;
  
  @XmlElement
  private final Map<CellRelationPosition, CellRelationAnnotation> cellRelationAnnotations;

  @SuppressWarnings("unused")
  private Result() {
    subjectColumnPosition = null;
    headerAnnotations = ImmutableList.of();
    cellAnnotations = new CellAnnotation[0][0];;
    columnRelationAnnotations = ImmutableMap.of();
    cellRelationAnnotations = ImmutableMap.of();
  }
  
  /**
   * @param subjectColumnPosition
   * @param headerAnnotations
   * @param cellAnnotations
   * @param columnRelationAnnotations
   * @param cellRelationAnnotations
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
    checkMandatory(headerAnnotations, cellAnnotations, columnRelationAnnotations,
        cellRelationAnnotations);
    
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
    checkMandatory(headerAnnotations, cellAnnotations, columnRelationAnnotations,
        cellRelationAnnotations);
    
    this.subjectColumnPosition = subjectColumnPosition;
    this.headerAnnotations = ImmutableList.copyOf(headerAnnotations);
    this.cellAnnotations = cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
    this.columnRelationAnnotations = ImmutableMap.copyOf(columnRelationAnnotations);
    this.cellRelationAnnotations = ImmutableMap.copyOf(cellRelationAnnotations);
  }

  /**
   * @return the subjectColumnPosition
   */
  @Nullable
  public ColumnPosition getSubjectColumnPosition() {
    return subjectColumnPosition;
  }

  /**
   * @return the headerAnnotations
   */
  public List<HeaderAnnotation> getHeaderAnnotations() {
    return headerAnnotations;
  }

  /**
   * @return the cellAnnotations
   */
  public CellAnnotation[][] getCellAnnotations() {
    return cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
  }

  /**
   * @return the columnRelationAnnotations
   */
  public Map<ColumnRelationPosition, ColumnRelationAnnotation> getColumnRelationAnnotations() {
    return columnRelationAnnotations;
  }

  /**
   * @return the cellRelationAnnotations
   */
  public Map<CellRelationPosition, CellRelationAnnotation> getCellRelationAnnotations() {
    return cellRelationAnnotations;
  }

  /* (non-Javadoc)
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

  /* (non-Javadoc)
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
