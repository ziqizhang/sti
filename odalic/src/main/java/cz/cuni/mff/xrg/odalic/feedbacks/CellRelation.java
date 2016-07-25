package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.CellRelationAdapter;
import cz.cuni.mff.xrg.odalic.positions.CellRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;

/**
 * Cell relation hint.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(CellRelationAdapter.class)
public final class CellRelation implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private final CellRelationPosition position;
  
  private final CellRelationAnnotation annotation;

  /**
   * Creates new hint for discovered cell relations.
   * 
   * @param position cell position
   * @param annotation custom annotation
   */
  public CellRelation(CellRelationPosition position, CellRelationAnnotation annotation) {
    Preconditions.checkNotNull(position);
    Preconditions.checkNotNull(annotation);
    
    this.position = position;
    this.annotation = annotation;
  }

  /**
   * @return the position
   */
  public CellRelationPosition getPosition() {
    return position;
  }

  /**
   * @return the annotation
   */
  public CellRelationAnnotation getAnnotation() {
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
    CellRelation other = (CellRelation) obj;
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
    return "CellRelation [position=" + position + ", annotation=" + annotation + "]";
  }
}