package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTableService;

/**
 * Definition of the resource providing a part of the result in the form of table annotations.
 * 
 * @author Václav Brodec
 * 
 * @see AnnotatedTable format of the annotations
 */
@Component
@Path("/tasks/{id}/result/annotated-table")
public final class AnnotatedTableResource {

  private final AnnotatedTableService annotatedTableService;

  @Autowired
  public AnnotatedTableResource(AnnotatedTableService annotatedTableService) {
    Preconditions.checkNotNull(annotatedTableService);

    this.annotatedTableService = annotatedTableService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAnnotatedTable(@PathParam("id") String taskId)
      throws InterruptedException, ExecutionException, CancellationException, IOException {
    final AnnotatedTable table = annotatedTableService.getAnnotatedTableForTaskId(taskId);

    return Response.ok(table).build();
  }
}
