package cz.cuni.mff.xrg.odalic.files;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.FileAdapter;

/**
 * File description.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(FileAdapter.class)
public class File implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private final String id;

  private final Date uploaded;

  private final String owner;
  
  private final URL location;

  /**
   * Create new file description.
   * 
   * @param id file ID
   * @param uploaded time of upload
   * @param owner file owner description
   * @param location file location
   */
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
   * Create new file description for a file uploaded now.
   * 
   * @param id file ID
   * @param owner file owner description
   * @param location file location
   */
  public File(String id, String owner, URL location) {
    this(id, new Date(), owner, location);
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
   * @return the location
   */
  public URL getLocation() {
    return location;
  }

  /**
   * Computes the hash code based on all components.
   * 
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

  /**
   * Compares for equivalence (only other File description with the same components passes).
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
