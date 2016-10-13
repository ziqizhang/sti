package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  private Map<KnowledgeBase, ColumnPosition> subjectColumnPositions;

  private List<HeaderAnnotation> headerAnnotations;

  private CellAnnotation[][] cellAnnotations;

  private Map<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotations;
  
  private List<String> warnings;

  public ResultValue() {
    subjectColumnPositions = ImmutableMap.of();
    headerAnnotations = ImmutableList.of();
    cellAnnotations = new CellAnnotation[0][0];;
    columnRelationAnnotations = ImmutableMap.of();
    warnings = ImmutableList.of();
  }

  public ResultValue(Result adaptee) {
    subjectColumnPositions = adaptee.getSubjectColumnPositions();
    headerAnnotations = adaptee.getHeaderAnnotations();
    cellAnnotations = adaptee.getCellAnnotations();
    warnings = adaptee.getWarnings();

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
  @XmlElement
  @JsonDeserialize(keyUsing = KnowledgeBaseKeyJsonDeserializer.class)
  @JsonSerialize(keyUsing = KnowledgeBaseKeyJsonSerializer.class)
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
      Map<? extends ColumnPosition, Map<? extends ColumnPosition, ? extends ColumnRelationAnnotation>> columnRelationAnnotations) {
    final ImmutableMap.Builder<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotationsBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends ColumnPosition, Map<? extends ColumnPosition, ? extends ColumnRelationAnnotation>> entry : columnRelationAnnotations
        .entrySet()) {
      columnRelationAnnotationsBuilder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
    }
    this.columnRelationAnnotations = columnRelationAnnotationsBuilder.build();
  }
  
  /**
   * @return the warnings
   */
  @XmlElement
  public List<String> getWarnings() {
    return warnings;
  }

  /**
   * @param warnings the warnings to set
   */
  public void setWarnings(List<String> warnings) {
    Preconditions.checkNotNull(warnings);

    this.warnings = ImmutableList.copyOf(warnings);
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
        + ", columnRelationAnnotations=" + columnRelationAnnotations + ", warnings=" +  warnings + "]";
  }
}
