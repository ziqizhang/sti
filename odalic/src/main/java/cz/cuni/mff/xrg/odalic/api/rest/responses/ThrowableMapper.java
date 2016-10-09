package cz.cuni.mff.xrg.odalic.api.rest.responses;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Catch-all throwable mapper.
 * 
 * @author Václav Brodec
 *
 */
public final class ThrowableMapper implements ExceptionMapper<Throwable> {

  /* (non-Javadoc)
   * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
   */
  @Override
  public Response toResponse(Throwable throwable) {
    final StatusType statusType = getHttpStatus(throwable);
    
    final String text = throwable.getMessage();
    
    final StringWriter trace = new StringWriter();
    throwable.printStackTrace(new PrintWriter(trace));
    final String debugContent = trace.toString();
    
    return Message.of(text, debugContent).toResponse(statusType);
  }

  /**
   * Defaults to internal server error in case the exception is not instance of {@link WebApplicationException}.
   * 
   * @param throwable throwable instance
   * @return HTTP status
   */
  private StatusType getHttpStatus(Throwable throwable) {
    if (throwable instanceof WebApplicationException) {
      return ((WebApplicationException) throwable).getResponse().getStatusInfo();
    } else {
      return Response.Status.INTERNAL_SERVER_ERROR;
    }
  }
}

