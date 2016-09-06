package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.jena.ext.com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionKeyJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionKeyJsonSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionToColumnPositionToCellRelationAnnotationMapMapDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionToColumnPositionToCellRelationAnnotationMapMapSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionToColumnRelationAnnotationMapDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionToColumnRelationAnnotationMapSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.RowPositionKeyJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.RowPositionKeyJsonSerializer;
import cz.cuni.mff.xrg.odalic.positions.CellRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Domain class {@link Result} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "result")
public final class ResultValue implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private ColumnPosition subjectColumnPosition;

  private List<HeaderAnnotation> headerAnnotations;

  private CellAnnotation[][] cellAnnotations;

  private Map<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotations;

  private Map<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> cellRelationAnnotations;

  public ResultValue() {
    subjectColumnPosition = null;
    headerAnnotations = ImmutableList.of();
    cellAnnotations = new CellAnnotation[0][0];;
    columnRelationAnnotations = ImmutableMap.of();
    cellRelationAnnotations = ImmutableMap.of();
  }

  public ResultValue(Result adaptee) {
    subjectColumnPosition = adaptee.getSubjectColumnPosition();
    headerAnnotations = adaptee.getHeaderAnnotations();
    cellAnnotations = adaptee.getCellAnnotations();

    initializeColumnRelationAnnotations(adaptee);
    initializeCellRelationAnnotations(adaptee);
  }

  private void initializeCellRelationAnnotations(Result adaptee) {
    cellRelationAnnotations = new HashMap<>();
    for (final Map.Entry<CellRelationPosition, CellRelationAnnotation> entry : adaptee
        .getCellRelationAnnotations().entrySet()) {
      final CellRelationPosition key = entry.getKey();
      final RowPosition row = key.getRowPosition();
      final ColumnRelationPosition columns = key.getColumnsPosition();

      final ColumnPosition firstColumn = columns.getFirst();
      final ColumnPosition secondColumn = columns.getSecond();

      final CellRelationAnnotation annotation = entry.getValue();

      final Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>> subMap =
          cellRelationAnnotations.get(row);
      if (subMap == null) {
        final Map<ColumnPosition, CellRelationAnnotation> newSubSubMap =
            Maps.newHashMap(ImmutableMap.of(secondColumn, annotation));
        final Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>> newSubMap =
            new HashMap<>();
        newSubMap.put(firstColumn, newSubSubMap);

        cellRelationAnnotations.put(row, newSubMap);
      } else {
        final Map<ColumnPosition, CellRelationAnnotation> subSubMap = subMap.get(firstColumn);
        if (subSubMap == null) {
          final Map<ColumnPosition, CellRelationAnnotation> newSubSubMap =
              Maps.newHashMap(ImmutableMap.of(secondColumn, annotation));
          subMap.put(firstColumn, newSubSubMap);
        } else {
          subSubMap.put(secondColumn, annotation);
        }
      }
    }
  }

  private void initializeColumnRelationAnnotations(Result adaptee) {
    columnRelationAnnotations = new HashMap<>();
    for (final Map.Entry<ColumnRelationPosition, ColumnRelationAnnotation> entry : adaptee
        .getColumnRelationAnnotations().entrySet()) {
      final ColumnRelationPosition key = entry.getKey();
      final ColumnPosition firstColumn = key.getFirst();
      final ColumnPosition secondColumn = key.getSecond();
      final ColumnRelationAnnotation annotation = entry.getValue();

      final Map<ColumnPosition, ColumnRelationAnnotation> subMap =
          columnRelationAnnotations.get(firstColumn);
      if (subMap == null) {
        columnRelationAnnotations.put(firstColumn,
            new HashMap<>(ImmutableMap.of(secondColumn, annotation)));
      } else {
        subMap.put(secondColumn, annotation);
      }
    }
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
    Preconditions.checkNotNull(subjectColumnPosition);

    this.subjectColumnPosition = subjectColumnPosition;
  }

  /**
   * @return the header annotations
   */
  @XmlElement
  public List<HeaderAnnotation> getHeaderAnnotations() {
    return headerAnnotations;
  }

  /**
   * @param headerAnnotations the header annotations to set
   */
  public void setHeaderAnnotations(List<HeaderAnnotation> headerAnnotations) {
    Preconditions.checkNotNull(headerAnnotations);

    this.headerAnnotations = ImmutableList.copyOf(headerAnnotations);
  }

  /**
   * @return the cell annotations
   */
  @XmlElement
  public CellAnnotation[][] getCellAnnotations() {
    return cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
  }

  /**
   * @param cellAnnotations the cell annotations to set
   */
  public void setCellAnnotations(CellAnnotation[][] cellAnnotations) {
    Preconditions.checkNotNull(cellAnnotations);

    this.cellAnnotations =
        cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
  }

  /**
   * @return the column relation Annotations
   */
  @XmlElement
  @JsonDeserialize(keyUsing = ColumnPositionKeyJsonDeserializer.class,
      contentUsing = ColumnPositionToColumnRelationAnnotationMapDeserializer.class)
  @JsonSerialize(keyUsing = ColumnPositionKeyJsonSerializer.class,
      contentUsing = ColumnPositionToColumnRelationAnnotationMapSerializer.class)
  public Map<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> getColumnRelationAnnotations() {
    return columnRelationAnnotations;
  }

  /**
   * @param columnRelationAnnotations the column relation annotations to set
   */
  public void setColumnRelationAnnotations(
      Map<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotations) {
    final ImmutableMap.Builder<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotationsBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> entry : columnRelationAnnotations
        .entrySet()) {
      columnRelationAnnotationsBuilder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
    }
    this.columnRelationAnnotations = columnRelationAnnotationsBuilder.build();
  }

  /**
   * @return the cell relation annotations
   */
  @XmlElement
  @JsonDeserialize(keyUsing = RowPositionKeyJsonDeserializer.class,
      contentUsing = ColumnPositionToColumnPositionToCellRelationAnnotationMapMapDeserializer.class)
  @JsonSerialize(keyUsing = RowPositionKeyJsonSerializer.class,
      contentUsing = ColumnPositionToColumnPositionToCellRelationAnnotationMapMapSerializer.class)
  public Map<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> getCellRelationAnnotations() {
    return cellRelationAnnotations;
  }

  /**
   * @param cellRelationAnnotations the cell relation annotations to set
   */
  public void setCellRelationAnnotations(
      Map<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> cellRelationAnnotations) {
    final ImmutableMap.Builder<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> cellRelationAnnotationsBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<RowPosition, Map<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>>> entry : cellRelationAnnotations
        .entrySet()) {
      final ImmutableMap.Builder<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>> innerMapBuilder =
          ImmutableMap.builder();
      for (final Map.Entry<ColumnPosition, Map<ColumnPosition, CellRelationAnnotation>> innerEntry : entry
          .getValue().entrySet()) {
        innerMapBuilder.put(innerEntry.getKey(), ImmutableMap.copyOf(innerEntry.getValue()));
      }

      cellRelationAnnotationsBuilder.put(entry.getKey(), innerMapBuilder.build());
    }
    this.cellRelationAnnotations = cellRelationAnnotationsBuilder.build();
  }

  /*
   * (non-Javadoc)
   * 
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
