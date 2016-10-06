package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.EntityCandidateValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;


public final class EntityCandidateAdapter
    extends XmlAdapter<EntityCandidateValue, EntityCandidate> {

  @Override
  public EntityCandidateValue marshal(EntityCandidate bound)
      throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public EntityCandidate unmarshal(EntityCandidateValue value)
      throws Exception {
    return new EntityCandidate(value.getEntity(),
        value.getScore());
  }
}
