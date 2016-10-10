package cz.cuni.mff.xrg.odalic.api.rest.values.util;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.values.EntityCandidateValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Annotation conversion utilities.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public final class Annotations {

  private Annotations() {}

  public static Map<KnowledgeBase, NavigableSet<EntityCandidateValue>> toNavigableValues(
      final Map<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidate>> candidates) {
    final ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidateValue>> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidate>> entry : candidates
        .entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final NavigableSet<? extends EntityCandidate> baseCandidates = entry.getValue();

      final Stream<EntityCandidateValue> stream =
          baseCandidates.stream().map(e -> new EntityCandidateValue(e));
      candidatesBuilder.put(base, ImmutableSortedSet.copyOf(stream.iterator()));
    }
    return candidatesBuilder.build();
  }

  public static Map<KnowledgeBase, Set<EntityCandidateValue>> toValues(
      final Map<KnowledgeBase, Set<EntityCandidate>> chosen) {
    final ImmutableMap.Builder<KnowledgeBase, Set<EntityCandidateValue>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<KnowledgeBase, Set<EntityCandidate>> entry : chosen.entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final Set<EntityCandidate> baseChosen = entry.getValue();

      final Stream<EntityCandidateValue> stream =
          baseChosen.stream().map(e -> new EntityCandidateValue(e));
      chosenBuilder.put(base, ImmutableSet.copyOf(stream.iterator()));
    }
    return chosenBuilder.build();
  }

  public static Map<KnowledgeBase, NavigableSet<EntityCandidateValue>> copyNavigableValues(
      Map<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidateValue>> candidates) {
    final ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidateValue>> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends Set<? extends EntityCandidateValue>> candidateEntry : candidates
        .entrySet()) {
      candidatesBuilder.put(candidateEntry.getKey(),
          ImmutableSortedSet.copyOf(candidateEntry.getValue()));
    }

    return candidatesBuilder.build();
  }

  public static Map<KnowledgeBase, Set<EntityCandidateValue>> copyValues(
      Map<? extends KnowledgeBase, ? extends Set<? extends EntityCandidateValue>> chosen) {
    final ImmutableMap.Builder<KnowledgeBase, Set<EntityCandidateValue>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends Set<? extends EntityCandidateValue>> chosenEntry : chosen
        .entrySet()) {
      chosenBuilder.put(chosenEntry.getKey(), ImmutableSet.copyOf(chosenEntry.getValue()));
    }

    return chosenBuilder.build();
  }

  public static Map<KnowledgeBase, Set<EntityCandidate>> toDomain(
      final Map<? extends KnowledgeBase, ? extends Set<? extends EntityCandidateValue>> chosenValues) {
    final ImmutableMap.Builder<KnowledgeBase, Set<EntityCandidate>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends Set<? extends EntityCandidateValue>> entry : chosenValues
        .entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final Set<? extends EntityCandidateValue> values = entry.getValue();

      chosenBuilder.put(base, ImmutableSet.copyOf(
          values.stream().map(e -> new EntityCandidate(e.getEntity(), e.getScore())).iterator()));
    }
    final Map<KnowledgeBase, Set<EntityCandidate>> chosen = chosenBuilder.build();
    return chosen;
  }

  public static Map<KnowledgeBase, NavigableSet<EntityCandidate>> toNavigableDomain(
      final Map<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidateValue>> candidateValues) {
    final ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidate>> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidateValue>> entry : candidateValues
        .entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final Set<? extends EntityCandidateValue> values = entry.getValue();

      candidatesBuilder.put(base, ImmutableSortedSet.copyOf(
          values.stream().map(e -> new EntityCandidate(e.getEntity(), e.getScore())).iterator()));
    }
    final Map<KnowledgeBase, NavigableSet<EntityCandidate>> candidates = candidatesBuilder.build();
    return candidates;
  }

}
