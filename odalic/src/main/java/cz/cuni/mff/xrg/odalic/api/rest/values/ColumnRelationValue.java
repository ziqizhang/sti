package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnRelation;
import cz.cuni.mff.xrg.odalic.feedbacks.types.ColumnPosition;
import cz.cuni.mff.xrg.odalic.feedbacks.types.Entity;

@XmlRootElement(name = "columnRelation")
public final class ColumnRelationValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  @XmlElement
  private ColumnPosition subjectColumnPosition;
  
  @XmlElement
  private ColumnPosition objectColumnPosition;
  
  @XmlElement
  private Entity entity;
  
  public ColumnRelationValue() {}
  
  public ColumnRelationValue(ColumnRelation adaptee) {
    this.subjectColumnPosition = adaptee.getSubjectColumnPosition();
    this.objectColumnPosition = adaptee.getObjectColumnPosition();
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
  public void setSubjectColumnPosition(ColumnPosition subjectColumnPosition) {
    Preconditions.checkNotNull(subjectColumnPosition);
    
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
        + ", entity=" + entity + "]";
  }
}
