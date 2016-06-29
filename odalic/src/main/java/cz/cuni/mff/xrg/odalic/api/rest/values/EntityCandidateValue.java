package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Likelihood;

public final class EntityCandidateValue implements Serializable {
  
  private static final long serialVersionUID = 3072774254576336747L;

  @XmlElement
  private Entity entity;
  
  @XmlElement
  private Likelihood likelihood;
  
  @XmlElement
  private boolean chosen;

  /**
   * @param entity
   * @param likelihood
   */
  public EntityCandidateValue() { }

  /**
   * @param adaptee
   */
  public EntityCandidateValue(EntityCandidate adaptee, boolean chosen) {
    entity = adaptee.getEntity();
    likelihood = adaptee.getLikelihood();
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
   * @return the likelihood
   */
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
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityCandidateValue [entity=" + entity + ", likelihood=" + likelihood + ", chosen="
        + chosen + "]";
  }
}
