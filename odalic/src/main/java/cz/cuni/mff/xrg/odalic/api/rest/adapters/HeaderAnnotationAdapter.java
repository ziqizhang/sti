package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.HeaderAnnotationValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;


public class HeaderAnnotationAdapter
    extends XmlAdapter<HeaderAnnotationValue, HeaderAnnotation> {

  @Override
  public HeaderAnnotationValue marshal(HeaderAnnotation bound)
      throws Exception {
    return new HeaderAnnotationValue(bound);
  }

  @Override
  public HeaderAnnotation unmarshal(HeaderAnnotationValue value)
      throws Exception {
    return new HeaderAnnotation(value.getCandidates(), value.getChosen());
  }
}
