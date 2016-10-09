package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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

import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.values.ConfigurationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.TaskValue;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;

/**
 * Task resource definition.
 * 
 * @author Václav Brodec
 */
@Component
@Path("/tasks")
public final class TaskResource {

  private final TaskService taskService;
  private final FileService fileService;

  @Autowired
  public TaskResource(TaskService taskService, FileService fileService) {
    Preconditions.checkNotNull(taskService);
    Preconditions.checkNotNull(fileService);

    this.taskService = taskService;
    this.fileService = fileService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTasks() {
    final Set<Task> tasks = taskService.getTasks();
    
    return Reply.data(Response.Status.OK, tasks).toResponse();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTaskById(@PathParam("id") String id) {
    final Task task = taskService.getById(id);
    
    return Reply.data(Response.Status.OK, task).toResponse();
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putTaskWithId(@Context UriInfo uriInfo, @PathParam("id") String id,
      TaskValue taskValue) throws MalformedURLException {
    final ConfigurationValue configurationValue = taskValue.getConfiguration();
    final File input = fileService.getById(configurationValue.getInput());
    final Configuration configuration = new Configuration(input,
        configurationValue.getPrimaryBase(), configurationValue.getFeedback());
    final Task task = new Task(taskValue.getId(), taskValue.getCreated(), configuration);

    if (!taskService.hasId(task, id)) {
      return Message.of("The ID in the payload is not the same as the ID of resource.")
          .toResponse(Response.Status.NOT_ACCEPTABLE);
    }

    Task taskById = taskService.verifyTaskExistenceById(id);

    final URL location = cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, id);

    if (taskById == null) {
      taskService.create(task);
      return Message.of("A new task has been created AT THE LOCATION you specified")
          .toResponse(Response.Status.CREATED, location);
    } else {
      taskService.replace(task);
      return Message
          .of("The task you specified has been fully updated AT THE LOCATION you specified.")
          .toResponse(Response.Status.OK, location);
    }
  }

  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteTaskById(@PathParam("id") String id) {
    taskService.deleteById(id);
    
    return Message.of("Task deleted.").toResponse(Response.Status.OK);
  }
}
