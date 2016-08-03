package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.CellRelationValue;
import cz.cuni.mff.xrg.odalic.feedbacks.CellRelation;


public final class CellRelationAdapter
    extends XmlAdapter<CellRelationValue, CellRelation> {

  @Override
  public CellRelationValue marshal(CellRelation bound)
      throws Exception {
    return new CellRelationValue(bound);
  }

  @Override
  public CellRelation unmarshal(CellRelationValue value)
      throws Exception {
    return new CellRelation(value.getPosition(), value.getAnnotation());
  }
}
