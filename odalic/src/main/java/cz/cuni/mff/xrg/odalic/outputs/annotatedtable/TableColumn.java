package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.util.List;

import com.google.common.base.Preconditions;

public class TableColumn {
  
  private final String name;
  
  private final List<String> titles;
  
  private final String description;
  
  private final String dataType;
  
  private final boolean virtual;
  
  private final boolean suppressOutput;
  
  private final String aboutUrl;
  
  private final String separator;
  
  private final String propertyUrl;
  
  private final String valueUrl;
  
  public TableColumn(String name, List<String> titles, String description,
      String dataType, boolean virtual, boolean suppressOutput,
      String aboutUrl, String separator, String propertyUrl, String valueUrl) {
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(titles);
    Preconditions.checkNotNull(description);
    Preconditions.checkNotNull(dataType);
    Preconditions.checkNotNull(virtual);
    Preconditions.checkNotNull(suppressOutput);
    Preconditions.checkNotNull(aboutUrl);
    Preconditions.checkNotNull(separator);
    Preconditions.checkNotNull(propertyUrl);
    Preconditions.checkNotNull(valueUrl);
    
    this.name = name;
    this.titles = titles;
    this.description = description;
    this.dataType = dataType;
    this.virtual = virtual;
    this.suppressOutput = suppressOutput;
    this.aboutUrl = aboutUrl;
    this.separator = separator;
    this.propertyUrl = propertyUrl;
    this.valueUrl = valueUrl;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TableColumn [name=" + name + ", titles=" + titles + ", description=" + description + ", dataType="
        + dataType + ", virtual=" + virtual + ", suppressOutput=" + suppressOutput + ", aboutUrl=" + aboutUrl
        + ", separator=" + separator + ", propertyUrl=" + propertyUrl + ", valueUrl=" + valueUrl + "]";
  }
}
