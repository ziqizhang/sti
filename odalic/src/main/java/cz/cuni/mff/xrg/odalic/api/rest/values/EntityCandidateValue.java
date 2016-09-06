package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Likelihood;

/**
 * <p>
 * Domain class {@link EntityCandidate} adapted for REST API.
 * </p>
 * 
 * <p>
 * In this version it supports a chosen flag instead of annotations classes providing the chosen set
 * separately.
 * </p>
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "entityCandidate")
public final class EntityCandidateValue implements Serializable, Comparable<EntityCandidateValue> {

  private static final long serialVersionUID = 3072774254576336747L;

  private Entity entity;

  private Likelihood likelihood;

  private boolean chosen;

  public EntityCandidateValue() {}

  public EntityCandidateValue(EntityCandidate adaptee, boolean chosen) {
    entity = adaptee.getEntity();
    likelihood = adaptee.getLikelihood();
    this.chosen = chosen;
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
   * @return the likelihood
   */
  @XmlElement
  @Nullable
  public Likelihood getLikelihood() {
    return likelihood;
  }

  /**
   * @param likelihood the likelihood to set
   */
  public void setLikelihood(Likelihood likelihood) {
    Preconditions.checkNotNull(likelihood);

    this.likelihood = likelihood;
  }

  /**
   * @return the chosen
   */
  @XmlElement
  public boolean isChosen() {
    return chosen;
  }

  /**
   * @param chosen the chosen to set
   */
  public void setChosen(boolean chosen) {
    this.chosen = chosen;
  }



  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + new EntityCandidate(entity, likelihood).hashCode();
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

    if (!new EntityCandidate(entity, likelihood)
        .equals(new EntityCandidate(other.entity, other.likelihood))) {
      return false;
    }

    return chosen == other.chosen;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(EntityCandidateValue other) {
    final int likelihoodComparison = -1 * likelihood.compareTo(other.likelihood);
    if (likelihoodComparison != 0) {
      return likelihoodComparison;
    }

    final int chosenComparison = -1 * Boolean.compare(chosen, other.chosen);
    if (chosenComparison != 0) {
      return chosenComparison;
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
    return "EntityCandidateValue [entity=" + entity + ", likelihood=" + likelihood + ", chosen="
        + chosen + "]";
  }
}
