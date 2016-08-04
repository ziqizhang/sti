package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;

/**
 * Domain class {@link Configuration} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "configuration")
public final class ConfigurationValue implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;
  
  @XmlElement(name = "input")
  private String input;

  @XmlElement(name = "feedback")
  private Feedback feedback;
  
  @XmlElement(name = "primary_base")
  private KnowledgeBase primaryBase;

  public ConfigurationValue() {}

  public ConfigurationValue(Configuration adaptee) {
    input = adaptee.getInput().getId();
    feedback = adaptee.getFeedback();
    primaryBase = adaptee.getPrimaryBase();
  }

  /**
   * @return the input
   */
  @Nullable
  public String getInput() {
    return input;
  }

  /**
   * @param input the input to set
   */
  public void setInput(String input) {
    Preconditions.checkNotNull(input);
    
    this.input = input;
  }
  
  /**
   * @return the feedback
   */
  @Nullable
  public Feedback getFeedback() {
    return feedback;
  }


  /**
   * @param feedback the feedback to set
   */
  public void setFeedback(Feedback feedback) {
    Preconditions.checkNotNull(feedback);
    
    this.feedback = feedback;
  }

  /**
   * @return the primary knowledge base
   */
  @Nullable
  public KnowledgeBase getPrimaryBase() {
    return primaryBase;
  }

  /**
   * @param primaryBase the primary knowledge base to set
   */
  public void setPrimaryBase(KnowledgeBase primaryBase) {
    Preconditions.checkNotNull(primaryBase);
    
    this.primaryBase = primaryBase;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ConfigurationValue [input=" + input + ", feedback=" + feedback + ", primaryBase="
        + primaryBase + "]";
  }
}
