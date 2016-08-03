package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnRelationPositionValue;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;


public final class ColumnRelationPositionAdapter
    extends XmlAdapter<ColumnRelationPositionValue, ColumnRelationPosition> {

  @Override
  public ColumnRelationPositionValue marshal(ColumnRelationPosition bound)
      throws Exception {
    return new ColumnRelationPositionValue(bound);
  }

  @Override
  public ColumnRelationPosition unmarshal(ColumnRelationPositionValue value)
      throws Exception {
    return new ColumnRelationPosition(value.getFirst(), value.getSecond());
  }
}
