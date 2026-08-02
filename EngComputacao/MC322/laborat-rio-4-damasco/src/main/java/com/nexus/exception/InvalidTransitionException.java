package com.nexus.exception;

/**
 * Thrown when a status transition violates the rules defined
 * in the project's {@code Workflow} (e.g. moving a task from a
 * terminal status or to a disallowed target status).
 */
public class InvalidTransitionException extends NexusDomainException {
    /**
     * Constructs a new {@code InvalidTransitionException} with the specified detail message.
     *
     * @param message the detail message describing the illegal transition
     */
    public InvalidTransitionException(String message) {
        super(message);
    }
}
