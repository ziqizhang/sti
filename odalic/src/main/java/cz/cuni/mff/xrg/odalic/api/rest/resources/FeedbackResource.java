package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;

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

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.FeedbackService;

@Component
@Path("/tasks/{id}/configuration/feedback")
public final class FeedbackResource {

  private final FeedbackService feedbackService;
  
  @Autowired
  public FeedbackResource(FeedbackService feedbackService) {
    Preconditions.checkNotNull(feedbackService);
    
    this.feedbackService = feedbackService;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFeedbackForTaskId(@PathParam("id") String id, Feedback feedback) {
    feedbackService.setForTaskId(id, feedback);
    return Response.status(Response.Status.OK).entity("Feedback set.").build();
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFeedbackForTaskId(@PathParam("id") String taskId) {
    Feedback feedbackForTaskId = feedbackService.getForTaskId(taskId);
    
    return Response.status(Response.Status.OK).entity(feedbackForTaskId).build();
  }
  
  @GET
  @Path("/input")
  @Produces(MediaType.APPLICATION_JSON)
  public Input getJsonDataById(@PathParam("id") String id) throws IOException {
    return feedbackService.getInputForTaskId(id);
  }
}
