package uk.ac.shef.dcs.sti.core.extension.positions;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * Position of a row in a table.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public final class RowPosition implements Serializable, Comparable<RowPosition> {

  private static final long serialVersionUID = 3435359552551500579L;
  
  private final int index;

  /**
   * Creates new row position representation.
   * 
   * @param index zero-base index
   */
  public RowPosition(int index) {
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
   * Compares for equality (only other row position with the same index passes).
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
    RowPosition other = (RowPosition) obj;
    if (index != other.index) {
      return false;
    }
    return true;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(RowPosition other) {
    return Integer.compare(index, other.index);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "[" + index + "]";
  }
}
