package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.values.CellRelationAnnotationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.EntityCandidateValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;


public final class CellRelationAnnotationAdapter extends XmlAdapter<CellRelationAnnotationValue, CellRelationAnnotation> {

  @Override
  public CellRelationAnnotationValue marshal(CellRelationAnnotation bound) throws Exception {
    return new CellRelationAnnotationValue(bound);
  }

  @Override
  public CellRelationAnnotation unmarshal(CellRelationAnnotationValue value) throws Exception {
    final ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidate>> candidatesBuilder =
        ImmutableMap.builder();
    final ImmutableMap.Builder<KnowledgeBase, Set<EntityCandidate>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<KnowledgeBase, NavigableSet<EntityCandidateValue>> entry : value
        .getCandidates().entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final Set<EntityCandidateValue> values = entry.getValue();

      candidatesBuilder.put(base, ImmutableSortedSet.copyOf(values.stream()
          .map(e -> new EntityCandidate(e.getEntity(), e.getLikelihood())).iterator()));
      chosenBuilder.put(base, ImmutableSet.copyOf(values.stream().filter(e -> e.isChosen())
          .map(e -> new EntityCandidate(e.getEntity(), e.getLikelihood())).iterator()));
    }

    return new CellRelationAnnotation(candidatesBuilder.build(), chosenBuilder.build());
  }
}
