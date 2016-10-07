package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.Ambiguity;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;

/**
 * Domain class {@link Ambiguity} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "ambiguity")
public final class AmbiguityValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private CellPosition position;
  
  public AmbiguityValue() {}
  
  public AmbiguityValue(Ambiguity adaptee) {
    this.position = adaptee.getPosition();
  }

  /**
   * @return the position
   */
  @XmlElement
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AmbiguityValue [position=" + position + "]";
  }
}
