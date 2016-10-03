package uk.ac.shef.dcs.sti.core.extension.annotations;

import java.io.Serializable;

import com.google.common.base.Preconditions;

/**
 * Groups the resource ID and its label in one handy class.
 * 
 * @author Václav Brodec
 */
public final class Entity implements Comparable<Entity>, Serializable {

  private static final long serialVersionUID = -3001706805535088480L;

  private final String resource;
  
  private final String label;
  
  /**
   * Creates new entity representation.
   * 
   * @param resource entity resource ID
   * @param label label
   */
  public Entity(String resource, String label) {
    Preconditions.checkNotNull(resource);
    Preconditions.checkNotNull(label);
    
    this.resource = resource;
    this.label = label;
  }
  
  /**
   * @return the resource ID
   */
  public String getResource() {
    return resource;
  }
  
  /**
   * @return the label
   */
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

  /**
   * Compares the entities by their resource ID lexicographically.
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   * @see java.lang.String#compareTo(String) for the definition of resource ID comparison
   */
  @Override
  public int compareTo(Entity o) {
    return resource.compareTo(o.resource);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Annotation [resource=" + resource + ", label=" + label + "]";
  }
}
