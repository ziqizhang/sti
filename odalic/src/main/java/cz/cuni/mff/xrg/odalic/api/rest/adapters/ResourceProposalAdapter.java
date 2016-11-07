package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.ws.rs.BadRequestException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ResourceProposalValue;
import cz.cuni.mff.xrg.odalic.entities.ResourceProposal;


public final class ResourceProposalAdapter
    extends XmlAdapter<ResourceProposalValue, ResourceProposal> {

  @Override
  public ResourceProposalValue marshal(ResourceProposal bound)
      throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResourceProposal unmarshal(ResourceProposalValue value)
      throws Exception {
    try {
      return new ResourceProposal(value.getLabel(), value.getAlternativeLabels(), value.getSuffix(), value.getClasses());
    } catch (final IllegalArgumentException | NullPointerException e) {
      throw new BadRequestException(e);
    }
  }
}
