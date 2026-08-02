package com.nexus.exception;

/**
 * Thrown when a domain object is in an invalid state for the requested
 * operation (e.g. a project without a linked workflow when creating a task).
 */
public class InvalidStateException extends NexusDomainException {
    /**
     * Constructs a new {@code InvalidStateException} with the specified detail message.
     *
     * @param message the detail message describing the invalid state
     */
    public InvalidStateException(String message) {
        super(message);
    }
}
