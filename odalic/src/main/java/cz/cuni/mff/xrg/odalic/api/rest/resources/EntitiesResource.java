package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.util.NavigableSet;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.entities.EntitiesService;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import uk.ac.shef.dcs.kbsearch.KBSearchException;

/**
 * Entities resource definition.
 * 
 * @author Václav Brodec
 *
 */
@Component
@Path("{base}/entities")
public final class EntitiesResource {

  private static final Logger logger = LoggerFactory.getLogger(EntitiesResource.class);
  private final EntitiesService entitiesService;

  @Autowired
  public EntitiesResource(EntitiesService entitiesService) {
    Preconditions.checkNotNull(entitiesService);
    this.entitiesService = entitiesService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("search")
  public Response search(@PathParam("base") String base, @QueryParam("query") String query,
      @DefaultValue("20") @QueryParam("limit") Integer limit) {
    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }

    final NavigableSet<Entity> result;
    try {
      result = entitiesService.search(new KnowledgeBase(base), query, limit);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e.getLocalizedMessage());
    } catch (final KBSearchException e){
      logger.error("KB search error", e);
      throw new InternalServerErrorException(e.getLocalizedMessage());
    } catch (final Exception e){
    logger.error("Unexpected error", e);
    throw new InternalServerErrorException(e.getLocalizedMessage());
  }

    return Reply.data(Response.Status.OK, result)
        .toResponse();
  }
}
