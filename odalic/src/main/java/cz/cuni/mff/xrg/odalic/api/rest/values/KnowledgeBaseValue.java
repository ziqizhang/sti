package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.xml.bind.annotation.XmlRootElement;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

@XmlRootElement(name = "knowledgeBase")
public class KnowledgeBaseValue {

  public KnowledgeBaseValue() {}
  
  public KnowledgeBaseValue(KnowledgeBase adaptee) {}
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "KnowledgeBaseValue";
  }
}
