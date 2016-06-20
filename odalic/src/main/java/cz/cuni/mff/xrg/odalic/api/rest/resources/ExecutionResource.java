package cz.cuni.mff.xrg.odalic.api.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odalic.tasks.executions.Execution;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;

@Component
@Path("/tasks/{id}/execution")
public class ExecutionResource {

  private ExecutionService executionService;

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putExecutionForTaskId(@PathParam("id") String id, Execution execution) {
    executionService.scheduleExecutionForTaskId(id, execution);
    return Response.status(Response.Status.OK).entity("Execution scheduled.").build();
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getExecutionForTaskId(@PathParam("id") String taskId) {
    Execution executionForTaskId = executionService.getExecutionForTaskId(taskId);
    
    return Response.status(Response.Status.OK).entity(executionForTaskId).build();
  }

  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response deleteExecutionForTaskId(@PathParam("id") String id) {
    executionService.stopExecutionForTaskId(id);
    return Response.status(Response.Status.OK)
        .entity("Execution signaled to stop.").build();
  }
  
  @Autowired
  public ExecutionResource(ExecutionService executionService) {
    this.executionService = executionService;
  }
}
