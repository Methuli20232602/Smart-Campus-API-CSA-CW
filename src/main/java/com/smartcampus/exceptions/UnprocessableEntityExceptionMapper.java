package com.smartcampus.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps UnprocessableEntityException to an HTTP 422 response.
 */
@Provider
public class UnprocessableEntityExceptionMapper implements ExceptionMapper<UnprocessableEntityException> {

    @Override
    public Response toResponse(UnprocessableEntityException exception) {
        // Return HTTP 422 Unprocessable Entity
        return Response.status(422)
                       .entity(exception.getMessage())
                       .type(MediaType.TEXT_PLAIN)
                       .build();
    }
}
