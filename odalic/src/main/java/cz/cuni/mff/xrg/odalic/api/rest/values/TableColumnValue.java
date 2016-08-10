package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableColumn;

/**
 * Domain class {@link TableColumn} adapted for REST API.
 * 
 * @author Josef Janoušek
 *
 */
@XmlRootElement(name = "tableColumn")
public final class TableColumnValue implements Serializable {
  
  private static final long serialVersionUID = -4987474937531923887L;
  
  @XmlElement
  private String name;
  
  @XmlElement
  private List<String> titles;
  
  @XmlElement
  private String description;
  
  @XmlElement
  private String dataType;
  
  @XmlElement
  private boolean virtual;
  
  @XmlElement
  private boolean suppressOutput;
  
  @XmlElement
  private String aboutUrl;
  
  @XmlElement
  private String separator;
  
  @XmlElement
  private String propertyUrl;
  
  @XmlElement
  private String valueUrl;
  
  public TableColumnValue() {}
  
  public TableColumnValue(TableColumn adaptee) {
    this.name = adaptee.getName();
    this.titles = adaptee.getTitles();
    this.description = adaptee.getDescription();
    this.dataType = adaptee.getDataType();
    this.virtual = adaptee.getVirtual();
    this.suppressOutput = adaptee.getSuppressOutput();
    this.aboutUrl = adaptee.getAboutUrl();
    this.separator = adaptee.getSeparator();
    this.propertyUrl = adaptee.getPropertyUrl();
    this.valueUrl = adaptee.getValueUrl();
  }
  
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return the titles
   */
  public List<String> getTitles() {
    return titles;
  }
  
  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * @return the dataType
   */
  public String getDataType() {
    return dataType;
  }
  
  /**
   * @return the virtual
   */
  public boolean getVirtual() {
    return virtual;
  }
  
  /**
   * @return the suppressOutput
   */
  public boolean getSuppressOutput() {
    return suppressOutput;
  }
  
  /**
   * @return the aboutUrl
   */
  public String getAboutUrl() {
    return aboutUrl;
  }
  
  /**
   * @return the separator
   */
  public String getSeparator() {
    return separator;
  }
  
  /**
   * @return the propertyUrl
   */
  public String getPropertyUrl() {
    return propertyUrl;
  }
  
  /**
   * @return the valueUrl
   */
  public String getValueUrl() {
    return valueUrl;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TableColumnValue [name=" + name + ", titles=" + titles + ", description=" + description + ", dataType="
        + dataType + ", virtual=" + virtual + ", suppressOutput=" + suppressOutput + ", aboutUrl=" + aboutUrl
        + ", separator=" + separator + ", propertyUrl=" + propertyUrl + ", valueUrl=" + valueUrl + "]";
  }
}
