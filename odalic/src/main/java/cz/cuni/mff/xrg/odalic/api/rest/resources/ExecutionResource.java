package cz.cuni.mff.xrg.odalic.api.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.values.ExecutionValue;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;

@Component
@Path("/tasks/{id}/execution")
public final class ExecutionResource {

  private ExecutionService executionService;
  
  @Autowired
  public ExecutionResource(ExecutionService executionService) {
    Preconditions.checkNotNull(executionService);
    
    this.executionService = executionService;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putExecutionForTaskId(@PathParam("id") String id, ExecutionValue execution) {
    executionService.submitForTaskId(id);
    return Response.status(Response.Status.OK).entity("Execution submitted.").build();
  }
  
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response deleteExecutionForTaskId(@PathParam("id") String id) {
    executionService.cancelForTaskId(id);
    return Response.status(Response.Status.OK)
        .entity("Execution canceled.").build();
  }
}
