package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.feedbacks.Classification;
import cz.cuni.mff.xrg.odalic.feedbacks.types.ColumnPosition;
import cz.cuni.mff.xrg.odalic.feedbacks.types.Entity;

@XmlRootElement(name = "classification")
public class ClassificationValue implements Serializable {

  private static final long serialVersionUID = 6470286409364911894L;

  @XmlElement
  private ColumnPosition position;

  @XmlElement
  private Set<Entity> entities;

  public ClassificationValue() {
    entities = ImmutableSet.of();
  }
  
  public ClassificationValue(Classification adaptee) {
    this.position = adaptee.getPosition();
    this.entities = adaptee.getEntities();
  }

  /**
   * @return the position
   */
  @Nullable
  public ColumnPosition getPosition() {
    return position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(ColumnPosition position) {
    Preconditions.checkNotNull(position);
    
    this.position = position;
  }

  /**
   * @return the entities
   */
  public Set<Entity> getEntities() {
    return entities;
  }

  /**
   * @param entities the entities to set
   */
  public void setEntities(Set<? extends Entity> entities) {
    Preconditions.checkNotNull(entities);
    
    this.entities = ImmutableSet.copyOf(entities);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ClassificationValue [position=" + position + ", entities=" + entities + "]";
  }
}
