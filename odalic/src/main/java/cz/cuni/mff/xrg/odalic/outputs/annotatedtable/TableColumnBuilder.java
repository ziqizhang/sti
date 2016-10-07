package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Incrementally helps to produce the complete {@link TableColumn}.
 * 
 * @author Josef Janoušek
 *
 */
public class TableColumnBuilder {

  private String name;
  
  private List<String> titles;
  
  private String description;
  
  private String dataType;
  
  private Boolean virtual;
  
  private Boolean suppressOutput;
  
  private String aboutUrl;
  
  private String separator;
  
  private String propertyUrl;
  
  private String valueUrl;
  
  public TableColumnBuilder() { }
  
  public void setName(String name) {
    Preconditions.checkNotNull(name);
    
    this.name = name;
  }
  
  public void setTitles(List<String> titles) {
    Preconditions.checkNotNull(titles);
    
    this.titles = titles;
  }
  
  public void setDescription(String description) {
    Preconditions.checkNotNull(description);
    
    this.description = description;
  }
  
  public void setDataType(String dataType) {
    Preconditions.checkNotNull(dataType);
    
    this.dataType = dataType;
  }
  
  public void setVirtual(Boolean virtual) {
    Preconditions.checkNotNull(virtual);
    
    this.virtual = virtual;
  }
  
  public void setSuppressOutput(Boolean suppressOutput) {
    Preconditions.checkNotNull(suppressOutput);
    
    this.suppressOutput = suppressOutput;
  }
  
  public void setAboutUrl(String aboutUrl) {
    Preconditions.checkNotNull(aboutUrl);
    
    this.aboutUrl = aboutUrl;
  }
  
  public void setSeparator(String separator) {
    Preconditions.checkNotNull(separator);
    
    this.separator = separator;
  }
  
  public void setPropertyUrl(String propertyUrl) {
    Preconditions.checkNotNull(propertyUrl);
    
    this.propertyUrl = propertyUrl;
  }
  
  public void setValueUrl(String valueUrl) {
    Preconditions.checkNotNull(valueUrl);
    
    this.valueUrl = valueUrl;
  }
  
  public TableColumn build() {
    return new TableColumn(this.name, this.titles, this.description, this.dataType, this.virtual,
        this.suppressOutput, this.aboutUrl, this.separator, this.propertyUrl, this.valueUrl);
  }
  
  public void clear() {
    this.name = null;
    this.titles = null;
    this.description = null;
    this.dataType = null;
    this.virtual = null;
    this.suppressOutput = null;
    this.aboutUrl = null;
    this.separator = null;
    this.propertyUrl = null;
    this.valueUrl = null;
  }
}
