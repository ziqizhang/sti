/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rest.responses;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.StatusType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Preconditions;

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

  @XmlElement
  private final StatusType status;

  @XmlElement
  private final ReplyType type;

  @XmlElement
  private final Object payload;

  @XmlTransient
  public static Reply of(StatusType status, ReplyType type, Object payload) {
    return new Reply(status, type, payload);
  }

  @XmlTransient
  public static Reply message(StatusType status, Message message) {
    return new Reply(status, ReplyType.MESSAGE, message);
  }

  @XmlTransient
  public static Reply data(StatusType status, Object data) {
    return new Reply(status, ReplyType.DATA, data);
  }



  /**
   * Creates a REST API response.
   * 
   * @param status HTTP status code
   * @param type response type
   * @param payload payload containing the kind of response indicated by the {@link ReplyType}
   */
  public Reply(StatusType status, ReplyType type, Object payload) {
    Preconditions.checkNotNull(type);
    Preconditions.checkNotNull(payload);

    Preconditions.checkArgument(
        Boolean.logicalXor(type == ReplyType.MESSAGE && payload instanceof Message,
            type != ReplyType.MESSAGE && !(payload instanceof Message)));

    this.status = status;
    this.type = type;
    this.payload = payload;
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
    return "Reply [status=" + status + ", type=" + type + ", payload=" + payload + "]";
  }
}
