package cz.cuni.mff.xrg.odalic.files;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.CustomJsonDateDeserializer;

@XmlRootElement
public class File implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  @XmlElement(name = "id")
  private String id;

  @JsonSerialize(using = CustomJsonDateSerializer.class)
  @JsonDeserialize(using = CustomJsonDateDeserializer.class)
  @XmlElement(name = "uploaded")
  private Date uploaded;

  @XmlElement(name = "format")
  private String format;

  @XmlElement(name = "owner")
  private String owner;

  public File(String id, Date uploaded, String format, String owner) {
    this.id = id;
    this.uploaded = uploaded;
    this.format = format;
    this.owner = owner;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getUploaded() {
    return uploaded;
  }

  public void setUploaded(Date uploaded) {
    this.uploaded = uploaded;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }
}
