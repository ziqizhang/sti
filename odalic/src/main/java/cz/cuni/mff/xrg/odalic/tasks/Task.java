package cz.cuni.mff.xrg.odalic.tasks;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateSerializer;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

@XmlRootElement
public class Task implements Serializable {

  private static final long serialVersionUID = 1610346823333685091L;

  @XmlElement(name = "id")
  private String id;
  
  @JsonSerialize(using = CustomJsonDateSerializer.class)
  @JsonDeserialize(using = CustomJsonDateDeserializer.class)
  @XmlElement(name = "created")
  private Date created;

  @XmlElement(name = "input")
  private URL input;

  @XmlElement(name = "feedback")
  private Feedback feedback;
  
  @XmlElement(name = "state")
  private State state;
  
  @XmlElement(name = "result")
  private Result result;

  public Task() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

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

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public Result getResult() {
    return result;
  }

  public void setResult(Result result) {
    this.result = result;
  }  
}
