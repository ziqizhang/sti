package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Domain class {@link KnowledgeBase} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "knowledgeBase")
public final class KnowledgeBaseValue {

  private String name;
  
  public KnowledgeBaseValue() {}
  
  public KnowledgeBaseValue(KnowledgeBase adaptee) {
    name = adaptee.getName();
  }
  
  /**
   * @return the name
   */
  @XmlElement
  @Nullable
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    Preconditions.checkNotNull(name);
    
    this.name = name;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "KnowledgeBaseValue [name=" + name + "]";
  }
}
