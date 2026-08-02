package com.nexus.exception;

/**
 * Thrown when a lookup in the {@code Workspace} or repositories fails to
 * find the requested entity (project, task, user, workflow, status, etc.).
 */
public class EntityNotFoundException extends NexusDomainException {
    /**
     * Constructs a new {@code EntityNotFoundException} with the specified detail message.
     *
     * @param message the detail message describing which entity was not found
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
