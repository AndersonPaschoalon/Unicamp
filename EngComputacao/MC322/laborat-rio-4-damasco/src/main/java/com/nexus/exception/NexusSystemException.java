package com.nexus.exception;

/**
 * Represents failures of infrastructure or the runtime environment.
 *
 * <p>Examples include I/O errors when reading or writing files and
 * dependency-injection failures. The system should not attempt to
 * recover silently from these; they indicate a structural problem.</p>
 */
public class NexusSystemException extends NexusException {
    /**
     * Constructs a new {@code NexusSystemException} with the specified detail message.
     *
     * @param message the detail message explaining the infrastructure failure
     */
    public NexusSystemException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code NexusSystemException} with the specified detail message and cause.
     *
     * @param message the detail message explaining the infrastructure failure
     * @param cause the underlying cause of this exception
     */
    public NexusSystemException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
