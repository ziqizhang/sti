package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnIgnoreValue;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;

public final class ColumnIgnoreAdapter extends XmlAdapter<ColumnIgnoreValue, ColumnIgnore> {

  @Override
  public ColumnIgnoreValue marshal(ColumnIgnore bound) throws Exception {
    return new ColumnIgnoreValue(bound);
  }

  @Override
  public ColumnIgnore unmarshal(ColumnIgnoreValue value) throws Exception {
    return new ColumnIgnore(value.getPosition());
  }

}
