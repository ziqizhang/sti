package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

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
  
  @Context
  private UriInfo uriInfo;
  
  @Autowired
  public ResultResource(ExecutionService executionService) {
    Preconditions.checkNotNull(executionService);
    
    this.executionService = executionService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getResult(@PathParam("id") String taskId) throws InterruptedException, ExecutionException {
    final Result resultForTaskId;
    try {
      resultForTaskId = executionService.getResultForTaskId(taskId);
    } catch (final CancellationException e) {
      throw new NotFoundException("Result is not available, because the processing was canceled.");
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task has not been scheduled or does not exist!");
    }
    
    return Reply.data(Response.Status.OK, resultForTaskId, uriInfo).toResponse();
  }
}
