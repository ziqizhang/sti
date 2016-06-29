package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Preconditions;

@XmlRootElement(name = "entityCandidate")
public final class EntityCandidate implements Comparable<EntityCandidate>, Serializable {
  
  private static final long serialVersionUID = 3072774254576336747L;

  @XmlElement
  private final Entity entity;
  
  @XmlElement
  private final Likelihood likelihood;

  /**
   * @param entity
   * @param likelihood
   */
  public EntityCandidate(Entity entity, Likelihood likelihood) {
    Preconditions.checkNotNull(entity);
        
    this.entity = entity;
    this.likelihood = likelihood;
  }

  /**
   * @return the entity
   */
  public Entity getEntity() {
    return entity;
  }

  /**
   * @return the likelihood
   */
  public Likelihood getLikelihood() {
    return likelihood;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entity == null) ? 0 : entity.hashCode());
    result = prime * result + ((likelihood == null) ? 0 : likelihood.hashCode());
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
    EntityCandidate other = (EntityCandidate) obj;
    if (entity == null) {
      if (other.entity != null) {
        return false;
      }
    } else if (!entity.equals(other.entity)) {
      return false;
    }
    if (likelihood == null) {
      if (other.likelihood != null) {
        return false;
      }
    } else if (!likelihood.equals(other.likelihood)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityCandidate [entity=" + entity + ", likelihood=" + likelihood + "]";
  }

  @Override
  public int compareTo(EntityCandidate o) {
    final int likelihoodComparison = likelihood.compareTo(o.likelihood);
    
    if (likelihoodComparison == 0) {
      return entity.compareTo(o.entity);
    } else {
      return likelihoodComparison;
    }
  }
}
