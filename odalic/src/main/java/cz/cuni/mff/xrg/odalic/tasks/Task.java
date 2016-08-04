package cz.cuni.mff.xrg.odalic.tasks;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.TaskAdapter;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;

/**
 * Task represents the single unit of work done by the Odalic core. Its configuration is
 * replaceable.
 * 
 * @author Václav Brodec
 *
 */
@XmlJavaTypeAdapter(TaskAdapter.class)
public final class Task implements Serializable {

  private static final long serialVersionUID = 1610346823333685091L;

  private final String id;

  private final Date created;

  private Configuration configuration;

  /**
   * Creates the task instance.
   * 
   * @param id ID of the task
   * @param created provided time of creation
   * @param configuration configuration of the task
   */
  public Task(String id, Date created, Configuration configuration) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(created);
    Preconditions.checkNotNull(configuration);

    this.id = id;
    this.created = created;
    this.configuration = configuration;
  }

  /**
   * Creates the task instance and sets it creation date to now.
   * 
   * @param id ID of the task
   * @param configuration configuration of the task
   */
  public Task(String id, Configuration configuration) {
    this(id, new Date(), configuration);
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
    Preconditions.checkNotNull(configuration);

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

  /**
   * Computes hash code value for this object based solely on its ID.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /**
   * Compares for equality (comparable to other {@link Task} instances only, based solely on their
   * IDs).
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Task [id=" + id + ", created=" + created + ", configuration=" + configuration + "]";
  }
}
