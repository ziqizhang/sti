package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.KnowledgeBaseValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;


public class KnowledgeBaseAdapter
    extends XmlAdapter<KnowledgeBaseValue, KnowledgeBase> {

  @Override
  public KnowledgeBaseValue marshal(KnowledgeBase bound)
      throws Exception {
    return new KnowledgeBaseValue(bound);
  }

  @Override
  public KnowledgeBase unmarshal(KnowledgeBaseValue value)
      throws Exception {
    return new KnowledgeBase();
  }
}
