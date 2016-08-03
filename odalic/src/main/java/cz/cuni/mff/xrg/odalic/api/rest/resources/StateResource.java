package cz.cuni.mff.xrg.odalic.api.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.values.StateValue;
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
  
  @Autowired
  public StateResource(ExecutionService executionService) {
    Preconditions.checkNotNull(executionService);
    
    this.executionService = executionService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public StateValue getStateForTaskId(@PathParam("id") String id) {
    final boolean scheduled = executionService.hasBeenScheduledForTaskId(id);
    if (!scheduled) {
      return StateValue.READY;
    }
    
    final boolean done = executionService.isDoneForTaskId(id);
    final boolean canceled = executionService.isCanceledForTaskId(id);
    
    if (done) {
      if (canceled) {
        return StateValue.CANCELLED;
      } else {
        return StateValue.FINISHED;
      }
    } else {
      return StateValue.SCHEDULED;
    }
  }
}
