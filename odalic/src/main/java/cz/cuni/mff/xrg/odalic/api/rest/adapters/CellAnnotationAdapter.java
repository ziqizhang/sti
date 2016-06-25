package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.CellAnnotationValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;


public class CellAnnotationAdapter
    extends XmlAdapter<CellAnnotationValue, CellAnnotation> {

  @Override
  public CellAnnotationValue marshal(CellAnnotation bound)
      throws Exception {
    return new CellAnnotationValue(bound);
  }

  @Override
  public CellAnnotation unmarshal(CellAnnotationValue value)
      throws Exception {
    return new CellAnnotation(value.getCandidates(), value.getChosen());
  }
}
