package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.Disambiguation;
import cz.cuni.mff.xrg.odalic.feedbacks.types.CellPosition;
import cz.cuni.mff.xrg.odalic.feedbacks.types.Entity;

@XmlRootElement(name = "disambiguation")
public final class DisambiguationValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  @XmlElement
  private CellPosition position;
  
  @XmlElement
  private Entity entity;
  
  public DisambiguationValue() {}
  
  public DisambiguationValue(Disambiguation adaptee) {
    this.position = adaptee.getPosition();
    this.entity = adaptee.getEntity();
  }

  /**
   * @return the position
   */
  @Nullable
  public CellPosition getPosition() {
    return position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(CellPosition position) {
    Preconditions.checkNotNull(position);
    
    this.position = position;
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "DisambiguationValue [position=" + position + ", entity=" + entity + "]";
  }
}
