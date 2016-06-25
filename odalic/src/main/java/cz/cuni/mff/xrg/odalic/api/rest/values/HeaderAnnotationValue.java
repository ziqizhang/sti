package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

@XmlRootElement(name = "headerAnnotation")
public final class HeaderAnnotationValue {

  @XmlElement
  private Map<KnowledgeBase, NavigableSet<EntityCandidate>> candidates;
  
  @XmlElement
  private Map<KnowledgeBase, Set<EntityCandidate>> chosen;
  
  public HeaderAnnotationValue() {
    candidates = ImmutableMap.of();
    chosen = ImmutableMap.of();
  }

  /**
   * @param entities
   */
  public HeaderAnnotationValue(HeaderAnnotation adaptee) {
    this.candidates = adaptee.getCandidates();
    this.chosen = adaptee.getChosen();
  }

  /**
   * @return the candidates
   */
  public Map<KnowledgeBase, NavigableSet<EntityCandidate>> getCandidates() {
    return candidates;
  }

  /**
   * @param candidates the candidates to set
   */
  public void setCandidates(Map<KnowledgeBase, NavigableSet<EntityCandidate>> candidates) {
    ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidate>> candidatesBuilder = ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends Set<? extends EntityCandidate>> candidateEntry : candidates.entrySet()) {
      candidatesBuilder.put(candidateEntry.getKey(), ImmutableSortedSet.copyOf(candidateEntry.getValue()));
    }
    
    for (final Map.Entry<? extends KnowledgeBase, ? extends Set<? extends EntityCandidate>> chosenEntry : chosen.entrySet()) {
      final KnowledgeBase chosenBase = chosenEntry.getKey();
      
      final Set<EntityCandidate> baseCandidates = candidates.get(chosenBase);
      Preconditions.checkArgument(baseCandidates != null);
      Preconditions.checkArgument(baseCandidates.containsAll(chosenEntry.getValue()));
    }
    
    this.candidates = candidatesBuilder.build();
  }

  /**
   * @return the chosen
   */
  public Map<KnowledgeBase, Set<EntityCandidate>> getChosen() {
    return chosen;
  }

  /**
   * @param chosen the chosen to set
   */
  public void setChosen(Map<KnowledgeBase, Set<EntityCandidate>> chosen) {
    ImmutableMap.Builder<KnowledgeBase, Set<EntityCandidate>> chosenBuilder = ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends Set<? extends EntityCandidate>> chosenEntry : chosen.entrySet()) {
      final KnowledgeBase chosenBase = chosenEntry.getKey();
      
      final Set<EntityCandidate> baseCandidates = this.candidates.get(chosenBase);
      Preconditions.checkArgument(baseCandidates != null);
      Preconditions.checkArgument(baseCandidates.containsAll(chosenEntry.getValue()));
      
      chosenBuilder.put(chosenEntry.getKey(), ImmutableSet.copyOf(chosenEntry.getValue()));
    }
    this.chosen = chosenBuilder.build();
  }
}
