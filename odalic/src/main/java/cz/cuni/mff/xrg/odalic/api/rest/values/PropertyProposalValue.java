package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.net.URI;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.entities.PropertyProposal;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

/**
 * Domain property {@link PropertyProposal} adapted for the REST API.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlRootElement(name = "propertyProposal")
public final class PropertyProposalValue implements Serializable {

  private static final long serialVersionUID = 4650112693694357493L;

  private String label;

  private NavigableSet<String> alternativeLabels;

  private URI suffix;

  private Entity superProperty;

  private Entity domain;

  private Entity range;

  public PropertyProposalValue() {
    this.alternativeLabels = ImmutableSortedSet.of();
  }

  /**
   * @param label the label to set
   */
  public void setLabel(String label) {
    Preconditions.checkNotNull(label);

    this.label = label;
  }

  /**
   * @param alternativeLabels the alternativeLabels to set
   */
  public void setAlternativeLabels(Set<? extends String> alternativeLabels) {
    this.alternativeLabels = ImmutableSortedSet.copyOf(alternativeLabels);
  }

  /**
   * @param suffix the suffix to set
   */
  public void setSuffix(URI suffix) {
    this.suffix = suffix;
  }

  /**
   * @param superProperty the superProperty to set
   */
  public void setSuperProperty(Entity superProperty) {
    this.superProperty = superProperty;
  }

  /**
   * @return the label
   */
  @Nullable
  @XmlElement
  public String getLabel() {
    return label;
  }

  /**
   * @return the alternative labels
   */
  @XmlElement
  public NavigableSet<String> getAlternativeLabels() {
    return alternativeLabels;
  }

  /**
   * @return the URI suffix
   */
  @Nullable
  @XmlElement
  public URI getSuffix() {
    return suffix;
  }

  /**
   * @return the super property
   */
  @Nullable
  @XmlElement
  public Entity getSuperProperty() {
    return superProperty;
  }

  /**
   * @return the domain
   */
  @Nullable
  @XmlElement
  public Entity getDomain() {
    return domain;
  }

  /**
   * @param domain the domain to set
   */
  public void setDomain(Entity domain) {
    this.domain = domain;
  }

  /**
   * @return the range
   */
  @Nullable
  @XmlElement
  public Entity getRange() {
    return range;
  }

  /**
   * @param range the range to set
   */
  public void setRange(Entity range) {
    this.range = range;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "PropertyProposalValue [label=" + label + ", alternativeLabels=" + alternativeLabels
        + ", suffix=" + suffix + ", superProperty=" + superProperty + ", domain=" + domain
        + ", range=" + range + "]";
  }
}
