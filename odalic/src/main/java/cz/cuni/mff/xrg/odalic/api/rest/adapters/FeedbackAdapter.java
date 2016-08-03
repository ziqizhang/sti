package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.FeedbackValue;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;


public final class FeedbackAdapter extends XmlAdapter<FeedbackValue, Feedback> {

  @Override
  public FeedbackValue marshal(Feedback bound) throws Exception {
    return new FeedbackValue(bound);
  }

  @Override
  public Feedback unmarshal(FeedbackValue value) throws Exception {
    return new Feedback(value.getSubjectColumnPosition(), value.getColumnIgnores(),
        value.getColumnAmbiguities(), value.getClassifications(), value.getCellRelations(),
        value.getColumnRelations(), value.getDisambiguations(), value.getAmbiguities());
  }
}
