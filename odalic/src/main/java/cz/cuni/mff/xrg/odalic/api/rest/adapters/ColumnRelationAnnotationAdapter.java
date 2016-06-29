package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnRelationAnnotationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.EntityCandidateValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;


public class ColumnRelationAnnotationAdapter extends XmlAdapter<ColumnRelationAnnotationValue, ColumnRelationAnnotation> {

  @Override
  public ColumnRelationAnnotationValue marshal(ColumnRelationAnnotation bound) throws Exception {
    return new ColumnRelationAnnotationValue(bound);
  }

  @Override
  public ColumnRelationAnnotation unmarshal(ColumnRelationAnnotationValue value) throws Exception {
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

    return new ColumnRelationAnnotation(candidatesBuilder.build(), chosenBuilder.build());
  }
}
