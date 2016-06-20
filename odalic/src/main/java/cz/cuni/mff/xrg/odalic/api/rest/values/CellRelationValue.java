package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.CellRelation;
import cz.cuni.mff.xrg.odalic.feedbacks.types.ColumnPosition;
import cz.cuni.mff.xrg.odalic.feedbacks.types.Entity;
import cz.cuni.mff.xrg.odalic.feedbacks.types.RowPosition;

@XmlRootElement(name = "cellRelation")
public final class CellRelationValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  @XmlElement
  private ColumnPosition subjectColumnPosition;
  
  @XmlElement
  private ColumnPosition objectColumnPosition;
  
  @XmlElement
  private RowPosition rowPosition;
  
  @XmlElement
  private Entity entity;
  
  public CellRelationValue() {}
  
  public CellRelationValue(CellRelation adaptee) {
    this.subjectColumnPosition = adaptee.getSubjectColumnPosition();
    this.objectColumnPosition = adaptee.getObjectColumnPosition();
    this.rowPosition = adaptee.getRowPosition();
    this.entity = adaptee.getEntity();
  }

  /**
   * @return the subjectColumnPosition
   */
  @Nullable
  public ColumnPosition getSubjectColumnPosition() {
    return subjectColumnPosition;
  }

  /**
   * @param subjectColumnPosition the subjectColumnPosition to set
   */
  public void setSubjectColumnPosition(@Nullable ColumnPosition subjectColumnPosition) {
    this.subjectColumnPosition = subjectColumnPosition;
  }

  /**
   * @return the objectColumnPosition
   */
  @Nullable
  public ColumnPosition getObjectColumnPosition() {
    return objectColumnPosition;
  }

  /**
   * @param objectColumnPosition the objectColumnPosition to set
   */
  public void setObjectColumnPosition(ColumnPosition objectColumnPosition) {
    Preconditions.checkNotNull(objectColumnPosition);
    
    this.objectColumnPosition = objectColumnPosition;
  }

  /**
   * @return the rowPosition
   */
  @Nullable
  public RowPosition getRowPosition() {
    return rowPosition;
  }

  /**
   * @param rowPosition the rowPosition to set
   */
  public void setRowPosition(RowPosition rowPosition) {
    Preconditions.checkNotNull(rowPosition);
    
    this.rowPosition = rowPosition;
  }

  /**
   * @return the entity
   */
  @Nullable
  public Entity getEntity() {
    return entity;
  }

  /**
   * @param entity the entity to set
   */
  public void setEntity(Entity entity) {
    Preconditions.checkNotNull(entity);
    
    this.entity = entity;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CellRelationValue [subjectColumnPosition=" + subjectColumnPosition
        + ", objectColumnPosition=" + objectColumnPosition + ", rowPosition="
        + rowPosition + ", entity=" + entity + "]";
  }
}
