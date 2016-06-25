package cz.cuni.mff.xrg.odalic.api.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.tasks.executions.State;

@Component
@Path("/tasks/{id}/state")
public class StateResource {

  private ExecutionService executionService;

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public State getStateForTaskId(@PathParam("id") String id) {
    final boolean scheduled = executionService.hasBeenScheduledForTaskId(id);
    if (!scheduled) {
      return State.READY;
    }
    
    final boolean done = executionService.isDoneForTaskId(id);
    final boolean cancelled = executionService.isCancelledForTaskId(id);
    
    if (done) {
      if (cancelled) {
        return State.CANCELLED;
      } else {
        return State.FINISHED;
      }
    } else {
      return State.SCHEDULED;
    }
  }
  
  @Autowired
  public StateResource(ExecutionService executionService) {
    this.executionService = executionService;
  }
}
