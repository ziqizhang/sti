package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.CellRelationPositionValue;
import cz.cuni.mff.xrg.odalic.positions.CellRelationPosition;


public final class CellRelationPositionAdapter
    extends XmlAdapter<CellRelationPositionValue, CellRelationPosition> {

  @Override
  public CellRelationPositionValue marshal(CellRelationPosition bound)
      throws Exception {
    return new CellRelationPositionValue(bound);
  }

  @Override
  public CellRelationPosition unmarshal(CellRelationPositionValue value)
      throws Exception {
    return new CellRelationPosition(value.getColumnsPosition(), value.getRowPosition());
  }
}
