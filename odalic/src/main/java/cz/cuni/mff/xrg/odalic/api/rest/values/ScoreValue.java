package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;

/**
 * Domain class {@link Score} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "score")
public final class ScoreValue implements Serializable {
  
  private static final long serialVersionUID = -901650058091668104L;
  
  @XmlElement
  private double value;

  public ScoreValue() {
    value = Double.MIN_VALUE;
  }
  
  public ScoreValue(Score adaptee) {
    value = adaptee.getValue();
  }

  /**
   * @return the value (negative when not set)
   */
  public double getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(double value) {
    Preconditions.checkArgument(value >= 0);
    
    this.value = value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ScoreValue [value=" + value + "]";
  }
}
