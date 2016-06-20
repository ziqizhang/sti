package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.types.ColumnPosition;

@XmlRootElement(name = "columnIgnore")
public final class ColumnIgnore implements Serializable {

  private static final long serialVersionUID = -4305681863714969261L;
  
  @XmlElement
  private final ColumnPosition position;

  @SuppressWarnings("unused")
  private ColumnIgnore() {
    position = null;
  }
  
  /**
   * @param position
   */
  public ColumnIgnore(ColumnPosition position) {
    Preconditions.checkNotNull(position);
        
    this.position = position;
  }

  /**
   * @return the position
   */
  public ColumnPosition getPosition() {
    return position;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((position == null) ? 0 : position.hashCode());
    return result;
  }

  /* (non-Javadoc)
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
    ColumnIgnore other = (ColumnIgnore) obj;
    if (position == null) {
      if (other.position != null) {
        return false;
      }
    } else if (!position.equals(other.position)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnIgnore [position=" + position + "]";
  }
}
