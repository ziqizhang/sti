package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
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
  
  @Context
  private UriInfo uriInfo;

  @Autowired
  public FileResource(FileService fileService) {
    Preconditions.checkNotNull(fileService);

    this.fileService = fileService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFiles() {
    final List<File> files = fileService.getFiles();

    return Reply.data(Response.Status.OK, files, uriInfo).toResponse();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFileById(@PathParam("id") String id) {
    final File file;
    try {
      file = fileService.getById(id);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The file does not exist!");
    }

    return Reply.data(Response.Status.OK, file, uriInfo).toResponse();
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(@PathParam("id") String id,
      @FormDataParam("input") InputStream fileInputStream) throws IOException {
    if (fileInputStream == null) {
      throw new BadRequestException("No input provided!");
    }
    
    final URL location = cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, id);
    final File file = new File(id, "", location, true);

    if (!fileService.existsFileWithId(id)) {
      fileService.create(file, fileInputStream);

      return Message.of("A new file has been created AT THE STANDARD LOCATION.")
          .toResponse(Response.Status.CREATED, location, uriInfo);
    } else {
      fileService.replace(file);
      return Message.of("The file you specified has been fully updated AT THE STANDARD LOCATION.")
          .toResponse(Response.Status.OK, location, uriInfo);
    }
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(@Context UriInfo uriInfo, @PathParam("id") String id,
      FileValueInput fileInput) throws MalformedURLException {
    if (fileInput == null) {
      throw new BadRequestException("No file description provided!");
    }
    
    if (fileInput.getLocation() == null) {
      throw new BadRequestException("No location provided!");
    }
    
    final File file = new File(id, "", fileInput.getLocation(), false);

    if (!fileService.existsFileWithId(id)) {
      fileService.create(file);

      return Message.of("A new remote file has been registered.").toResponse(
          Response.Status.CREATED,
          cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, id), uriInfo);
    } else {
      fileService.replace(file);

      return Message.of("The file description has been updated.").toResponse(Response.Status.OK,
          cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, id), uriInfo);
    }
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postFile(@Context UriInfo uriInfo,
      @FormDataParam("input") InputStream fileInputStream,
      @FormDataParam("input") FormDataContentDisposition fileDetail) throws IOException {
    if (fileInputStream == null) {
      throw new BadRequestException("No input provided!");
    }
    
    if (fileDetail == null) {
      throw new BadRequestException("No input detail provided!");
    }
    
    final String id = fileDetail.getFileName();
    if (id == null) {
      throw new BadRequestException("No file name provided!");
    }
    
    final File file = new File(id, "",
        cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, id), true);

    if (fileService.existsFileWithId(id)) {
      throw new WebApplicationException(
          "There already exists a file with the same name as you provided.",
          Response.Status.CONFLICT);
    }

    fileService.create(file, fileInputStream);
    return Message
        .of("A new file has been registered AT THE LOCATION DERIVED from the name of the one uploaded.")
        .toResponse(Response.Status.CREATED, file.getLocation(), uriInfo);
  }

  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteFileById(@PathParam("id") String id) {
    try {
      fileService.deleteById(id);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The file does not exist!");
    }

    return Message.of("File definition deleted.").toResponse(Response.Status.OK, uriInfo);
  }

  @GET
  @Path("{id}")
  @Produces(TEXT_CSV_MEDIA_TYPE)
  public Response getCsvDataById(@PathParam("id") String id) throws IOException {
    final String data;
    try {
      data = fileService.getDataById(id);
    } catch (final IllegalArgumentException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok(data).build();
  }
}
