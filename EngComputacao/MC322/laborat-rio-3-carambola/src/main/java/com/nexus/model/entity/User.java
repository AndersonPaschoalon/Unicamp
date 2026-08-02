/**
 * Represents a user in the Nexus system.
 * 
 * This class encapsulates user information including username and email address.
 * It provides validation for both fields during instantiation and methods to retrieve
 * user data and calculate workload based on assigned tasks.
 * 
 * The class enforces the following constraints:
 * - Username cannot be null, empty, or contain only whitespace
 * - Email must be a valid email format according to RFC standards
 * 
 * @author Sanchez
 * @version 1.0
 */

package com.nexus.model.entity;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.nexus.exception.NexusSystemException;

public class User {
    private final String username;
    private final String email;

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    /**
     * Constructs a new User with the specified username and email.
     * 
     * <p>Both parameters are validated during construction. The username must not be
     * null, empty, or contain only whitespace. The email must be a valid email format
     * according to RFC standards.</p>
     *
     * @param username the username for the new user (must not be null, empty, or whitespace-only)
     * @param email the email address for the new user (must be a valid email format)
     * @throws IllegalArgumentException if username or email is null or blank
     * @throws NexusSystemException if username contains only whitespace or email format is invalid
     */
    public User(String username, String email) {
        validateUsername(username);
        validateEmail(email);

        this.username = username;
        this.email = email;
    }

    /**
     * Validates the provided username string.
     * 
     * @param username the username to validate
     * @throws IllegalArgumentException if the username is null or blank
     * @throws NexusValidationException if the username contains only whitespace characters
     */
    private void validateUsername(String username) throws IllegalArgumentException, NexusSystemException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        if (username.trim().length() == 0) {
            throw new NexusSystemException("Username não pode ser apenas espaços em branco.");
        }
    }

    /**
     * Validates the provided email address.
     *
     * @param email the email address to validate
     * @throws IllegalArgumentException if the email is null or blank
     * @throws NexusValidationException if the email format is invalid according to the email regex pattern
     */
    private void validateEmail(String email) throws NexusSystemException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email não pode ser vazio.");
        }
        if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches()) {
            throw new NexusSystemException("Email inválido.");
        }
    }

    /**
     * Retrieves the email address associated with this user.
     *
     * @return the email address of the user
     */
    public String consultEmail() {
        return email;
    }

    /**
     * Retrieves the username of the user.
     *
     * @return the username associated with this user
     */
    public String consultUsername() {
        return username;
    }

    /**
     * Calculates the workload for this user by counting the number of in-progress tasks assigned to them.
     *
     * @param tasks the list of tasks to filter and count
     * @return the number of tasks that are assigned to this user and have a status of IN_PROGRESS
     */
    public long calculateWorkload(List<Task> tasks) {
        return tasks.stream()
                    .filter(t -> t.getOwner() != null && t.getOwner().equals(this) && t.getStatus().getName().equals("IN_PROGRESS"))
                    .count(); 
    }

    /**
     * Compares this user with another object for equality.
     * Two users are considered equal if they have the same username OR the same email address.
     *
     * @param o the object to compare with this user
     * @return true if the users have the same username or email, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) ||
               Objects.equals(email, user.email);
    }

    /**
     * Returns the hash code for this user.
     * The hash code is computed based on both the username and email address.
     *
     * @return the hash code value for this user
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }
}