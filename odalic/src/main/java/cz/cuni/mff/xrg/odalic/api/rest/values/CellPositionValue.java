package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;

@XmlRootElement(name = "cellPosition")
public class CellPositionValue {

  @XmlElement
  private RowPosition rowPosition;
  
  @XmlElement
  private ColumnPosition columnPosition;
  
  public CellPositionValue() {}
  
  /**
   * @param adaptee
   */
  public CellPositionValue(CellPosition adaptee) {
    this.columnPosition = adaptee.getColumnPosition();
    this.rowPosition = adaptee.getRowPosition();
  }

  /**
   * @return the rowPosition
   */
  @Nullable
  public RowPosition getRowPosition() {
    return rowPosition;
  }

  /**
   * @param rowPosition the rowPosition to set
   */
  public void setRowPosition(RowPosition rowPosition) {
    Preconditions.checkNotNull(rowPosition);
    
    this.rowPosition = rowPosition;
  }

  /**
   * @return the columnPosition
   */
  @Nullable
  public ColumnPosition getColumnPosition() {
    return columnPosition;
  }

  /**
   * @param columnPosition the columnPosition to set
   */
  public void setColumnPosition(ColumnPosition columnPosition) {
    Preconditions.checkNotNull(columnPosition);
    
    this.columnPosition = columnPosition;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CellPositionValue [rowPosition=" + rowPosition + ", columnPosition=" + columnPosition
        + "]";
  }
}
