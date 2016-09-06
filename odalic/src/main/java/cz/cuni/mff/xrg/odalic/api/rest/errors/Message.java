package cz.cuni.mff.xrg.odalic.api.rest.errors;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;


/**
 * Reporting message with extra details for developers.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement
public final class Message {

  private int status;

  private String text;

  private String link;

  private String developerText;

  public Message() {
    status = Integer.MIN_VALUE;
  }

  /**
   * Creates new message.
   * 
   * @param status HTTP status code
   */
  public Message(int status) {
    Preconditions.checkArgument(status >= 100);
    Preconditions.checkArgument(status <= 599);
    
    this.status = status;
  }

  /**
   * @return the status
   */
  @XmlElement
  public int getStatus() {
    return status;
  }

  /**
   * @return the text
   */
  @XmlElement
  @Nullable
  public String getText() {
    return text;
  }

  /**
   * @param text the text to set
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * @return the link
   */
  @XmlElement
  @Nullable
  public String getLink() {
    return link;
  }

  /**
   * @param link the link to set
   */
  public void setLink(String link) {
    this.link = link;
  }

  /**
   * @return the developer text
   */
  @XmlElement
  @Nullable
  public String getDeveloperText() {
    return developerText;
  }

  /**
   * @param developerText the developer text to set
   */
  public void setDeveloperText(String developerText) {
    this.developerText = developerText;
  }
}
