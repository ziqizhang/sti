package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;


import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnRelationAnnotationValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;


public final class ColumnRelationAnnotationAdapter extends XmlAdapter<ColumnRelationAnnotationValue, ColumnRelationAnnotation> {

  @Override
  public ColumnRelationAnnotationValue marshal(ColumnRelationAnnotation bound) throws Exception {
    return new ColumnRelationAnnotationValue(bound);
  }

  @Override
  public ColumnRelationAnnotation unmarshal(ColumnRelationAnnotationValue value) throws Exception {
    return new ColumnRelationAnnotation(value.getCandidates(), value.getChosen());
  }
}
