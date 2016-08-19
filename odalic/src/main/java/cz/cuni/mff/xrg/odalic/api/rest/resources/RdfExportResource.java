package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.outputs.rdfexport.RdfExportService;

/**
 * Definition of the resource providing the result as serialized RDF data.
 * 
 * @author Václav Brodec
 *
 */
@Component
@Path("/tasks/{id}/result/rdf-export")
public final class RdfExportResource {

  public static final String TURTLE_MIME_TYPE = "text/turtle";
  public static final String JSON_LD_MIME_TYPE = "application/ld+json";
  
  private final RdfExportService rdfExportService;

  @Autowired
  public RdfExportResource(RdfExportService RdfExportService) {
    Preconditions.checkNotNull(RdfExportService);

    this.rdfExportService = RdfExportService;
  }

  @GET
  @Produces(TURTLE_MIME_TYPE)
  public Response getTurtleExport(@PathParam("id") String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    final String rdfContent = rdfExportService.exportToTurtle(taskId);

    return Response.ok(rdfContent).build();
  }
  
  @GET
  @Produces(JSON_LD_MIME_TYPE)
  public Response getJsonLdExport(@PathParam("id") String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    final String rdfContent = rdfExportService.exportToJsonLd(taskId);

    return Response.ok(rdfContent).build();
  }
}
