package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateSerializer;
import cz.cuni.mff.xrg.odalic.files.File;

/**
 * Domain class {@link File} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "file")
public final class FileValueOutput implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  @XmlElement
  private String id;

  @JsonSerialize(using = CustomJsonDateSerializer.class)
  @XmlElement
  private Date uploaded;

  @XmlElement
  private String owner;
  
  @XmlElement
  private URL location;

  public FileValueOutput() {}
  
  public FileValueOutput(File adaptee) {
    id = adaptee.getId();
    uploaded = adaptee.getUploaded();
    owner = adaptee.getOwner();
    location = adaptee.getLocation();
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the uploaded
   */
  public Date getUploaded() {
    return uploaded;
  }

  /**
   * @return the owner
   */
  public String getOwner() {
    return owner;
  }

  /**
   * @param owner the owner to set
   */
  public void setOwner(String owner) {
    Preconditions.checkNotNull(owner);
    
    this.owner = owner;
  }

  /**
   * @return the location
   */
  public URL getLocation() {
    return location;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FileValueOutput [id=" + id + ", uploaded=" + uploaded + ", owner=" + owner + ", location="
        + location + "]";
  }
}
