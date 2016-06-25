package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.CellPositionValue;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;


public class CellPositionAdapter
    extends XmlAdapter<CellPositionValue, CellPosition> {

  @Override
  public CellPositionValue marshal(CellPosition bound)
      throws Exception {
    return new CellPositionValue(bound);
  }

  @Override
  public CellPosition unmarshal(CellPositionValue value)
      throws Exception {
    return new CellPosition(value.getRowPosition(), value.getColumnPosition());
  }
}
