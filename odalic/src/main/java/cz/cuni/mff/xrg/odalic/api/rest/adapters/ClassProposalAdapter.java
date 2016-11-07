package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.ws.rs.BadRequestException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ClassProposalValue;
import cz.cuni.mff.xrg.odalic.entities.ClassProposal;


public final class ClassProposalAdapter
    extends XmlAdapter<ClassProposalValue, ClassProposal> {

  @Override
  public ClassProposalValue marshal(ClassProposal bound)
      throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public ClassProposal unmarshal(ClassProposalValue value)
      throws Exception {
    try {
      return new ClassProposal(value.getLabel(), value.getAlternativeLabels(), value.getSuffix(), value.getSuperClass());
    } catch (final IllegalArgumentException | NullPointerException e) {
      throw new BadRequestException(e);
    }
  }
}
