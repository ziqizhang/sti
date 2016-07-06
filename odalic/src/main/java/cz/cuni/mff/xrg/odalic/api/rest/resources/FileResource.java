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
  public List<File> getFiles() {
    return fileService.getFiles();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFileById(@PathParam("id") String id) {
    File file = fileService.getById(id);
    return Response.status(Response.Status.OK).entity(file).build();
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(@Context UriInfo uriInfo, @PathParam("id") String id,
      @FormDataParam("input") InputStream fileInputStream, @FormDataParam("file") File file)
      throws IOException {

    if (!fileService.hasId(file, id)) {
      return Response.status(Response.Status.NOT_ACCEPTABLE)
          .entity("The ID in the payload is not the same as the ID of resource.").build();
    }

    if (!file.getLocation().equals(cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, id))) {
      return Response.status(Response.Status.NOT_ACCEPTABLE)
          .entity(
              "The location you provided for the file is not equal to the default location for uploaded file.")
          .build();
    }

    final URL location = file.getLocation();

    if (!fileService.existsFileWithId(id)) {
      fileService.create(file, fileInputStream);

      return Response.status(Response.Status.CREATED)
          .entity("A new file has been created AT THE STANDARD LOCATION.")
          .header("Location", location).build();
    } else {
      fileService.replace(file);
      return Response.status(Response.Status.OK)
          .entity("The file you specified has been fully updated AT THE STANDARD LOCATION.")
          .header("Location", location).build();
    }
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(@PathParam("id") String id, File file) throws MalformedURLException {

    if (!fileService.hasId(file, id)) {
      return Response.status(Response.Status.NOT_ACCEPTABLE)
          .entity("The ID in the payload is not the same as the ID of resource.").build();
    }

    final URL location = file.getLocation();

    if (!fileService.existsFileWithId(id)) {
      fileService.create(file);
      return Response.status(Response.Status.CREATED)
          .entity("A new file has been registered FOR THE LOCATION you specified")
          .header("Location", location).build();
    } else {
      fileService.replace(file);
      return Response.status(Response.Status.OK)
          .entity(
              "The file description you specified has been fully updated FOR THE LOCATION you specified.")
          .header("Location", location).build();
    }
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postFile(@Context UriInfo uriInfo,
      @FormDataParam("file") InputStream fileInputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {

    final String id = fileDetail.getFileName();
    final File file = new File(id, "", cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, id));

    if (fileService.existsFileWithId(id)) {
      return Response.status(Response.Status.NOT_ACCEPTABLE)
          .entity("There already exists a file with the same name as you provided.").build();
    }

    fileService.create(file, fileInputStream);
    return Response.status(Response.Status.CREATED)
        .entity("A new file has been registered AT THE LOCATION DERIVED from the name of the one uploaded.")
        .header("Location", file.getLocation()).build();
  }

  @DELETE
  @Path("{id}")
  public Response deleteFileById(@PathParam("id") String id) {
    fileService.deleteById(id);
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @GET
  @Path("{id}")
  @Produces(TEXT_CSV_MEDIA_TYPE)
  public Response getCsvDataById(@PathParam("id") String id) throws IOException {
    String data = fileService.getDataById(id);

    return Response.ok(data).build();
  }
}
