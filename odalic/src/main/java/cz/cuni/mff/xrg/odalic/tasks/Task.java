package cz.cuni.mff.xrg.odalic.tasks;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateSerializer;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.executions.Execution;

@XmlRootElement(name = "task")
public final class Task implements Serializable {

  private static final long serialVersionUID = 1610346823333685091L;

  @XmlElement
  private final String id;
  
  @JsonSerialize(using = CustomJsonDateSerializer.class)
  @JsonDeserialize(using = CustomJsonDateDeserializer.class)
  @XmlElement
  private final Date created;

  @XmlElement
  private Configuration configuration;

  @SuppressWarnings("unused")
  private Task() {
    id = null;
    created = null;
    configuration = null;
  }

  /**
   * @param id
   * @param created
   * @param configuration   * 
   */
  public Task(String id, Date created, Configuration configuration) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(created);
    Preconditions.checkNotNull(configuration);    
    
    this.id = id;
    this.created = created;
    this.configuration = configuration;
  }
  
  public Task(String id, Date created, Configuration configuration, Execution execution) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(created);
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(execution);
    
    this.id = id;
    this.created = created;
    this.configuration = configuration;
  }

  /**
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the created
   */
  public Date getCreated() {
    return created;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /* (non-Javadoc)
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
    Task other = (Task) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Task [id=" + id + ", created=" + created + ", configuration=" + configuration
        + "]";
  }
}
