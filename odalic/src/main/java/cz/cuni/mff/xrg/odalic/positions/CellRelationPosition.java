package cz.cuni.mff.xrg.odalic.positions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

@XmlRootElement(name = "cellRelationPosition")
public final class CellRelationPosition {

  @XmlElement
  private final ColumnRelationPosition columnsPosition;
  
  @XmlElement
  private final RowPosition rowPosition;
  
  @SuppressWarnings("unused")
  private CellRelationPosition() {
    columnsPosition = null;
    rowPosition = null;
  }
  
  public CellRelationPosition(ColumnRelationPosition columnsPosition, RowPosition rowPosition) {
    Preconditions.checkNotNull(columnsPosition);
    Preconditions.checkNotNull(rowPosition);
    
    this.columnsPosition = columnsPosition;
    this.rowPosition = rowPosition;
  }

  /**
   * @return the rowPosition
   */
  public RowPosition getRowPosition() {
    return rowPosition;
  }
  
  /**
   * @return the columnsPosition
   */
  public ColumnRelationPosition getColumnsPosition() {
    return columnsPosition;
  }
  
  /* (non-Javadoc)
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CellRelationPosition [columnsPosition=" + columnsPosition + ", rowPosition="
        + rowPosition + "]";
  }
}
