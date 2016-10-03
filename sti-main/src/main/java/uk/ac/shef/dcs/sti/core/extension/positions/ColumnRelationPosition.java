package uk.ac.shef.dcs.sti.core.extension.positions;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * Position of columns in a relation.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public final class ColumnRelationPosition {

  private final ColumnPosition first;

  private final ColumnPosition second;

  /**
   * Creates new representation of a position of columns in relation.
   * 
   * @param first first column position
   * @param second second column position
   */
  public ColumnRelationPosition(ColumnPosition first, ColumnPosition second) {
    Preconditions.checkNotNull(first);
    Preconditions.checkNotNull(second);
    Preconditions.checkArgument(first.getIndex() != second.getIndex());

    this.first = first;
    this.second = second;
  }
  
  /**
   * Creates new representation of a position of columns in relation.
   * 
   * @param firstIndex first column index
   * @param second second column index
   */
  public ColumnRelationPosition(int firstIndex, int secondIndex) {
    this(new ColumnPosition(firstIndex), new ColumnPosition(secondIndex));
  }

  /**
   * @return the first column position
   */
  public ColumnPosition getFirst() {
    return first;
  }

  /**
   * @return the second column position
   */
  public ColumnPosition getSecond() {
    return second;
  }
  
  /**
   * @return the first column index
   */
  public int getFirstIndex() {
    return first.getIndex();
  }
  
  /**
   * @return the second column index
   */
  public int getSecondIndex() {
    return second.getIndex();
  }

  /**
   * Computes hash code based on the first and second column position.
   * 
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

  /**
   * Compares for equivalence (only other column relation position with the same first and second
   * column position passes).
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnRelationPosition [first=" + first + ", second=" + second + "]";
  }
}
