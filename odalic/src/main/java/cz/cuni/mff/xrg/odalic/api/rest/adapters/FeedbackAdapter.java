package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.jena.ext.com.google.common.collect.ImmutableMap;

import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.values.FeedbackValue;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;


public final class FeedbackAdapter extends XmlAdapter<FeedbackValue, Feedback> {

  @Override
  public FeedbackValue marshal(Feedback bound) throws Exception {
    return new FeedbackValue(bound);
  }

  @Override
  public Feedback unmarshal(FeedbackValue value) throws Exception {
    if (value == null) {
      return new Feedback();
    }

    return new Feedback(
        value.getSubjectColumnPositions() == null ? ImmutableMap.of()
            : value.getSubjectColumnPositions(),
        value.getColumnIgnores() == null ? ImmutableSet.of() : value.getColumnIgnores(),
        value.getColumnAmbiguities() == null ? ImmutableSet.of() : value.getColumnAmbiguities(),
        value.getClassifications() == null ? ImmutableSet.of() : value.getClassifications(),
        value.getColumnRelations() == null ? ImmutableSet.of() : value.getColumnRelations(),
        value.getDisambiguations() == null ? ImmutableSet.of() : value.getDisambiguations(),
        value.getAmbiguities() == null ? ImmutableSet.of() : value.getAmbiguities());
  }
}
