package cz.cuni.mff.xrg.odalic.positions;

import com.google.common.base.Preconditions;

public final class ColumnRelationPosition {

  private final ColumnPosition first;
  
  private final ColumnPosition second;
  
  public ColumnRelationPosition(ColumnPosition first, ColumnPosition second) {
    Preconditions.checkNotNull(first);
    Preconditions.checkNotNull(second);
    Preconditions.checkArgument(first.getIndex() != second.getIndex());
    
    this.first = first;
    this.second = second;
  }

  /**
   * @return the first
   */
  public ColumnPosition getFirst() {
    return first;
  }

  /**
   * @return the second
   */
  public ColumnPosition getSecond() {
    return second;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((first == null) ? 0 : first.hashCode());
    result = prime * result + ((second == null) ? 0 : second.hashCode());
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
    ColumnRelationPosition other = (ColumnRelationPosition) obj;
    if (first == null) {
      if (other.first != null) {
        return false;
      }
    } else if (!first.equals(other.first)) {
      return false;
    }
    if (second == null) {
      if (other.second != null) {
        return false;
      }
    } else if (!second.equals(other.second)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnRelationPosition [first=" + first + ", second=" + second + "]";
  }
}
