package com.smartcampus.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps AccessForbiddenException to an HTTP 403 response.
 */
@Provider
public class AccessForbiddenExceptionMapper implements ExceptionMapper<AccessForbiddenException> {

    @Override
    public Response toResponse(AccessForbiddenException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                       .entity(exception.getMessage())
                       .type(MediaType.TEXT_PLAIN)
                       .build();
    }
}
