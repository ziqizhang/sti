package cz.cuni.mff.xrg.odalic.api.rest.values;

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
import cz.cuni.mff.xrg.odalic.positions.RowPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

@XmlRootElement(name = "result")
public class ResultValue implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  @XmlElement
  private ColumnPosition subjectColumnPosition;
  
  @XmlElement
  private List<HeaderAnnotation> headerAnnotations;
  
  @XmlElement
  private CellAnnotation[][] cellAnnotations;
  
  @XmlElement
  private Map<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotations;
  
  @XmlElement
  private Map<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> cellRelationAnnotations;

  @SuppressWarnings("unused")
  private ResultValue() {
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
  public ResultValue(Result adaptee) {
    subjectColumnPosition = adaptee.getSubjectColumnPosition();
    headerAnnotations = adaptee.getHeaderAnnotations();
    cellAnnotations = adaptee.getCellAnnotations();
    
    final ImmutableMap.Builder<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotationsBuilder = ImmutableMap.builder();
    for (final Map.Entry<ColumnRelationPosition, ColumnRelationAnnotation> entry : adaptee.getColumnRelationAnnotations().entrySet()) {
      final ColumnRelationPosition key = entry.getKey();
      final ColumnPosition firstColumn = key.getFirst();
      final ColumnPosition secondColumn = key.getSecond();
      final ColumnRelationAnnotation annotation = entry.getValue();
      
      columnRelationAnnotationsBuilder.put(firstColumn, ImmutableMap.of(secondColumn, annotation));
    }
    columnRelationAnnotations = columnRelationAnnotationsBuilder.build();
    
    final ImmutableMap.Builder<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> cellRelationAnnotationsBuilder = ImmutableMap.builder();
    for (final Map.Entry<CellRelationPosition, CellRelationAnnotation> entry : adaptee.getCellRelationAnnotations().entrySet()) {
      final CellRelationPosition key = entry.getKey();
      final RowPosition row = key.getRowPosition();
      final ColumnRelationPosition columns = key.getColumnsPosition();
      
      final ColumnPosition firstColumn = columns.getFirst();
      final ColumnPosition secondColumn = columns.getSecond();
      
      final CellRelationAnnotation annotation = entry.getValue();
      
      cellRelationAnnotationsBuilder.put(row, ImmutableMap.of(firstColumn, ImmutableMap.of(secondColumn, annotation)));
    }
    cellRelationAnnotations = cellRelationAnnotationsBuilder.build();
  }

  /**
   * @return the subjectColumnPosition
   */
  public ColumnPosition getSubjectColumnPosition() {
    return subjectColumnPosition;
  }

  /**
   * @param subjectColumnPosition the subjectColumnPosition to set
   */
  public void setSubjectColumnPosition(ColumnPosition subjectColumnPosition) {
    Preconditions.checkNotNull(subjectColumnPosition);
    
    this.subjectColumnPosition = subjectColumnPosition;
  }

  /**
   * @return the headerAnnotations
   */
  @Nullable
  public List<HeaderAnnotation> getHeaderAnnotations() {
    return headerAnnotations;
  }

  /**
   * @param headerAnnotations the headerAnnotations to set
   */
  public void setHeaderAnnotations(List<HeaderAnnotation> headerAnnotations) {
    Preconditions.checkNotNull(headerAnnotations);
    
    this.headerAnnotations = ImmutableList.copyOf(headerAnnotations);
  }

  /**
   * @return the cellAnnotations
   */
  @Nullable
  public CellAnnotation[][] getCellAnnotations() {
    return cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
  }

  /**
   * @param cellAnnotations the cellAnnotations to set
   */
  public void setCellAnnotations(CellAnnotation[][] cellAnnotations) {
    Preconditions.checkNotNull(cellAnnotations);
    
    this.cellAnnotations = cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
  }

  /**
   * @return the columnRelationAnnotations
   */
  @Nullable
  public Map<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> getColumnRelationAnnotations() {
    return columnRelationAnnotations;
  }

  /**
   * @param columnRelationAnnotations the columnRelationAnnotations to set
   */
  public void setColumnRelationAnnotations(
      Map<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotations) {
    final ImmutableMap.Builder<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotationsBuilder = ImmutableMap.builder();
    for (final Map.Entry<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> entry : columnRelationAnnotations.entrySet()) {
      columnRelationAnnotationsBuilder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
    }
    this.columnRelationAnnotations = columnRelationAnnotationsBuilder.build();
  }

  /**
   * @return the cellRelationAnnotations
   */
  @Nullable
  public Map<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> getCellRelationAnnotations() {
    return cellRelationAnnotations;
  }

  /**
   * @param cellRelationAnnotations the cellRelationAnnotations to set
   */
  public void setCellRelationAnnotations(
      Map<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> cellRelationAnnotations) {
    final ImmutableMap.Builder<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> cellRelationAnnotationsBuilder = ImmutableMap.builder();
    for (final Map.Entry<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> entry : cellRelationAnnotations.entrySet()) {
      final ImmutableMap.Builder<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>> innerMapBuilder = ImmutableMap.builder();
      for (final Map.Entry<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>> innerEntry : entry.getValue().entrySet()) {
        innerMapBuilder.put(innerEntry.getKey(), ImmutableMap.copyOf(innerEntry.getValue()));
      }
            
      cellRelationAnnotationsBuilder.put(entry.getKey(), innerMapBuilder.build());
    }
    this.cellRelationAnnotations = cellRelationAnnotationsBuilder.build();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ResultValue [subjectColumnPosition=" + subjectColumnPosition + ", headerAnnotations="
        + headerAnnotations + ", cellAnnotations=" + Arrays.toString(cellAnnotations)
        + ", columnRelationAnnotations=" + columnRelationAnnotations + ", cellRelationAnnotations="
        + cellRelationAnnotations + "]";
  }  
}
