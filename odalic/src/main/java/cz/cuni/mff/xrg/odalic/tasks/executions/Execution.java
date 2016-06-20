package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.Serializable;
import java.net.URL;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import cz.cuni.mff.xrg.odalic.tasks.State;

@XmlRootElement
public class Execution implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;
  
  @XmlElement(name = "state")
  private State state;
  
  @XmlElement(name = "draft")
  private boolean draft;
  
  @XmlElement(name = "result")
  private URL result;

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public boolean isDraft() {
    return draft;
  }

  public void setDraft(boolean draft) {
    this.draft = draft;
  }

  public URL getResult() {
    return result;
  }

  public void setResult(URL result) {
    this.result = result;
  }
  
  public Execution() {}
}
