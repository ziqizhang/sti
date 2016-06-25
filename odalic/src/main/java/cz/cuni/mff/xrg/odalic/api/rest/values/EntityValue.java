package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.net.URI;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

@XmlRootElement(name = "entity")
public class EntityValue implements Serializable {

  private static final long serialVersionUID = 5750987769573292984L;

  @XmlElement
  private URI resource;

  @XmlElement
  private String label;

  public EntityValue() {}

  public EntityValue(Entity adaptee) {
    this.resource = adaptee.getResource();
    this.label = adaptee.getLabel();
  }

  /**
   * @return the resource
   */
  @Nullable
  public URI getResource() {
    return resource;
  }

  /**
   * @param resource the resource to set
   */
  public void setResouce(URI resource) {
    Preconditions.checkNotNull(resource);
    
    this.resource = resource;
  }

  /**
   * @return the label
   */
  @Nullable
  public String getLabel() {
    return label;
  }

  /**
   * @param label the label to set
   */
  public void setLabel(String label) {
    Preconditions.checkNotNull(label);
    
    this.label = label;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityValue [resource=" + resource + ", label=" + label + "]";
  }
}
