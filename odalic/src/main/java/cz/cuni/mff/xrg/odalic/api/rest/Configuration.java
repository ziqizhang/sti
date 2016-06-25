/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rest;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import cz.cuni.mff.xrg.odalic.api.rest.errors.ThrowableMapper;
import cz.cuni.mff.xrg.odalic.api.rest.filters.CorsResponseFilter;
import cz.cuni.mff.xrg.odalic.api.rest.filters.LoggingResponseFilter;
import cz.cuni.mff.xrg.odalic.api.rest.resources.ConfigurationResource;
import cz.cuni.mff.xrg.odalic.api.rest.resources.DraftResource;
import cz.cuni.mff.xrg.odalic.api.rest.resources.ExecutionResource;
import cz.cuni.mff.xrg.odalic.api.rest.resources.FeedbackResource;
import cz.cuni.mff.xrg.odalic.api.rest.resources.FileResource;
import cz.cuni.mff.xrg.odalic.api.rest.resources.ResultResource;
import cz.cuni.mff.xrg.odalic.api.rest.resources.StateResource;
import cz.cuni.mff.xrg.odalic.api.rest.resources.TaskResource;

public final class Configuration extends ResourceConfig {

  public Configuration() {
    // Resources registration
    register(FileResource.class);
    register(TaskResource.class);
    register(ConfigurationResource.class);
    register(FeedbackResource.class);
    register(DraftResource.class);
    register(ExecutionResource.class);
    register(ResultResource.class);
    register(StateResource.class);
    
    // Filters registration
    register(RequestContextFilter.class);
    register(LoggingResponseFilter.class);
    register(CorsResponseFilter.class);
    
    // Exception mappers registration
    register(ThrowableMapper.class);

    // Features registration
    register(JacksonFeature.class);
    register(MultiPartFeature.class);
  }
}
