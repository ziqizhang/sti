package cz.cuni.mff.xrg.odalic.entities;

import java.net.URI;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ResourceProposalAdapter;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

/**
 * <p>
 * Model of the proposal for a new entity (representing a disambiguated cell) in the primary base, that the user provides to the server.
 * </p>
 * 
 * <p>
 * Every entity used in the user feedback must already exist in any of the present bases, if it does
 * not, the user is encouraged to enter it first as a proposal.
 * </p>
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(ResourceProposalAdapter.class)
public final class ResourceProposal {

  private final String label;

  private final NavigableSet<String> alternativeLabels;

  private final URI suffix;

  private final Set<Entity> classes;

  public ResourceProposal(final String label, final Set<? extends String> alternativeLabels,
      final URI suffix, final Set<? extends Entity> classes) {
    Preconditions.checkNotNull(label);
    Preconditions.checkArgument(suffix == null || !suffix.isAbsolute(),
        "The suffix must be a relative URI!");

    this.label = label;
    this.alternativeLabels = ImmutableSortedSet.copyOf(alternativeLabels);
    this.suffix = suffix;
    this.classes = ImmutableSet.copyOf(classes);
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * @return the alternative labels
   */
  public NavigableSet<String> getAlternativeLabels() {
    return alternativeLabels;
  }

  /**
   * @return the URI suffix
   */
  public URI getSuffix() {
    return suffix;
  }

  /**
   * @return the classes the entity is a member of
   */
  @Nullable
  public Set<Entity> getClasses() {
    return this.classes;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
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
    ResourceProposal other = (ResourceProposal) obj;
    if (suffix == null) {
      if (other.suffix != null) {
        return false;
      }
    } else if (!suffix.equals(other.suffix)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ResourceProposal [label=" + label + ", alternativeLabels=" + alternativeLabels
        + ", suffix=" + suffix + ", classes=" + classes + "]";
  }
}
