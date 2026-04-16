package com.smartcampus.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Global Logging Filter for API Observability.
 * Implements both ContainerRequestFilter and ContainerResponseFilter to
 * track incoming calls, final response statuses, and processing latency.
 *
 * The @Provider annotation ensures JAX-RS auto-discovers and registers
 * this filter globally across all resource endpoints.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());
    private static final String START_TIME_PROPERTY = "request-start-time";

    /**
     * Request Filter Phase: Executed BEFORE the resource method is invoked.
     * Records the timestamp and logs the incoming HTTP method and path.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Capture the current system time in milliseconds for latency calculation
        long startTime = System.currentTimeMillis();
        requestContext.setProperty(START_TIME_PROPERTY, startTime);

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();

        LOGGER.info(">>> INCOMING REQUEST: " + method + " /" + path);
    }

    /**
     * Response Filter Phase: Executed AFTER the resource method completes.
     * Calculates the processing latency and logs the final HTTP status code.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        // Retrieve the start time that was stored during the request phase
        Long startTime = (Long) requestContext.getProperty(START_TIME_PROPERTY);
        long latency = 0;

        if (startTime != null) {
            latency = System.currentTimeMillis() - startTime;
        }

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        int status = responseContext.getStatus();

        LOGGER.info("<<< OUTGOING RESPONSE: " + method + " /" + path
                + " | Status: " + status
                + " | Latency: " + latency + "ms");
    }
}
