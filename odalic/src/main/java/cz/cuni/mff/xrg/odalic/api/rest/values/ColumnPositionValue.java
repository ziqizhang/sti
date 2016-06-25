package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

@XmlRootElement(name = "columnPosition")
public class ColumnPositionValue {

  @XmlElement
  private int index;
  
  public ColumnPositionValue() {}
  
  /**
   * @param adaptee
   */
  public ColumnPositionValue(ColumnPosition adaptee) {
    this.index = adaptee.getIndex();
  }

  /**
   * @return the index
   */
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
    return "ColumnPositionValue [index=" + index + "]";
  }
}
