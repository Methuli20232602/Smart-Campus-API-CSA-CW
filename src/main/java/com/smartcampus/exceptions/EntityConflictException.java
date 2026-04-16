package com.smartcampus.exceptions;

/**
 * Custom exception representing an HTTP 409 Conflict.
 * Used when a business logic constraint prevents a requested state transition.
 */
public class EntityConflictException extends RuntimeException {
    public EntityConflictException(String message) {
        super(message);
    }
}
