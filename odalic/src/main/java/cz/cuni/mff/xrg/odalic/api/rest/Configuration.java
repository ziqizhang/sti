/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rest;

import javax.xml.bind.JAXBException;

import cz.cuni.mff.xrg.odalic.api.rest.resources.*;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import cz.cuni.mff.xrg.odalic.api.rest.filters.CorsResponseFilter;
import cz.cuni.mff.xrg.odalic.api.rest.filters.LoggingResponseFilter;
import cz.cuni.mff.xrg.odalic.api.rest.responses.ThrowableMapper;

/**
 * Configures the provided resources, filters, mappers and features.
 * 
 * @author Václav Brodec
 * 
 * @see org.glassfish.jersey.server.ResourceConfig
 */
public final class Configuration extends ResourceConfig {

  public Configuration() throws JAXBException {
    // Resources registration
    register(FileResource.class);
    register(TaskResource.class);
    register(ConfigurationResource.class);
    register(FeedbackResource.class);
    register(ExecutionResource.class);
    register(ResultResource.class);
    register(StateResource.class);
    register(AnnotatedTableResource.class);
    register(CsvExportResource.class);
    register(RdfExportResource.class);
    register(EntitiesResource.class);
    register(BasesResource.class);

    // Filters registration
    register(RequestContextFilter.class);
    register(LoggingResponseFilter.class);
    register(CorsResponseFilter.class);
    
    // Exception mappers registration
    register(ThrowableMapper.class);

    // Features registration
    register(JacksonFeature.class);
    register(MultiPartFeature.class);

    // Prevent the container to interfere with the error entities. 
    property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
  }
}
