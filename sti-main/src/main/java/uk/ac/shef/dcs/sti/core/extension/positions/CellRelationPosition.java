package uk.ac.shef.dcs.sti.core.extension.positions;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * Position of cells at the same row in a relation.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public final class CellRelationPosition {

  private final ColumnRelationPosition columnsPosition;

  private final RowPosition rowPosition;

  /**
   * Creates new representation of position of two cells at the same row in a relation.
   * 
   * @param columnsPosition position of columns in which the cells are located
   * @param rowPosition position of row that the cells share
   */
  public CellRelationPosition(ColumnRelationPosition columnsPosition, RowPosition rowPosition) {
    Preconditions.checkNotNull(columnsPosition);
    Preconditions.checkNotNull(rowPosition);

    this.columnsPosition = columnsPosition;
    this.rowPosition = rowPosition;
  }

  /**
   * Creates new representation of position of two cells at the same row in a relation.
   * 
   * @param first column position of the first cell
   * @param second column position of the second cell
   * @param rowPosition position of row that the cells share
   */
  public CellRelationPosition(ColumnPosition first, ColumnPosition second,
      RowPosition rowPosition) {
    this(new ColumnRelationPosition(first, second), rowPosition);
  }

  /**
   * Creates new representation of position of two cells at the same row in a relation.
   * 
   * @param firstColumnIndex column index of the first cell
   * @param secondColumnIndex column index of the second cell
   * @param rowIndex index of the row that the cells share
   */
  public CellRelationPosition(int firstColumnIndex, int secondColumnIndex, int rowIndex) {
    this(new ColumnRelationPosition(firstColumnIndex, secondColumnIndex),
        new RowPosition(rowIndex));
  }

  /**
   * @return the row position
   */
  public RowPosition getRowPosition() {
    return rowPosition;
  }

  /**
   * @return the columns position
   */
  public ColumnRelationPosition getColumnsPosition() {
    return columnsPosition;
  }

  /**
   * @return the row index
   */
  public int getRowIndex() {
    return rowPosition.getIndex();
  }

  /**
   * @return the first cell column position
   */
  public ColumnPosition getFirstColumnPosition() {
    return columnsPosition.getFirst();
  }

  /**
   * @return the second cell column position
   */
  public ColumnPosition getSecondColumnPosition() {
    return columnsPosition.getSecond();
  }

  /**
   * @return the first cell column index
   */
  public int getFirstColumnIndex() {
    return columnsPosition.getFirstIndex();
  }

  /**
   * @return the second cell column index
   */
  public int getSecondColumnIndex() {
    return columnsPosition.getSecondIndex();
  }

  /**
   * Computes hash code based on the column position and the row position.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((columnsPosition == null) ? 0 : columnsPosition.hashCode());
    result = prime * result + ((rowPosition == null) ? 0 : rowPosition.hashCode());
    return result;
  }

  /**
   * Compares for equality (only other cell relation position with the same column position and row
   * position passes).
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
    CellRelationPosition other = (CellRelationPosition) obj;
    if (columnsPosition == null) {
      if (other.columnsPosition != null) {
        return false;
      }
    } else if (!columnsPosition.equals(other.columnsPosition)) {
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CellRelationPosition [columnsPosition=" + columnsPosition + ", rowPosition="
        + rowPosition + "]";
  }
}
