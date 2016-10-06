package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ScoreValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;


public final class ScoreAdapter
    extends XmlAdapter<ScoreValue, Score> {

  @Override
  public ScoreValue marshal(Score bound)
      throws Exception {
    return new ScoreValue(bound);
  }

  @Override
  public Score unmarshal(ScoreValue value)
      throws Exception {
    return new Score(value.getValue());
  }
}
