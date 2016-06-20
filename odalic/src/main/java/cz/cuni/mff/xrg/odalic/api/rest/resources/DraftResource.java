package cz.cuni.mff.xrg.odalic.api.rest.resources;

import javax.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odalic.tasks.drafts.DraftService;

@Component
@Path("/tasks/{id}/draft")
public class DraftResource {

  @SuppressWarnings("unused")
  private DraftService draftService;

  //TODO: Methods.

  @Autowired
  public DraftResource(DraftService draftService) {
    this.draftService = draftService;
  }
}
