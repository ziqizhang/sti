package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import cz.cuni.mff.xrg.odalic.api.rest.values.StatefulTaskValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.TaskValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.States;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;

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
  private final ExecutionService executionService;

  @Autowired
  public TaskResource(TaskService taskService, FileService fileService,
      ExecutionService executionService) {
    Preconditions.checkNotNull(taskService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(executionService);

    this.taskService = taskService;
    this.fileService = fileService;
    this.executionService = executionService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTasks(@QueryParam("states") Boolean states) {
    final Set<Task> tasks = taskService.getTasks();

    if (states == null || (!states)) {
      return Reply.data(Response.Status.OK, tasks).toResponse();
    } else {
      final Set<StatefulTaskValue> statefulStasks = tasks.stream()
          .map(e -> new StatefulTaskValue(e, States.queryStateValue(executionService, e.getId())))
          .collect(Collectors.toSet());

      return Reply.data(Response.Status.OK, statefulStasks).toResponse();
    }
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTaskById(@PathParam("id") String id) {
    final Task task;
    try {
      task = taskService.getById(id);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task does not exist!");
    }

    return Reply.data(Response.Status.OK, task).toResponse();
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putTaskWithId(@Context UriInfo uriInfo, @PathParam("id") String id,
      TaskValue taskValue) throws MalformedURLException {
    if (taskValue == null) {
      throw new BadRequestException("No task definition provided!");
    }

    if (taskValue.getConfiguration() == null) {
      throw new BadRequestException("Configuration must be included!");
    }

    final ConfigurationValue configurationValue = taskValue.getConfiguration();

    if (taskValue.getId() != null && !taskValue.getId().equals(id)) {
      throw new BadRequestException("The ID in the payload is not the same as the ID of resource.");
    }

    final File input;
    try {
      input = fileService.getById(configurationValue.getInput());
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The input file does not exist!");
    }

    final Configuration configuration = new Configuration(input,
        configurationValue.getPrimaryBase(), configurationValue.getFeedback());
    final Task task = new Task(id,
        taskValue.getDescription() == null ? "" : taskValue.getDescription(), configuration);

    final Task taskById = taskService.verifyTaskExistenceById(id);

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
    try {
      taskService.deleteById(id);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task does not exist!");
    }

    return Message.of("Task deleted.").toResponse(Response.Status.OK);
  }
}
