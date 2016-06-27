package cz.cuni.mff.xrg.odalic.tasks.configurations;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ConfigurationAdapter;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.files.File;

@XmlJavaTypeAdapter(ConfigurationAdapter.class)
@XmlRootElement(name = "configuration")
public class Configuration implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;
  
  @XmlElement(name = "input")
  private final File input; 

  @XmlElement(name = "feedback")
  private final Feedback feedback;

  @SuppressWarnings("unused")
  private Configuration() {
    input = null;
    feedback = new Feedback();
  }

  public Configuration(File input) {
    Preconditions.checkNotNull(input);
    
    this.input = input;
    this.feedback = new Feedback();
  }
  
  public Configuration(File input, Feedback feedback) {
    Preconditions.checkNotNull(input);
    Preconditions.checkNotNull(feedback);
    
    this.input = input;
    this.feedback = feedback;
  }

  /**
   * @return the input
   */
  public File getInput() {
    return input;
  }

  /**
   * @return the feedback
   */
  public Feedback getFeedback() {
    return feedback;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((feedback == null) ? 0 : feedback.hashCode());
    result = prime * result + ((input == null) ? 0 : input.hashCode());
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
    Configuration other = (Configuration) obj;
    if (feedback == null) {
      if (other.feedback != null) {
        return false;
      }
    } else if (!feedback.equals(other.feedback)) {
      return false;
    }
    if (input == null) {
      if (other.input != null) {
        return false;
      }
    } else if (!input.equals(other.input)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Configuration [input=" + input + ", feedback=" + feedback + "]";
  }
}
