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

import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.csvexport.CsvExportService;

/**
 * Definition of the resource providing a complementary part of the result (extended CSV data) to
 * the annotations represented by {@link AnnotatedTable}.
 * 
 * @author Václav Brodec
 *
 */
@Component
@Path("/tasks/{id}/result/csv-export")
public final class CsvExportResource {

  public static final String TEXT_CSV_MEDIA_TYPE = "text/csv";
  
  private final CsvExportService csvExportService;

  @Autowired
  public CsvExportResource(CsvExportService csvExportService) {
    Preconditions.checkNotNull(csvExportService);

    this.csvExportService = csvExportService;
  }

  @GET
  @Produces(TEXT_CSV_MEDIA_TYPE)
  public Response getCsvExport(@PathParam("id") String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    final String csvContent = csvExportService.getExtendedCsvForTaskId(taskId);

    return Response.ok(csvContent).build();
  }
}
