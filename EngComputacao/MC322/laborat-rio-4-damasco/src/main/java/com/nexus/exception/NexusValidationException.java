package com.nexus.exception;

/**
 * Exception thrown when validation rules are violated in the Nexus system.
 * 
 * <p>This runtime exception is used throughout the Nexus application to indicate
 * that business rules or validation constraints have been violated. Examples include:</p>
 * <ul>
 *   <li>Invalid email format during user creation</li>
 *   <li>Attempting to add a task that would exceed project budget</li>
 *   <li>Invalid status transitions in workflows</li>
 *   <li>Missing required parameters in commands</li>
 *   <li>Attempting to access non-existent resources</li>
 * </ul>
 * 
 */
public class NexusValidationException extends RuntimeException {
    
    /**
     * Constructs a new NexusValidationException with the specified detail message.
     * 
     * <p>The detail message should provide specific information about what
     * validation rule was violated to help with debugging and user feedback.</p>
     *
     * @param message the detail message explaining the validation failure
     */
    public NexusValidationException(String message) {
        super(message);
    }
}