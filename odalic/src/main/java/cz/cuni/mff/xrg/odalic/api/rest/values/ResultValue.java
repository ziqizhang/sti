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
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionKeyJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionKeyJsonSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionToColumnRelationAnnotationMapDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionToColumnRelationAnnotationMapSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonSerializer;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
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

  @XmlElement
  @JsonDeserialize(keyUsing = KnowledgeBaseKeyJsonDeserializer.class)
  @JsonSerialize(keyUsing = KnowledgeBaseKeyJsonSerializer.class)
  private Map<KnowledgeBase, ColumnPosition> subjectColumnPositions;

  @XmlElement
  private List<HeaderAnnotation> headerAnnotations;

  @XmlElement
  private CellAnnotation[][] cellAnnotations;

  @XmlElement
  @JsonDeserialize(keyUsing = ColumnPositionKeyJsonDeserializer.class,
      contentUsing = ColumnPositionToColumnRelationAnnotationMapDeserializer.class)
  @JsonSerialize(keyUsing = ColumnPositionKeyJsonSerializer.class,
      contentUsing = ColumnPositionToColumnRelationAnnotationMapSerializer.class)
  private Map<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotations;

  public ResultValue() {
    subjectColumnPositions = ImmutableMap.of();
    headerAnnotations = ImmutableList.of();
    cellAnnotations = new CellAnnotation[0][0];;
    columnRelationAnnotations = ImmutableMap.of();
  }

  public ResultValue(Result adaptee) {
    subjectColumnPositions = adaptee.getSubjectColumnPositions();
    headerAnnotations = adaptee.getHeaderAnnotations();
    cellAnnotations = adaptee.getCellAnnotations();

    initializeColumnRelationAnnotations(adaptee);
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
  @Nullable
  public Map<KnowledgeBase, ColumnPosition> getSubjectColumnPositions() {
    return subjectColumnPositions;
  }

  /**
   * @param subjectColumnPositions the subject column position to set
   */
  public void setSubjectColumnPosition(Map<? extends KnowledgeBase, ? extends ColumnPosition> subjectColumnPositions) {
    Preconditions.checkNotNull(subjectColumnPositions);

    this.subjectColumnPositions = ImmutableMap.copyOf(subjectColumnPositions);
  }

  /**
   * @return the header annotations
   */
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
  public Map<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> getColumnRelationAnnotations() {
    return columnRelationAnnotations;
  }

  /**
   * @param columnRelationAnnotations the column relation annotations to set
   */
  public void setColumnRelationAnnotations(
      Map<? extends ColumnPosition, Map<? extends ColumnPosition, ? extends ColumnRelationAnnotation>> columnRelationAnnotations) {
    final ImmutableMap.Builder<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotationsBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends ColumnPosition, Map<? extends ColumnPosition, ? extends ColumnRelationAnnotation>> entry : columnRelationAnnotations
        .entrySet()) {
      columnRelationAnnotationsBuilder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
    }
    this.columnRelationAnnotations = columnRelationAnnotationsBuilder.build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ResultValue [subjectColumnPositions=" + subjectColumnPositions + ", headerAnnotations="
        + headerAnnotations + ", cellAnnotations=" + Arrays.toString(cellAnnotations)
        + ", columnRelationAnnotations=" + columnRelationAnnotations + "]";
  }
}
