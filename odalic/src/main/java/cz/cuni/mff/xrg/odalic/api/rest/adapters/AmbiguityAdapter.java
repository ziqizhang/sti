package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.AmbiguityValue;
import cz.cuni.mff.xrg.odalic.feedbacks.Ambiguity;


public final class AmbiguityAdapter
    extends XmlAdapter<AmbiguityValue, Ambiguity> {

  @Override
  public AmbiguityValue marshal(Ambiguity bound)
      throws Exception {
    return new AmbiguityValue(bound);
  }

  @Override
  public Ambiguity unmarshal(AmbiguityValue value)
      throws Exception {
    return new Ambiguity(value.getPosition());
  }
}
