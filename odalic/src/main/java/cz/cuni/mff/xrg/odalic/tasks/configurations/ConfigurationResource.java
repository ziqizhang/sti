package cz.cuni.mff.xrg.odalic.tasks.configurations;

import javax.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/tasks/{id}/configuration")
public class ConfigurationResource {

  @SuppressWarnings("unused")
  private ConfigurationService configurationService;

  //TODO: Methods.

  @Autowired
  public ConfigurationResource(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }
}
