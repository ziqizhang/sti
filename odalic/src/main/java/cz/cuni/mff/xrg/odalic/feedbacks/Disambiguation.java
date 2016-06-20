package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.types.CellPosition;
import cz.cuni.mff.xrg.odalic.feedbacks.types.Entity;

@XmlRootElement(name = "disambiguation")
public final class Disambiguation implements Serializable {

  private static final long serialVersionUID = -5229197850609921790L;

  @XmlElement
  private final CellPosition position;
  
  @XmlElement
  private final Entity entity;

  public Disambiguation() {
    position = null;
    entity = null;
  }
  
  /**
   * @param position
   * @param entity
   */
  public Disambiguation(CellPosition position, Entity entity) {
    Preconditions.checkNotNull(position);
    Preconditions.checkNotNull(entity);
        
    this.position = position;
    this.entity = entity;
  }

  /**
   * @return the position
   */
  public CellPosition getPosition() {
    return position;
  }

  /**
   * @return the entity
   */
  public Entity getEntity() {
    return entity;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entity == null) ? 0 : entity.hashCode());
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
    if (entity == null) {
      if (other.entity != null) {
        return false;
      }
    } else if (!entity.equals(other.entity)) {
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
    return "Disambiguation [position=" + position + ", entity=" + entity + "]";
  }
}
