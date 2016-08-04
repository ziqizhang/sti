package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.util.List;

import com.google.common.base.Preconditions;

public class TableSchema {
  
  private final List<TableColumn> columns;
  
  public TableSchema(List<TableColumn> columns) {
    Preconditions.checkNotNull(columns);
    
    this.columns = columns;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TableSchema [columns=" + columns + "]";
  }
}
