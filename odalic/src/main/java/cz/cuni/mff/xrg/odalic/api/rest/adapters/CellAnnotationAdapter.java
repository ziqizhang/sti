package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.values.CellAnnotationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.EntityCandidateValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;


public final class CellAnnotationAdapter extends XmlAdapter<CellAnnotationValue, CellAnnotation> {

  @Override
  public CellAnnotationValue marshal(CellAnnotation bound) throws Exception {
    return new CellAnnotationValue(bound);
  }

  @Override
  public CellAnnotation unmarshal(CellAnnotationValue value) throws Exception {
    final ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidate>> candidatesBuilder =
        ImmutableMap.builder();
    final ImmutableMap.Builder<KnowledgeBase, Set<EntityCandidate>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<KnowledgeBase, NavigableSet<EntityCandidateValue>> entry : value
        .getCandidates().entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final Set<EntityCandidateValue> values = entry.getValue();

      candidatesBuilder.put(base, ImmutableSortedSet.copyOf(values.stream()
          .map(e -> new EntityCandidate(e.getEntity(), e.getScore())).iterator()));
      chosenBuilder.put(base, ImmutableSet.copyOf(values.stream().filter(e -> e.isChosen())
          .map(e -> new EntityCandidate(e.getEntity(), e.getScore())).iterator()));
    }

    return new CellAnnotation(candidatesBuilder.build(), chosenBuilder.build());
  }
}
