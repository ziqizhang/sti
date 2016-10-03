package uk.ac.shef.dcs.sti.core.extension.annotations;

import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;


/**
 * Annotates cell in a table.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public final class CellAnnotation {

  private final NavigableSet<EntityCandidate> candidates;
  
  private final Set<EntityCandidate> chosen;

  /**
   * Creates new annotation.
   * 
   * @param candidates all possible candidates for the assigned entity sorted by with their
   *        likelihood
   * @param chosen subset of candidates chosen to annotate the element
   */
  public CellAnnotation(Set<? extends EntityCandidate> candidates,
      Set<? extends EntityCandidate> chosen) {
    Preconditions.checkNotNull(candidates);
    Preconditions.checkNotNull(chosen);
    Preconditions.checkArgument(candidates.containsAll(chosen));
    
    this.candidates = ImmutableSortedSet.copyOf(candidates);
    this.chosen = ImmutableSet.copyOf(chosen);
  }

  /**
   * @return the candidates
   */
  public NavigableSet<EntityCandidate> getCandidates() {
    return candidates;
  }

  /**
   * @return the chosen
   */
  public Set<EntityCandidate> getChosen() {
    return chosen;
  }

  /**
   * Computes hash code based on the candidates and the chosen.
   * 
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

  /**
   * Compares for equality (only other annotation of the same kind with equally ordered set of
   * candidates and the same chosen set passes).
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public String toString() {
    return "CellAnnotation [candidates=" + candidates + ", chosen=" + chosen + "]";
  }
}
