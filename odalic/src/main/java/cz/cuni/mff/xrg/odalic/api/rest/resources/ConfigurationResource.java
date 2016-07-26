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

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.values.ConfigurationValue;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;

@Component
@Path("/tasks/{id}/configuration")
public final class ConfigurationResource {

  private final ConfigurationService configurationService;
  private final FileService fileService;
  
  @Autowired
  public ConfigurationResource(ConfigurationService configurationService, FileService fileService) {
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(fileService);
    
    this.configurationService = configurationService;
    this.fileService = fileService;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putConfigurationForTaskId(@PathParam("id") String id,
      ConfigurationValue configurationValue) {
    final File input = fileService.getById(configurationValue.getInput());
    
    final Configuration configuration;
    if (configurationValue.getFeedback() == null) {
        configuration = new Configuration(input, configurationValue.getPrimaryBase());
    } else {
      configuration = new Configuration(input, configurationValue.getPrimaryBase(), configurationValue.getFeedback());
    }

    configurationService.setForTaskId(id, configuration);
    return Response.status(Response.Status.OK).entity("Configuration set.").build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConfigurationForTaskId(@PathParam("id") String taskId) {
    Configuration configurationForTaskId = configurationService.getForTaskId(taskId);

    return Response.status(Response.Status.OK).entity(configurationForTaskId).build();
  }
}
