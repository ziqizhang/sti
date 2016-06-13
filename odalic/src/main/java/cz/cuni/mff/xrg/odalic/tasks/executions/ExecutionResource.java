package cz.cuni.mff.xrg.odalic.tasks.executions;

import javax.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/tasks/{id}/execution")
public class ExecutionResource {

  @SuppressWarnings("unused")
  private ExecutionService executionService;

  // TODO: Methods.

  @Autowired
  public ExecutionResource(ExecutionService executionService) {
    this.executionService = executionService;
  }
}
