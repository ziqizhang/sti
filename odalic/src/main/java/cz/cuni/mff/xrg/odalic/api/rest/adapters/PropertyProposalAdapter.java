package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.ws.rs.BadRequestException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.PropertyProposalValue;
import cz.cuni.mff.xrg.odalic.entities.PropertyProposal;


public final class PropertyProposalAdapter
    extends XmlAdapter<PropertyProposalValue, PropertyProposal> {

  @Override
  public PropertyProposalValue marshal(PropertyProposal bound) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public PropertyProposal unmarshal(PropertyProposalValue value) throws Exception {
    try {
      return new PropertyProposal(value.getLabel(), value.getAlternativeLabels(), value.getSuffix(),
          value.getSuperProperty(), value.getDomain(), value.getRange());
    } catch (final IllegalArgumentException | NullPointerException e) {
      throw new BadRequestException(e);
    }
  }
}
