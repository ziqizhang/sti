package cz.cuni.mff.xrg.odalic.feedbacks.types;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

@XmlRootElement(name = "columnPosition")
public final class ColumnPosition implements Serializable {

  private static final long serialVersionUID = -1179554576389130985L;
  
  @XmlElement
  private final int index;

  @SuppressWarnings("unused")
  private ColumnPosition() {
    index = Integer.MIN_VALUE;
  }
  
  /**
   * @param index
   */
  public ColumnPosition(int index) {
    Preconditions.checkArgument(index >= 0);
    
    this.index = index;
  }


  /**
   * @return the index
   */
  public int getIndex() {
    return index;
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
    result = prime * result + index;
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
    ColumnPosition other = (ColumnPosition) obj;
    if (index != other.index) {
      return false;
    }
    return true;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "[" + index + "]";
  }
}
