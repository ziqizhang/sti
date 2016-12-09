/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rest.responses;

import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.StatusTypeJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.StatusTypeJsonSerializer;
import cz.cuni.mff.xrg.odalic.util.URL;

/**
 * <p>
 * A wrapper that either contains the actual data returned by the API implementation or any kind of
 * alternative content, typically a {@link Message}.
 * </p>
 * 
 * <p>
 * It helps the receiver to determine the correct processing workflow by providing a type of the
 * payload in the type attribute.
 * </p>
 * 
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement
public final class Reply {

  /**
   * Name of the URI query parameter that hold the optional string sent by a client that is sent
   * back to it as a part of the response.
   */
  public static final String STAMP_QUERY_PARAMETER_NAME = "stamp";

  @XmlElement
  @JsonSerialize(using = StatusTypeJsonSerializer.class)
  @JsonDeserialize(using = StatusTypeJsonDeserializer.class)
  private final StatusType status;

  @XmlElement
  private final ReplyType type;

  @XmlElement
  private final Object payload;

  @XmlElement
  @Nullable
  private final String stamp;

  @XmlTransient
  public static Reply of(StatusType status, ReplyType type, Object payload,
      @Nullable String stamp) {
    return new Reply(status, type, payload, stamp);
  }

  @XmlTransient
  public static Reply message(StatusType status, Message message, UriInfo uriInfo) {
    return new Reply(status, ReplyType.MESSAGE, message,
        URL.getStamp(uriInfo, STAMP_QUERY_PARAMETER_NAME));
  }

  @XmlTransient
  public static Reply data(StatusType status, Object data, UriInfo uriInfo) {
    return new Reply(status, ReplyType.DATA, data, URL.getStamp(uriInfo, STAMP_QUERY_PARAMETER_NAME));
  }

  /**
   * Creates a REST API response.
   * 
   * @param status HTTP status code
   * @param type response type
   * @param payload payload containing the kind of response indicated by the {@link ReplyType}
   * @param stamp a client-set string received in the request that originated this reply
   */
  public Reply(StatusType status, ReplyType type, Object payload, @Nullable String stamp) {
    Preconditions.checkNotNull(type);
    Preconditions.checkNotNull(payload);

    Preconditions
        .checkArgument(Boolean.logicalXor(type == ReplyType.MESSAGE && payload instanceof Message,
            type != ReplyType.MESSAGE && !(payload instanceof Message)));

    this.status = status;
    this.type = type;
    this.payload = payload;
    this.stamp = stamp;
  }

  /**
   * @return the status
   */
  public StatusType getStatus() {
    return status;
  }

  /**
   * @return the type
   */
  public ReplyType getType() {
    return type;
  }

  /**
   * @return the payload
   */
  public Object getPayload() {
    return payload;
  }

  /**
   * @return the stamp
   */
  @Nullable
  public Object getStamp() {
    return stamp;
  }

  @XmlTransient
  public ResponseBuilder toResponseBuilder() {
    return Response.status(status).entity(this).type(MediaType.APPLICATION_JSON);
  }

  @XmlTransient
  public Response toResponse() {
    return toResponseBuilder().build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Reply [status=" + status + ", type=" + type + ", payload=" + payload + ", stamp="
        + stamp + "]";
  }
}
