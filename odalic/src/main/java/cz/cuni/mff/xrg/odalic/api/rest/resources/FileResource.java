package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.values.FileValueInput;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;

/**
 * File resource definition.
 * 
 * @author Václav Brodec
 */
@Component
@Path("/files")
public final class FileResource {

  public static final String TEXT_CSV_MEDIA_TYPE = "text/csv";

  private final FileService fileService;

  @Autowired
  public FileResource(FileService fileService) {
    Preconditions.checkNotNull(fileService);

    this.fileService = fileService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFiles() {
    final List<File> files = fileService.getFiles();

    return Reply.data(Response.Status.OK, files).toResponse();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFileById(@PathParam("id") String id) {
    final File file = fileService.getById(id);

    return Reply.data(Response.Status.OK, file).toResponse();
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(@Context UriInfo uriInfo, @PathParam("id") String id,
      @FormDataParam("input") InputStream fileInputStream) throws IOException {
    final URL location = cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, id);
    final File file = new File(id, "", location, true);
    
    if (!fileService.existsFileWithId(id)) {
      fileService.create(file, fileInputStream);

      return Message.of("A new file has been created AT THE STANDARD LOCATION.")
          .toResponse(Response.Status.CREATED, location);
    } else {
      fileService.replace(file);
      return Message.of("The file you specified has been fully updated AT THE STANDARD LOCATION.")
          .toResponse(Response.Status.OK, location);
    }
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(@Context UriInfo uriInfo, @PathParam("id") String id,
      FileValueInput fileInput) throws MalformedURLException {
    final File file = new File(id, "", fileInput.getLocation(), false);
    
    if (!fileService.existsFileWithId(id)) {
      fileService.create(file);

      return Message.of("A new remote file has been registered.").toResponse(
          Response.Status.CREATED,
          cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, id));
    } else {
      fileService.replace(file);

      return Message.of("The file description has been updated.").toResponse(
          Response.Status.OK,
          cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, id));
    }
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postFile(@Context UriInfo uriInfo,
      @FormDataParam("file") InputStream fileInputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
    final String id = fileDetail.getFileName();
    final File file =
        new File(id, "", cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, id), true);

    if (fileService.existsFileWithId(id)) {
      return Message.of("There already exists a file with the same name as you provided.")
          .toResponse(Response.Status.BAD_REQUEST);
    }

    fileService.create(file, fileInputStream);
    return Message
        .of("A new file has been registered AT THE LOCATION DERIVED from the name of the one uploaded.")
        .toResponse(Response.Status.CREATED, file.getLocation());
  }

  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteFileById(@PathParam("id") String id) {
    fileService.deleteById(id);

    return Message.of("File definition deleted.").toResponse(Response.Status.OK);
  }

  @GET
  @Path("{id}")
  @Produces(TEXT_CSV_MEDIA_TYPE)
  public Response getCsvDataById(@PathParam("id") String id) throws IOException {
    final String data = fileService.getDataById(id);

    return Response.ok(data).build();
  }
}
