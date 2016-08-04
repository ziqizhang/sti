package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.LikelihoodValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Likelihood;


public final class LikelihoodAdapter
    extends XmlAdapter<LikelihoodValue, Likelihood> {

  @Override
  public LikelihoodValue marshal(Likelihood bound)
      throws Exception {
    return new LikelihoodValue(bound);
  }

  @Override
  public Likelihood unmarshal(LikelihoodValue value)
      throws Exception {
    return new Likelihood(value.getValue());
  }
}
