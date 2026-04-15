package com.smartcampus.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Global Catch-All Exception Mapper.
 * Intercepts any unhandled RuntimeExceptions or errors (Throwable) 
 * and returns a standardized HTTP 500 response.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // Log the exception (in Day 18 we will add robust logging)
        System.err.println("CRITICAL ERROR: " + exception.getMessage());
        exception.printStackTrace();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity("An unexpected internal server error occurred. Please contact the administrator.")
                       .type(MediaType.TEXT_PLAIN)
                       .build();
    }
}
