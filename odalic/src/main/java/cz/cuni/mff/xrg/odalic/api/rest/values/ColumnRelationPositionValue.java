package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;

/**
 * Domain class {@link ColumnRelationPosition} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "cellPosition")
public final class ColumnRelationPositionValue {

  private ColumnPosition first;
  
  private ColumnPosition second;
  
  public ColumnRelationPositionValue() {}
  
  public ColumnRelationPositionValue(ColumnRelationPosition adaptee) {
    this.first = adaptee.getFirst();
    this.second = adaptee.getSecond();
  }

  /**
   * @return the first
   */
  @XmlElement
  @Nullable
  public ColumnPosition getFirst() {
    return first;
  }

  /**
   * @param first the first to set
   */
  public void setFirst(ColumnPosition first) {
    Preconditions.checkNotNull(first);
    
    this.first = first;
  }

  /**
   * @return the second
   */
  @XmlElement
  @Nullable
  public ColumnPosition getSecond() {
    return second;
  }

  /**
   * @param second the second to set
   */
  public void setSecond(ColumnPosition second) {
    Preconditions.checkNotNull(second);
    
    this.second = second;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnRelationPositionValue [first=" + first + ", second=" + second + "]";
  }
}
