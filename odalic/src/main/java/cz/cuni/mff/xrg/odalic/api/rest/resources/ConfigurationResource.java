package cz.cuni.mff.xrg.odalic.api.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;

@Component
@Path("/tasks/{id}/configuration")
public class ConfigurationResource {

  private ConfigurationService configurationService;

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putConfigurationForTaskId(@PathParam("id") String id, Configuration configuration) {
    configurationService.setForTaskId(id, configuration);
    return Response.status(Response.Status.OK).entity("Configuration set.").build();
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConfigurationForTaskId(@PathParam("id") String taskId) {
    Configuration configurationForTaskId = configurationService.getForTaskId(taskId);
    
    return Response.status(Response.Status.OK).entity(configurationForTaskId).build();
  }

  @Autowired
  public ConfigurationResource(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }
}
