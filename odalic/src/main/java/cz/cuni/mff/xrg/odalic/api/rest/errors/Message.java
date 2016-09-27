package cz.cuni.mff.xrg.odalic.api.rest.errors;

import java.net.URI;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;


/**
 * Reporting message with extra details for developers.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement
public final class Message {

  @XmlElement
  private final String text;

  @XmlElement
  private final List<URI> additionalResources;

  @XmlElement
  private final String debugContent;


  @XmlTransient
  public static Message of(@Nullable String text, @Nullable String debugContent) {
    return new MessageBuilder().text(text).debugContent(debugContent).build();
  }

  @XmlTransient
  public static Message of(@Nullable String text) {
    return new MessageBuilder().text(text).build();
  }

  public Message(@Nullable String text, List<URI> additionalResources,
      @Nullable String debugContent) {
    Preconditions.checkNotNull(additionalResources);

    this.text = text;
    this.additionalResources = ImmutableList.copyOf(additionalResources);
    this.debugContent = debugContent;
  }

  /**
   * @return the text
   */
  @Nullable
  public String getText() {
    return text;
  }

  /**
   * @return the additional resources
   */
  public List<URI> getAdditionalResources() {
    return additionalResources;
  }

  /**
   * @return the debug content
   */
  @Nullable
  public String getDebugContent() {
    return debugContent;
  }

  /**
   * Utility method that wraps the message into a JSON response and assigns it the provided
   * {@link StatusType}.
   * 
   * @param statusType status type
   * @return a {@link Response}
   */
  @XmlTransient
  public Response toResponse(@Nonnull StatusType statusType) {
    return Response.status(statusType).entity(this).type(MediaType.APPLICATION_JSON).build();
  }
}
