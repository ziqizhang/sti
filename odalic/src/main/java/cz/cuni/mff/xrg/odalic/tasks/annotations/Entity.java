package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;
import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Preconditions;


/**
 * Groups the resource URI and its label in one handy class.
 * 
 * @author Václav Brodec
 */
@XmlRootElement(name = "entity")
public final class Entity implements Comparable<Entity>, Serializable {

  private static final long serialVersionUID = -3001706805535088480L;

  @XmlElement
  private final String resourceID;
  
  @XmlElement
  private final String label;
  
  @SuppressWarnings("unused")
  private Entity() {
    resourceID = null;
    label = null;
  }
  
  public Entity(String resourceID, String label) {
    Preconditions.checkNotNull(resourceID);
    Preconditions.checkNotNull(label);
    
    this.resourceID = resourceID;
    this.label = label;
  }
  
  public String getResourceID() {
    return resourceID;
  }
  
  public String getLabel() {
    return label;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((resourceID == null) ? 0 : resourceID.hashCode());
    result = prime * result + ((label == null) ? 0 : label.hashCode());
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
    Entity other = (Entity) obj;
    if (resourceID == null) {
      if (other.resourceID != null) {
        return false;
      }
    } else if (!resourceID.equals(other.resourceID)) {
      return false;
    }
    if (label == null) {
      if (other.label != null) {
        return false;
      }
    } else if (!label.equals(other.label)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Annotation [resourceID=" + resourceID + ", label=" + label + "]";
  }

  @Override
  public int compareTo(Entity o) {
    return resourceID.compareTo(o.resourceID);
  }
  
  
}
