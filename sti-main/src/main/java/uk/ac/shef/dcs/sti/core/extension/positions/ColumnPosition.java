package uk.ac.shef.dcs.sti.core.extension.positions;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * Position of column in a table.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public final class ColumnPosition implements Serializable {

  private static final long serialVersionUID = -1179554576389130985L;
  
  private final int index;

  /**
   * Creates new column position representation.
   * 
   * @param index zero-based index
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
  
  /**
   * Computes hash code based on the index.
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

  /**
   * Compares for equality (only other column position with the same index passes).
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
