package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "execution")
public class Execution implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;
  
  @XmlElement
  private final boolean draft;
  
  public Execution() {
    draft = false;
  }
  
  public Execution(boolean draft) {
    this.draft = draft;
  }

  /**
   * @return the draft
   */
  public boolean isDraft() {
    return draft;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Execution [draft=" + draft + "]";
  }
}
