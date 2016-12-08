package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.net.URI;
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
import cz.cuni.mff.xrg.odalic.entities.ClassProposal;
import cz.cuni.mff.xrg.odalic.entities.EntitiesService;
import cz.cuni.mff.xrg.odalic.entities.PropertyProposal;
import cz.cuni.mff.xrg.odalic.entities.ResourceProposal;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

import uk.ac.shef.dcs.kbproxy.KBProxyException;

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
  public Response search(@PathParam("base") String base, @QueryParam("query") String query,
      @DefaultValue("20") @QueryParam("limit") Integer limit) {
    return searchResources(base, query, limit);
  }
  
  @GET
  @Path("classes")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchClasses(@PathParam("base") String base, @QueryParam("query") String query,
      @DefaultValue("20") @QueryParam("limit") Integer limit) {
    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }

    final NavigableSet<Entity> result;
    try {
      result = entitiesService.searchClasses(new KnowledgeBase(base), query, limit);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e.getLocalizedMessage());
    } catch (final KBProxyException e) {
      logger.error("KB search error", e);
      throw new InternalServerErrorException(e.getLocalizedMessage());
    } catch (final Exception e) {
      logger.error("Unexpected error", e);
      throw new InternalServerErrorException(e.getLocalizedMessage());
    }

    return Reply.data(Response.Status.OK, result).toResponse();
  }
  
  @GET
  @Path("resources")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchResources(@PathParam("base") String base, @QueryParam("query") String query,
      @DefaultValue("20") @QueryParam("limit") Integer limit) {
    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }

    final NavigableSet<Entity> result;
    try {
      result = entitiesService.searchResources(new KnowledgeBase(base), query, limit);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e.getLocalizedMessage());
    } catch (final KBProxyException e) {
      logger.error("KB search error", e);
      throw new InternalServerErrorException(e.getLocalizedMessage());
    } catch (final Exception e) {
      logger.error("Unexpected error", e);
      throw new InternalServerErrorException(e.getLocalizedMessage());
    }

    return Reply.data(Response.Status.OK, result).toResponse();
  }
  
  @GET
  @Path("properties")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchProperties(@PathParam("base") String base, @QueryParam("query") String query,
      @DefaultValue("20") @QueryParam("limit") Integer limit, @QueryParam("domain") URI domain, @QueryParam("range") URI range) {
    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }
    
    final NavigableSet<Entity> result;
    try {
      result = entitiesService.searchProperties(new KnowledgeBase(base), query, limit, domain, range);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e.getLocalizedMessage());
    } catch (final KBProxyException e) {
      logger.error("KB search error", e);
      throw new InternalServerErrorException(e.getLocalizedMessage());
    } catch (final Exception e) {
      logger.error("Unexpected error", e);
      throw new InternalServerErrorException(e.getLocalizedMessage());
    }

    return Reply.data(Response.Status.OK, result).toResponse();
  }

  @POST
  @Path("classes")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(@PathParam("base") String base, ClassProposal proposal) {
    final Entity createdClass;
    try {
      createdClass = this.entitiesService.propose(new KnowledgeBase(base), proposal);
    } catch (KBProxyException e) {
      logger.error("KB proxy error", e);
      throw new InternalServerErrorException(e.getLocalizedMessage());
    }

    return Reply.data(Response.Status.OK, createdClass).toResponse();
  }

  @POST
  @Path("resources")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(@PathParam("base") String base, ResourceProposal proposal) {
    final Entity createdEntity;
    try {
      createdEntity = this.entitiesService.propose(new KnowledgeBase(base), proposal);
    } catch (KBProxyException e) {
      logger.error("KB proxy error", e);
      throw new InternalServerErrorException(e.getLocalizedMessage());
    }

    return Reply.data(Response.Status.OK, createdEntity).toResponse();
  }
  
  @POST
  @Path("properties")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(@PathParam("base") String base, PropertyProposal proposal) {
    final Entity createdProperty;
    try {
      createdProperty = this.entitiesService.propose(new KnowledgeBase(base), proposal);
    } catch (KBProxyException e) {
      logger.error("KB proxy error", e);
      throw new InternalServerErrorException(e.getLocalizedMessage());
    }

    return Reply.data(Response.Status.OK, createdProperty).toResponse();
  }
}
