package cz.cuni.mff.xrg.odalic.api.rest.errors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Message {

  @XmlElement(name = "status")
  int status;

  @XmlElement(name = "text")
  String text;

  @XmlElement(name = "link")
  String link;

  @XmlElement(name = "developerText")
  String developerText;

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getDeveloperText() {
    return developerText;
  }

  public void setDeveloperText(String developerText) {
    this.developerText = developerText;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public Message() {}

  public Message(int status) {
    this.status = status;
  }
}
