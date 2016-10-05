package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.errors.Message;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;

/**
 * Result resource definition.
 * 
 * @author Václav Brodec
 *
 */
@Component
@Path("/tasks/{id}/result")
public final class ResultResource {

  private final ExecutionService executionService;
  
  @Autowired
  public ResultResource(ExecutionService executionService) {
    Preconditions.checkNotNull(executionService);
    
    this.executionService = executionService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getResult(@PathParam("id") String taskId) throws InterruptedException, ExecutionException {
    if (executionService.isCanceledForTaskId(taskId)) {
      return Message.of("The execution was canceled.").toResponse(Response.Status.NOT_FOUND);
    }

    return Response.ok(executionService.getResultForTaskId(taskId)).build();
  }
}
