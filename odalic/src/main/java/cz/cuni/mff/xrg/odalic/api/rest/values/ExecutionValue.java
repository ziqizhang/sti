package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Abstract resource used to symbolize submitting a task for execution when put and canceling the
 * execution when deleted.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "execution")
public final class ExecutionValue implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  /**
   * Draft flag (reserved for future versions).
   */
  private boolean draft;

  public ExecutionValue() {
    draft = false;
  }

  /**
   * Creates execution resource representation.
   * 
   * @param draft draft indicator
   */
  public ExecutionValue(boolean draft) {
    this.draft = draft;
  }

  /**
   * @return indicates draft execution
   */
  @XmlElement
  public boolean isDraft() {
    return draft;
  }

  /**
   * @param draft the draft indicator to set
   */
  public void setDraft(boolean draft) {
    this.draft = draft;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ExecutionValue [draft=" + draft + "]";
  }
}
