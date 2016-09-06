package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableColumn;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableSchema;

/**
 * Domain class {@link TableSchema} adapted for REST API.
 * 
 * @author Josef Janoušek
 *
 */
@XmlRootElement(name = "tableSchema")
@JsonInclude(Include.NON_NULL)
public final class TableSchemaValue implements Serializable {
  
  private static final long serialVersionUID = 6093586476544509692L;
  
  private List<TableColumn> columns;
  
  public TableSchemaValue() {}
  
  public TableSchemaValue(TableSchema adaptee) {
    this.columns = adaptee.getColumns();
  }
  
  /**
   * @return the columns
   */
  @XmlElement
  public List<TableColumn> getColumns() {
    return columns;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TableSchemaValue [columns=" + columns + "]";
  }
}
