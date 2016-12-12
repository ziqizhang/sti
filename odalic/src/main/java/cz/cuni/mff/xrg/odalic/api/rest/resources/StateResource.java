package cz.cuni.mff.xrg.odalic.api.rest.resources;

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
import cz.cuni.mff.xrg.odalic.api.rest.values.StateValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.States;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;

/**
 * State resource definition.
 * 
 * @author Václav Brodec
 *
 */
@Component
@Path("/tasks/{id}/state")
public final class StateResource {

  private final ExecutionService executionService;

  @Context
  private UriInfo uriInfo;
  
  @Autowired
  public StateResource(ExecutionService executionService) {
    Preconditions.checkNotNull(executionService);

    this.executionService = executionService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getStateForTaskId(@PathParam("id") String id) {
    final StateValue state;
    try {
      state = States.queryStateValue(executionService, id);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task does not exist!");
    }
    
    return Reply.data(Response.Status.OK, state, uriInfo)
        .toResponse();
  }
}
