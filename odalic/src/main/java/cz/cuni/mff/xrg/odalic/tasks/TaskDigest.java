package cz.cuni.mff.xrg.odalic.tasks;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateSerializer;
import cz.cuni.mff.xrg.odalic.tasks.executions.State;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;


@XmlRootElement
public class TaskDigest implements Serializable {

  private static final long serialVersionUID = 1610346823333685091L;

  @XmlElement(name = "id")
  private String id;

  @JsonSerialize(using = CustomJsonDateSerializer.class)
  @JsonDeserialize(using = CustomJsonDateDeserializer.class)
  @XmlElement(name = "created")
  private Date created;
  
  @XmlElement(name = "input")
  private URL input;

  @XmlElement(name = "state")
  private State state;
  
  @XmlElement(name = "result")
  private Result result;

  public TaskDigest() {}

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
