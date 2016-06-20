package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import cz.cuni.mff.xrg.odalic.feedbacks.types.RowPosition;

@XmlRootElement(name = "rowPosition")
public class RowPositionValue {

  @XmlElement
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
  public int getIndex() {
    return index;
  }

  /**
   * @param index the index to set
   */
  public void setIndex(int index) {
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
