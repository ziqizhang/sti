package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import com.google.common.base.Preconditions;

public class AnnotatedTable {
  
  private final String context = "http://www.w3.org/ns/csvw";
  
  private final String url;
  
  private final TableSchema tableSchema;
  
  public AnnotatedTable(String url, TableSchema tableSchema) {
    Preconditions.checkNotNull(url);
    Preconditions.checkNotNull(tableSchema);
    
    this.url = url;
    this.tableSchema = tableSchema;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AnnotatedTable [context=" + context + ", url=" + url + ", tableSchema=" + tableSchema + "]";
  }
}
