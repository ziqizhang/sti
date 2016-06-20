package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.types.ColumnPosition;
import cz.cuni.mff.xrg.odalic.feedbacks.types.Entity;

@XmlRootElement(name = "columnRelation")
public final class ColumnRelation implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  @XmlElement
  private final ColumnPosition subjectColumnPosition;

  @XmlElement
  private final ColumnPosition objectColumnPosition;

  @XmlElement
  private final Entity entity;

  @SuppressWarnings("unused")
  private ColumnRelation() {
    subjectColumnPosition = null;
    objectColumnPosition = null;
    entity = null;
  }
  
  /**
   * @param subjectColumnPosition
   * @param objectColumnPosition
   * @param entity
   */
  public ColumnRelation(ColumnPosition subjectColumnPosition, ColumnPosition objectColumnPosition,
      Entity entity) {
    Preconditions.checkNotNull(subjectColumnPosition);
    Preconditions.checkNotNull(objectColumnPosition);
    Preconditions.checkNotNull(entity);

    this.subjectColumnPosition = subjectColumnPosition;
    this.objectColumnPosition = objectColumnPosition;
    this.entity = entity;
  }

  /**
   * @return the subjectColumnPosition
   */
  public ColumnPosition getSubjectColumnPosition() {
    return subjectColumnPosition;
  }

  /**
   * @return the objectColumnPosition
   */
  public ColumnPosition getObjectColumnPosition() {
    return objectColumnPosition;
  }

  /**
   * @return the entity
   */
  public Entity getEntity() {
    return entity;
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
    result = prime * result + ((entity == null) ? 0 : entity.hashCode());
    result =
        prime * result + ((objectColumnPosition == null) ? 0 : objectColumnPosition.hashCode());
    result =
        prime * result + ((subjectColumnPosition == null) ? 0 : subjectColumnPosition.hashCode());
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
    ColumnRelation other = (ColumnRelation) obj;
    if (entity == null) {
      if (other.entity != null) {
        return false;
      }
    } else if (!entity.equals(other.entity)) {
      return false;
    }
    if (objectColumnPosition == null) {
      if (other.objectColumnPosition != null) {
        return false;
      }
    } else if (!objectColumnPosition.equals(other.objectColumnPosition)) {
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
    return "CellRelation [subjectColumnPosition=" + subjectColumnPosition
        + ", objectColumnPosition=" + objectColumnPosition + ", entity=" + entity + "]";
  }
}
