package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableSchema;

/**
 * Domain class {@link AnnotatedTable} adapted for REST API.
 * 
 * @author Josef Janoušek
 *
 */
@XmlRootElement(name = "annotatedTable")
@JsonInclude(Include.NON_NULL)
public final class AnnotatedTableValue implements Serializable {
  
  private static final long serialVersionUID = -7973901982616352L;
  
  @XmlElement
  private String context = "http://www.w3.org/ns/csvw";
  
  @XmlElement
  private String url;
  
  @XmlElement
  private TableSchema tableSchema;
  
  public AnnotatedTableValue() {}
  
  public AnnotatedTableValue(AnnotatedTable adaptee) {
    this.url = adaptee.getUrl();
    this.tableSchema = adaptee.getTableSchema();
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
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AnnotatedTableValue [context=" + context + ", url=" + url + ", tableSchema=" + tableSchema + "]";
  }
}
