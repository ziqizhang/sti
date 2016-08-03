package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnRelationValue;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnRelation;


public final class ColumnRelationAdapter extends XmlAdapter<ColumnRelationValue, ColumnRelation> {

  @Override
  public ColumnRelationValue marshal(ColumnRelation bound) throws Exception {
    return new ColumnRelationValue(bound);
  }

  @Override
  public ColumnRelation unmarshal(ColumnRelationValue value) throws Exception {
    return new ColumnRelation(value.getPosition(), value.getAnnotation());
  }
}
