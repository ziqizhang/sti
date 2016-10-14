package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.util.NavigableSet;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.entities.EntitiesService;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Entities resource definition.
 * 
 * @author Václav Brodec
 *
 */
@Component
@Path("/entities/{base}/")
public final class EntititesResource {

  private final EntitiesService entitiesService;

  @Autowired
  public EntititesResource(EntitiesService entitiesService) {
    Preconditions.checkNotNull(entitiesService);

    this.entitiesService = entitiesService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response search(@PathParam("base") String base, @QueryParam("query") String query,
      @DefaultValue("20") @QueryParam("limit") Integer limit) {
    if (base == null) {
      throw new WebApplicationException("Base not provided!", Response.Status.BAD_REQUEST);
    }
    if (query == null) {
      throw new WebApplicationException("Query not provided!", Response.Status.BAD_REQUEST);
    }

    final NavigableSet<Entity> result = entitiesService.search(new KnowledgeBase(base), query, limit);
    return Reply.data(Response.Status.OK, result)
        .toResponse();
  }
}
