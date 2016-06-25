package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.CellRelationAnnotationValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;


public class CellRelationAnnotationAdapter
    extends XmlAdapter<CellRelationAnnotationValue, CellRelationAnnotation> {

  @Override
  public CellRelationAnnotationValue marshal(CellRelationAnnotation bound)
      throws Exception {
    return new CellRelationAnnotationValue(bound);
  }

  @Override
  public CellRelationAnnotation unmarshal(CellRelationAnnotationValue value)
      throws Exception {
    return new CellRelationAnnotation(value.getCandidates(), value.getChosen());
  }
}
