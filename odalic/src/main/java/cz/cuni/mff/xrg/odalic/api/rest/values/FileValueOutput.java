package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomDateJsonSerializer;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomDateJsonDeserializer;

/**
 * Domain class {@link File} adapted for REST API output.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "file")
public final class FileValueOutput implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private String id;

  private Date uploaded;

  private String owner;
  
  private URL location;
  
  private boolean cached;

  public FileValueOutput() {}
  
  public FileValueOutput(File adaptee) {
    id = adaptee.getId();
    uploaded = adaptee.getUploaded();
    owner = adaptee.getOwner();
    location = adaptee.getLocation();
    cached = adaptee.isCached();
  }

  /**
   * @return the id
   */
  @XmlElement
  @Nullable
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    Preconditions.checkNotNull(id);
    
    this.id = id;
  }

  /**
   * @return the uploaded
   */
  @JsonSerialize(using = CustomDateJsonSerializer.class)
  @JsonDeserialize(using = CustomDateJsonDeserializer.class)
  @XmlElement
  @Nullable
  public Date getUploaded() {
    return uploaded;
  }

  /**
   * @param uploaded the uploaded to set
   */
  public void setUploaded(Date uploaded) {
    Preconditions.checkNotNull(uploaded);
    
    this.uploaded = uploaded;
  }

  /**
   * @return the owner
   */
  @XmlElement
  @Nullable
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
  @XmlElement
  @Nullable
  public URL getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(URL location) {
    Preconditions.checkNotNull(location);
    
    this.location = location;
  }
  
  /**
   * @return cached
   */
  @XmlElement
  public boolean isCached() {
    return cached;
  }

  /**
   * @param cached cached
   */
  public void setCached(boolean cached) {    
    this.cached = cached;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FileValueOutput [id=" + id + ", uploaded=" + uploaded + ", owner=" + owner + ", location="
        + location + ", cached=" + cached + "]";
  }
}
