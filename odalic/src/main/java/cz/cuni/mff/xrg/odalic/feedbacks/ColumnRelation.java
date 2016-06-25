package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;

@XmlRootElement(name = "columnRelation")
public final class ColumnRelation implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  @XmlElement
  private final ColumnRelationPosition position;

  @XmlElement
  private final ColumnRelationAnnotation annotation;

  @SuppressWarnings("unused")
  private ColumnRelation() {
    position = null;
    annotation = null;
  }
  
  /**
   * @param subjectColumnPosition
   * @param objectColumnPosition
   * @param entity
   */
  public ColumnRelation(ColumnRelationPosition position, ColumnRelationAnnotation annotation) {
    Preconditions.checkNotNull(position);
    Preconditions.checkNotNull(annotation);

    this.position = position;
    this.annotation = annotation;
  }

  /**
   * @return the position
   */
  public ColumnRelationPosition getPosition() {
    return position;
  }

  /**
   * @return the annotation
   */
  public ColumnRelationAnnotation getAnnotation() {
    return annotation;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
    result = prime * result + ((position == null) ? 0 : position.hashCode());
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
    ColumnRelation other = (ColumnRelation) obj;
    if (annotation == null) {
      if (other.annotation != null) {
        return false;
      }
    } else if (!annotation.equals(other.annotation)) {
      return false;
    }
    if (position == null) {
      if (other.position != null) {
        return false;
      }
    } else if (!position.equals(other.position)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnRelation [position=" + position + ", annotation=" + annotation + "]";
  }
}
