package com.smartcampus.exceptions;

/**
 * Custom exception representing an HTTP 422 Unprocessable Entity.
 * Used when a request payload is syntactically correct but semantically incorrect 
 * (e.g., a missing foreign key or dependency).
 */
public class UnprocessableEntityException extends RuntimeException {
    public UnprocessableEntityException(String message) {
        super(message);
    }
}
