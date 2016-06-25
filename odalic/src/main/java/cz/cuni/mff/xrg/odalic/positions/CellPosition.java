package cz.cuni.mff.xrg.odalic.positions;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

@XmlRootElement(name = "cellPosition")
public final class CellPosition implements Serializable {

  private static final long serialVersionUID = 7955615617737637528L;
  
  @XmlElement
  private final RowPosition rowPosition;
  
  @XmlElement
  private final ColumnPosition columnPosition;
  
  
  @SuppressWarnings("unused")
  private CellPosition() {
    rowPosition = null;
    columnPosition = null;
  }
  
  /**
   * @param rowPosition
   * @param columnPosition
   */
  public CellPosition(RowPosition rowPosition, ColumnPosition columnPosition) {
    Preconditions.checkNotNull(rowPosition);
    Preconditions.checkNotNull(columnPosition);
    
    this.rowPosition = rowPosition;
    this.columnPosition = columnPosition;
  }
  
  /**
   * @return the rowPosition
   */
  public RowPosition getRowPosition() {
    return rowPosition;
  }
  
  /**
   * @return the columnPosition
   */
  public ColumnPosition getColumnPosition() {
    return columnPosition;
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
