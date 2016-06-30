package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonSerializer;


@XmlRootElement(name = "cellAnnotation")
public final class CellAnnotation {

  @XmlElement
  @JsonDeserialize(keyUsing = KnowledgeBaseKeyJsonDeserializer.class)
  @JsonSerialize(keyUsing = KnowledgeBaseKeyJsonSerializer.class)
  private final Map<KnowledgeBase, NavigableSet<EntityCandidate>> candidates;
  
  @XmlElement
  @JsonDeserialize(keyUsing = KnowledgeBaseKeyJsonDeserializer.class)
  @JsonSerialize(keyUsing = KnowledgeBaseKeyJsonSerializer.class)
  private final Map<KnowledgeBase, Set<EntityCandidate>> chosen;

  @SuppressWarnings("unused")
  private CellAnnotation() {
    candidates = ImmutableMap.of();
    chosen = ImmutableMap.of();
  }
  
  /**
   * @param candidates
   * @param chosen
   */
  public CellAnnotation(Map<? extends KnowledgeBase, ? extends Set<? extends EntityCandidate>> candidates,
      Map<? extends KnowledgeBase, ? extends Set<? extends EntityCandidate>> chosen) {
    Preconditions.checkNotNull(candidates);
    Preconditions.checkNotNull(chosen);
    
    ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidate>> candidatesBuilder = ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends Set<? extends EntityCandidate>> candidateEntry : candidates.entrySet()) {
      candidatesBuilder.put(candidateEntry.getKey(), ImmutableSortedSet.copyOf(candidateEntry.getValue()));
    }
    this.candidates = candidatesBuilder.build();
    
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

  /**
   * @return the candidates
   */
  public Map<KnowledgeBase, NavigableSet<EntityCandidate>> getCandidates() {
    return candidates;
  }

  /**
   * @return the chosen
   */
  public Map<KnowledgeBase, Set<EntityCandidate>> getChosen() {
    return chosen;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((candidates == null) ? 0 : candidates.hashCode());
    result = prime * result + ((chosen == null) ? 0 : chosen.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CellAnnotation other = (CellAnnotation) obj;
    if (candidates == null) {
      if (other.candidates != null) {
        return false;
      }
    } else if (!candidates.equals(other.candidates)) {
      return false;
    }
    if (chosen == null) {
      if (other.chosen != null) {
        return false;
      }
    } else if (!chosen.equals(other.chosen)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CellAnnotation [candidates=" + candidates + ", chosen=" + chosen + "]";
  }
}
