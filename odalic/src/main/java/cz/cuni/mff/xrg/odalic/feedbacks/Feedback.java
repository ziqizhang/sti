package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Feedback implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  @XmlElement(name = "subjectColumnIndex")
  private int subjectColumnIndex;

  @XmlElement(name = "columns")
  private Set<ColumnFeedback> columns;

  public int getSubjectColumnIndex() {
    return subjectColumnIndex;
  }

  public void setSubjectColumnIndex(int subjectColumnIndex) {
    this.subjectColumnIndex = subjectColumnIndex;
  }

  public Set<ColumnFeedback> getColumns() {
    return columns;
  }

  public void setColumns(Set<ColumnFeedback> columns) {
    this.columns = columns;
  }
}
