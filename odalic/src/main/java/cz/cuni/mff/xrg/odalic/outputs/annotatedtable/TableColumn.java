package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.io.Serializable;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.TableColumnAdapter;

/**
 * <p>
 * This class represents a column of the annotated table.
 * </p>
 * 
 * @author Josef Janoušek
 *
 */
@Immutable
@XmlJavaTypeAdapter(TableColumnAdapter.class)
public class TableColumn implements Serializable {
  
  private static final long serialVersionUID = 4108201271318916447L;
  
  private final String name;
  
  private final List<String> titles;
  
  private final String description;
  
  private final String dataType;
  
  private final Boolean virtual;
  
  private final Boolean suppressOutput;
  
  private final String aboutUrl;
  
  private final String separator;
  
  private final String propertyUrl;
  
  private final String valueUrl;
  
  /**
   * Creates new annotated table column representation.
   * 
   * @param name column name
   * @param titles column header titles
   * @param description column header description
   * @param dataType column content data type
   * @param virtual column virtual
   * @param suppressOutput column with suppressed output
   * @param aboutUrl column value aboutUrl
   * @param separator column value separator
   * @param propertyUrl column value propertyUrl
   * @param valueUrl column value valueUrl
   */
  public TableColumn(String name, List<String> titles, String description,
      String dataType, Boolean virtual, Boolean suppressOutput,
      String aboutUrl, String separator, String propertyUrl, String valueUrl) {
    Preconditions.checkNotNull(name);
    
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
  public Boolean getVirtual() {
    return virtual;
  }
  
  /**
   * @return the suppressOutput
   */
  public Boolean getSuppressOutput() {
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
  
  /**
   * Computes hash code based on all its parts.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((titles == null) ? 0 : titles.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
    result = prime * result + ((virtual == null) ? 0 : virtual.hashCode());
    result = prime * result + ((suppressOutput == null) ? 0 : suppressOutput.hashCode());
    result = prime * result + ((aboutUrl == null) ? 0 : aboutUrl.hashCode());
    result = prime * result + ((separator == null) ? 0 : separator.hashCode());
    result = prime * result + ((propertyUrl == null) ? 0 : propertyUrl.hashCode());
    result = prime * result + ((valueUrl == null) ? 0 : valueUrl.hashCode());
    return result;
  }
  
  /**
   * Compares to another object for equality (only another TableColumn composed from equal parts passes).
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
    TableColumn other = (TableColumn) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (titles == null) {
      if (other.titles != null) {
        return false;
      }
    } else if (!titles.equals(other.titles)) {
      return false;
    }
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }
    if (dataType == null) {
      if (other.dataType != null) {
        return false;
      }
    } else if (!dataType.equals(other.dataType)) {
      return false;
    }
    if (virtual == null) {
      if (other.virtual != null) {
        return false;
      }
    } else if (!virtual.equals(other.virtual)) {
      return false;
    }
    if (suppressOutput == null) {
      if (other.suppressOutput != null) {
        return false;
      }
    } else if (!suppressOutput.equals(other.suppressOutput)) {
      return false;
    }
    if (aboutUrl == null) {
      if (other.aboutUrl != null) {
        return false;
      }
    } else if (!aboutUrl.equals(other.aboutUrl)) {
      return false;
    }
    if (separator == null) {
      if (other.separator != null) {
        return false;
      }
    } else if (!separator.equals(other.separator)) {
      return false;
    }
    if (propertyUrl == null) {
      if (other.propertyUrl != null) {
        return false;
      }
    } else if (!propertyUrl.equals(other.propertyUrl)) {
      return false;
    }
    if (valueUrl == null) {
      if (other.valueUrl != null) {
        return false;
      }
    } else if (!valueUrl.equals(other.valueUrl)) {
      return false;
    }
    return true;
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
