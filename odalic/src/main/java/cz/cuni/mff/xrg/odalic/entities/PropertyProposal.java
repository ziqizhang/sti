package cz.cuni.mff.xrg.odalic.entities;

import java.net.URI;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.PropertyProposalAdapter;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

/**
 * <p>
 * Model of the proposal for a new property in the primary base, that the user provides to the
 * server.
 * </p>
 * 
 * <p>
 * Every property used in the user feedback must already exist in any of the present bases, if it
 * does not, the user is encouraged to enter it first as a proposal.
 * </p>
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(PropertyProposalAdapter.class)
public final class PropertyProposal {

  private final String label;

  private final NavigableSet<String> alternativeLabels;

  private final URI suffix;

  private final Entity superProperty;

  private final Entity domain;

  private final Entity range;

  public PropertyProposal(final String label, final Set<? extends String> alternativeLabels,
      final URI suffix, final Entity superProperty, final Entity domain, final Entity range) {
    Preconditions.checkNotNull(label);
    Preconditions.checkArgument(suffix == null || !suffix.isAbsolute(),
        "The suffix must be a relative URI!");

    this.label = label;
    this.alternativeLabels = ImmutableSortedSet.copyOf(alternativeLabels);
    this.suffix = suffix;
    this.superProperty = superProperty;
    this.domain = domain;
    this.range = range;
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
   * @return the super property
   */
  @Nullable
  public Entity getSuperProperty() {
    return superProperty;
  }

  /**
   * @return the domain
   */
  public Entity getDomain() {
    return domain;
  }

  /**
   * @return the range
   */
  public Entity getRange() {
    return range;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
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
    PropertyProposal other = (PropertyProposal) obj;
    if (suffix == null) {
      if (other.suffix != null) {
        return false;
      }
    } else if (!suffix.equals(other.suffix)) {
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "PropertyProposal [label=" + label + ", alternativeLabels=" + alternativeLabels
        + ", suffix=" + suffix + ", superProperty=" + superProperty + ", domain=" + domain
        + ", range=" + range + "]";
  }
}
