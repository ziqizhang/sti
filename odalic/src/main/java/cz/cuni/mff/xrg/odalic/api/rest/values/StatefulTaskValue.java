package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomDateJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomDateJsonSerializer;
import cz.cuni.mff.xrg.odalic.tasks.Task;

/**
 * Aggregation of {@link TaskValue} with {@link StateValue}.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "stateful_task")
public final class StatefulTaskValue implements Serializable {

  private static final long serialVersionUID = 1610346823333685091L;

  private String id;

  private String description;

  private Date created;
  
  private ConfigurationValue configuration;

  private StateValue state;

  public StatefulTaskValue() {}

  public StatefulTaskValue(Task adaptee, StateValue state) {
    Preconditions.checkNotNull(state);
    
    this.state = state;
    
    id = adaptee.getId();
    description = adaptee.getDescription();
    created = adaptee.getCreated();
    configuration = new ConfigurationValue(adaptee.getConfiguration());
  }

  /**
   * @return the id
   */
  @XmlElement
  @Nullable
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    Preconditions.checkNotNull(id);

    this.id = id;
  }

  /**
   * @return the description
   */
  @XmlElement
  @Nullable
  public String getDescription() {
    return description;
  }

  /**
   * @param description the task description
   */
  public void setDescription(String description) {
    Preconditions.checkNotNull(description);

    this.description = description;
  }

  /**
   * @return the created
   */
  @XmlElement
  @JsonSerialize(using = CustomDateJsonSerializer.class)
  @JsonDeserialize(using = CustomDateJsonDeserializer.class)
  @Nullable
  public Date getCreated() {
    return created;
  }

  /**
   * @param created the created to set
   */
  public void setCreated(Date created) {
    Preconditions.checkNotNull(created);

    this.created = created;
  }

  /**
   * @return the configuration
   */
  @XmlElement
  @Nullable
  public ConfigurationValue getConfiguration() {
    return configuration;
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration(ConfigurationValue configuration) {
    Preconditions.checkNotNull(configuration);

    this.configuration = configuration;
  }
  
  /**
   * @return the state
   */
  @XmlElement
  @Nullable
  public StateValue getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(StateValue state) {
    Preconditions.checkNotNull(state);

    this.state = state;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "StatefulTaskValue [id=" + id + ", description=" + description + ", created=" + created
        + ", configuration=" + configuration + ", state=" + state + "]";
  }
}
