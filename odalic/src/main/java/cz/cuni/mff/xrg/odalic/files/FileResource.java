package cz.cuni.mff.xrg.odalic.files;

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

/**
 * File resource definition.
 * 
 * @author Václav Brodec
 */
@Component
@Path("/files")
public class FileResource {

  private FileService fileService;

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
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(@PathParam("id") String id, File file) {
    if (!fileService.hasId(file, id)) {
      return Response.status(Response.Status.NOT_ACCEPTABLE)
          .entity("The ID in the payload is not the same as the ID of resource.").build();
    }

    File fileById = fileService.verifyFileExistenceById(id);

    if (fileById == null) {
      fileService.create(file);
      return Response.status(Response.Status.CREATED)
          .entity("A new file has been created AT THE LOCATION you specified")
          .header("Location", "/files/" + String.valueOf(id)).build();
    } else {
      fileService.replace(file);
      return Response.status(Response.Status.OK)
          .entity(
              "The file you specified has been fully updated AT THE LOCATION you specified.")
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

  @Autowired
  public FileResource(FileService fileService) {
    this.fileService = fileService;
  }
}
