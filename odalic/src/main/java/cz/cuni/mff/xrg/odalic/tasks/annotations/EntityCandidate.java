package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.EntityCandidateAdapter;

/**
 * Encapsulates annotating entity and the score that is assigned to it.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(EntityCandidateAdapter.class)
public final class EntityCandidate implements Comparable<EntityCandidate>, Serializable {

  private static final long serialVersionUID = 3072774254576336747L;

  private final Entity entity;

  private final Score score;

  /**
   * @param entity
   * @param score
   */
  public EntityCandidate(Entity entity, Score score) {
    Preconditions.checkNotNull(entity);

    this.entity = entity;
    this.score = score;
  }

  /**
   * @return the entity
   */
  public Entity getEntity() {
    return entity;
  }

  /**
   * @return the score
   */
  public Score getScore() {
    return score;
  }

  /**
   * Computes hash code based on the entity and the score.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entity == null) ? 0 : entity.hashCode());
    result = prime * result + ((score == null) ? 0 : score.hashCode());
    return result;
  }

  /**
   * Compares for equality (only other candidates entity with the same score passes).
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
    EntityCandidate other = (EntityCandidate) obj;
    if (entity == null) {
      if (other.entity != null) {
        return false;
      }
    } else if (!entity.equals(other.entity)) {
      return false;
    }
    if (score == null) {
      if (other.score != null) {
        return false;
      }
    } else if (!score.equals(other.score)) {
      return false;
    }
    return true;
  }
  
  /**
   * Entity candidates are naturally ordered by their score in ascending order. In case of the
   * equal score the natural ordering of entities is taken into account.
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(EntityCandidate o) {
    final int scoreComparison = score.compareTo(o.score);
    
    if (scoreComparison == 0) {
      return entity.compareTo(o.entity);
    } else {
      return scoreComparison;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityCandidate [entity=" + entity + ", score=" + score + "]";
  }
}
