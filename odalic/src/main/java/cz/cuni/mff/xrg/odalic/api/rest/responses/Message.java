package cz.cuni.mff.xrg.odalic.api.rest.responses;

import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriInfo;
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
  public static Message of(String text, String debugContent) {
    return new MessageBuilder().text(text).debugContent(debugContent).build();
  }

  @XmlTransient
  public static Message of(String text) {
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
  @XmlElement
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
  @XmlElement
  @Nullable
  public String getDebugContent() {
    return debugContent;
  }

  /**
   * Utility method that wraps the message into a JSON response builder and assigns it the provided
   * {@link StatusType}.
   * 
   * @param statusType status type
   * @param uriInfo request URI information
   * @return a {@link ReplyBuilder}
   */
  @XmlTransient
  public ResponseBuilder toResponseBuilder(StatusType statusType, UriInfo uriInfo) {
    Preconditions.checkNotNull(statusType);

    return Reply.message(statusType, this, uriInfo).toResponseBuilder();
  }

  /**
   * Utility method that wraps the message into a JSON response and assigns it the provided
   * {@link StatusType}.
   * 
   * @param statusType status type
   * @param UriInfo request URI information
   * @return a {@link Reply}
   */
  @XmlTransient
  public Response toResponse(StatusType statusType, UriInfo uriInfo) {
    Preconditions.checkNotNull(statusType);

    return Reply.message(statusType, this, uriInfo).toResponse();
  }

  /**
   * Utility method that wraps the message into a JSON response and assigns it the provided
   * {@link StatusType} and location header content.
   * 
   * @param statusType status type
   * @param location location header content
   * @param UriInfo request URI information
   * @return a {@link Reply}
   */
  @XmlTransient
  public Response toResponse(StatusType statusType, URL location, UriInfo uriInfo) {
    Preconditions.checkNotNull(statusType);
    Preconditions.checkNotNull(location);

    return toResponseBuilder(statusType, uriInfo).header(LOCATION_HEADER_NAME, location).build();
  }
}
