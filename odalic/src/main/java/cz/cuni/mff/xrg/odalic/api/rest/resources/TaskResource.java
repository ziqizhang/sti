package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.TaskDigest;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;

/**
 * Task resource definition.
 * 
 * @author Václav Brodec
 */
@Component
@Path("/tasks")
public class TaskResource {

  private TaskService taskService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<TaskDigest> getTasks() {
    return taskService.getTasks();
  }

  @GET
  @Path("/tasks/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTaskById(@PathParam("id") String id) {
    Task task = taskService.getById(id);
    return Response.status(Response.Status.OK).entity(task).build();
  }

  @PUT
  @Path("/tasks/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putTaskWithId(@PathParam("id") String id, Task task) {
    if (!taskService.hasId(task, id)) {
      return Response.status(Response.Status.NOT_ACCEPTABLE)
          .entity("The ID in the payload is not the same as the ID of resource.").build();
    }

    Task taskById = taskService.verifyTaskExistenceById(id);

    if (taskById == null) {
      taskService.create(task);
      return Response.status(Response.Status.CREATED)
          .entity("A new task has been created AT THE LOCATION you specified")
          .header("Location", "/tasks/" + String.valueOf(id)).build();
    } else {
      taskService.replace(task);
      return Response.status(Response.Status.OK)
          .entity(
              "The task you specified has been fully updated AT THE LOCATION you specified.")
          .header("Location", "/tasks/" + String.valueOf(id)).build();
    }
  }

  @DELETE
  @Path("/tasks/{id}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response deleteTaskById(@PathParam("id") String id) {
    taskService.deleteById(id);
    return Response.status(Response.Status.NO_CONTENT)
        .entity("Task successfully removed from database").build();
  }

  @Autowired
  public TaskResource(TaskService taskService) {
    this.taskService = taskService;
  }
}