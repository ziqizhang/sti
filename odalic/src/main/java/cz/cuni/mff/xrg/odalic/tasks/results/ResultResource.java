package cz.cuni.mff.xrg.odalic.tasks.results;

import javax.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/tasks/{id}/result")
public class ResultResource {

  @SuppressWarnings("unused")
  private ResultService resultService;

  //TODO: Methods.

  @Autowired
  public ResultResource(ResultService resultService) {
    this.resultService = resultService;
  }
}
