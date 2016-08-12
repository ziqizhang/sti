package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.AnnotatedTableAdapter;

/**
 * <p>
 * This class represents an annotated table (CSV on the Web standard).
 * </p>
 * 
 * @author Josef Janoušek
 *
 */
@Immutable
@XmlJavaTypeAdapter(AnnotatedTableAdapter.class)
public class AnnotatedTable implements Serializable {
  
  private static final long serialVersionUID = 164936506495425123L;
  
  private final String context = "http://www.w3.org/ns/csvw";
  
  private final String url;
  
  private final TableSchema tableSchema;
  
  /**
   * Creates new annotated table representation.
   * 
   * @param url name of corresponding CSV file
   * @param tableSchema annotated table schema
   */
  public AnnotatedTable(String url, TableSchema tableSchema) {
    Preconditions.checkNotNull(url);
    Preconditions.checkNotNull(tableSchema);
    
    this.url = url;
    this.tableSchema = tableSchema;
  }
  
  /**
   * @return the context
   */
  public String getContext() {
    return context;
  }
  
  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }
  
  /**
   * @return the table schema
   */
  public TableSchema getTableSchema() {
    return tableSchema;
  }
  
  /**
   * Computes hash code based on all its parts.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((context == null) ? 0 : context.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    result = prime * result + ((tableSchema == null) ? 0 : tableSchema.hashCode());
    return result;
  }
  
  /**
   * Compares to another object for equality (only another AnnotatedTable composed from equal parts passes).
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
    AnnotatedTable other = (AnnotatedTable) obj;
    if (context == null) {
      if (other.context != null) {
        return false;
      }
    } else if (!context.equals(other.context)) {
      return false;
    }
    if (url == null) {
      if (other.url != null) {
        return false;
      }
    } else if (!url.equals(other.url)) {
      return false;
    }
    if (tableSchema == null) {
      if (other.tableSchema != null) {
        return false;
      }
    } else if (!tableSchema.equals(other.tableSchema)) {
      return false;
    }
    return true;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AnnotatedTable [context=" + context + ", url=" + url + ", tableSchema=" + tableSchema + "]";
  }
}
