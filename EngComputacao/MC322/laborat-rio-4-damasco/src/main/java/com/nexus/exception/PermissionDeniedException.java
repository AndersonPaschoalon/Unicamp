package com.nexus.exception;

/**
 * Thrown when a user attempts an action (such as moving a task) without
 * possessing the required {@code Role} in the project.
 */
public class PermissionDeniedException extends NexusDomainException {
    /**
     * Constructs a new {@code PermissionDeniedException} with the specified detail message.
     *
     * @param message the detail message describing the permission violation
     */
    public PermissionDeniedException(String message) {
        super(message);
    }
}
