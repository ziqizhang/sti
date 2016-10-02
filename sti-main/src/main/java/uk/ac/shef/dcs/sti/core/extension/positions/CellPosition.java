package uk.ac.shef.dcs.sti.core.extension.positions;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * Position of a common cell in a table. Headers do not count.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public final class CellPosition implements Serializable {

  private static final long serialVersionUID = 7955615617737637528L;
  
  private final RowPosition rowPosition;
  
  private final ColumnPosition columnPosition;
  
  
  /**
   * Creates new representation of cell position in a table.
   * 
   * @param rowPosition row position
   * @param columnPosition column position
   */
  public CellPosition(RowPosition rowPosition, ColumnPosition columnPosition) {
    Preconditions.checkNotNull(rowPosition);
    Preconditions.checkNotNull(columnPosition);
    
    this.rowPosition = rowPosition;
    this.columnPosition = columnPosition;
  }
  
  /**
   * Creates new representation of cell position in a table.
   * 
   * @param rowIndex row index
   * @param columnIndex column index
   */
  public CellPosition(int rowIndex, int columnIndex) {
    this(new RowPosition(rowIndex), new ColumnPosition(columnIndex));
  }
  
  /**
   * @return the row position
   */
  public RowPosition getRowPosition() {
    return rowPosition;
  }
  
  /**
   * @return the column position
   */
  public ColumnPosition getColumnPosition() {
    return columnPosition;
  }
  
  /**
   * @return the row index
   */
  public int getRowIndex() {
    return rowPosition.getIndex();
  }
  
  /**
   * @return the column index
   */
  public int getColumnIndex() {
    return columnPosition.getIndex();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((columnPosition == null) ? 0 : columnPosition.hashCode());
    result = prime * result + ((rowPosition == null) ? 0 : rowPosition.hashCode());
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
    CellPosition other = (CellPosition) obj;
    if (columnPosition == null) {
      if (other.columnPosition != null) {
        return false;
      }
    } else if (!columnPosition.equals(other.columnPosition)) {
      return false;
    }
    if (rowPosition == null) {
      if (other.rowPosition != null) {
        return false;
      }
    } else if (!rowPosition.equals(other.rowPosition)) {
      return false;
    }
    return true;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "" + rowPosition + columnPosition;
  }
}
