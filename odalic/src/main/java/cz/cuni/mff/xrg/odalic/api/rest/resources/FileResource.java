package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.io.InputStream;
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
public class FileResource {

  private FileService fileService;

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
  public Response putFileById(
      @PathParam("id") String id,
      @FormDataParam("input") InputStream fileInputStream,
      @FormDataParam("disposition") FormDataContentDisposition disposition,
      @FormDataParam("file") File file) throws IOException {
            
    if (!fileService.hasId(file, id)) {
      return Response.status(Response.Status.NOT_ACCEPTABLE)
          .entity("The ID in the payload is not the same as the ID of resource.").build();
    }

    if (!fileService.existsFileWithId(id)) {
      fileService.create(file, fileInputStream);
      
      return Response.status(Response.Status.CREATED)
          .entity("A new file has been created AT THE STANDARD LOCATION.")
          .header("Location", "/files/" + String.valueOf(id)).build();
    } else {
      fileService.replace(file);
      return Response.status(Response.Status.OK)
          .entity(
              "The file you specified has been fully updated AT THE STANDARD LOCATION.")
          .header("Location", "/files/" + String.valueOf(id)).build();
    }
  }
  
  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(@PathParam("id") String id, File file) {
            
    if (!fileService.hasId(file, id)) {
      return Response.status(Response.Status.NOT_ACCEPTABLE)
          .entity("The ID in the payload is not the same as the ID of resource.").build();
    }

    if (!fileService.existsFileWithId(id)) {
      fileService.create(file);
      return Response.status(Response.Status.CREATED)
          .entity("A new file has been registered FOR THE LOCATION you specified")
          .header("Location", "/files/" + String.valueOf(id)).build();
    } else {
      fileService.replace(file);
      return Response.status(Response.Status.OK)
          .entity(
              "The file description you specified has been fully updated FOR THE LOCATION you specified.")
          .header("Location", "/files/" + String.valueOf(id)).build();
    }
  }

  @DELETE
  @Path("{id}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response deleteFileById(@PathParam("id") String id) {
    fileService.deleteById(id);
    return Response.status(Response.Status.NO_CONTENT)
        .entity("File successfully removed from database").build();
  }
}
