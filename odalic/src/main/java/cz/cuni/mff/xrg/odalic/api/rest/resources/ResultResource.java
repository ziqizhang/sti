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

import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;

@Component
@Path("/tasks/{id}/result")
public class ResultResource {

  private final ExecutionService executionService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getResult(@PathParam("id") String taskId) throws InterruptedException, ExecutionException {
    if (executionService.isCancelledForTaskId(taskId)) {
      return Response.status(Response.Status.NOT_FOUND).entity("The execution was cancelled.")
          .build();
    }

    return Response.ok(executionService.getResultForTaskId(taskId)).build();
  }

  @Autowired
  public ResultResource(ExecutionService executionService) {
    this.executionService = executionService;
  }
}
