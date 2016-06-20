package cz.cuni.mff.xrg.odalic.api.rest.resources;

import javax.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odalic.tasks.results.ResultService;

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
