package cz.cuni.mff.xrg.odalic.tasks.configurations;

import java.io.Serializable;
import java.net.URL;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;

@XmlRootElement
public class Configuration implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;
  
  @XmlElement(name = "input")
  private URL input;

  @XmlElement(name = "feedback")
  private Feedback feedback;

  public Configuration() {}

  public URL getInput() {
    return input;
  }

  public void setInput(URL input) {
    this.input = input;
  }

  public Feedback getFeedback() {
    return feedback;
  }

  public void setFeedback(Feedback feedback) {
    this.feedback = feedback;
  }
}
