package com.smartcampus.exception;

/**
 * Thrown when a resource references a linked resource that does not exist.
 * For example, creating a Sensor with a roomId that doesn't exist.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
