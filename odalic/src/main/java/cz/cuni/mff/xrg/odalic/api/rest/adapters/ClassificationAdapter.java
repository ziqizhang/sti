package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ClassificationValue;
import cz.cuni.mff.xrg.odalic.feedbacks.Classification;


public final class ClassificationAdapter
    extends XmlAdapter<ClassificationValue, Classification> {

  @Override
  public ClassificationValue marshal(Classification bound)
      throws Exception {
    return new ClassificationValue(bound);
  }

  @Override
  public Classification unmarshal(ClassificationValue value)
      throws Exception {
    return new Classification(value.getPosition(), value.getAnnotation());
  }
}
