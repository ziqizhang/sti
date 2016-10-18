package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.CellAnnotationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;


public final class CellAnnotationAdapter
    extends XmlAdapter<CellAnnotationValue, CellAnnotation> {

  @Override
  public CellAnnotationValue marshal(CellAnnotation bound) throws Exception {
    return new CellAnnotationValue(bound);
  }

  @Override
  public CellAnnotation unmarshal(CellAnnotationValue value) throws Exception {
    final Map<KnowledgeBase, NavigableSet<EntityCandidate>> candidates =
        Annotations.toNavigableDomain(value.getCandidates());
    final Map<KnowledgeBase, Set<EntityCandidate>> chosen = Annotations.toDomain(value.getChosen());

    return new CellAnnotation(candidates, chosen);
  }
}
