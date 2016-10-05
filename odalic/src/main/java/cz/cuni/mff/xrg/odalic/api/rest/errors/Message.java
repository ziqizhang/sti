package cz.cuni.mff.xrg.odalic.api.rest.errors;

import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
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

  private static final String LOCATION_HEADER_NAME = "Location";

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
   * Utility method that wraps the message into a JSON response builder and assigns it the provided
   * {@link StatusType}.
   * 
   * @param statusType status type
   * @return a {@link ResponseBuilder}
   */
  @XmlTransient
  public ResponseBuilder toResponseBuilder(StatusType statusType) {
    Preconditions.checkNotNull(statusType);

    return Response.status(statusType).entity(this).type(MediaType.APPLICATION_JSON);
  }

  /**
   * Utility method that wraps the message into a JSON response and assigns it the provided
   * {@link StatusType}.
   * 
   * @param statusType status type
   * @return a {@link Response}
   */
  @XmlTransient
  public Response toResponse(StatusType statusType) {
    return toResponseBuilder(statusType).build();
  }

  /**
   * Utility method that wraps the message into a JSON response and assigns it the provided
   * {@link StatusType} and location header content.
   * 
   * @param statusType status type
   * @param location location header content
   * @return a {@link Response}
   */
  @XmlTransient
  public Response toResponse(StatusType statusType, URL location) {
    return toResponseBuilder(statusType).header(LOCATION_HEADER_NAME, location).build();
  }
}
