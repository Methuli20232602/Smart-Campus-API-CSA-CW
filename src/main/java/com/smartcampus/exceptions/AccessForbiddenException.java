package com.smartcampus.exceptions;

/**
 * Custom exception representing an HTTP 403 Forbidden error.
 * Used when a client attempts an action that is blocked by current business rules
 * (e.g., posting readings to a sensor in maintenance mode).
 */
public class AccessForbiddenException extends RuntimeException {
    public AccessForbiddenException(String message) {
        super(message);
    }
}
