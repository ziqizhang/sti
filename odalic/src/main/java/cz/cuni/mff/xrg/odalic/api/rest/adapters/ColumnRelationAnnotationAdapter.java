package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnRelationAnnotationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;


public final class ColumnRelationAnnotationAdapter
    extends XmlAdapter<ColumnRelationAnnotationValue, ColumnRelationAnnotation> {

  @Override
  public ColumnRelationAnnotationValue marshal(ColumnRelationAnnotation bound) throws Exception {
    return new ColumnRelationAnnotationValue(bound);
  }

  @Override
  public ColumnRelationAnnotation unmarshal(ColumnRelationAnnotationValue value) throws Exception {
    final Map<KnowledgeBase, NavigableSet<EntityCandidate>> candidates =
        Annotations.toNavigableDomain(value.getCandidates());
    final Map<KnowledgeBase, Set<EntityCandidate>> chosen = Annotations.toDomain(value.getChosen());

    return new ColumnRelationAnnotation(candidates, chosen);
  }
}
