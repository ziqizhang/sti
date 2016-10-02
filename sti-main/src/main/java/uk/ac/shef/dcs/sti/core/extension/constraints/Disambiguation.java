package uk.ac.shef.dcs.sti.core.extension.constraints;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.sti.core.extension.annotations.CellAnnotation;
import uk.ac.shef.dcs.sti.core.extension.positions.CellPosition;

@Immutable
public final class Disambiguation implements Serializable {

  private static final long serialVersionUID = -5229197850609921790L;

  private final CellPosition position;
  
  private final CellAnnotation annotation;

  /**
   * Creates new cell disambiguation hint.
   * 
   * @param position cell position
   * @param annotation hinted cell annotation
   */
  public Disambiguation(CellPosition position, CellAnnotation annotation) {
    Preconditions.checkNotNull(position);
    Preconditions.checkNotNull(annotation);
        
    this.position = position;
    this.annotation = annotation;
  }

  /**
   * @return the position
   */
  public CellPosition getPosition() {
    return position;
  }

  /**
   * @return the annotation
   */
  public CellAnnotation getAnnotation() {
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
    Disambiguation other = (Disambiguation) obj;
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
    return "Disambiguation [position=" + position + ", annotation=" + annotation + "]";
  }
}
