package cz.cuni.mff.xrg.odalic.api.rest.filters;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter that logs the API responses. 
 * 
 * @author Václav Brodec
 *
 */
public final class LoggingResponseFilter
        implements ContainerResponseFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingResponseFilter.class);

    /* (non-Javadoc)
     * @see javax.ws.rs.container.ContainerResponseFilter#filter(javax.ws.rs.container.ContainerRequestContext, javax.ws.rs.container.ContainerResponseContext)
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException {
        final String method = requestContext.getMethod();

        logger.debug("Requesting " + method + " for path " + requestContext.getUriInfo().getPath());
        final Object entity = responseContext.getEntity();
        if (entity != null) {
            logger.debug("Reply " + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(entity));
        }
    }

}