package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;
import java.net.URI;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ColumnFeedback implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  @XmlElement(name = "index")
  private int index;
  
  @XmlElement(name = "suggestions")
  private Set<URI> suggestions;

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public Set<URI> getSuggestions() {
    return suggestions;
  }

  public void setSuggestions(Set<URI> suggestions) {
    this.suggestions = suggestions;
  }  
}
