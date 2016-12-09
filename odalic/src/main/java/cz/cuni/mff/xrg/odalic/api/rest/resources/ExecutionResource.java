package cz.cuni.mff.xrg.odalic.api.rest.resources;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.values.ExecutionValue;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;

@Component
@Path("/tasks/{id}/execution")
public final class ExecutionResource {

  private ExecutionService executionService;
  
  @Context
  private UriInfo uriInfo;
  
  @Autowired
  public ExecutionResource(ExecutionService executionService) {
    Preconditions.checkNotNull(executionService);
    
    this.executionService = executionService;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putExecutionForTaskId(@PathParam("id") String id, ExecutionValue execution) {
    if (execution == null) {
      throw new BadRequestException("The execution must be provided!");
    }
    
    try {
      executionService.submitForTaskId(id);
    } catch (final IllegalStateException e) {
      throw new WebApplicationException("The task has already been scheduled!", Response.Status.CONFLICT);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The task does not exist!");
    }
    
    return Message.of("Execution submitted.").toResponse(Response.Status.OK, uriInfo);
  }
  
  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  public Response deleteExecutionForTaskId(@PathParam("id") String id) {
    try {
      executionService.cancelForTaskId(id);
    } catch (final IllegalStateException e) {
      throw new WebApplicationException("The task has already finished!", Response.Status.CONFLICT);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task has not been scheduled or does not exist!");
    }
    
    return Message.of("Execution canceled.").toResponse(Response.Status.OK, uriInfo);
  }
}
