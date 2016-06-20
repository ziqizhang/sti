package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.feedbacks.types.ColumnPosition;
import cz.cuni.mff.xrg.odalic.feedbacks.types.Entity;

@XmlRootElement(name = "classification")
public final class Classification implements Serializable {

  private static final long serialVersionUID = 6053349406668481968L;

  @XmlElement
  private final ColumnPosition position;

  @XmlElement
  private final Set<Entity> entities;

  @SuppressWarnings("unused")
  private Classification() {
    position = null;
    entities = ImmutableSet.of();
  }
  
  public Classification(ColumnPosition position, Set<? extends Entity> entity) {
    Preconditions.checkNotNull(position);
    Preconditions.checkNotNull(entity);

    this.position = position;
    this.entities = ImmutableSet.copyOf(entity);
  }

  /**
   * @return the position
   */
  public ColumnPosition getPosition() {
    return position;
  }

  /**
   * @return the entity
   */
  public Set<Entity> getEntities() {
    return ImmutableSet.copyOf(entities);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entities == null) ? 0 : entities.hashCode());
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
    Classification other = (Classification) obj;
    if (entities == null) {
      if (other.entities != null) {
        return false;
      }
    } else if (!entities.equals(other.entities)) {
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
    return "Classification [position=" + position + ", entities=" + entities + "]";
  }
}
