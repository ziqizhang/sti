package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.Ambiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.types.CellPosition;

@XmlRootElement(name = "ambiguity")
public final class AmbiguityValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  @XmlElement
  private CellPosition position;
  
  public AmbiguityValue() {}
  
  public AmbiguityValue(Ambiguity adaptee) {
    this.position = adaptee.getPosition();
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AmbiguityValue [position=" + position + "]";
  }
}
