package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnPositionValue;
import cz.cuni.mff.xrg.odalic.feedbacks.types.ColumnPosition;


public class ColumnPositionAdapter
    extends XmlAdapter<ColumnPositionValue, ColumnPosition> {

  @Override
  public ColumnPositionValue marshal(ColumnPosition bound)
      throws Exception {
    return new ColumnPositionValue(bound);
  }

  @Override
  public ColumnPosition unmarshal(ColumnPositionValue value)
      throws Exception {
    return new ColumnPosition(value.getIndex());
  }
}
