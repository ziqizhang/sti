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
  
  private String context = "http://www.w3.org/ns/csvw";
  
  private String url;
  
  private TableSchema tableSchema;
  
  public AnnotatedTableValue() {}
  
  public AnnotatedTableValue(AnnotatedTable adaptee) {
    this.url = adaptee.getUrl();
    this.tableSchema = adaptee.getTableSchema();
  }
  
  /**
   * @return the context
   */
  @XmlElement
  public String getContext() {
    return context;
  }
  
  /**
   * @return the url
   */
  @XmlElement
  public String getUrl() {
    return url;
  }
  
  /**
   * @return the table schema
   */
  @XmlElement
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
