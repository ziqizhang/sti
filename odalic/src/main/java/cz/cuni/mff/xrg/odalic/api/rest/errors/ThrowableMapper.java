package cz.cuni.mff.xrg.odalic.api.rest.errors;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ThrowableMapper implements ExceptionMapper<Throwable> {

  public Response toResponse(Throwable throwable) {
    Message errorMessage = new Message(getHttpStatus(throwable));
    
    errorMessage.setText(throwable.getMessage());
    
    StringWriter trace = new StringWriter();
    throwable.printStackTrace(new PrintWriter(trace));
    errorMessage.setDeveloperText(trace.toString());

    return Response.status(errorMessage.getStatus()).entity(errorMessage)
        .type(MediaType.APPLICATION_JSON).build();
  }

  /**
   * Defaults to internal server error in case the exception is not instance of {@link WebApplicationException}.
   * 
   * @param throwable throwable instance
   * @return HTTP status code
   */
  private int getHttpStatus(Throwable throwable) {
    if (throwable instanceof WebApplicationException) {
      return ((WebApplicationException) throwable).getResponse().getStatus();
    } else {
      return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }
  }
}

