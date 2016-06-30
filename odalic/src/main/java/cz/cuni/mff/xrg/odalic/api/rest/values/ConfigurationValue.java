package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;

@XmlRootElement(name = "configuration")
public class ConfigurationValue implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;
  
  @XmlElement(name = "input")
  private final String input;

  @XmlElement(name = "feedback")
  private final Feedback feedback;

  @SuppressWarnings("unused")
  private ConfigurationValue() {
    input = null;
    feedback = null;
  }

  public ConfigurationValue(Configuration adaptee) {
    input = adaptee.getInput().getId();
    feedback = adaptee.getFeedback();
  }

  /**
   * @return the input
   */
  public String getInput() {
    return input;
  }

  /**
   * @return the feedback
   */
  public Feedback getFeedback() {
    return feedback;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ConfigurationValue [input=" + input + ", feedback=" + feedback + "]";
  }
}
