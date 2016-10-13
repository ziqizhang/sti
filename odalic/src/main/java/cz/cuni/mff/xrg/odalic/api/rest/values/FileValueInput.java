package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.net.URL;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.files.File;

/**
 * Domain class {@link File} adapted for REST API input.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "file")
public final class FileValueInput implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  @XmlElement
  private URL location;

  public FileValueInput() {}
  
  public FileValueInput(File adaptee) {
    location = adaptee.getLocation();
  }

  /**
   * @return the location
   */
  @Nullable
  @XmlElement
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FileValueInput [location=" + location + "]";
  }
}
