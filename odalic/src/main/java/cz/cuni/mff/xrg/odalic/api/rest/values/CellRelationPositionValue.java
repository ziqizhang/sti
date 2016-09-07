package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.CellRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;

/**
 * Domain class {@link CellRelationPosition} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "cellRelationPosition")
public class CellRelationPositionValue implements Serializable {
  
  private static final long serialVersionUID = 2155060933372152653L;

  private RowPosition rowPosition;
  
  private ColumnRelationPosition columnsPosition;
  
  public CellRelationPositionValue() {}
  
  public CellRelationPositionValue(CellRelationPosition adaptee) {
    this.rowPosition = adaptee.getRowPosition();
    this.columnsPosition = adaptee.getColumnsPosition();
  }

  /**
   * @return the rowPosition
   */
  @XmlElement
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
   * @return the columnsPosition
   */
  @XmlElement
  @Nullable
  public ColumnRelationPosition getColumnsPosition() {
    return columnsPosition;
  }

  /**
   * @param columnsPosition the columnsPosition to set
   */
  public void setColumnsPosition(ColumnRelationPosition columnsPosition) {
    Preconditions.checkNotNull(columnsPosition);
    
    this.columnsPosition = columnsPosition;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CellRelationPositionValue [rowPosition=" + rowPosition + ", columnsPosition="
        + columnsPosition + "]";
  }
}
