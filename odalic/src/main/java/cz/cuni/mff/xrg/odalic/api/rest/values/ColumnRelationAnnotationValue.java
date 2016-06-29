package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

@XmlRootElement(name = "columnRelationAnnotation")
public final class ColumnRelationAnnotationValue {

  @XmlElement
  private Map<KnowledgeBase, NavigableSet<EntityCandidateValue>> candidates;
  
  public ColumnRelationAnnotationValue() {
    candidates = ImmutableMap.of();
  }

  /**
   * @param entities
   */
  public ColumnRelationAnnotationValue(ColumnRelationAnnotation adaptee) {
    final Map<KnowledgeBase, Set<EntityCandidate>> chosen = adaptee.getChosen();
    
    final ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidateValue>> candidatesBuilder = ImmutableMap.builder();
    for (final Map.Entry<KnowledgeBase, NavigableSet<EntityCandidate>> entry : adaptee.getCandidates().entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final Set<EntityCandidate> baseChosen = chosen.get(base);
      final NavigableSet<EntityCandidate> baseCandidates = entry.getValue();
      
      final Stream<EntityCandidateValue> stream = baseCandidates.stream().map(e -> new EntityCandidateValue(e, baseChosen.contains(e)));
      candidatesBuilder.put(entry.getKey(), ImmutableSortedSet.copyOf(stream.iterator()));
    }
    
    this.candidates = candidatesBuilder.build();
  }

  /**
   * @return the candidates
   */
  public Map<KnowledgeBase, NavigableSet<EntityCandidateValue>> getCandidates() {
    return candidates;
  }

  /**
   * @param candidates the candidates to set
   */
  public void setCandidates(Map<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidateValue>> candidates) {
    ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidateValue>> candidatesBuilder = ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends Set<? extends EntityCandidateValue>> candidateEntry : candidates.entrySet()) {
      candidatesBuilder.put(candidateEntry.getKey(), ImmutableSortedSet.copyOf(candidateEntry.getValue()));
    }
    
    this.candidates = candidatesBuilder.build();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnRelationAnnotationValue [candidates=" + candidates + "]";
  }
}
