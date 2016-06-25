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
  private final URI resource;
  
  @XmlElement
  private final String label;
  
  @SuppressWarnings("unused")
  private Entity() {
    resource = null;
    label = null;
  }
  
  public Entity(URI resource, String label) {
    Preconditions.checkNotNull(resource);
    Preconditions.checkNotNull(label);
    
    this.resource = resource;
    this.label = label;
  }
  
  public URI getResource() {
    return resource;
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
    result = prime * result + ((resource == null) ? 0 : resource.hashCode());
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
    if (resource == null) {
      if (other.resource != null) {
        return false;
      }
    } else if (!resource.equals(other.resource)) {
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
    return "Annotation [resource=" + resource + ", label=" + label + "]";
  }

  @Override
  public int compareTo(Entity o) {
    return resource.compareTo(o.resource);
  }
  
  
}
