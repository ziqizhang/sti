package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.HeaderAnnotationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;


public final class HeaderAnnotationAdapter
    extends XmlAdapter<HeaderAnnotationValue, HeaderAnnotation> {

  @Override
  public HeaderAnnotationValue marshal(HeaderAnnotation bound) throws Exception {
    return new HeaderAnnotationValue(bound);
  }

  @Override
  public HeaderAnnotation unmarshal(HeaderAnnotationValue value) throws Exception {
    final Map<KnowledgeBase, NavigableSet<EntityCandidate>> candidates =
        Annotations.toNavigableDomain(value.getCandidates());
    final Map<KnowledgeBase, Set<EntityCandidate>> chosen = Annotations.toDomain(value.getChosen());

    return new HeaderAnnotation(candidates, chosen);
  }
}
