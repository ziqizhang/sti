package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;

/**
 * <p>
 * Domain class {@link EntityCandidate} adapted for REST API.
 * </p>
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "entityCandidate")
public final class EntityCandidateValue implements Serializable, Comparable<EntityCandidateValue> {

  private static final long serialVersionUID = 3072774254576336747L;

  private Entity entity;

  private Score score;

  public EntityCandidateValue() {}

  public EntityCandidateValue(EntityCandidate adaptee) {
    entity = adaptee.getEntity();
    score = adaptee.getScore();
  }

  /**
   * @return the entity
   */
  @XmlElement
  @Nullable
  public Entity getEntity() {
    return entity;
  }

  /**
   * @param entity the entity to set
   */
  public void setEntity(Entity entity) {
    Preconditions.checkNotNull(entity);

    this.entity = entity;
  }

  /**
   * @return the score
   */
  @XmlElement
  @Nullable
  public Score getScore() {
    return score;
  }

  /**
   * @param score the score to set
   */
  public void setScore(Score score) {
    Preconditions.checkNotNull(score);

    this.score = score;
  } 
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + new EntityCandidate(entity, score).hashCode();
    return result;
  }

  /*
   * (non-Javadoc)
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
    EntityCandidateValue other = (EntityCandidateValue) obj;
    return new EntityCandidate(entity, score).equals(new EntityCandidate(other.entity, other.score));
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(EntityCandidateValue other) {
    final int likelihoodComparison = -1 * score.compareTo(other.score);
    if (likelihoodComparison != 0) {
      return likelihoodComparison;
    }

    return entity.compareTo(other.entity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityCandidateValue [entity=" + entity + ", score=" + score + "]";
  }
}
