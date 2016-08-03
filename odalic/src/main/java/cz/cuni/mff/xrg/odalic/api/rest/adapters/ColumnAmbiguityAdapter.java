package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnAmbiguityValue;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnAmbiguity;

public final class ColumnAmbiguityAdapter extends XmlAdapter<ColumnAmbiguityValue, ColumnAmbiguity> {

  @Override
  public ColumnAmbiguityValue marshal(ColumnAmbiguity bound) throws Exception {
    return new ColumnAmbiguityValue(bound);
  }

  @Override
  public ColumnAmbiguity unmarshal(ColumnAmbiguityValue value) throws Exception {
    return new ColumnAmbiguity(value.getPosition());
  }

}
