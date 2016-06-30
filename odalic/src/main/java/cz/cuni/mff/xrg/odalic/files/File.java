package cz.cuni.mff.xrg.odalic.files;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateDeserializer;

@XmlRootElement(name = "file")
public class File implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  @XmlElement
  private String id;

  @JsonSerialize(using = CustomJsonDateSerializer.class)
  @JsonDeserialize(using = CustomJsonDateDeserializer.class)
  @XmlElement
  private Date uploaded;

  @XmlElement
  private String owner;
  
  @XmlElement
  private URL location;

  @SuppressWarnings("unused")
  private File() {
    id = null;
    uploaded = null;    
    owner = null;
  }
  
  public File(String id, String owner, URL location) throws MalformedURLException {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(owner);
    Preconditions.checkNotNull(location);
    
    this.id = id;
    this.uploaded = new Date();
    this.owner = owner;
    this.location = location;
  }
  
  public File(String id, Date uploaded, String owner, URL location) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(uploaded);
    Preconditions.checkNotNull(owner);
    Preconditions.checkNotNull(location);
    
    this.id = id;
    this.uploaded = uploaded;
    this.owner = owner;
    this.location = location;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the uploaded
   */
  public Date getUploaded() {
    return uploaded;
  }

  /**
   * @param uploaded the uploaded to set
   */
  public void setUploaded(Date uploaded) {
    this.uploaded = uploaded;
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
    this.owner = owner;
  }

  /**
   * @return the location
   */
  public URL getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(URL location) {
    this.location = location;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((location == null) ? 0 : location.hashCode());
    result = prime * result + ((owner == null) ? 0 : owner.hashCode());
    result = prime * result + ((uploaded == null) ? 0 : uploaded.hashCode());
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
    File other = (File) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (location == null) {
      if (other.location != null) {
        return false;
      }
    } else if (!location.equals(other.location)) {
      return false;
    }
    if (owner == null) {
      if (other.owner != null) {
        return false;
      }
    } else if (!owner.equals(other.owner)) {
      return false;
    }
    if (uploaded == null) {
      if (other.uploaded != null) {
        return false;
      }
    } else if (!uploaded.equals(other.uploaded)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "File [id=" + id + ", uploaded=" + uploaded + ", owner=" + owner + ", location="
        + location + "]";
  }
}
