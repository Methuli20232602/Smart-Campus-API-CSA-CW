package com.smartcampus.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global Catch-All Exception Mapper.
 * Intercepts any unhandled RuntimeExceptions or errors (Throwable) 
 * and returns a standardized HTTP 500 response.
 * Raw stack traces are never leaked to the client for security reasons.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full exception server-side for debugging (never sent to client)
        LOGGER.log(Level.SEVERE, "Unhandled exception intercepted", exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity("An unexpected internal server error occurred. Please contact the administrator.")
                       .type(MediaType.TEXT_PLAIN)
                       .build();
    }
}
