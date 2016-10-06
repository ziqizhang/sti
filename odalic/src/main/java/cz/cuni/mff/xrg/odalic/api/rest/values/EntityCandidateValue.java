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
 * <p>
 * In this version it supports a chosen flag instead of annotations classes providing the chosen
 * set separately.
 * </p>
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "entityCandidate")
public final class EntityCandidateValue implements Serializable, Comparable<EntityCandidateValue> {

  private static final long serialVersionUID = 3072774254576336747L;

  @XmlElement
  private Entity entity;

  @XmlElement
  private Score score;

  @XmlElement
  private boolean chosen;

  public EntityCandidateValue() {}

  public EntityCandidateValue(EntityCandidate adaptee, boolean chosen) {
    entity = adaptee.getEntity();
    score = adaptee.getScore();
    this.chosen = chosen;
  }

  /**
   * @return the entity
   */
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

  /**
   * @return the chosen
   */
  public boolean isChosen() {
    return chosen;
  }

  /**
   * @param chosen the chosen to set
   */
  public void setChosen(boolean chosen) {
    this.chosen = chosen;
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
    EntityCandidateValue other = (EntityCandidateValue) obj;
    return new EntityCandidate(entity, score).equals(new EntityCandidate(other.entity, other.score));
  }
  
  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(EntityCandidateValue other) {
    return new EntityCandidate(entity, score)
        .compareTo(new EntityCandidate(other.entity, other.score));
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityCandidateValue [entity=" + entity + ", score=" + score + ", chosen="
        + chosen + "]";
  }
}
