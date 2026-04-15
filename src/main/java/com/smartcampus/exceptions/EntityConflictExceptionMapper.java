package com.smartcampus.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps EntityConflictException to an HTTP 409 response.
 * The @Provider annotation registers this class with the JAX-RS runtime.
 */
@Provider
public class EntityConflictExceptionMapper implements ExceptionMapper<EntityConflictException> {

    @Override
    public Response toResponse(EntityConflictException exception) {
        return Response.status(Response.Status.CONFLICT)
                       .entity(exception.getMessage())
                       .type(MediaType.TEXT_PLAIN) // Providing clear error messages in plain text
                       .build();
    }
}
