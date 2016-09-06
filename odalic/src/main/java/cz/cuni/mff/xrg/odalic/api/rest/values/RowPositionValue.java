package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.RowPosition;

/**
 * Domain class {@link RowPosition} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "rowPosition")
public final class RowPositionValue {

  private int index;
  
  public RowPositionValue() {}
  
  /**
   * @param adaptee
   */
  public RowPositionValue(RowPosition adaptee) {
    this.index = adaptee.getIndex();
  }

  /**
   * @return the index
   */
  @XmlElement
  public int getIndex() {
    return index;
  }

  /**
   * @param index the index to set
   */
  public void setIndex(int index) {
    Preconditions.checkArgument(index >= 0);
    
    this.index = index;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "RowPositionValue [index=" + index + "]";
  }
}
