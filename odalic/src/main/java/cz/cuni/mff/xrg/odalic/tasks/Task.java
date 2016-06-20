package cz.cuni.mff.xrg.odalic.tasks;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateSerializer;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

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
  private final URL input;

  @XmlElement
  private final Feedback feedback;
  
  @XmlElement
  private final Result result;

  @SuppressWarnings("unused")
  private Task() {
    id = null;
    created = null;
    input = null;
    feedback = null;
    result = null;
  }

  /**
   * @param id
   * @param created
   * @param input
   * @param feedback
   * @param result
   */
  public Task(String id, Date created, URL input, Feedback feedback, Result result) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(input);
    Preconditions.checkNotNull(feedback);
    Preconditions.checkNotNull(result);
    
    this.id = id;
    this.created = created;
    this.input = input;
    this.feedback = feedback;
    this.result = result;
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
   * @return the input
   */
  public URL getInput() {
    return input;
  }

  /**
   * @return the feedback
   */
  public Feedback getFeedback() {
    return feedback;
  }

  /**
   * @return the result
   */
  public Result getResult() {
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((created == null) ? 0 : created.hashCode());
    result = prime * result + ((feedback == null) ? 0 : feedback.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((input == null) ? 0 : input.hashCode());
    result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
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
    if (created == null) {
      if (other.created != null) {
        return false;
      }
    } else if (!created.equals(other.created)) {
      return false;
    }
    if (feedback == null) {
      if (other.feedback != null) {
        return false;
      }
    } else if (!feedback.equals(other.feedback)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (input == null) {
      if (other.input != null) {
        return false;
      }
    } else if (!input.equals(other.input)) {
      return false;
    }
    if (result == null) {
      if (other.result != null) {
        return false;
      }
    } else if (!result.equals(other.result)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Task [id=" + id + ", created=" + created + ", input=" + input + ", feedback=" + feedback
        + ", result=" + result + "]";
  }
}
